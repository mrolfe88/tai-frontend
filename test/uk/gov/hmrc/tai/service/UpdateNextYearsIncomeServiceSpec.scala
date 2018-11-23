/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.Matchers
import org.mockito.Mockito.{times, verify, when}
import org.mockito.Matchers.{any, eq => Meq}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.connectors.JourneyCacheConnector
import uk.gov.hmrc.tai.connectors.responses.TaiSuccessResponse
import uk.gov.hmrc.tai.model.TaxYear
import uk.gov.hmrc.tai.model.cache.UpdateNextYearsIncomeCacheModel
import uk.gov.hmrc.tai.model.domain.income._
import uk.gov.hmrc.tai.model.domain.{Employment, EmploymentIncome}
import uk.gov.hmrc.tai.util.constants.journeyCache.UpdateNextYearsIncomeConstants
import uk.gov.hmrc.time.TaxYearResolver
import utils.WireMockHelper

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

class UpdateNextYearsIncomeServiceSpec extends PlaySpec with MockitoSugar with WireMockHelper {

  "get" must {
    "return journey data" when {
      "cache does not exist" in {
        val nino = generateNino
        val updateNextYearsIncomeService = newSUT

        when(
          updateNextYearsIncomeService.employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any())
        ).thenReturn(
          Future.successful(Some(employment(employmentName)))
        )

        when(updateNextYearsIncomeService.taxAccountService.taxCodeIncomeForEmployment(
          Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any())
        ).thenReturn(
          Future.successful(Some(taxCodeIncome(employmentName, employmentId, employmentAmount)))
        )

        val connectorResponse = UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, None)
        when(
          updateNextYearsIncomeService.cacheConnector.getCache[UpdateNextYearsIncomeCacheModel](Meq(UpdateNextYearsIncomeConstants.JOURNEY_KEY))(any(), any())
        ).thenReturn(
          Future.successful(None)
        )

        when(
          updateNextYearsIncomeService.journeyCacheService.cache(Meq(connectorResponse.toCacheMap))(any())
        ).thenReturn(Future.successful(Map.empty[String, String]))

        val result = Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

        verify(updateNextYearsIncomeService.journeyCacheService, times(1)).cache(
          expectedMap(employmentName, employmentId, isPension, employmentAmount)
        )

        verify(updateNextYearsIncomeService.journeyCacheService, times(1)).cache(
          connectorResponse.toCacheMap
        )

        result mustBe connectorResponse

      }

