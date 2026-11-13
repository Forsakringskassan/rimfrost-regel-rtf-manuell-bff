package se.fk.github.rtfmanuellbff;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.informationsaccess.InformationsaccessEvent;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class RtfManuellBffControllerTest
{

   static final String TEST_ID = "550e8400-e29b-41d4-a716-446655440000";
   static final String UPPGIFT_ID = "8a2b3c4d-5e6f-4a1b-8c2d-3e4f5a6b7c8d";
   static final String REGEL_ID = "a11f3429-3a6b-4389-ba1c-c2747b0fb45b";
   static final String ANVANDARE_TYP_ID = "9b1e4f2a-6c3d-4e8b-a7f5-2d0c1b9e8a36";

   private static final String TASK_DATA = """
         {
           "handlaggning_id": "%s",
           "kund": {
             "fornamn": "Anna",
             "efternamn": "Svensson",
             "kon": "KVINNA",
             "anstallning": {
               "anstallningsdag": "2020-01-01",
               "arbetstid_procent": 100,
               "sista_anstallningsdag": "2025-12-31",
               "organisationsnamn": "Testbolaget AB",
               "organisationsnummer": "556000-0000"
             }
           },
           "ersattningar": [{
             "ersattning_id": "aabb1122-0000-0000-0000-000000000001",
             "ersattningstyp": "SGI",
             "omfattning_procent": 100,
             "belopp": 1000,
             "berakningsgrund": 500,
             "beslutsutfall": "JA",
             "from": "2024-01-01",
             "tom": "2024-01-14",
             "avslagsanledning": null
           }]
         }
         """.formatted(TEST_ID);

   // The regeltjänst wraps its data together with informationsaccess data (FRMM-FR-09.1)
   static final String TASK_BODY = """
         {
           "data": %s,
           "informationsaccess": {
             "uppgiftId": "%s",
             "regelId": "%s",
             "regelVersion": "1",
             "anvandare": {
               "typId": "%s",
               "varde": "H123456"
             }
           }
         }
         """.formatted(TASK_DATA, UPPGIFT_ID, REGEL_ID, ANVANDARE_TYP_ID);

   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
      inMemoryConnector.sink("ilogg-event").clear();
   }

   @Test
   void getTask_returnsTransformedData()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(TASK_BODY)));

      given()
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200)
            .body("handlaggning_id", equalTo(TEST_ID))
            .body("kund.fornamn", equalTo("Anna"))
            .body("kund.kon", equalTo("KVINNA"))
            .body("kund.anstallning.anstallningsdag", equalTo("2020-01-01"))
            .body("kund.anstallning.arbetstid_procent", equalTo(100))
            .body("kund.anstallning.sista_anstallningsdag", equalTo("2025-12-31"))
            .body("ersattningar[0].ersattning_id", equalTo("aabb1122-0000-0000-0000-000000000001"))
            .body("ersattningar[0].omfattning_procent", equalTo(100))
            .body("ersattningar[0].beslutsutfall", equalTo("JA"))
            .body("ersattningar[0].from", equalTo("2024-01-01"))
            .body("ersattningar[0].tom", equalTo("2024-01-14"));
   }

   @Test
   void getTask_returnsEmptyErsattningar_whenBackendSendsNull()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
                          "data": {
                            "handlaggning_id": "%s",
                            "kund": { "fornamn": "Anna" },
                            "ersattningar": null
                          },
                          "informationsaccess": {
                            "uppgiftId": "%s",
                            "regelId": "%s",
                            "regelVersion": "1",
                            "anvandare": { "typId": "%s", "varde": "H123456" }
                          }
                        }
                        """.formatted(TEST_ID, UPPGIFT_ID, REGEL_ID, ANVANDARE_TYP_ID))));

      given()
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200)
            .body("ersattningar.size()", equalTo(0));
   }

   @Test
   void getTask_returns500_whenBackendFails()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse().withStatus(500)));

      given()
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void patchErsattningar_returns204_onSuccess()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse().withStatus(200)));
      WireMockTestResource.getServer().stubFor(post(urlEqualTo("/" + TEST_ID + "/done"))
            .willReturn(aResponse().withStatus(204)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("""
                  {
                    "ersattningar": [{
                      "ersattningId": "aabb1122-0000-0000-0000-000000000001",
                      "beslutsutfall": "JA",
                      "avslagsanledning": null
                    }]
                  }
                  """)
            .when()
            .post("/api/" + TEST_ID + "/patchErsattningar")
            .then()
            .statusCode(204);
   }

   @Test
   void patchErsattningar_returns400_whenBodyIsInvalid()
   {
      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("{\"ersattningar\": []}")
            .when()
            .post("/api/" + TEST_ID + "/patchErsattningar")
            .then()
            .statusCode(400);
   }

   @Test
   void patchErsattningar_returns500_whenPatchFails()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse().withStatus(500)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("""
                  {
                    "ersattningar": [{
                      "ersattningId": "aabb1122-0000-0000-0000-000000000001",
                      "beslutsutfall": "JA",
                      "avslagsanledning": null
                    }]
                  }
                  """)
            .when()
            .post("/api/" + TEST_ID + "/patchErsattningar")
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void patchErsattningar_returns500_whenDoneFails()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse().withStatus(200)));
      WireMockTestResource.getServer().stubFor(post(urlEqualTo("/" + TEST_ID + "/done"))
            .willReturn(aResponse().withStatus(500)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("""
                  {
                    "ersattningar": [{
                      "ersattningId": "aabb1122-0000-0000-0000-000000000001",
                      "beslutsutfall": "JA",
                      "avslagsanledning": null
                    }]
                  }
                  """)
            .when()
            .post("/api/" + TEST_ID + "/patchErsattningar")
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void getUppgiftsbeskrivning_returnsData()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/utokadUppgiftsbeskrivning"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"beskrivning\": \"Kontrollera rätt till försäkring\"}")));

      given()
            .when()
            .get("/api/uppgiftsbeskrivning/RTF_MANUELL")
            .then()
            .statusCode(200)
            .body("beskrivning", equalTo("Kontrollera rätt till försäkring"));
   }

   @Test
   void getUppgiftsbeskrivning_returns503_whenBackendReturnsError()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/utokadUppgiftsbeskrivning"))
            .willReturn(aResponse().withStatus(503)));

      given()
            .when()
            .get("/api/uppgiftsbeskrivning/RTF_MANUELL")
            .then()
            .statusCode(503)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void getUppgiftsbeskrivning_returns500_whenBackendConnectionReset()
   {
      // In this fk-logging version, a connection reset surfaces as a bare NPE from
      // LoggingContextClientResponseFilter with no suppressed IOException, so the framework's
      // masked-network-error detection (GlobalExceptionMapper.isNetworkError) doesn't match it
      // and it falls through to the generic 500 branch rather than 502.
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/utokadUppgiftsbeskrivning"))
            .willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

      given()
            .when()
            .get("/api/uppgiftsbeskrivning/RTF_MANUELL")
            .then()
            .statusCode(500)
            .body("error", equalTo("Internal server error"));
   }

   @Test
   void getTask_forwardsAuthorizationHeaderToBackend()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(TASK_BODY)));

      given()
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200);

      WireMockTestResource.getServer().verify(getRequestedFor(urlEqualTo("/" + TEST_ID))
            .withHeader("Authorization", com.github.tomakehurst.wiremock.client.WireMock.equalTo("Bearer test-token")));
   }

   @Test
   void patchErsattningar_forwardsAuthorizationHeaderToPatchAndDone()
   {
      WireMockTestResource.getServer().stubFor(patch(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse().withStatus(200)));
      WireMockTestResource.getServer().stubFor(post(urlEqualTo("/" + TEST_ID + "/done"))
            .willReturn(aResponse().withStatus(204)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("""
                  {
                    "ersattningar": [{
                      "ersattningId": "aabb1122-0000-0000-0000-000000000001",
                      "beslutsutfall": "JA",
                      "avslagsanledning": null
                    }]
                  }
                  """)
            .when()
            .post("/api/" + TEST_ID + "/patchErsattningar")
            .then()
            .statusCode(204);

      WireMockTestResource.getServer().verify(patchRequestedFor(urlEqualTo("/" + TEST_ID))
            .withHeader("Authorization", com.github.tomakehurst.wiremock.client.WireMock.equalTo("Bearer test-token")));
      WireMockTestResource.getServer().verify(postRequestedFor(urlEqualTo("/" + TEST_ID + "/done"))
            .withHeader("Authorization", com.github.tomakehurst.wiremock.client.WireMock.equalTo("Bearer test-token")));
   }

   @Test
   void getTask_sendsNoAuthorizationHeader_whenIncomingRequestHasNone()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(TASK_BODY)));

      given()
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200);

      WireMockTestResource.getServer().verify(getRequestedFor(urlEqualTo("/" + TEST_ID))
            .withoutHeader("Authorization"));
   }

   @Test
   void readiness_reportsBackendUp()
   {
      WireMockTestResource.getServer().stubFor(head(urlMatching(".*"))
            .willReturn(aResponse().withStatus(200)));

      given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("checks.find { it.name == 'backend' }.status", equalTo("UP"));
   }

   @Test
   @DisplayName("RTFB-FR-04.1, RTFB-FR-04.2: Hämtning av ärendeunderlag ger ett informationsaccess-event med förväntat innehåll")
   @SuppressWarnings("unchecked")
   void getTask_publishesInformationsaccessEvent()
   {
      stubTask();

      given()
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200);

      var received = inMemoryConnector.sink("ilogg-event").received();
      assertEquals(1, received.size());
      var event = (InformationsaccessEvent) received.getFirst().getPayload();
      assertNotNull(event.getId());
      assertEquals(ANVANDARE_TYP_ID, event.getAnvandare().getTypId());
      assertEquals("H123456", event.getAnvandare().getVarde());
      assertNotNull(event.getTidpunkt());
      assertEquals("rimfrost-regel-rtf-manuell-bff", event.getKalla());
      assertEquals(TEST_ID, event.getHandlaggningId());
      assertEquals(UPPGIFT_ID, event.getUppgiftId());
      assertEquals(REGEL_ID, event.getRegelId());
      assertEquals("1", event.getRegelVersion());
      var kund = (Map<String, Object>) event.getVisadInformation().get("kund");
      assertEquals("Anna", kund.get("fornamn"));
      assertTrue(event.getVisadInformation().containsKey("ersattningar"));
   }

   @Test
   @DisplayName("RTFB-FR-04.2: Två hämtningar ger två event med olika id")
   void getTask_publishesEventWithUniqueIdPerCall()
   {
      stubTask();

      given().when().get("/api/task/" + TEST_ID).then().statusCode(200);
      given().when().get("/api/task/" + TEST_ID).then().statusCode(200);

      var received = inMemoryConnector.sink("ilogg-event").received();
      assertEquals(2, received.size());
      assertNotEquals(((InformationsaccessEvent) received.get(0).getPayload()).getId(),
            ((InformationsaccessEvent) received.get(1).getPayload()).getId());
   }

   @Test
   @DisplayName("RTFB-FR-04.3: Informationsaccessdata skickas inte vidare till frontend")
   void getTask_doesNotReturnInformationsaccessToFrontend()
   {
      stubTask();

      given()
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200)
            .body("informationsaccess", nullValue())
            .body("data", nullValue())
            .body("handlaggning_id", equalTo(TEST_ID));
   }

   @Test
   @DisplayName("RTFB-FR-04.4: Hämtning av hjälptext ger inget informationsaccess-event")
   void getUppgiftsbeskrivning_publishesNoEvent()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/utokadUppgiftsbeskrivning"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"beskrivning\": \"Kontrollera rätt till försäkring\"}")));

      given().when().get("/api/uppgiftsbeskrivning/RTF_MANUELL").then().statusCode(200);

      assertTrue(inMemoryConnector.sink("ilogg-event").received().isEmpty());
   }

   private static void stubTask()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(TASK_BODY)));
   }

   @Test
   void readiness_reportsBackendDown_whenBackendUnreachable()
   {
      WireMockTestResource.getServer().stubFor(head(urlMatching(".*"))
            .willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

      given()
            .when()
            .get("/q/health/ready")
            .then()
            .statusCode(503)
            .body("status", equalTo("DOWN"))
            .body("checks.find { it.name == 'backend' }.status", equalTo("DOWN"));
   }
}
