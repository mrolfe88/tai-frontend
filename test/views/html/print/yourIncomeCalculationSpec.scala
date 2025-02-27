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

package views.html.print

import org.joda.time.LocalDate
import play.twirl.api.Html
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.model.domain.income._
import uk.gov.hmrc.tai.util.DateHelper
import uk.gov.hmrc.tai.util.viewHelpers.TaiViewSpec
import uk.gov.hmrc.tai.viewModels.{LatestPayment, PaymentDetailsViewModel, YourIncomeCalculationViewModel}

class yourIncomeCalculationSpec extends TaiViewSpec {

  "YourIncomeCalculationView" must {

    behave like pageWithBackLink

    behave like pageWithTitle(s"${messages("tai.yourIncome.heading")} - ${messages("tai.service.navTitle")} - GOV.UK")

    "show details for potentially ceased employment" when {
      "payments are empty" in {
        val model = incomeCalculationViewModel(
          payments = Seq.empty[PaymentDetailsViewModel],
          employmentStatus = PotentiallyCeased)

        def potentiallyCeasedView = views.html.print.yourIncomeCalculation(model)

        doc(potentiallyCeasedView) must haveStrongWithText(
          messages(
            "tai.income.calculation.heading",
            s"${TaxYear().start.toString(dateFormatPattern)}",
            s"${TaxYear().end.toString(dateFormatPattern)}")
        )
      }

      "payments are present" in {
        val model = incomeCalculationViewModel(employmentStatus = PotentiallyCeased)

        def potentiallyCeasedView = views.html.print.yourIncomeCalculation(model)

        doc(potentiallyCeasedView) must haveStrongWithText(
          messages("tai.income.calculation.heading.withRti", model.latestPayment.get.date.toString(dateFormatPattern))
        )
        doc(potentiallyCeasedView) must haveParagraphWithText(
          messages("tai.income.calculation.potentially.ceased.lede"))
      }
    }

    "show details for ceased employment" when {
      "payments are empty" in {
        val model = incomeCalculationViewModel(payments = Seq.empty[PaymentDetailsViewModel], employmentStatus = Ceased)

        def ceasedView = views.html.print.yourIncomeCalculation(model)

        doc(ceasedView) must haveStrongWithText(
          messages(
            "tai.income.calculation.heading",
            s"${TaxYear().start.toString(dateFormatPattern)}",
            s"${TaxYear().end.toString(dateFormatPattern)}")
        )
      }

      "payments are present" in {
        val model = incomeCalculationViewModel(employmentStatus = Ceased)

        def ceasedView = views.html.print.yourIncomeCalculation(model)

        doc(ceasedView) must haveStrongWithText(
          messages("tai.income.calculation.ceased.heading", model.latestPayment.get.date.toString(dateFormatPattern))
        )
        doc(ceasedView) must haveParagraphWithText(
          messages("tai.income.calculation.rti.ceased.emp", s"${DateHelper.toDisplayFormat(model.endDate)}"))
      }
    }

    "show details for live employment" when {
      "payments are empty" in {
        val model = incomeCalculationViewModel(payments = Seq.empty[PaymentDetailsViewModel])

        def liveView = views.html.print.yourIncomeCalculation(model)

        doc(liveView) must haveStrongWithText(
          messages(
            "tai.income.calculation.heading",
            s"${TaxYear().start.toString(dateFormatPattern)}",
            s"${TaxYear().end.toString(dateFormatPattern)}")
        )
      }

      "payments are present" in {
        doc(view) must haveStrongWithText(
          messages("tai.income.calculation.heading.withRti", model.latestPayment.get.date.toString(dateFormatPattern))
        )
      }

      "rti is down" in {
        val model = incomeCalculationViewModel(realTimeStatus = TemporarilyUnavailable)

        def liveView = views.html.print.yourIncomeCalculation(model)

        doc(liveView) must haveParagraphWithText(messages("tai.income.calculation.rtiUnavailableCurrentYear.message"))
        doc(liveView) must haveParagraphWithText(
          messages("tai.income.calculation.rtiUnavailableCurrentYear.message.contact"))
      }

    }

    "show payment details" in {
      doc(view) must haveThWithText(messages("tai.income.calculation.incomeTable.dateHeader"))
      doc(view) must haveThWithText(messages("tai.income.calculation.incomeTable.print.incomeHeader"))
      doc(view) must haveThWithText(messages("tai.income.calculation.incomeTable.print.taxPaidHeader"))
      doc(view) must haveThWithText(messages("tai.income.calculation.incomeTable.print.nationalInsuranceHeader"))

      doc(view) must haveTdWithText(messages("tai.taxFree.total"))
      doc(view) must haveTdWithText(messages("£ " + f"${model.latestPayment.get.amountYearToDate}%,.2f"))
      doc(view) must haveTdWithText(messages("£ " + f"${model.latestPayment.get.taxAmountYearToDate}%,.2f"))
      doc(view) must haveTdWithText(
        messages("£ " + f"${model.latestPayment.get.nationalInsuranceAmountYearToDate}%,.2f"))

      model.payments.foreach { payment =>
        doc(view) must haveTdWithText(payment.date.toString("d MMM yyyy"))
        doc(view) must haveTdWithText("£ " + f"${payment.taxableIncome}%,.2f")
        doc(view) must haveTdWithText("£ " + f"${payment.taxAmount}%,.2f")
        doc(view) must haveTdWithText("£ " + f"${payment.nationalInsuranceAmount}%,.2f")
      }
    }

    "show total not equal message" when {
      "total not equal message is present" in {
        val model = incomeCalculationViewModel(totalNotEqualMessage = Some("Test"))
        def totalNotEqualView = views.html.print.yourIncomeCalculation(model)

        doc(totalNotEqualView) must haveParagraphWithText(model.messageWhenTotalNotEqual.get)
        doc(totalNotEqualView) must haveParagraphWithText(messages("tai.income.calculation.totalNotMatching.message"))
      }
    }

    "show income calculation message" when {
      "employment is live" in {
        val model = incomeCalculationViewModel(
          incomeCalculationMessage = "TEST",
          incomeCalculationEstimateMessage = Some("ESTIMATE"))

        def incomeMessagesView = views.html.print.yourIncomeCalculation(model)

        doc(incomeMessagesView) must haveParagraphWithText(model.incomeCalculationMessage)
      }
    }

    "show income calculation estimate message" when {

      "employment is live" in {
        val model = incomeCalculationViewModel(
          incomeCalculationMessage = "TEST",
          incomeCalculationEstimateMessage = Some("ESTIMATE"))

        def incomeMessagesView = views.html.print.yourIncomeCalculation(model)

        doc(incomeMessagesView) must haveHeadingH4WithText(model.incomeCalculationEstimateMessage.get)
      }

      "employment is ceased" in {
        val model = incomeCalculationViewModel(
          employmentStatus = Ceased,
          incomeCalculationMessage = "TEST",
          incomeCalculationEstimateMessage = Some("ESTIMATE"))

        def incomeMessagesView = views.html.print.yourIncomeCalculation(model)

        doc(incomeMessagesView) must haveHeadingH4WithText(model.incomeCalculationEstimateMessage.get)
      }
    }

    "show payroll messages" when {
      "hasPayrolled benefit is true" in {
        val model = incomeCalculationViewModel(hasPayrolledBenefit = true)

        def payrolledView = views.html.print.yourIncomeCalculation(model)

        doc(payrolledView) must haveParagraphWithText(messages("tai.income.calculation.payrollingBik.message1"))
        doc(payrolledView) must haveParagraphWithText(messages("tai.income.calculation.payrollingBik.message2"))
      }
    }
  }

