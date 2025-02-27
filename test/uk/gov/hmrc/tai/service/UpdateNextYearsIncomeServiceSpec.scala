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

import org.mockito.{Matchers, Mockito}
import org.mockito.Mockito.{times, verify, when}
import org.mockito.Matchers.{any, eq => Meq}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.connectors.responses.TaiSuccessResponse
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.cache.UpdateNextYearsIncomeCacheModel
import uk.gov.hmrc.tai.model.domain.income._
import uk.gov.hmrc.tai.model.domain.{Employment, EmploymentIncome}
import uk.gov.hmrc.tai.service.journeyCache.JourneyCacheService
import uk.gov.hmrc.tai.util.constants.journeyCache.UpdateNextYearsIncomeConstants
import utils.WireMockHelper

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

class UpdateNextYearsIncomeServiceSpec extends PlaySpec with MockitoSugar with WireMockHelper {

  override def beforeEach: Unit =
    Mockito.reset(successfulJourneyCacheService)

  "setup" must {
    "initialize the journey cache and return the cache model" when {
      "an taxCodeIncome and Employment is returned" in {
        val nino = generateNino

        when(employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(Some(employment(employmentName))))

        when(taxAccountService
          .taxCodeIncomeForEmployment(Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(Some(taxCodeIncome(employmentName, employmentId, employmentAmount))))

        when(journeyCacheService.currentCache(any())).thenReturn(
          Future.successful(Map.empty[String, String])
        )
        val result = Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        verify(journeyCacheService, times(1))
          .cache(expectedMap(employmentName, employmentId, isPension, employmentAmount))

        result mustBe UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, None)
      }
    }

    "throw a runtime exception" when {
      "could not retrieve a TaxCodeIncome" in {
        val nino = generateNino

        when(employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(Some(employment(employmentName))))

        when(taxAccountService
          .taxCodeIncomeForEmployment(Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(None))

        val result: RuntimeException = the[RuntimeException] thrownBy Await
          .result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        result.getMessage mustBe "[UpdateNextYearsIncomeService] Could not set up next years estimated income journey"
      }

      "could not retrieve a Employment" in {
        val nino = generateNino

        when(employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(None))

        when(taxAccountService
          .taxCodeIncomeForEmployment(Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(Some(taxCodeIncome(employmentName, employmentId, employmentAmount))))

        val result: RuntimeException = the[RuntimeException] thrownBy Await
          .result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        result.getMessage mustBe "[UpdateNextYearsIncomeService] Could not set up next years estimated income journey"
      }
    }
  }

  "get" must {
    "retrieve the cached values and returns the cache model" when {
      "journey values exist without a new amount in the cache" in {
        val nino = generateNino

        when(journeyCacheService.currentCache(any())).thenReturn(
          Future.successful(expectedMap(employmentName, employmentId, isPension, employmentAmount))
        )

        val result = Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        result mustBe UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, None)
      }

      "journey values exist with a new amount in the cache" in {
        val nino = generateNino

        when(journeyCacheService.currentCache(any())).thenReturn(
          Future.successful(fullMap(employmentName, employmentId, isPension, employmentAmount))
        )

        val result = Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        result mustBe UpdateNextYearsIncomeCacheModel(
          employmentName,
          employmentId,
          isPension,
          employmentAmount,
          Some(employmentAmount))
      }
    }

    "setup the cache" when {
      "journey values do not exist in the cache" in {
        val nino = generateNino

        when(employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(Some(employment(employmentName))))

        when(taxAccountService
          .taxCodeIncomeForEmployment(Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any()))
          .thenReturn(Future.successful(Some(taxCodeIncome(employmentName, employmentId, employmentAmount))))

        when(journeyCacheService.currentCache(any())).thenReturn(
          Future.successful(Map[String, String]())
        )

        val result = Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        result mustBe UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, None)
      }
    }
  }

  "setNewAmount" must {
    "update the cache with the new amount" when {
      "journey values are successfully retrieved from the cache" in {
        val nino = generateNino

        when(journeyCacheService.currentCache(any())).thenReturn(
          Future.successful(expectedMap(employmentName, employmentId, isPension, employmentAmount))
        )

        val result = Await
          .result(updateNextYearsIncomeService.setNewAmount(employmentAmount.toString, employmentId, nino), 5.seconds)

        result mustBe UpdateNextYearsIncomeCacheModel(
          employmentName,
          employmentId,
          isPension,
          employmentAmount,
          Some(employmentAmount))

        verify(journeyCacheService, times(1))
          .cache(fullMap(employmentName, employmentId, isPension, employmentAmount))
      }
    }
  }

