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

package uk.gov.hmrc.tai.service

import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils.Dates
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.util.{HtmlFormatter}

object TaxPeriodLabelService {

  def taxPeriodLabel(year: Int)(implicit messages: Messages): String =
    s"${HtmlFormatter.htmlNonBroken(Dates.formatDate(TaxYear(year).start))} ${messages("language.to")} " +
      s"${HtmlFormatter.htmlNonBroken(Dates.formatDate(TaxYear(year).end))}"

  def taxPeriodLabelSingleLine(year: Int)(implicit messages: Messages): String = {

    val dateFrom = Dates.formatDate(TaxYear(year).start)
    val dateTo = Dates.formatDate(TaxYear(year).end)
    val messageto = messages("language.to")
    val message = s"$dateFrom $messageto $dateTo"

    s"${HtmlFormatter.htmlNonBroken(message)}"
  }

}
