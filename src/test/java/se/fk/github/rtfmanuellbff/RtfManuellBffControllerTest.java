package se.fk.github.rtfmanuellbff;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class RtfManuellBffControllerTest
{

   private static final String TEST_ID = "550e8400-e29b-41d4-a716-446655440000";

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
   }

   @Test
   void getTask_returnsTransformedData()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/" + TEST_ID))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
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
                        """.formatted(TEST_ID))));

      given()
            .header("Authorization", "Bearer test-token")
            .when()
            .get("/api/task/" + TEST_ID)
            .then()
            .statusCode(200)
            .body("handlaggningId", equalTo(TEST_ID))
            .body("kund.fornamn", equalTo("Anna"))
            .body("kund.anstallning.arbetstidProcent", equalTo(100))
            .body("kund.anstallning.sistaAnstallningsdag", equalTo("2025-12-31"))
            .body("ersattningar[0].ersattningId", equalTo("aabb1122-0000-0000-0000-000000000001"))
            .body("ersattningar[0].omfattningProcent", equalTo(100));
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
                  .withBody("[{\"beskrivning\": \"Kontrollera rätt till försäkring\"}]")));

      given()
            .when()
            .get("/api/uppgiftsbeskrivning/RTF_MANUELL")
            .then()
            .statusCode(200)
            .body("[0].beskrivning", equalTo("Kontrollera rätt till försäkring"));
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
   void getUppgiftsbeskrivning_returns500_whenBackendUnreachable()
   {
      WireMockTestResource.getServer().stubFor(get(urlEqualTo("/utokadUppgiftsbeskrivning"))
            .willReturn(aResponse().withFault(com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER)));

      given()
            .when()
            .get("/api/uppgiftsbeskrivning/RTF_MANUELL")
            .then()
            .statusCode(500)
            .body("error", equalTo("Internal server error"));
   }
}
