package io.github.gryum.crawler

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.StrictLogging

object Main extends App with StrictLogging {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "simple-crawler-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  private val maybeAppConfig = ApplicationConfig.load()

  logger.trace("Parsing configuration...")

  maybeAppConfig match {
    case Right(appConfig) =>
      logger.trace("Configuration was successfully parsed.")
      logger.debug(s"Starting server with applicationConfig:\n${appConfig.toDebugString}")
      val bindingFuture = createServer(appConfig) transform {
        case r@Success(serverBinding) =>
          logger.info(
            s"""Http server now online. Available API:
               |POST http://${serverBinding.localAddress.getHostName}:${serverBinding.localAddress.getPort}/crawler-api/v1/crawler
               |[ "http://url1", "http://url2" ]
               |""".stripMargin)
          r
        case r@Failure(e) =>
          logger.error("Unable to start http server", e)
          r
      }

      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    case Left(errors) =>
      logger.error(s"Unable to parse config after decryption, errors: ${errors.prettyPrint()}")
  }

  private def createServer(applicationConfig: ApplicationConfig): Future[Http.ServerBinding] = {
    val server = HttpServer(applicationConfig)
    Http().newServerAt(applicationConfig.host, applicationConfig.port).bind(server.route)
  }
}
