export function transformBackendResponse(backendData: any) {
  const lon = {
    from: backendData.kund.anstallning.lon.from,
    tom: backendData.kund.anstallning.lon.tom,
    lonesumma: backendData.kund.anstallning.lon.lonesumma,
  };

  const anstallning = {
    anstallningsdag: backendData.kund.anstallning.anstallningsdag,
    arbetstidProcent: backendData.kund.anstallning.arbetstid_procent,
    sistaAnstallningsdag: backendData.kund.anstallning.sista_anstallningsdag,
    organisationsnamn: backendData.kund.anstallning.organisationsnamn,
    organisationsnummer: backendData.kund.anstallning.organisationsnummer,
    lon,
  };

  const kund = {
    fornamn: backendData.kund.fornamn,
    efternamn: backendData.kund.efternamn,
    kon: backendData.kund.kon,
    anstallning,
  };

  const ersattning = backendData.ersattning.map((e: any) => ({
    ersattningId: e.ersattning_id,
    ersattningstyp: e.ersattningstyp,
    omfattningProcent: e.omfattning_procent,
    belopp: e.belopp,
    berakningsgrund: e.berakningsgrund,
    beslutsutfall: e.beslutsutfall,
    from: e.from,
    tom: e.tom,
    avslagsanledning: e.avslagsanledning,
  }));

  return {
    handlaggningId: backendData.handlaggning_id,
    kund,
    ersattning,
  };
}
