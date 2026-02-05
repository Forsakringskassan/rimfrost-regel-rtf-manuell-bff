// Transformera backend snake_case till frontend camelCase
export function transformBackendResponse(backendData: any) {
    return {
        kundbehovsflodeId: backendData.kundbehovsflode_id,
        kund: {
            fornamn: backendData.kund.fornamn,
            efternamn: backendData.kund.efternamn,
            kon: backendData.kund.kon,
            anstallning: {
                anstallningsdag: backendData.kund.anstallning.anstallningsdag,
                arbetstidProcent: backendData.kund.anstallning.arbetstid_procent,
                sistaAnstallningsdag: backendData.kund.anstallning.sista_anstallningsdag,
                organisationsnamn: backendData.kund.anstallning.organisationsnamn,
                organisationsnummer: backendData.kund.anstallning.organisationsnummer,
                lon: {
                    from: backendData.kund.anstallning.lon.from,
                    tom: backendData.kund.anstallning.lon.tom,
                    lonesumma: backendData.kund.anstallning.lon.lonesumma,
                },
            },
        },
        ersattning: backendData.ersattning.map((e: any) => ({
            ersattningId: e.ersattning_id,
            ersattningstyp: e.ersattningstyp,
            omfattningProcent: e.omfattning_procent,
            belopp: e.belopp,
            berakningsgrund: e.berakningsgrund,
            beslutsutfall: e.beslutsutfall,
            from: e.from,
            tom: e.tom,
            avslagsanledning: e.avslagsanledning,
        })),
    };
}