  "submit" must {
    "post the values from cache to the tax account" in {
      val nino = generateNino
      val service = new UpdateNextYearsIncomeServiceTest

      when(
        journeyCacheService.currentCache(any())
      ).thenReturn(
        Future.successful(fullMap(employmentName, employmentId, false, employmentAmount))
      )

      when(
        taxAccountService.updateEstimatedIncome(
          Meq(nino),
          Meq(employmentAmount),
          Meq(TaxYear().next),
          Meq(employmentId)
        )(any())
      ).thenReturn(
        Future.successful(TaiSuccessResponse)
      )

      Await.result(service.submit(employmentId, nino), 5.seconds)

      verify(
        taxAccountService,
        times(1)
      ).updateEstimatedIncome(
        Meq(nino),
        Meq(employmentAmount),
        Meq(TaxYear().next),
        Meq(employmentId)
      )(any())
    }

    "cache as a successful journey" in {
      val nino = generateNino
      val service = new UpdateNextYearsIncomeServiceTest

      when(
        journeyCacheService.currentCache(any())
      ).thenReturn(
        Future.successful(fullMap(employmentName, employmentId, false, employmentAmount))
      )

      when(
        taxAccountService.updateEstimatedIncome(
          Meq(nino),
          Meq(employmentAmount),
          Meq(TaxYear().next),
          Meq(employmentId)
        )(any())
      ).thenReturn(
        Future.successful(TaiSuccessResponse)
      )

      Await.result(service.submit(employmentId, nino), 5.seconds)

      verify(successfulJourneyCacheService, times(1)).cache(Map(UpdateNextYearsIncomeConstants.SUCCESSFUL -> "true"))
    }
  }

  "isEstimatedPayJourneyComplete" must {
    "be true when a journey is successful" in {
      val service = new UpdateNextYearsIncomeServiceTest

      when(successfulJourneyCacheService.currentCache(any()))
        .thenReturn(Future.successful(Map(UpdateNextYearsIncomeConstants.SUCCESSFUL -> "true")))

      val result = Await.result(service.isEstimatedPayJourneyComplete, 5.seconds)
      result mustBe true
    }

    "be false when a journey is incomplete" in {
      val service = new UpdateNextYearsIncomeServiceTest

      when(successfulJourneyCacheService.currentCache(any())).thenReturn(Future.successful(Map.empty[String, String]))

      val result = Await.result(service.isEstimatedPayJourneyComplete, 5.seconds)
      result mustBe false
    }
  }

  private def employment(name: String): Employment =
    Employment(
      name = name,
      payrollNumber = None,
      startDate = TaxYear().start,
      endDate = None,
      annualAccounts = Seq.empty,
      taxDistrictNumber = "123",
      payeNumber = "321",
      sequenceNumber = 1,
      hasPayrolledBenefit = false,
      receivingOccupationalPension = false,
      cessationPay = None
    )

  private def taxCodeIncome(name: String, id: Int, amount: Int): TaxCodeIncome =
    TaxCodeIncome(EmploymentIncome, Some(id), amount, "description", "1185L", name, OtherBasisOfOperation, Live)

  private def expectedMap(name: String, id: Int, isPension: Boolean, amount: Int): Map[String, String] =
    Map(
      UpdateNextYearsIncomeConstants.EMPLOYMENT_NAME -> name,
      UpdateNextYearsIncomeConstants.EMPLOYMENT_ID   -> id.toString,
      UpdateNextYearsIncomeConstants.IS_PENSION      -> isPension.toString,
      UpdateNextYearsIncomeConstants.CURRENT_AMOUNT  -> amount.toString
    )

  private def fullMap(name: String, id: Int, isPension: Boolean, amount: Int): Map[String, String] =
    expectedMap(name, id, isPension, amount) ++ Map(UpdateNextYearsIncomeConstants.NEW_AMOUNT -> amount.toString)

  private val employmentName = "employmentName"
  private val employmentId = 1
  private val isPension = false
  private val employmentAmount = 1000

  private def generateNino: Nino = new Generator(new Random).nextNino

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  val employmentService = mock[EmploymentService]
  val taxAccountService = mock[TaxAccountService]
  val journeyCacheService = mock[JourneyCacheService]
  val successfulJourneyCacheService = mock[JourneyCacheService]

  class UpdateNextYearsIncomeServiceTest
      extends UpdateNextYearsIncomeService(
        journeyCacheService,
        successfulJourneyCacheService,
        employmentService,
        taxAccountService
      )

  val updateNextYearsIncomeService = new UpdateNextYearsIncomeServiceTest
}
