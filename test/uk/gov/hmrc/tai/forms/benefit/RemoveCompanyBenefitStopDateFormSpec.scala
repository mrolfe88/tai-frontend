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

package uk.gov.hmrc.tai.forms.benefit

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import uk.gov.hmrc.tai.forms.benefits.RemoveCompanyBenefitStopDateForm
import uk.gov.hmrc.tai.util.constants.FormValuesConstants

class RemoveCompanyBenefitStopDateFormSpec
    extends PlaySpec with OneAppPerSuite with I18nSupport with FormValuesConstants {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val choice = RemoveCompanyBenefitStopDateForm.StopDateChoice
  private val form = RemoveCompanyBenefitStopDateForm.form

  "RemoveCompanyBenefitStopDateFormSpec" must {
    "return no errors with valid 'yes' choice" in {
      val validYesChoice = Json.obj(choice -> YesValue)
      val validatedForm = form.bind(validYesChoice)

      validatedForm.errors mustBe empty
      validatedForm.value.get mustBe Some(YesValue)
    }

    "return no errors with valid 'no' choice" in {
      val validNoChoice = Json.obj(choice -> NoValue)
      val validatedForm = form.bind(validNoChoice)

      validatedForm.errors mustBe empty
      validatedForm.value.get mustBe Some(NoValue)
    }

    "return an error for invalid choice" in {
      val invalidChoice = Json.obj(choice -> "")
      val invalidatedForm = form.bind(invalidChoice)

      invalidatedForm.errors.head.messages mustBe List(Messages("tai.error.chooseOneOption"))
      invalidatedForm.value mustBe None
    }

  }
}
