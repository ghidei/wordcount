package wordcounter

import io.circe.parser._
import io.circe.syntax._
import wordcounter.types._
import zhttp.http._
import zio._
import zio.concurrent.ConcurrentMap
import zio.duration._
import zio.stream._

import java.nio.file._
import java.util.concurrent.TimeUnit

object WordCount {
  type WordMap = ConcurrentMap[Word, ConcurrentMap[EventType, WordData]]

  def server(wordMap: WordMap): HttpApp[Any, Nothing] =
    Http.collectZIO[Request] { case Method.GET -> !! / "word" / word =>
      for {
        currentCount <- wordMap.get(Word(word))
        wc           <- currentCount match {
                          case None => ZIO.succeed(List.empty)
                          case Some(eventTypeMap) => eventTypeMap.toList.map(_.map(_._2))
                        }
      } yield Response.json(wc.asJson.spaces2)
    }

  def processEvents(slidingWindow: Duration, wordMap: WordMap): ZStream[ZEnv, Throwable, Unit] =
    ZStream
      .fromFile(Paths.get("events.txt"))
      .transduce(ZTransducer.utfDecode >>> ZTransducer.splitLines)
      .schedule(Schedule.spaced(500.millis))
      .tap(input => console.putStrLn(s"Processing input: $input"))
      .map(decode[Event])
      .collectRight
      .mapConcat { event =>
        event.data.split("\\s+").groupBy(identity).map { case (word, words) =>
          WordData(event.eventType, words.size, Word(word), event.timestamp)
        }
      }
      .mapM(insertInWordMap(_, wordMap))
      .mapM(wd => removeAfterWindow(slidingWindow, wd, wordMap).forkDaemon.unit)

  def insertInWordMap(newData: WordData, wordMap: WordMap): UIO[WordData] =
    wordMap
      .get(newData.word)
      .flatMap {
        case None => ConcurrentMap.make((newData.eventType, newData)).flatMap(wordMap.put(newData.word, _))
        case Some(eventMap) =>
          eventMap.get(newData.eventType).flatMap {
            case None => eventMap.put(newData.eventType, newData)
            case Some(wd) => eventMap.put(newData.eventType, wd.copy(count = wd.count + newData.count))
          }
      }
      .as(newData)

  def removeAfterWindow(slidingWindow: Duration, newData: WordData, wordMap: WordMap): ZIO[ZEnv, Throwable, Unit] =
    for {
      now          <- clock.instant
      sleepDuration = newData.timestamp.plusSeconds(slidingWindow.getSeconds).getEpochSecond - now.getEpochSecond
      _            <- clock.sleep(Duration(sleepDuration, TimeUnit.SECONDS))
      eventMap     <- wordMap.get(newData.word).someOrFail(new Exception(s"$newData not inserted in word map!!"))
      wd           <- eventMap.get(newData.eventType).someOrFail(new Exception(s"$newData not inserted in event map!!"))
      _            <- eventMap.put(newData.eventType, wd.copy(count = wd.count - newData.count))
    } yield ()
}
