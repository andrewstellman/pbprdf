lazy val root = (project in file("."))
.settings(
  name := "pbprdf",
  organization := "com.stellmangreene",
  version := "1.0.0",

  // Scala options
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq("-feature"),
  fork in run := true,

  // Java options
  javaOptions ++= Seq("-Xms2G", "-Xmx8G", "-XX:MaxPermSize=8G"),

  // sbt options
  logLevel := Level.Info,
)

// sbt-eclipse settings
EclipseKeys.withSource := true

