/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import builders.UserBuilder
import controllers.auth.AuthedUser
import mocks.{MockPartialRetriever, MockTemplateRenderer}
import org.scalatestplus.play.PlaySpec
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, _}
import play.twirl.api.Html
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer

import scala.concurrent.Future

trait ControllerViewTestHelper extends PlaySpec {

  implicit val authedUser: AuthedUser = UserBuilder()
  implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
  implicit val partialRetriever: FormPartialRetriever = MockPartialRetriever

  implicit class ViewMatcherHelper(result: Future[Result]) {
    def rendersTheSameViewAs(expected: Html): Unit =
      contentAsString(result) must equal(expected.toString)
  }
}
