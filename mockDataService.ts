// Mock data service for development fallback in the regel BFF
import mockTasksData from './mockTasks.json' with { type: 'json' };

type BackendResponse = any;

const tasks: Record<string, BackendResponse> = {};

// Initialize tasks map from mockTasks.json
for (const task of mockTasksData) {
  tasks[task.kundbehovsflode_id] = task;
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
    const e = task.ersattning.find((x: any) => x.ersattning_id === body.ersattningId || x.ersattning_id === `ers-${kundbehovsflodeId}`);
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
