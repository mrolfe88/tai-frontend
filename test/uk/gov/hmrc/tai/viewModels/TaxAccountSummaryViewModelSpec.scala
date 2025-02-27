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

import controllers.FakeTaiPlayApplication
import org.joda.time.LocalDate
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.model.domain.income._
import uk.gov.hmrc.tai.service.ThreeWeeks
import uk.gov.hmrc.tai.util.{HtmlFormatter, TaxYearRangeUtil}
import uk.gov.hmrc.tai.util.constants.TaiConstants._
import utils.TaxAccountSummaryTestData

class TaxAccountSummaryViewModelSpec
    extends PlaySpec with FakeTaiPlayApplication with I18nSupport with TaxAccountSummaryTestData {
  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "TaxAccountSummaryViewModel apply method" must {
    "return a view model" which {

      val currentTaxYearRange = Messages(
        "tai.heading.taxYear.interval",
        HtmlFormatter.htmlNonBroken(TaxYear().start.toString("d MMMM yyyy")),
        HtmlFormatter.htmlNonBroken(TaxYear().end.toString("d MMMM yyyy"))
      )

      "has header relating to current tax year" in {
        val expectedHeader =
          Messages("tai.incomeTaxSummary.heading.part1", TaxYearRangeUtil.currentTaxYearRangeSingleLine)

        val sut = TaxAccountSummaryViewModel(taxAccountSummary, ThreeWeeks, nonTaxCodeIncome, noIncomesSources, Seq())
        sut.header mustBe expectedHeader
      }

      "has title relating to current tax year" in {
        val expectedTitle =
          Messages("tai.incomeTaxSummary.heading.part1", TaxYearRangeUtil.currentTaxYearRangeSingleLine)
        val sut = TaxAccountSummaryViewModel(taxAccountSummary, ThreeWeeks, nonTaxCodeIncome, noIncomesSources, Seq())
        sut.title mustBe expectedTitle
      }

      "has correctly formatted positive tax free amount and estimated income" when {
        "taxAccountSummary has positive values" in {
          val sut = TaxAccountSummaryViewModel(taxAccountSummary, ThreeWeeks, nonTaxCodeIncome, noIncomesSources, Seq())
          sut.taxFreeAmount mustBe "£2,222"
          sut.estimatedIncomeTaxAmount mustBe "£1,111"
        }
      }

      "has correctly formatted negative tax free amount and estimated income" when {
        "taxAccountSummary has a negative values" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(-54321, -12345, 333.32, 444.44, 111.11),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.taxFreeAmount mustBe s"$encodedMinusSign£12,345"
          sut.estimatedIncomeTaxAmount mustBe s"$encodedMinusSign£54,321"
        }
      }

      "has correctly formatted zero tax free amount and estimated income" when {
        "taxAccountSummary has zero values" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.taxFreeAmount mustBe "£0"
          sut.estimatedIncomeTaxAmount mustBe "£0"
        }
      }

      "has correctly formatted lastTaxYearEnd" in {
        val expectedLastTaxYearEnd = TaxYear().end.minusYears(1).toString("d MMMM yyyy")
        val sut = TaxAccountSummaryViewModel(taxAccountSummary, ThreeWeeks, nonTaxCodeIncome, noIncomesSources, Seq())
        sut.lastTaxYearEnd mustBe expectedLastTaxYearEnd
      }

      "has empty employments, pensions and ceasedEmployments list" when {
        "taxCodeIncomes and employments are empty" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.employments mustBe Seq.empty[IncomeSourceViewModel]
          sut.pensions mustBe Seq.empty[IncomeSourceViewModel]
          sut.ceasedEmployments mustBe Seq.empty[IncomeSourceViewModel]
        }

        "taxCodeIncomes doesn't have the corresponding element as employments, and none of the employments have an end date" in {
          val employmentsWithNoEndDate = employments.map(_.copy(endDate = None))
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.employments mustBe Seq.empty[IncomeSourceViewModel]
          sut.pensions mustBe Seq.empty[IncomeSourceViewModel]
          sut.ceasedEmployments mustBe Seq.empty[IncomeSourceViewModel]
        }

        "employments doesn't have the corresponding element as taxCodeIncomes" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.employments mustBe Seq.empty[IncomeSourceViewModel]
          sut.pensions mustBe Seq.empty[IncomeSourceViewModel]
          sut.ceasedEmployments mustBe Seq.empty[IncomeSourceViewModel]
        }
      }
      "has an employment list" when {
        "taxCodeIncomes and employments have matching employments" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            incomeSources,
            Seq())

          sut.employments.size mustBe 2
          sut.employments.head.name mustBe "Employer name1"
          sut.employments(1).name mustBe "Employer name2"
        }
      }
      "has a pension list" when {
        "taxCodeIncomes and employments have matching pensions" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            incomeSources,
            Seq())
          sut.pensions.size mustBe 2
          sut.pensions.head.name mustBe "Pension name1"
          sut.pensions(1).name mustBe "Pension name2"
        }
      }
      "has a ceased employment list" when {
        "taxCodeIncomes and employments have matching ceased and potentially ceased employments" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            incomeSources.copy(liveEmploymentIncomeSources = Seq(), livePensionIncomeSources = Seq()),
            Seq())
          sut.ceasedEmployments.size mustBe 2
          sut.ceasedEmployments.head.name mustBe "Employer name3"
          sut.ceasedEmployments(1).name mustBe "Employer name4"
        }
      }
      "has the iya banner boolean set to true" when {
        "tax account summary shows an in year adjustment value greater than zero" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0.01, 0.02, 0.01),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.displayIyaBanner mustBe true
        }
      }
      "has the iya banner boolean set to false" when {
        "tax account summary shows an in year adjsutment value of zero or less" in {
          val sut = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, 0, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sut.displayIyaBanner mustBe false
          val sutNeg = TaxAccountSummaryViewModel(
            TaxAccountSummary(0, 0, -0.01, 0, 0),
            ThreeWeeks,
            nonTaxCodeIncome,
            noIncomesSources,
            Seq())
          sutNeg.displayIyaBanner mustBe false
        }
      }

      "has the other income sources with links" when {
        "other income sources with untaxed interest are available and bank accounts are not available" in {
//          val otherIncomeSourceViewModel1 = otherIncomeSourceViewModel.copy(name = Messages("tai.typeDecodes.UntaxedInterestIncome"), amount = "£100",
//            detailsLinkLabel = Messages("tai.bbsi.viewDetails"), detailsLinkUrl = controllers.income.bbsi.routes.BbsiController.untaxedInterestDetails().url,
//            displayDetailsLink = false)
          val otherIncomeSourceViewModel2 = otherIncomeSourceViewModel.copy(
            name = "Profit",
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(OtherIncomeIform).url
          )

          val sut = TaxAccountSummaryViewModel(taxAccountSummary, ThreeWeeks, nonTaxCodeIncome, noIncomesSources, Seq())

          sut.otherIncomeSources mustBe Seq(otherIncomeSourceViewModel2)
        }

        "other income sources with untaxed interest are available and bank accounts are also available" in {
//          val otherIncomeSourceViewModel1 = otherIncomeSourceViewModel.copy(name = Messages("tai.typeDecodes.UntaxedInterestIncome"), amount = "£100",
//            detailsLinkLabel = Messages("tai.bbsi.viewDetails"), detailsLinkUrl = controllers.income.bbsi.routes.BbsiController.untaxedInterestDetails().url,
//            displayDetailsLink = true)
          val otherIncomeSourceViewModel2 = otherIncomeSourceViewModel.copy(
            name = "Profit",
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(OtherIncomeIform).url
          )

          val bankAccounts = Seq(BankAccount(1, Some("ACCNo"), None, None, 100, None))
          val nonTaxCodeIncomeWithBankAccounts = NonTaxCodeIncome(
            Some(
              uk.gov.hmrc.tai.model.domain.income
                .UntaxedInterest(UntaxedInterestIncome, None, 100, "Untaxed Interest", bankAccounts)),
            Seq(
              OtherNonTaxCodeIncome(Profit, None, 100, "Profit")
            )
          )

          val sut = TaxAccountSummaryViewModel(
            taxAccountSummary,
            ThreeWeeks,
            nonTaxCodeIncomeWithBankAccounts,
            noIncomesSources,
            Seq())
          sut.otherIncomeSources mustBe Seq(otherIncomeSourceViewModel2)
        }

        "other income sources without untaxed interest are available" in {
          val nonTaxCodeIncomeWithOutUntaxedInterest = nonTaxCodeIncome.copy(untaxedInterest = None)
          val otherIncomeSourceViewModel1 = otherIncomeSourceViewModel.copy(
            name = "Profit",
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(OtherIncomeIform).url
          )

          val sut = TaxAccountSummaryViewModel(
            taxAccountSummary,
            ThreeWeeks,
            nonTaxCodeIncomeWithOutUntaxedInterest,
            noIncomesSources,
            Seq())

          sut.otherIncomeSources mustBe Seq(otherIncomeSourceViewModel1)
        }

        "multiple other income with untaxed interest are present" in {
//          val otherIncomeSourceViewModel1 = otherIncomeSourceViewModel.copy(name = Messages("tai.typeDecodes.UntaxedInterestIncome"), amount = "£100",
//            detailsLinkLabel = Messages("tai.bbsi.viewDetails"), detailsLinkUrl = controllers.income.bbsi.routes.BbsiController.untaxedInterestDetails().url,
//            displayDetailsLink = true)
          val otherIncomeSourceViewModel2 = otherIncomeSourceViewModel.copy(
            name = "Tips",
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(OtherIncomeIform).url
          )
          val otherIncomeSourceViewModel3 = otherIncomeSourceViewModel.copy(
            name = Messages("tai.typeDecodes.OccupationalPension"),
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(EmployeePensionIForm).url
          )
          val otherIncomeSourceViewModel4 = otherIncomeSourceViewModel.copy(
            name = Messages("tai.typeDecodes.UkDividend"),
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(InvestIncomeIform).url
          )
          val otherIncomeSourceViewModel5 = otherIncomeSourceViewModel.copy(
            name = Messages("tai.typeDecodes.JobSeekersAllowance"),
            amount = "£100",
            detailsLinkLabel = Messages("tai.updateOrRemove"),
            detailsLinkUrl = controllers.routes.AuditController.auditLinksToIForm(StateBenefitsIform).url
          )

          val bankAccounts = Seq(BankAccount(1, Some("ACCNo"), None, None, 100, None))

          val nonTaxCodeIncomeWithBankAccounts = NonTaxCodeIncome(
            Some(
              uk.gov.hmrc.tai.model.domain.income
                .UntaxedInterest(UntaxedInterestIncome, None, 100, "Untaxed Interest", bankAccounts)),
            Seq(
              OtherNonTaxCodeIncome(Tips, None, 100, "Tips"),
              OtherNonTaxCodeIncome(OccupationalPension, None, 100, "OccupationalPension"),
              OtherNonTaxCodeIncome(UkDividend, None, 100, "UkDividend"),
              OtherNonTaxCodeIncome(JobSeekersAllowance, None, 100, "JobSeekersAllowance")
            )
          )

          val sut = TaxAccountSummaryViewModel(
            taxAccountSummary,
            ThreeWeeks,
            nonTaxCodeIncomeWithBankAccounts,
            noIncomesSources,
            Seq())

          sut.otherIncomeSources mustBe Seq(
            otherIncomeSourceViewModel2,
            otherIncomeSourceViewModel3,
            otherIncomeSourceViewModel4,
            otherIncomeSourceViewModel5)

        }
      }

      "has no other income sources" when {
        "there is no other income sources are available" in {
          val nonTaxCodeIncomeWithOutAnything = NonTaxCodeIncome(None, Seq.empty[OtherNonTaxCodeIncome])
          val sut = TaxAccountSummaryViewModel(
            taxAccountSummary,
            ThreeWeeks,
            nonTaxCodeIncomeWithOutAnything,
            noIncomesSources,
            Seq())
          sut.otherIncomeSources mustBe Seq.empty[IncomeSourceViewModel]
        }
      }
    }

    "return a view model instance for an end dated employment record that has no matching TaxCodeIncomeSource record" in {
      val sut = TaxAccountSummaryViewModel(
        TaxAccountSummary(0, 0, 0, 0, 0),
        ThreeWeeks,
        nonTaxCodeIncome,
        noIncomesSources,
        Seq(ceasedEmployment.copy(sequenceNumber = nonMatchingSequenceNumber)))
      sut.ceasedEmployments.size mustBe 1
      sut.ceasedEmployments(0) mustBe IncomeSourceViewModel(
        name = "Ceased employer name",
        amount = "£123",
        taxCode = "",
        displayTaxCode = false,
        payrollNumber = "123ABC",
        displayPayrollNumber = true,
        endDate = "21 April 2018",
        displayEndDate = true,
        detailsLinkLabel = Messages("tai.incomeTaxSummary.employment.link"),
        detailsLinkUrl =
          controllers.routes.YourIncomeCalculationController.yourIncomeCalculationPage(nonMatchingSequenceNumber).url,
        true
      )
    }
  }

  "IncomeSourceViewModel apply method (two parameter version)" must {
    "return a viewmodel" which {
      "has the name field coming from employment model" in {
        val sut = IncomeSourceViewModel(taxCodeIncome, employment)
        sut.name mustBe employment.name
      }
      "has the amount field as positive formatted value coming from taxCodeIncome model" in {
        val sut = IncomeSourceViewModel(taxCodeIncome, employment)
        sut.amount mustBe "£1,111"
      }
      "has the amount field as negative formatted value coming from taxCodeIncome model" in {
        val taxCodeIncomeNegative = taxCodeIncome.copy(amount = -1111)
        val sut = IncomeSourceViewModel(taxCodeIncomeNegative, employment)
        sut.amount mustBe s"$encodedMinusSign£1,111"
      }
      "has the amount field as zero formatted value coming from taxCodeIncome model" in {
        val taxCodeIncomeZero = taxCodeIncome.copy(amount = 0)
        val sut = IncomeSourceViewModel(taxCodeIncomeZero, employment)
        sut.amount mustBe "£0"
      }
      "has the displayTaxCode field as true" when {
        "employment status is live" in {
          val sut = IncomeSourceViewModel(taxCodeIncome, employment)
          sut.displayTaxCode mustBe true
        }
      }
      "has the displayTaxCode field as false" when {
        "employment status is not live" in {
          val ceasedTaxCodeIncome = taxCodeIncome.copy(status = Ceased)
          val potantiallyCasedTaxCodeIncome = taxCodeIncome.copy(status = PotentiallyCeased)
          val sut1 = IncomeSourceViewModel(ceasedTaxCodeIncome, employment)
          val sut2 = IncomeSourceViewModel(potantiallyCasedTaxCodeIncome, employment)
          sut1.displayTaxCode mustBe false
          sut2.displayTaxCode mustBe false
        }
      }
      "has the payrollNumber field as empty and displayPayrollNumber as false" when {
        "employment model doesn't have payrollNo" in {
          val employmentWithoutPayrollNo = employment.copy(payrollNumber = None)
          val sut = IncomeSourceViewModel(taxCodeIncome, employmentWithoutPayrollNo)
          sut.payrollNumber mustBe ""
          sut.displayPayrollNumber mustBe false
        }
      }
      "has the payrollNumber field as the same as employment model and displayPayrollNumber as true" when {
        "employment model has payrollNo" in {
          val sut = IncomeSourceViewModel(taxCodeIncome, employment)
          sut.payrollNumber mustBe "123ABC"
          sut.displayPayrollNumber mustBe true
        }
      }
      "has the endDate field as empty and displayEndDate as false" when {
        "employment model doesn't have endDate and employment status is live" in {
          val employmentWithoutPayrollNo = employment.copy(endDate = None)
          val sut = IncomeSourceViewModel(taxCodeIncome, employmentWithoutPayrollNo)
          sut.endDate mustBe ""
          sut.displayEndDate mustBe false
        }
        "employment model has endDate and employment status is live" in {
          val employmentWithoutPayrollNo = employment.copy(endDate = Some(new LocalDate(2018, 4, 21)))
          val sut = IncomeSourceViewModel(taxCodeIncome, employmentWithoutPayrollNo)
          sut.endDate mustBe "21 April 2018"
          sut.displayEndDate mustBe false
        }
        "employment model doesn't have endDate and employment status is ceased" in {
          val employmentWithoutPayrollNo = employment.copy(endDate = None)
          val taxCodeIncomeCeased = taxCodeIncome.copy(status = Ceased)
          val sut = IncomeSourceViewModel(taxCodeIncomeCeased, employmentWithoutPayrollNo)
          sut.endDate mustBe ""
          sut.displayEndDate mustBe false
        }
        "employment model doesn't have endDate and employment status is potentially ceased" in {
          val employmentWithoutPayrollNo = employment.copy(endDate = None)
          val taxCodeIncomePotentiallyCeased = taxCodeIncome.copy(status = PotentiallyCeased)
          val sut = IncomeSourceViewModel(taxCodeIncomePotentiallyCeased, employmentWithoutPayrollNo)
          sut.endDate mustBe ""
          sut.displayEndDate mustBe false
        }
      }
      "has formatted endDate field as same as employment model and displayEndDate as true" when {
        "employment model has endDate and employment status is ceased" in {
          val taxCodeIncomeCeased = taxCodeIncome.copy(status = Ceased)
          val sut = IncomeSourceViewModel(taxCodeIncomeCeased, employment)
          sut.endDate mustBe "21 April 2018"
          sut.displayEndDate mustBe true
        }
        "employment model has endDate and employment status is potentially ceased" in {
          val taxCodeIncomePotentiallyCeased = taxCodeIncome.copy(status = PotentiallyCeased)
          val sut = IncomeSourceViewModel(taxCodeIncomePotentiallyCeased, employment)
          sut.endDate mustBe "21 April 2018"
          sut.displayEndDate mustBe true
        }
      }
      "has details link with employment and benefits label" when {
        "income source type is employment" in {
          val sut = IncomeSourceViewModel(taxCodeIncome, employment)
          sut.detailsLinkLabel mustBe Messages("tai.incomeTaxSummary.employmentAndBenefits.link")
          sut.detailsLinkUrl mustBe controllers.routes.IncomeSourceSummaryController
            .onPageLoad(employment.sequenceNumber)
            .url
        }
      }
      "has details link with employment only label for ceased employment" when {
        "income source type is employment" in {
          val sut = IncomeSourceViewModel(taxCodeIncomeCeased, ceasedEmployment)
          sut.detailsLinkLabel mustBe Messages("tai.incomeTaxSummary.employment.link")
          sut.detailsLinkUrl mustBe controllers.routes.YourIncomeCalculationController.yourIncomeCalculationPage(1).url
        }
      }
      "has details link with pension label" when {
        "income source type is pension" in {
          val pension = taxCodeIncome.copy(componentType = PensionIncome)
          val sut = IncomeSourceViewModel(pension, employment)
          sut.detailsLinkLabel mustBe Messages("tai.incomeTaxSummary.pension.link")
          sut.detailsLinkUrl mustBe controllers.routes.IncomeSourceSummaryController
            .onPageLoad(employment.sequenceNumber)
            .url
        }
      }
      "has details link with income label" when {
        "income source type is not pension or employment" in {
          val pension = taxCodeIncome.copy(componentType = JobSeekerAllowanceIncome)
          val sut = IncomeSourceViewModel(pension, employment)
          sut.detailsLinkLabel mustBe Messages("tai.incomeTaxSummary.income.link")
        }
      }
    }
  }

  "IncomeSourceViewModel single parameter apply method (Employment instance only)" must {
    "return a viewmodel" which {
      "has the name field coming from employment model" in {
        val sut = IncomeSourceViewModel(ceasedEmployment)
        sut.name mustBe ceasedEmployment.name
      }
      "has the amount field as the latest payment 'amountYearToDate' value" in {
        val sut = IncomeSourceViewModel(ceasedEmployment)
        sut.amount mustBe "£123"
      }
      "does not display any message" when {
        "the latest annual account is absent" in {
          val sut = IncomeSourceViewModel(ceasedEmployment.copy(annualAccounts = Nil))
          sut.amount mustBe ""
        }
        "the latest payment is None" in {
          val sut =
            IncomeSourceViewModel(ceasedEmployment.copy(annualAccounts = Seq(annualAccount.copy(payments = Nil))))
          sut.amount mustBe ""
        }
      }
      "has an empty taxCode field, and a 'false' value corrresponding boolean set to instruct non display" in {
        val sut = IncomeSourceViewModel(employment)
        sut.taxCode mustBe ""
        sut.displayTaxCode mustBe false
      }
      "has a payrollNumber field and the corresponding boolean set to instruct display" when {
        "employment model has payrollNo" in {
          val sut = IncomeSourceViewModel(ceasedEmployment)
          sut.payrollNumber mustBe "123ABC"
          sut.displayPayrollNumber mustBe true
        }
      }
      "has no payrollNumber field and the corresponding boolean set to instruct non display" when {
        "employment model has payrollNo" in {
          val sut = IncomeSourceViewModel(ceasedEmployment.copy(payrollNumber = None))
          sut.payrollNumber mustBe ""
          sut.displayPayrollNumber mustBe false
        }
      }
      "has the endDate field as empty and the corresponding boolean set to instruct non display" when {
        "employment model doesn't have endDate" in {
          val sut = IncomeSourceViewModel(ceasedEmployment.copy(endDate = None))
          sut.endDate mustBe ""
          sut.displayEndDate mustBe false
        }
      }
      "has a formatted endDate field and the corresponding boolean set to instruct display" when {
        "employment model has  endDate" in {
          val sut = IncomeSourceViewModel(ceasedEmployment)
          sut.endDate mustBe "21 April 2018"
          sut.displayEndDate mustBe true
        }
      }
      "has details link with employment only label " in {
        val sut = IncomeSourceViewModel(ceasedEmployment)
        sut.detailsLinkLabel mustBe Messages("tai.incomeTaxSummary.employment.link")
        sut.detailsLinkUrl mustBe controllers.routes.YourIncomeCalculationController.yourIncomeCalculationPage(1).url
      }
    }
  }

  val employments: Seq[Employment] = Seq(
    empEmployment1,
    empEmployment2,
    pensionEmployment3,
    pensionEmployment4,
    Employment(
      "JobSeekerAllowance name1",
      Some("5ABC"),
      new LocalDate(2017, 3, 1),
      None,
      Seq.empty[AnnualAccount],
      "DIST5",
      "PAYE5",
      5,
      None,
      false,
      false),
    Employment(
      "JobSeekerAllowance name2",
      Some("6ABC"),
      new LocalDate(2017, 3, 1),
      None,
      Seq.empty[AnnualAccount],
      "DIST6",
      "PAYE6",
      6,
      None,
      false,
      false),
    Employment(
      "OtherIncome name1",
      Some("7ABC"),
      new LocalDate(2017, 3, 1),
      None,
      Seq.empty[AnnualAccount],
      "DIST7",
      "PAYE7",
      7,
      None,
      false,
      false),
    Employment(
      "OtherIncome name2",
      Some("8ABC"),
      new LocalDate(2017, 3, 1),
      None,
      Seq.empty[AnnualAccount],
      "DIST8",
      "PAYE8",
      8,
      None,
      false,
      false),
    empEmployment9,
    empEmployment10,
    Employment(
      "Pension name3",
      Some("11ABC"),
      new LocalDate(2017, 3, 1),
      None,
      Seq.empty[AnnualAccount],
      "DIST11",
      "PAYE11",
      11,
      None,
      false,
      false),
    Employment(
      "Pension name4",
      Some("12ABC"),
      new LocalDate(2017, 3, 1),
      Some(new LocalDate(2018, 4, 21)),
      Seq.empty[AnnualAccount],
      "DIST12",
      "PAYE12",
      12,
      None,
      false,
      false)
  )

  val emptyTaxCodeIncomes = Seq.empty[TaxCodeIncome]
  val emptyEmployments = Seq.empty[Employment]

  val otherIncomeSourceViewModel = IncomeSourceViewModel(
    "",
    "",
    "",
    displayTaxCode = false,
    "",
    displayPayrollNumber = false,
    "",
    displayEndDate = false,
    "",
    "",
    displayDetailsLink = true
  )

  val noIncomesSources = IncomesSources(Seq(), Seq(), Seq())
  val incomeSources =
    IncomesSources(livePensionIncomeSources, liveEmploymentIncomeSources, ceasedEmploymentIncomeSources)

}
