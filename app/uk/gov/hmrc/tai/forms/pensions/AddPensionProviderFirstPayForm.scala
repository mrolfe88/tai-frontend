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

package uk.gov.hmrc.tai.forms.pensions

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import uk.gov.hmrc.tai.util.constants.AddPensionFirstPayChoiceConstants

object AddPensionProviderFirstPayForm extends AddPensionFirstPayChoiceConstants {

  def form(implicit messages: Messages): Form[Option[String]] = Form[Option[String]](
    single(
      FirstPayChoice ->
        optional(text).verifying(Messages("tai.error.chooseOneOption"), { _.isDefined }))
  )
}
