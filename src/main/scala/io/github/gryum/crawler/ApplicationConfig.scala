package io.github.gryum.crawler

import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter}

object ApplicationConfig {

  import pureconfig.generic.auto._

  def load(config: Config): ConfigReader.Result[ApplicationConfig] = {
    val pureConfigSource = ConfigSource.fromConfig(config)
    pureConfigSource.at("application").load[ApplicationConfig]
  }

  def load(): ConfigReader.Result[ApplicationConfig] = {
    load(ConfigFactory.load())
  }
}

//TODO: add another params for Jsoup (maxBodySize etc.)?
final case class ApplicationConfig(host: String, port: Int, crawlTimeout: Int) {

  import pureconfig.generic.auto._

  def toDebugString: String = ConfigWriter[ApplicationConfig].to(this).render()
}
