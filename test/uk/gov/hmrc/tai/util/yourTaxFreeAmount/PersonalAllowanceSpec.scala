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

package uk.gov.hmrc.tai.util.yourTaxFreeAmount

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.tai.model.domain.{ChildBenefit, PersonalAllowanceAgedPAA, PersonalAllowanceElderlyPAE, PersonalAllowancePA}

class PersonalAllowanceSpec extends PlaySpec {

  "method" should {
    "return true" when {
      "componentType is a AllowanceType" in {
        PersonalAllowance.isA(PersonalAllowancePA) mustBe true
        PersonalAllowance.isA(PersonalAllowanceAgedPAA) mustBe true
        PersonalAllowance.isA(PersonalAllowanceElderlyPAE) mustBe true
      }
    }

    "return false" when {
      "componentType is NOT a AllowanceType" in {
        PersonalAllowance.isA(ChildBenefit) mustBe false
      }
    }
  }

}
