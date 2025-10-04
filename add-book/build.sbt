name := "add-book"

version := "0.1.0"

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "requests" % "0.9.0",
  "org.jsoup" % "jsoup" % "1.21.1"
)

assembly / mainClass := Some("AddBook")
assembly / assemblyJarName := "add-book.jar"
