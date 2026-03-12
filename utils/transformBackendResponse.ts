export function transformBackendResponse(rawData: any) {
  return {
    handlaggningId: rawData.handlaggning_id,
    kund: rawData.kund,
    ersattning: rawData.ersattning,
  };
}
