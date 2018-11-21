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

package utils.factories

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.tai.model.domain.TaxCodeMismatch

object TaxCodeMismatchFactory {


  def matchedTaxCodes: TaxCodeMismatch = {
    TaxCodeMismatch(
      mismatchConfirmedTCH = false,
      mismatchUnconfirmedTCH = false,
      confirmed = Seq("1185L"),
      unconfirmed = Seq("1185L"),
      taxCodeHistory = Seq("1185L"))
  }

  def mismatchedOnConfirmed: TaxCodeMismatch = {
    TaxCodeMismatch(
      mismatchConfirmedTCH = true,
      mismatchUnconfirmedTCH = false,
      confirmed = Seq("0T"),
      unconfirmed = Seq("1185L"),
      taxCodeHistory = Seq("1185L"))
  }

  def mismatchedOnUnconfirmed: TaxCodeMismatch = {
    TaxCodeMismatch(
      mismatchConfirmedTCH = false,
      mismatchUnconfirmedTCH = true,
      confirmed = Seq("1185L"),
      unconfirmed = Seq("0T"),
      taxCodeHistory = Seq("1185L"))
  }

  def matchedTaxCodeJson: JsObject = {
    Json.obj(
      "data" -> Json.obj(
        "mismatchConfirmedTCH" -> false,
        "mismatchUnconfirmedTCH" -> false,
        "confirmed" -> Json.arr("1185L"),
        "unconfirmed" -> Json.arr("1185L"),
        "taxCodeHistory" -> Json.arr("1185L")
      )
    )
  }

  def mismatchedOnConfirmedJson: JsObject = {
    Json.obj(
      "data" -> Json.obj(
        "mismatchConfirmedTCH" -> true,
        "mismatchUnconfirmedTCH" -> false,
        "confirmed" -> Json.arr("0T"),
        "unconfirmed" -> Json.arr("1185L"),
        "taxCodeHistory" -> Json.arr("1185L")
      )
    )
  }

  def mismatchedOnUnconfirmedJson: JsObject = {
    Json.obj(
      "data" -> Json.obj(
        "mismatchConfirmedTCH" -> false,
        "mismatchUnconfirmedTCH" -> true,
        "confirmed" -> Json.arr("1185L"),
        "unconfirmed" -> Json.arr("0T"),
        "taxCodeHistory" -> Json.arr("1185L")
      )
    )
  }
}
