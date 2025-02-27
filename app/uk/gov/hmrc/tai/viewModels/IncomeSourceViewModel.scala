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

package uk.gov.hmrc.tai.viewModels

import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils.Dates
import uk.gov.hmrc.play.views.helpers.MoneyPounds
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.model.domain.income.{Live, NonTaxCodeIncome, TaxCodeIncome}
import uk.gov.hmrc.tai.util.ViewModelHelper
import uk.gov.hmrc.tai.util.constants.TaiConstants.{EmployeePensionIForm, InvestIncomeIform, OtherIncomeIform, StateBenefitsIform}

case class IncomeSourceViewModel(
  name: String,
  amount: String,
  taxCode: String,
  displayTaxCode: Boolean,
  payrollNumber: String,
  displayPayrollNumber: Boolean,
  endDate: String,
  displayEndDate: Boolean,
  detailsLinkLabel: String,
  detailsLinkUrl: String,
  displayDetailsLink: Boolean = true)

object IncomeSourceViewModel extends ViewModelHelper {

  def apply(employment: Employment)(implicit messages: Messages): IncomeSourceViewModel = {
    val amountNumeric: BigDecimal = (
      for {
        latestAccount <- employment.latestAnnualAccount
        latestPayment <- latestAccount.latestPayment
      } yield latestPayment.amountYearToDate
    ).getOrElse(0)

    val amountString = if (amountNumeric != BigDecimal(0)) withPoundPrefixAndSign(MoneyPounds(amountNumeric, 0)) else ""

    val endDate: Option[String] = employment.endDate.map(Dates.formatDate(_))

    IncomeSourceViewModel(
      employment.name,
      amountString,
      "",
      displayTaxCode = false,
      employment.payrollNumber.getOrElse(""),
      displayPayrollNumber = employment.payrollNumber.isDefined,
      endDate.getOrElse(""),
      displayEndDate = endDate.isDefined,
      messages("tai.incomeTaxSummary.employment.link"),
      controllers.routes.YourIncomeCalculationController.yourIncomeCalculationPage(employment.sequenceNumber).url
    )
  }

  def apply(incomeSource: TaxedIncome)(implicit messages: Messages): IncomeSourceViewModel = {
    val endDate: Option[String] = incomeSource.employment.endDate.map(Dates.formatDate(_))
    val detailsLinkLabel = incomeSource.taxCodeIncome.componentType match {
      case EmploymentIncome if incomeSource.taxCodeIncome.status == Live =>
        messages("tai.incomeTaxSummary.employmentAndBenefits.link")
      case EmploymentIncome if incomeSource.taxCodeIncome.status != Live =>
        messages("tai.incomeTaxSummary.employment.link")
      case PensionIncome => messages("tai.incomeTaxSummary.pension.link")
      case _             => messages("tai.incomeTaxSummary.income.link")
    }

    val incomeSourceSummaryUrl =
      if (incomeSource.taxCodeIncome.componentType == EmploymentIncome && incomeSource.taxCodeIncome.status != Live) {
        controllers.routes.YourIncomeCalculationController
          .yourIncomeCalculationPage(incomeSource.employment.sequenceNumber)
          .url
      } else {
        controllers.routes.IncomeSourceSummaryController.onPageLoad(incomeSource.employment.sequenceNumber).url
      }

    IncomeSourceViewModel(
      incomeSource.employment.name,
      withPoundPrefixAndSign(MoneyPounds(incomeSource.taxCodeIncome.amount, 0)),
      incomeSource.taxCodeIncome.taxCode,
      incomeSource.taxCodeIncome.status == Live,
      incomeSource.employment.payrollNumber.getOrElse(""),
      incomeSource.employment.payrollNumber.isDefined,
      endDate.getOrElse(""),
      incomeSource.taxCodeIncome.status != Live && endDate.isDefined,
      detailsLinkLabel,
      incomeSourceSummaryUrl
    )
  }

  def apply(taxCodeIncome: TaxCodeIncome, employment: Employment)(
    implicit messages: Messages): IncomeSourceViewModel = {

    val endDate: Option[String] = employment.endDate.map(Dates.formatDate(_))
    val detailsLinkLabel = taxCodeIncome.componentType match {
      case EmploymentIncome if taxCodeIncome.status == Live =>
        messages("tai.incomeTaxSummary.employmentAndBenefits.link")
      case EmploymentIncome if taxCodeIncome.status != Live => messages("tai.incomeTaxSummary.employment.link")
      case PensionIncome                                    => messages("tai.incomeTaxSummary.pension.link")
      case _                                                => messages("tai.incomeTaxSummary.income.link")
    }

    val incomeSourceSummaryUrl =
      if (taxCodeIncome.componentType == EmploymentIncome && taxCodeIncome.status != Live) {
        controllers.routes.YourIncomeCalculationController.yourIncomeCalculationPage(employment.sequenceNumber).url
      } else {
        controllers.routes.IncomeSourceSummaryController.onPageLoad(employment.sequenceNumber).url
      }

    IncomeSourceViewModel(
      employment.name,
      withPoundPrefixAndSign(MoneyPounds(taxCodeIncome.amount, 0)),
      taxCodeIncome.taxCode,
      taxCodeIncome.status == Live,
      employment.payrollNumber.getOrElse(""),
      employment.payrollNumber.isDefined,
      endDate.getOrElse(""),
      taxCodeIncome.status != Live && endDate.isDefined,
      detailsLinkLabel,
      incomeSourceSummaryUrl
    )
  }

  def apply(nonTaxCodeIncome: NonTaxCodeIncome)(implicit messages: Messages): Seq[IncomeSourceViewModel] = {

    val untaxedInterest = nonTaxCodeIncome.untaxedInterest.map(
      u =>
        IncomeSourceViewModel(
          messages("tai.typeDecodes." + u.incomeComponentType.toString),
          withPoundPrefixAndSign(MoneyPounds(u.amount, 0)),
          "",
          displayTaxCode = false,
          "",
          displayPayrollNumber = false,
          "",
          displayEndDate = false,
          messages("tai.bbsi.viewDetails"),
          controllers.income.bbsi.routes.BbsiController.untaxedInterestDetails().url,
          displayDetailsLink = u.bankAccounts.nonEmpty
      ))

    val otherIncomeSources = nonTaxCodeIncome.otherNonTaxCodeIncomes
      .withFilter(
        _.incomeComponentType != BankOrBuildingSocietyInterest
      )
      .map(otherNonTaxCodeIncome => {

        val model = IncomeSourceViewModel(
          messages("tai.typeDecodes." + otherNonTaxCodeIncome.incomeComponentType.toString),
          withPoundPrefixAndSign(MoneyPounds(otherNonTaxCodeIncome.amount, 0)),
          "",
          displayTaxCode = false,
          "",
          displayPayrollNumber = false,
          "",
          displayEndDate = false,
          messages("tai.updateOrRemove"),
          ""
        )
        otherNonTaxCodeIncome.incomeComponentType match {
          case _: OtherIncomes =>
            model.copy(detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(OtherIncomeIform).url)
          case _: TaxableStateBenefits =>
            model.copy(detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(StateBenefitsIform).url)
          case _: EmploymentPensions =>
            model.copy(detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(EmployeePensionIForm).url)
          case _: SavingAndInvestments =>
            model.copy(detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(InvestIncomeIform).url)
          case _ => model.copy(displayDetailsLink = false)
        }
      })
    otherIncomeSources
  }
}