  lazy val defaultPayments = Seq(
    PaymentDetailsViewModel(new LocalDate().minusWeeks(1), 100, 50, 25),
    PaymentDetailsViewModel(new LocalDate().minusWeeks(2), 100, 50, 25),
    PaymentDetailsViewModel(new LocalDate().minusWeeks(3), 100, 50, 25),
    PaymentDetailsViewModel(new LocalDate().minusWeeks(4), 100, 50, 25)
  )
  lazy val dateFormatPattern = "d MMMM yyyy"
  lazy val model = incomeCalculationViewModel()

  override def view: Html = views.html.print.yourIncomeCalculation(model)

  private def incomeCalculationViewModel(
    realTimeStatus: RealTimeStatus = Available,
    payments: Seq[PaymentDetailsViewModel] = defaultPayments,
    employmentStatus: TaxCodeIncomeSourceStatus = Live,
    employmentType: TaxCodeIncomeComponentType = EmploymentIncome,
    totalNotEqualMessage: Option[String] = None,
    incomeCalculationMessage: String = "",
    incomeCalculationEstimateMessage: Option[String] = None,
    hasPayrolledBenefit: Boolean = false) = {

    val latestPayment =
      if (payments.isEmpty) None else Some(LatestPayment(new LocalDate().minusWeeks(4), 400, 50, 25, Weekly))
    YourIncomeCalculationViewModel(
      2,
      "test employment",
      payments,
      employmentStatus,
      realTimeStatus,
      latestPayment,
      if (employmentStatus == Ceased) Some(LocalDate.parse("2017-08-08")) else None,
      employmentType == PensionIncome,
      totalNotEqualMessage,
      incomeCalculationMessage,
      incomeCalculationEstimateMessage,
      hasPayrolledBenefit
    )
  }
}
