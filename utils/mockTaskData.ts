import type { GetDataResponse } from './types.js';

export function getMockTask(handlaggningId: string): GetDataResponse {
    return {
        handlaggningId,
        kund: {
            fornamn: "Lisa",
            efternamn: "Tass",
            kon: "KVINNA",
            anstallning: {
                anstallningsdag: "2020-01-01",
                arbetstidProcent: 100,
                sistaAnstallningsdag: null,
                organisationsnamn: "Mock AB",
                organisationsnummer: "556000-0000",
                lon: {
                    from: "2025-01-01",
                    tom: null,
                    lonesumma: 40000,
                }
            }
        },
        ersattning: [
            {
                ersattningId: `ers-${handlaggningId}-1`,
                ersattningstyp: "HUNDBIDRAG",
                omfattningProcent: 100,
                belopp: 40000,
                berakningsgrund: 40000,
                beslutsutfall: null,
                from: "2025-01-10",
                tom: "2025-01-10",
                avslagsanledning: null,
            }
        ]
    };
}