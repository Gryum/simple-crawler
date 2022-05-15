package io.github.gryum.crawler.model

import java.net.UnknownHostException

import io.github.gryum.crawler.{CrawlUrl, Title}
import org.jsoup.HttpStatusException
import spray.json.DefaultJsonProtocol

final case class CrawlResponse(successes: Seq[SuccessCrawlResult], failures: Seq[ErrorCrawlResult])

object CrawlResponse extends DefaultJsonProtocol {

  implicit val successCrawlerResultFormat = jsonFormat2(SuccessCrawlResult.apply)
  implicit val errorCrawlerResultFormat = jsonFormat2[CrawlUrl, Title, ErrorCrawlResult](ErrorCrawlResult.apply)
  implicit val crawlResponseFormat = jsonFormat2(CrawlResponse.apply)
}

final case class SuccessCrawlResult(url: CrawlUrl, title: Title)

final case class ErrorCrawlResult(url: CrawlUrl, error: String)

object ErrorCrawlResult {
  def apply(url: CrawlUrl, t: Throwable): ErrorCrawlResult = ErrorCrawlResult(url, throwableToString(t))

  private def throwableToString(t: Throwable) = t match {
    case _: IllegalArgumentException => "Invalid uri format"
    case e: UnknownHostException => s"Unknown host ${e.getMessage}"
    case e: HttpStatusException => e.getMessage
    //TODO: more specified exceptions
    case e: Throwable => s"Unknown exception occurred while trying to crawl web resource: ${e.getMessage}"
  }
}
