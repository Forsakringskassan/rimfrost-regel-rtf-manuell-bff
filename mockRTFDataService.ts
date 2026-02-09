import { start } from "node:repl";

interface Ersattning {
  ersattning_id: string;
  ersattningstyp: string;
  omfattning_procent: number;
  belopp: number;
  berakningsgrund: number;
  beslutsutfall: string | null;
  avslagsanledning: string | null;
  from: string;
  tom: string;
}

interface Anstallning {
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
}

interface Kund {
  efternamn: string | null;
  fornamn: string | null;
  kon: string | null;
  anstallning: Anstallning;
}

interface RTFUppgiftData {
  kundbehovsflode_id: string;
  kund: Kund;
  ersattning: Ersattning[];
}

const mockUppgifter = new Map<string, RTFUppgiftData>();

function generateMockErsattningar(): Ersattning[] {
  const ersattningar: Ersattning[] = [];
  const startDate = new Date('2026-01-01');

  for (let i = 0; i < 3; i++) {
    const currentDate = new Date(startDate);
    currentDate.setDate(startDate.getDate() + i);
    const dateStr = currentDate.toISOString().split('T')[0];

    ersattningar.push({
      ersattning_id: (0o0 + i).toString(),
      ersattningstyp: "HUNDBIDRAG",
      omfattning_procent: 100,
      belopp: 40000,
      berakningsgrund: 0,
      beslutsutfall: null,
      avslagsanledning: null,
      from: dateStr,
      tom: dateStr
    });
  }

  return ersattningar;
}

export function getUppgiftData(kundbehovsflodeId: string): RTFUppgiftData {
  return mockUppgifter.get(kundbehovsflodeId) || {
    kundbehovsflode_id: kundbehovsflodeId,
    kund: {
      efternamn: 'Doe',
      fornamn: 'John',
      kon: 'Man',
      anstallning: {
        organisationsnummer: "123456-7890",
        organisationsnamn: "Mockad Arbetsgivare AB",
        arbetstid_procent: 100,
        anstallningsdag: "2022-02-05",
        sista_anstallningsdag: null,
        lon: {
          lonesumma: 40000,
          from: "2022-02-05",
          tom: null
        }
      }
    },
    ersattning: generateMockErsattningar()
  };
}

export function updateUppgiftStatus(kundbehovsflodeId: string, updateData: any): RTFUppgiftData {
  const existing = getUppgiftData(kundbehovsflodeId);
  const updated = { ...existing, ...updateData };
  mockUppgifter.set(kundbehovsflodeId, updated);
  return updated;
}