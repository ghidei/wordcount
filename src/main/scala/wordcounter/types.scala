package wordcounter

import cats.syntax.all._
import io.circe._
import io.circe.generic.JsonCodec
import io.circe.generic.extras._
import io.circe.generic.extras.semiauto._

import java.time.Instant
import scala.util.Try

object types {

  @ConfiguredJsonCodec
  case class Event(
    @JsonKey("event_type") eventType: EventType,
    data: String,
    timestamp: Instant
  )
  object Event {
    def fromLong(long: Long): Either[String, Instant] = Try(Instant.ofEpochSecond(long)).toEither.leftMap(_.toString)

    implicit val enc: Encoder[Instant] = Encoder[Long].contramap(_.getEpochSecond)
    implicit val dec: Decoder[Instant] = Decoder[Long].emap(fromLong)
    implicit val config: Configuration = Configuration.default
  }

  case class Word(value: String) extends AnyVal
  object Word {
    implicit val e: Encoder[Word] = deriveUnwrappedEncoder
    implicit val d: Decoder[Word] = deriveUnwrappedDecoder
  }

  case class EventType(value: String) extends AnyVal
  object EventType {
    implicit val e: Encoder[EventType] = deriveUnwrappedEncoder
    implicit val d: Decoder[EventType] = deriveUnwrappedDecoder
  }

  @JsonCodec
  final case class WordData(
    eventType: EventType,
    count: Int,
    word: Word,
    timestamp: Instant
  )

}
