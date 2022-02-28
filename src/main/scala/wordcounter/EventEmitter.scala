package wordcounter

import io.circe.syntax._
import wordcounter.types._
import zio._
import zio.stream._
import zio.test._
import zio.test.magnolia._

import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.time.Instant

// Not part of the task. Just emitting some events because I couldn't run the binary.
object EventEmitter {

  private val genEventType =
    Gen.elements("CREDIT_TRANSFER", "WIRE_TRANSFER", "CARD_CAPTURE", "DIRECT_DEBIT").map(EventType.apply)

  private val genData =
    Gen.elements("transaction", "payment", "debt", "payout", "direct payment", "fast transfer")

  private val genEvent =
    for {
      eventType <- genEventType
      data      <- genData
      timestamp <- implicitly[DeriveGen[Instant]].derive
    } yield Event(eventType, data, timestamp)

  def writeEventsToFile(now: Instant): ZIO[ZEnv, Throwable, Long] =
    Gen
      .listOfN(50)(genEvent)
      .sample
      .mapConcat(_.value)
      .zipWithIndex
      .map { case (ev, idx) => ev.copy(timestamp = now.plusSeconds(idx)) }
      .mapConcat { v =>
        val event   = s"${v.asJson.noSpaces}\n"
        val badData = "garbage\n"
        (event + badData).getBytes(StandardCharsets.UTF_8)
      }
      .run(ZSink.fromFile(Paths.get("events.txt")))
      .provideSomeLayer[ZEnv](Sized.live(100))

}
