export default function validateErsattningArray(ersattning: any): boolean {
    console.log("Validating ersattning array:", ersattning);
    if (!Array.isArray(ersattning)) {
        return false;
    }

    for (const item of ersattning) {
        if (typeof item !== 'object' || item === null) {
            return false;
        }
        if (typeof item.ersattning_id !== 'string' ||
            typeof item.beslutsutfall !== 'string' ||
            (item.avslagsanledning !== null && typeof item.avslagsanledning !== 'string')) {
            return false;
        }
    }

    return true;
};