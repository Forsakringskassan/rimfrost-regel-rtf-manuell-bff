export function transformBackendResponse(backendData: any) {
  const rawAnstallning = backendData.kund.anstallning;
  const anstallning = rawAnstallning ? {
    anstallningsdag: rawAnstallning.anstallningsdag,
    arbetstidProcent: rawAnstallning.arbetstid_procent,
    sistaAnstallningsdag: rawAnstallning.sista_anstallningsdag,
    organisationsnamn: rawAnstallning.organisationsnamn,
    organisationsnummer: rawAnstallning.organisationsnummer,
  } : undefined;

  const kund = {
    fornamn: backendData.kund.fornamn,
    efternamn: backendData.kund.efternamn,
    kon: backendData.kund.kon,
    anstallning,
  };

  const ersattningar = backendData.ersattningar.map((e: any) => ({
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
    ersattningar,
  };
}
