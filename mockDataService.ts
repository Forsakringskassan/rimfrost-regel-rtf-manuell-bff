// Mock data service for development fallback in the regel BFF
type BackendResponse = {
  kundbehovsflode_id: string;
  kund: {
    fornamn: string;
    efternamn: string;
    kon: string;
    anstallning: {
      organisationsnummer: string;
      organisationsnamn: string;
      arbetstid_procent: number;
      anstallningsdag: string;
      sista_anstallningsdag: string | null;
      lon: {
        lonesumma: number;
        from: string;
        tom: string | null;
      };
    };
  };
  ersattning: Array<{
    ersattning_id: string;
    ersattningstyp: string;
    omfattning_procent: number;
    belopp: number;
    berakningsgrund: number;
    beslutsutfall: string | null;
    avslagsanledning: string | null;
    from: string;
    tom: string;
  }>;
};

const FIXED_FLOW_IDS = Array.from({ length: 20 }, (_, i) => `flow-${String(i + 1).padStart(3, '0')}`);

const tasks: Record<string, BackendResponse> = {};

function buildMockForFlow(flowId: string): BackendResponse {
  const ersattningId = `ers-${flowId}`;
  return {
    kundbehovsflode_id: flowId,
    kund: {
      fornamn: `Test${flowId}`,
      efternamn: `User${flowId}`,
      kon: 'MAN',
      anstallning: {
        organisationsnummer: '555555-1234',
        organisationsnamn: 'Mock Org',
        arbetstid_procent: 100,
        anstallningsdag: '2020-01-01',
        sista_anstallningsdag: null,
        lon: {
          lonesumma: 25000,
          from: '2025-01-01',
          tom: null,
        },
      },
    },
    ersattning: [
      {
        ersattning_id: ersattningId,
        ersattningstyp: 'Sjukpenning',
        omfattning_procent: 100,
        belopp: 20000,
        berakningsgrund: 20000,
        beslutsutfall: null,
        avslagsanledning: null,
        from: '2025-01-01',
        tom: '2025-12-31',
      },
    ],
  };
}

// Initialize tasks map
for (const f of FIXED_FLOW_IDS) {
  tasks[f] = buildMockForFlow(f);
}

export function getTask(kundbehovsflodeId: string): BackendResponse | null {
  // accept both flow-001 and numeric-like ids
  if (tasks[kundbehovsflodeId]) return structuredClone(tasks[kundbehovsflodeId]);
  return null;
}

export function patchTask(kundbehovsflodeId: string, body: any): BackendResponse | null {
  const task = tasks[kundbehovsflodeId];
  if (!task) return null;

  // Modify ersattning entries if matching ersattning_id provided
  if (body && body.ersattningId) {
    const e = task.ersattning.find((x) => x.ersattning_id === body.ersattningId || x.ersattning_id === `ers-${kundbehovsflodeId}`);
    if (e) {
      if (body.beslutsutfall !== undefined) e.beslutsutfall = body.beslutsutfall;
      if (body.avslagsanledning !== undefined) e.avslagsanledning = body.avslagsanledning;
    }
  }

  // Return a cloned object to avoid accidental mutations
  return structuredClone(task);
}

export function getAvailableFlows(): string[] {
  return Object.keys(tasks);
}
