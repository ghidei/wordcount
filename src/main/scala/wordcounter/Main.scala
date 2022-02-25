package wordcounter

import wordcounter.types._
import zhttp.service.Server
import zio._
import zio.concurrent.ConcurrentMap
import zio.duration._
import zio.stream._

object Main extends App {

  val slidingWindow = 15.seconds

  val program: ZIO[ZEnv, Nothing, ExitCode] =
    for {
      wordMap  <- ConcurrentMap.empty[Word, ConcurrentMap[EventType, WordData]]
      server    = Server.start(8080, WordCount.server(wordMap))
      now      <- clock.instant
      _        <- console.putStrLn(s"Writing events to file...").ignore
      _        <- EventEmitter.writeEventsToFile(now).orDie
      _        <- console.putStrLn("Starting program...").ignore
      exitCode <- ZStream
                    .mergeAllUnbounded()(
                      WordCount.processEvents(slidingWindow, wordMap),
                      ZStream.fromEffect(server)
                    )
                    .runDrain
                    .forever
                    .exitCode
    } yield exitCode

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    program.exitCode

}
