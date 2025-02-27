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

package uk.gov.hmrc.tai.model.domain

import play.api.libs.json.Json

case class BankAccount(
  id: Int,
  accountNumber: Option[String],
  sortCode: Option[String],
  bankName: Option[String],
  grossInterest: BigDecimal,
  source: Option[String]) {

  val formattedSortCode: Option[String] = sortCode.map(_.sliding(2, 2).mkString(" "))
}

object BankAccount {
  implicit val Format = Json.format[BankAccount]
}