      "cache does exist" must {
        "returns the cached value" in {
          val nino = generateNino
          val updateNextYearsIncomeService = newSUT

          when(
            updateNextYearsIncomeService.cacheConnector.getCache[UpdateNextYearsIncomeCacheModel](Meq(UpdateNextYearsIncomeConstants.JOURNEY_KEY))(any(), any())
          ).thenReturn(
            Future.successful(
              Some(UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount))
            )
          )

          val result = Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

          result mustBe UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, None)
        }
      }

      "throw a runtime exception" when {
        "could not retrieve a TaxCodeIncome" in {
          val nino = generateNino
          val updateNextYearsIncomeService = newSUT

          when(
            updateNextYearsIncomeService.cacheConnector.getCache[UpdateNextYearsIncomeCacheModel](Meq(UpdateNextYearsIncomeConstants.JOURNEY_KEY))(any(), any())
          ).thenReturn(
            Future.successful(None)
          )

          when(updateNextYearsIncomeService.employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any()))
            .thenReturn(Future.successful(Some(employment(employmentName))))

          when(updateNextYearsIncomeService.taxAccountService.taxCodeIncomeForEmployment(
            Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any())
          ).thenReturn(Future.successful(None))

          val result: RuntimeException = the[RuntimeException] thrownBy Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

          result.getMessage mustBe "[UpdateNextYearsIncomeService] Could not set up next years estimated income journey"
        }

        "could not retrieve a Employment" in {
          val nino = generateNino
          val updateNextYearsIncomeService = newSUT

          when(
            updateNextYearsIncomeService.cacheConnector.getCache[UpdateNextYearsIncomeCacheModel](Meq(UpdateNextYearsIncomeConstants.JOURNEY_KEY))(any(), any())
          ).thenReturn(
            Future.successful(None)
          )

          when(updateNextYearsIncomeService.employmentService.employment(Matchers.eq(nino), Matchers.eq(employmentId))(any()))
            .thenReturn(Future.successful(None))

          when(updateNextYearsIncomeService.taxAccountService.taxCodeIncomeForEmployment(
            Matchers.eq(nino), Matchers.eq(TaxYear().next), Matchers.eq(employmentId))(any())
          ).thenReturn(Future.successful(Some(taxCodeIncome(employmentName, employmentId, employmentAmount))))

          val result: RuntimeException = the[RuntimeException] thrownBy Await.result(updateNextYearsIncomeService.get(employmentId, nino), 5.seconds)

          result.getMessage mustBe "[UpdateNextYearsIncomeService] Could not set up next years estimated income journey"
        }
      }
    }
  }

  "setNewAmount" must {
    "update the cache with the new amount" when {
      "journey values are successfully retrieved from the cache" in {
        val nino = generateNino
        val updateNextYearsIncomeService = newSUT

        when(updateNextYearsIncomeService.cacheConnector.getCache[UpdateNextYearsIncomeCacheModel](any())(any(), any())
        ).thenReturn(
          Future.successful(
            Some(UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount))
          )
        )

        val result = Await.result(updateNextYearsIncomeService.setNewAmount(employmentAmount.toString, employmentId, nino), 5.seconds)

        result mustBe UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, Some(employmentAmount))

        verify(updateNextYearsIncomeService.journeyCacheService, times(1))
          .cache(fullMap(employmentName, employmentId, isPension, employmentAmount))
      }
    }
  }

  "submit" must {
    "post the values from cache to the tax account" in {
      val nino = generateNino
      val service = newSUT
      val newAmount = employmentAmount + 1

      when(
        service.cacheConnector.getCache[UpdateNextYearsIncomeCacheModel](any())(any(), any())
      ).thenReturn(
        Future.successful(
          Some(UpdateNextYearsIncomeCacheModel(employmentName, employmentId, isPension, employmentAmount, Some(newAmount)))
        )
      )

      when(
        service.journeyCacheService.currentCache(any())
      ).thenReturn(
        Future.successful(fullMap(employmentName, employmentId, false, employmentAmount))
      )

      when(
        service.taxAccountService.updateEstimatedIncome(
          Meq(nino), Meq(newAmount), Meq(TaxYear().next), Meq(employmentId)
        )(any())
      ).thenReturn(
        Future.successful(TaiSuccessResponse)
      )

      val result = Await.result(service.submit(employmentId, nino), 5.seconds)

      verify(
        service.taxAccountService, times(1)
      ).updateEstimatedIncome(
        Meq(nino), Meq(newAmount), Meq(TaxYear().next), Meq(employmentId)
      )(any())

    }
  }

  private def employment(name: String): Employment = {
    Employment(
      name = name,
      payrollNumber = None,
      startDate = TaxYearResolver.startOfCurrentTaxYear,
      endDate = None,
      annualAccounts = Seq.empty,
      taxDistrictNumber = "123",
      payeNumber = "321",
      sequenceNumber = 1,
      hasPayrolledBenefit = false,
      receivingOccupationalPension = false,
      cessationPay = None
    )
  }

  private def taxCodeIncome(name: String, id: Int, amount: Int): TaxCodeIncome = {
    TaxCodeIncome(EmploymentIncome, Some(id), amount, "description", "1185L", name, OtherBasisOfOperation, Live)
  }

  private def expectedMap(name: String, id: Int, isPension: Boolean, amount: Int): Map[String, String] = {
    Map(
      UpdateNextYearsIncomeConstants.EMPLOYMENT_NAME -> name,
      UpdateNextYearsIncomeConstants.EMPLOYMENT_ID -> id.toString,
      UpdateNextYearsIncomeConstants.IS_PENSION -> isPension.toString,
      UpdateNextYearsIncomeConstants.CURRENT_AMOUNT -> amount.toString
    )
  }

  private def fullMap(name: String, id: Int, isPension: Boolean, amount: Int): Map[String, String] = {
    expectedMap(name, id, isPension, amount) ++ Map(UpdateNextYearsIncomeConstants.NEW_AMOUNT -> amount.toString)
  }

  private val employmentName = "employmentName"
  private val employmentId = 1
  private val isPension = false
  private val employmentAmount = 1000
  private def generateNino: Nino = new Generator(new Random).nextNino

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  def newSUT = new UpdateNextYearsIncomeService {
    override lazy val journeyCacheService: JourneyCacheService = mock[JourneyCacheService]
    override lazy val cacheConnector: JourneyCacheConnector = mock[JourneyCacheConnector]
    override lazy val employmentService: EmploymentService = mock[EmploymentService]
    override lazy val taxAccountService: TaxAccountService = mock[TaxAccountService]
  }
}
