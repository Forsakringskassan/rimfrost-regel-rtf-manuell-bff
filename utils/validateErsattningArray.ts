export default function validateErsattningArray(ersattning: any): boolean {
    if (!Array.isArray(ersattning)) {
        return false;
    }

    for (const item of ersattning) {
        if (typeof item !== 'object' || item === null) {
            return false;
        }
        if (typeof item.ersattningId !== 'string' ||
            typeof item.beslutsutfall !== 'string' ||
            typeof item.avslagsanledning !== 'string') {
            return false;
        }
    }

    return true;
};