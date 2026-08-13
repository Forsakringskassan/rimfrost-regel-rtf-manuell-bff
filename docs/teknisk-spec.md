# Teknisk spec — RTF Manuell BFF (RTFB)

## Översikt

Tunn, synkron Quarkus REST-BFF utan egen datalagring och utan meddelandeintegration. Enda
uppströmsberoende är RTF Manuell-regeltjänsten, nådd via en generad REST-klient från dess
OpenAPI-specifikation.

## Komponentstruktur

```text
src/main/java/se/fk/github/rtfmanuellbff
├── RtfManuellBffController   # REST-ändpunkter mot frontend
├── integration/RtfManuellClient  # REST-klient mot regeltjänsten
├── GlobalExceptionMapper      # Enhetlig felmappning, inkl. hantering av avbrutna anslutningar
└── model/                    # PatchErsattningRequest m.fl. (övriga typer kommer från OpenAPI-specen)
```

## API-specifikationer

| API | Specifikationsartefakt | Basväg |
|---|---|---|
| RTF Manuell-regeltjänsten | `rimfrost-regel-rtf-manuell-openapi` | Konfigurerad via `BACKEND_URL` |

| Metod | Sökväg | Beskrivning |
|---|---|---|
| GET | `/api/task/{handlaggningId}` | Hämta ärendeunderlag |
| POST | `/api/{handlaggningId}/patchErsattningar` | Lämna in beslut och slutföra uppgiften |
| GET | `/api/uppgiftsbeskrivning/{uppgiftstyp}` | Hämta hjälptext |

## Kafka-integration

Ingen. Tjänsten har ingen meddelandeintegration.

## Konfiguration

| Egenskap | Beskrivning | Standardvärde |
|---|---|---|
| `quarkus.rest-client.backend.url` (`BACKEND_URL`, alt. `BE_RTF_MANUELL_URL`+`BE_RULE_PATH`) | Bas-URL till regeltjänsten | `http://localhost:8080/regel/rtf-manuell` |
| `CORS_ORIGINS` | Tillåtna ursprung för CORS | Lokala mikrofrontend-portar |

## Liveness

`/q/health`.

## Kända begränsningar och framtida arbete

| Begränsning | Föreslagen åtgärd |
|---|---|
| Ingen kompensationslogik om beslutsinlämning lyckas men slutförandet misslyckas | Utforma en återförsöks- eller kompensationsstrategi |
| Felmeddelanden från regeltjänsten ersätts med ett generellt meddelande, detaljer om felorsak går förlorade | Bevara relevant feldetalj i felresponsen |
| `{uppgiftstyp}` i beskrivningsändpunkten används inte av bakomliggande tjänst | Klargör om typspecifika beskrivningar behövs |
| Genererad specifikationsversion (0.2.4) ligger efter den publicerade OpenAPI-specen (1.1.2) | Uppgradera beroendet och verifiera fältkompatibilitet |
| Signeringsflaggan är alltid satt till sant, ingen väg finns för att spara utan signering | Bedöm om ett "spara utkast"-flöde behövs |
