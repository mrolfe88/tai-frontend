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

package uk.gov.hmrc.tai.util

import controllers.FakeTaiPlayApplication
import org.joda.time.LocalDate
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.play.language.LanguageUtils.Dates

import play.api.i18n.Messages.Implicits._
import play.api.i18n.MessagesApi
import uk.gov.hmrc.tai.model.TaxYear

class TaxYearRangeUtilSpec extends PlaySpec with FakeTaiPlayApplication {

  implicit val messages: MessagesApi = app.injector.instanceOf[MessagesApi]

  "Tax year range util " must {

    "return the current tax year as a range delimited with the word 'to' " in {
      val expectedTaxYear = messages(
        "tai.taxYear",
        HtmlFormatter.htmlNonBroken(TaxYear().start.toString("d MMMM yyyy")),
        HtmlFormatter.htmlNonBroken(TaxYear().end.toString("d MMMM yyyy")))

      TaxYearRangeUtil.currentTaxYearRange mustBe expectedTaxYear
    }

    "return the current tax year as a range delimited with the word 'to' formatted on one line " in {
      val expectedTaxYear = HtmlFormatter.htmlNonBroken(
        messages(
          "tai.taxYear",
          HtmlFormatter.htmlNonBroken(TaxYear().start.toString("d MMMM yyyy")),
          HtmlFormatter.htmlNonBroken(TaxYear().end.toString("d MMMM yyyy"))))

      TaxYearRangeUtil.currentTaxYearRangeSingleLine mustBe expectedTaxYear
    }

    "return the current tax year as a range delimited with the word 'and' " in {
      val expectedTaxYear = messages(
        "tai.taxYear.between",
        HtmlFormatter.htmlNonBroken(TaxYear().start.toString("d MMMM yyyy")),
        HtmlFormatter.htmlNonBroken(TaxYear().end.toString("d MMMM yyyy"))
      )

      TaxYearRangeUtil.currentTaxYearRangeBetweenDelimited mustBe expectedTaxYear
    }

    "return the current tax year as a range delimited with the word 'and' formatted on one line" in {
      val expectedTaxYear = HtmlFormatter.htmlNonBroken(
        messages(
          "tai.taxYear.between",
          HtmlFormatter.htmlNonBroken(TaxYear().start.toString("d MMMM yyyy")),
          HtmlFormatter.htmlNonBroken(TaxYear().end.toString("d MMMM yyyy"))
        ))

      TaxYearRangeUtil.currentTaxYearRangeSingleLineBetweenDelimited mustBe expectedTaxYear
    }

    "return the current tax year as a range that only contains the year" in {
      val expectedTaxYear = messages("tai.taxYear", TaxYear().start.toString("yyyy"), TaxYear().end.toString("yyyy"))

      TaxYearRangeUtil.currentTaxYearRangeYearOnly mustBe expectedTaxYear
    }

    "given two dates return a formatted string" in {
      val now = new LocalDate()
      val endOfTaxYear = TaxYear().end
      val expectedNow = HtmlFormatter.htmlNonBroken(Dates.formatDate(now))
      val expectedEnd = HtmlFormatter.htmlNonBroken(Dates.formatDate(endOfTaxYear))

      TaxYearRangeUtil.dynamicDateRange(now, endOfTaxYear) mustBe s"$expectedNow to $expectedEnd"
    }

    "throw an exception if 'from' date is after the 'to' date" in {
      val now = new LocalDate()
      val yesterday = now.minusDays(1)

      val caught = intercept[IllegalArgumentException] {
        TaxYearRangeUtil.dynamicDateRange(now, yesterday)
      }

      caught.getMessage mustBe s"From date:$now cannot be after To date:$yesterday"
    }

    "futureTaxYearRangeHtmlNonBreak" when {
      "returns a future date" in {
        val thisYear = TaxYear().next
        val message = s"${Dates.formatDate(thisYear.start)} to ${Dates.formatDate(thisYear.end)}"
        val nonBrokenDateRange = s"${HtmlFormatter.htmlNonBroken(message)}"

        TaxYearRangeUtil.futureTaxYearRangeHtmlNonBreak(1) mustBe nonBrokenDateRange
      }
    }
  }
}
