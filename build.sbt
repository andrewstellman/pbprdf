scalaVersion := "2.11.7"
scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
   // Runtime dependencies
   "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
   "org.openrdf.sesame" % "sesame-runtime" % "2.7.9",
   "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
      // Exclude conflicting transitive dependency
      excludeAll(
         ExclusionRule(organization = "org.scala-lang", name = "scala-reflect")
      ),
   "org.slf4j" % "slf4j-api" % "1.7.12",
   "ch.qos.logback" % "logback-classic" % "1.0.13",
   "org.ccil.cowan.tagsoup" % "tagsoup" % "1.2",
   "joda-time" % "joda-time" % "2.8.2",
   "org.joda" % "joda-convert" % "1.7",

   // Test dependencies
   "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
      // Exclude conflicting transitive dependency
      excludeAll(
         ExclusionRule(organization = "org.scala-lang", name = "scala-reflect")
      )
)

// Enable sbt-dependency-graph plugin (because it's not an auto-plugin)
net.virtualvoid.sbt.graph.Plugin.graphSettings

// sbt-eclipse settings
EclipseKeys.withSource := true

