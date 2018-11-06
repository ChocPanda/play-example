package io.panda.example

import io.panda.example.controllers.tools.AuthenticatedAction
import io.panda.example.controllers.{ ExampleController, ExampleControllerComponents, ExampleControllerComponentsImpl }
import io.panda.example.model.dao.im.IMDao
import com.softwaremill.macwire._
import io.panda.example.model.UserId
import io.panda.example.model.dto.ExampleDto
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator }
import play.filters.HttpFiltersComponents
import play.api.routing.Router
import router.Routes

class ServiceLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    LoggerConfigurator(context.environment.classLoader) foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }

    new ExampleServiceApplication(context).application
  }
}

class ExampleServiceApplication(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents {
  self =>

  lazy val ExampleDao    = new IMDao[UserId, ExampleDto]()
  lazy val authenticator = new AuthenticatedAction(self.defaultBodyParser)

  lazy val listingsControllerComponents: ExampleControllerComponents =
    wire[ExampleControllerComponentsImpl]

  lazy val listingsController = new ExampleController(listingsControllerComponents)

  lazy val router: Router = {
    val prefix = "/"
    wire[Routes]
  }
}
