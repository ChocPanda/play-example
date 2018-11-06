// *****************************************************************************
// Projects
// *****************************************************************************

lazy val PlayExample =
  project
    .in(file("."))
    .settings(settings)
    .settings(
      name := "Example Play Web Service",
      version := "1.0",
      libraryDependencies ++= Seq(
        library.cats,
        library.macwire,
        library.playCirce,
        library.shapeless
      ),
      libraryDependencies ++= Seq(
        library.scalacheck    % Test,
        library.playScalaTest % Test,
        library.mockitoCore   % Test
      )
    )
    .enablePlugins(PlayScala)

// *****************************************************************************
// Library dependencies
// *****************************************************************************

val library = new {

  val Version = new {
    val scalacheck    = "1.14.0"
    val cats          = "1.4.0"
    val playScalaTest = "3.1.2"
    val macwire       = "2.3.0"
    val mockitoCore   = "2.18.3"
    val playCirce     = "2610.0"
    val shapeless     = "2.3.3"
  }

  val scalacheck    = "org.scalacheck"           %% "scalacheck"         % Version.scalacheck
  val shapeless     = "com.chuusai"              %% "shapeless"          % Version.shapeless
  val cats          = "org.typelevel"            %% "cats-core"          % Version.cats
  val playScalaTest = "org.scalatestplus.play"   %% "scalatestplus-play" % Version.playScalaTest
  val playCirce     = "com.dripower"             %% "play-circe"         % Version.playCirce
  val macwire       = "com.softwaremill.macwire" %% "macros"             % Version.macwire % "provided"
  val mockitoCore   = "org.mockito"              % "mockito-core"        % Version.mockitoCore
}

// *****************************************************************************
// Commands
// *****************************************************************************

addCommandAlias("fix", "; compile:scalafix; test:scalafix")
addCommandAlias("fixCheck", "; compile:scalafix --check; test:scalafix --check")
addCommandAlias("fmt", "; compile:scalafmt; test:scalafmt; scalafmtSbt")
addCommandAlias("fmtCheck", "; compile:scalafmtCheck; test:scalafmtCheck; scalafmtSbtCheck")
addCommandAlias("styleCheck", "; compile:scalastyle; test:scalastyle")

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
commonSettings ++
scalafmtSettings ++
scalaFixSettings ++
scalastyleSettings

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

lazy val commonSettings =
  Seq(
    scalaVersion := "2.12.7",
    organization := "io.panda",
    organizationName := "Matt Searle",
    startYear := Some(2018),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-Ypartial-unification",
      "-language:higherKinds",
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    wartremoverWarnings in (Compile, compile) ++= Warts.unsafe,
    wartremoverExcluded ++= Seq(
      crossTarget.value / "routes" / "main" / "router" / "Routes.scala",
      crossTarget.value / "routes" / "main" / "router" / "RoutesPrefix.scala",
      crossTarget.value / "routes" / "main" / "controllers" / "ReverseRoutes.scala",
      crossTarget.value / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala"
    )
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val scalaFixSettings =
  Seq(
    libraryDependencies += compilerPlugin(scalafixSemanticdb),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-Ywarn-unused-import",
      "-P:semanticdb:exclude:.*routes.*|.*Routes.*"
    )
  )

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
lazy val scalastyleSettings = {
  Seq(
    scalastyleFailOnError := true,
    scalastyleFailOnWarning := true
  )
}
