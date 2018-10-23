/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.tai.connectors

import play.api.Mode.Mode
import play.api.{Configuration, Play}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.tai.config.TaiConfig

import scala.concurrent.Future

trait SessionConnector extends TaiUrls with TaiConfig {

  def httpHandler: HttpHandler

  def invalidateCache()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    httpHandler.deleteFromApi(invalidateCacheUrl())
  }

}
// $COVERAGE-OFF$
object SessionConnector extends SessionConnector {
  override val httpHandler: HttpHandler = HttpHandler

  override protected val mode: Mode = Play.current.mode

  override protected val runModeConfiguration: Configuration = Play.current.configuration
}
// $COVERAGE-ON$