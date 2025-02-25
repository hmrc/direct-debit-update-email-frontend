/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.directdebitupdateemailfrontend.testsupport

import ddUpdateEmail.crypto.CryptoFormat.OperationalCryptoFormat
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.{DefaultTestServerFactory, RunningServer}
import play.api.{Application, Mode}
import play.core.server.ServerConfig
import uk.gov.hmrc.crypto.{AesCrypto, Decrypter, Encrypter}
import uk.gov.hmrc.directdebitupdateemailfrontend.testsupport.stubs.AuditStub
import uk.gov.hmrc.http.HttpReadsInstances

class ItSpec extends UnitSpec, GuiceOneServerPerSuite, WireMockSupport, HttpReadsInstances, CommonBehaviour {

  override def beforeEach(): Unit = {
    super.beforeEach()
    AuditStub.audit()
    ()
  }

  val testPort: Int = 19001

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(300, Millis)), interval = scaled(Span(2, Seconds)))

  protected lazy val configOverrides: Map[String, Any] = Map()

  implicit val testCrypto: Encrypter with Decrypter = new AesCrypto {
    override protected val encryptionKey: String = "P5xsJ9Nt+quxGZzB4DeLfw=="
  }

  val testOperationCryptoFormat: OperationalCryptoFormat = OperationalCryptoFormat(testCrypto)

  protected lazy val configMap: Map[String, Any] = Map[String, Any](
    "logger.application"                                           -> "INFO",
    "logger.connector"                                             -> "INFO",
    "microservice.services.auth.port"                              -> WireMockSupport.port,
    "microservice.services.direct-debit-update-email-backend.port" -> WireMockSupport.port,
    "microservice.services.direct-debit-backend.port"              -> WireMockSupport.port,
    "microservice.services.payments-email-verification.port"       -> WireMockSupport.port,
    "auditing.consumer.baseUri.port"                               -> WireMockSupport.port,
    "auditing.enabled"                                             -> true,
    "auditing.traceRequests"                                       -> false
  ) ++ configOverrides

  lazy val modules: List[GuiceableModule] =
    List(
      bind[OperationalCryptoFormat].toInstance(testOperationCryptoFormat)
    )

  // in tests use `app`
  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(modules: _*)
    .disable(classOf[ddUpdateEmail.module.CryptoModule])
    .configure(configMap)
    .build()

  override implicit protected lazy val runningServer: RunningServer =
    TestServerFactory.start(app)

  object TestServerFactory extends DefaultTestServerFactory {
    override protected def serverConfig(app: Application): ServerConfig = {
      val sc = ServerConfig(port = Some(testPort), sslPort = Some(0), mode = Mode.Test, rootDir = app.path)
      sc.copy(configuration = sc.configuration.withFallback(overrideServerConfiguration(app)))
    }
  }

}
