name := "phash-hierarchical-clustering"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies += "com.github.haifengl" %% "smile-scala" % "1.3.1" exclude("xpp3", "xpp3_min")
libraryDependencies += "net.liftweb" %% "lift-json" % "3.0.1"

mainClass in Compile := Some("com.brandwatch.ClusterDisplay")