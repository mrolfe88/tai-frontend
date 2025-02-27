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

import javax.inject.Inject
import controllers.actions.ValidatePerson
import controllers.auth.AuthAction
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.connectors.responses.TaiSuccessResponseWithPayload
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.domain.tax.TotalTax
import uk.gov.hmrc.tai.service._
import uk.gov.hmrc.tai.service.benefits.CompanyCarService
import uk.gov.hmrc.tai.util.Referral
import uk.gov.hmrc.tai.viewModels.PreviousYearUnderpaymentViewModel
import views.html.previousYearUnderpayment

class UnderpaymentFromPreviousYearController @Inject()(
  codingComponentService: CodingComponentService,
  employmentService: EmploymentService,
  companyCarService: CompanyCarService,
  taxAccountService: TaxAccountService,
  authenticate: AuthAction,
  validatePerson: ValidatePerson,
  override implicit val partialRetriever: FormPartialRetriever,
  override implicit val templateRenderer: TemplateRenderer)
    extends TaiBaseController with Referral {

  def underpaymentExplanation = (authenticate andThen validatePerson).async { implicit request =>
    implicit val user = request.taiUser

    val nino = user.nino
    val year = TaxYear()
    val totalTaxFuture = taxAccountService.totalTax(nino, year)
    val employmentsFuture = employmentService.employments(nino, year.prev)
    val codingComponentsFuture = codingComponentService.taxFreeAmountComponents(nino, year)

    for {
      employments      <- employmentsFuture
      codingComponents <- codingComponentsFuture
      totalTax         <- totalTaxFuture
    } yield {
      totalTax match {
        case TaiSuccessResponseWithPayload(totalTax: TotalTax) =>
          val model = PreviousYearUnderpaymentViewModel(codingComponents, employments, totalTax, referer, resourceName)

          Ok(previousYearUnderpayment(model))
        case _ => throw new RuntimeException("Failed to fetch total tax details")
      }
    }

  }
}
