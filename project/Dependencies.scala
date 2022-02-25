import sbt._

object Dependencies {

  val CirceVersion = "0.14.1"
  val ZIOVersion   = "1.0.13"

  lazy val zio =
    Seq(
      "dev.zio" %% "zio"               % ZIOVersion,
      "dev.zio" %% "zio-concurrent"    % ZIOVersion,
      "dev.zio" %% "zio-test"          % ZIOVersion,
      "dev.zio" %% "zio-test-magnolia" % ZIOVersion,
      "io.d11"  %% "zhttp"             % "1.0.0.0-RC25"
    )

  lazy val circe =
    Seq(
      "io.circe" %% "circe-core"           % CirceVersion,
      "io.circe" %% "circe-generic"        % CirceVersion,
      "io.circe" %% "circe-generic-extras" % CirceVersion,
      "io.circe" %% "circe-parser"         % CirceVersion
    )

  lazy val all = zio ++ circe
}
