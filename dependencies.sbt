// Runtime dependencies
libraryDependencies ++= Seq(
"org.scala-lang.modules" %% "scala-xml" % "1.0.6",
"org.openrdf.sesame" % "sesame-runtime" % "2.7.9",
"com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
   // Exclude conflicting transitive dependency
   excludeAll(
      ExclusionRule(organization = "org.scala-lang", name = "scala-reflect")
   ),
"org.slf4j" % "slf4j-api" % "1.7.12",
"ch.qos.logback" % "logback-classic" % "1.0.13",
"org.ccil.cowan.tagsoup" % "tagsoup" % "1.2",
"joda-time" % "joda-time" % "2.8.2",
"org.joda" % "joda-convert" % "1.7",
"org.eclipse.rdf4j" % "rdf4j-runtime" % "2.2.2",
"com.github.pathikrit" %% "better-files" % "3.4.0",

// Test dependencies
"org.scalatest" %% "scalatest" % "3.0.1" % "test"
   // Exclude conflicting transitive dependency
   excludeAll(
      ExclusionRule(organization = "org.scala-lang", name = "scala-reflect")
)
)
