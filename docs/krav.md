# Krav — RTF Manuell BFF (RTFB)

## Bakgrund och syfte

RTF Manuell BFF är backend-för-frontend för RTF Manuell-mikrofrontenden. Den förmedlar
handläggarens hämtning av ärendeunderlag, inlämning av beslut om ersättningsperioder, och
slutförande av uppgiften, mot den bakomliggande regeltjänsten för RTF. Den existerar för att
mikrofrontenden ska kunna anropa en enda, stabil tjänst utan kännedom om regeltjänstens
kontrakt eller det generiska ramverk den bygger på.

---

## Intressenter och aktörer

| Aktör | Roll |
|---|---|
| RTF Manuell Frontend | Anropar BFF:n för att hämta ärendeunderlag och lämna in beslut |
| RTF Manuell-regeltjänsten | Bakomliggande tjänst som äger ärende- och ersättningsdata |

---

## Funktionella krav

### RTFB-FR-01 — Hämta ärendeunderlag

- **RTFB-FR-01.1** BFF:n ska kunna hämta ärendeunderlag för en given handläggning från
  bakomliggande regeltjänst och returnera det oförändrat till frontend.
- **RTFB-FR-01.2** BFF:n ska kunna hämta en hjälptext för en uppgiftstyp, oavsett vilken
  uppgiftstyp som efterfrågas, eftersom bakomliggande tjänst i dagsläget tillhandahåller en
  och samma generella beskrivning.

### RTFB-FR-02 — Lämna in beslut och slutföra uppgift

- **RTFB-FR-02.1** BFF:n ska ta emot en icke-tom lista av beslut om ersättningsperioder från
  frontend och validera att listan uppfyller detta krav innan vidare behandling.
- **RTFB-FR-02.2** BFF:n ska skicka de inlämnade besluten till bakomliggande regeltjänst.
- **RTFB-FR-02.3** Efter att beslut lämnats in ska BFF:n markera handläggningen som slutförd hos
  bakomliggande regeltjänst.
- **RTFB-FR-02.4** Inlämning av beslut ska alltid ske tillsammans med en signeringsmarkering,
  utan att frontend kan styra detta.
- **RTFB-FR-02.5** Om beslut lämnats in men slutförandet av handläggningen misslyckas ska
  detta returneras som ett fel till frontend.

### RTFB-FR-03 — Felhantering vid integration mot regeltjänsten

- **RTFB-FR-03.1** Om regeltjänsten svarar med ett felstatus ska BFF:n returnera samma
  HTTP-statuskod till frontend.
- **RTFB-FR-03.2** Om regeltjänsten inte går att nå, inklusive vid avbrutna
  nätverksanslutningar, ska BFF:n returnera en statuskod som tydligt anger att bakomliggande
  tjänst är otillgänglig.

---

## Icke-funktionella krav

### RTFB-NFR-01 — Observerbarhet

- **RTFB-NFR-01.1** BFF:n ska exponera en hälsokontroll för sin egen driftstatus.

### RTFB-NFR-02 — Säkerhet

- **RTFB-NFR-02.1** BFF:n ska vidarebefordra anropande handläggares auktoriseringsuppgifter till
  bakomliggande regeltjänst oförändrade.

---

## API-gränssnitt (översikt)

| API | Målgrupp | Specifikationsartefakt |
|---|---|---|
| RTF Manuell BFF REST-API | RTF Manuell Frontend | Ingen dedikerad BFF-specifikation |
| RTF Manuell-regeltjänstens API | Denna BFF | `rimfrost-regel-rtf-manuell-openapi` |

---

## Integration med RTF Manuell-regeltjänsten

BFF:n är en ren synkron REST-till-REST-integration mot regeltjänsten, som i sin tur bygger på
det generiska manuella regelramverket. BFF:n lagrar inget tillstånd själv.
