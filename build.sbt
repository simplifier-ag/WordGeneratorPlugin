ThisBuild / organization := "io.simplifier"
ThisBuild / version := sys.env.get("VERSION").getOrElse("NA")
ThisBuild / scalaVersion := "2.12.15"

ThisBuild / useCoursier := true


lazy val wordGeneratorPlugin = (project in file("."))
  .settings(
    name := "wordGeneratorPlugin",
    assembly / assemblyJarName := "wordGeneratorPlugin.jar",
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    libraryDependencies ++= Seq(
      "org.scalatest"           %% "scalatest"                            % "3.0.4"  % "test" withSources() withJavadoc(),
      "org.mockito"             %% "mockito-scala"                        % "1.17.7" % Test,
      "fr.opensagres.xdocreport" % "fr.opensagres.poi.xwpf.converter.pdf" % "2.0.2"           withSources() withJavadoc(),
      "io.github.simplifier-ag" %% "simplifier-plugin-base"               % "1.0.0"           withSources()
    )
  )

//Security Options for Java >= 18
val moduleSecurityRuntimeOptions = Seq(
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/sun.security.jca=ALL-UNNAMED",
  // used by W3CXmlUtil
  "--add-exports=java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED"
)

run / javaOptions ++= moduleSecurityRuntimeOptions
run / fork := true