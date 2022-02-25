ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"

lazy val root = (project in file("."))
  .settings(
    name                 := "wordcounter",
    run / fork           := true,
    run / connectInput   := true,
    cancelable in Global := true,
    scalacOptions       ++= CompilerSettings.master,
    libraryDependencies ++= Dependencies.all
  )

addCommandAlias("runProgram", "runMain wordcounter.Main")