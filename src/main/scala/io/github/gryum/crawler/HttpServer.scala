package io.github.gryum.crawler

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

import akka.actor.typed.{ActorSystem, DispatcherSelector}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import io.github.gryum.crawler.model.{CrawlResponse, ErrorCrawlResult, SuccessCrawlResult}
import org.jsoup.Jsoup

object HttpServer {
  def apply(applicationConfig: ApplicationConfig)
           (implicit system: ActorSystem[_]): HttpServer = new HttpServer(applicationConfig.crawlTimeout)
}

class HttpServer(crawlTimeout: Int)
                (implicit system: ActorSystem[_]) extends LazyLogging {

  import CrawlResponse._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private implicit val executionContext: ExecutionContextExecutor = system.executionContext
  private val crawlBlockingDispatcher =
    system.dispatchers.lookup(DispatcherSelector.fromConfig("crawl-blocking-dispatcher"))

  lazy val route: Route =
    (pathPrefix("crawler-api" / "v1" / "crawler") & pathEndOrSingleSlash) {
      post {
        entity(as[Seq[CrawlUrl]]) { urls =>
          logger.debug(s"New crawl request for urls: [${urls.mkString(", ")}]")

          def crawlWithLogging(url: CrawlUrl): Future[Title] = withLoggingResult(url, crawlEventually(url))

          val crawlResponse =
            partitionAsyncResults(urls, crawlWithLogging, SuccessCrawlResult.apply, ErrorCrawlResult.apply) map {
              case (crawlResults, failures) =>
                logger.debug(s"Crawl request is being completed with ${crawlResults.size} success results" +
                  s" and ${failures.size} failures results.")
                CrawlResponse(crawlResults, failures)
            }
          complete(crawlResponse)
        }
      }
    }

  //TODO: log full crawl time
  private def crawlEventually(crawlUrl: CrawlUrl): Future[Title] =
    Future(Jsoup.connect(crawlUrl).timeout(crawlTimeout).get().title())(crawlBlockingDispatcher)

  private def withLoggingResult[T](origUrl: String, eventualResult: Future[T]): Future[T] =
    eventualResult.transform {
      case r@Success(title) =>
        logger.trace(s"[$origUrl] Web resource was successfully parsed, title: [$title]")
        r
      case r@Failure(t) =>
        logger.warn(s"[$origUrl] Unable to parse web resource", t)
        r
    }

  private def partitionAsyncResults[A, T, TSuccess, TFailure](origValues: Seq[A], eventualProcess: A => Future[T],
                                                              successMapper: (A, T) => TSuccess,
                                                              failureMapper: (A, Throwable) => TFailure): Future[(Seq[TSuccess], Seq[TFailure])] = {
    val guardedFutures = origValues map { origValue =>
      eventualProcess(origValue).transform {
        case Success(result) => Success(Right(origValue -> result))
        case Failure(t) => Success(Left(origValue -> t))
      }
    }

    val allFuturesResults = Future.sequence(guardedFutures)

    allFuturesResults map { results =>
      val (successes, failures) = results.partition(_.isRight)
      val s = successes collect {
        case Right((origValue, result)) => successMapper(origValue, result)
      }

      val f = failures collect {
        case Left((origValue, f)) => failureMapper(origValue, f)
      }
      (s, f)
    }
  }
}
