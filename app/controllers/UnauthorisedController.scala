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

import com.google.inject.Inject
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.auth.ConfigProperties
import uk.gov.hmrc.tai.config.ApplicationConfig
import uk.gov.hmrc.tai.util.ViewModelHelper

import scala.concurrent.Future

class UnauthorisedController @Inject()(override implicit val partialRetriever: FormPartialRetriever,
                                       override implicit val templateRenderer: TemplateRenderer) extends TaiBaseController {

  def onPageLoad: Action[AnyContent] = Action {
    implicit request =>
      Ok(unauthorisedView())
  }

  def login: Action[AnyContent] = Action.async {
    implicit request =>
      ggRedirect
  }

  private def ggRedirect(implicit request: Request[_]): Future[Result] = {
    val postSignInUpliftUrl = s"${ViewModelHelper.urlEncode(ApplicationConfig.pertaxServiceUrl)}/do-uplift?redirectUrl=${ViewModelHelper.
      urlEncode(ConfigProperties.postSignInRedirectUrl.
        getOrElse(controllers.routes.WhatDoYouWantToDoController.whatDoYouWantToDoPage().url))}"

    lazy val ggSignIn = s"${ApplicationConfig.
      companyAuthUrl}/${ApplicationConfig.gg_web_context}/sign-in?continue=${postSignInUpliftUrl}&accountType=individual"

    Future.successful(Redirect(ggSignIn))
  }

  private def unauthorisedView()(implicit request: Request[_])
  = views.html.error_template_noauth(
    Messages("global.error.InternalServerError500.title"),
    Messages("tai.technical.error.heading"),
    Messages("tai.technical.error.message"))
}
