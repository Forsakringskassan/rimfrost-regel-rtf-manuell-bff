import { transformBackendResponse } from '#utils/transformBackendResponse.js';
import express from 'express';

const app = express();
const PORT = process.env.PORT || 9002;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, PUT, POST, PATCH, DELETE, OPTIONS");
    res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");
    if (req.method === "OPTIONS") {
        res.sendStatus(200);
    } else {
        next();
    }
});

app.get("/api/health", (req, res) => {
    console.log("Health check called");
    res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// Endpoint för att hämta uppgiftinformation via BFF. Route: POST /api/task
app.post("/api/task", async (req, res) => {
    const { handlaggningId } = req.body;
    const backendBaseUrl = process.env.BE_RTF_MANUELL_URL ?? "";
    const backendRuleUrl = process.env.BE_RULE_PATH ?? "";
    const backendUrl = `${backendBaseUrl}/${backendRuleUrl}/${handlaggningId}`;

    console.log(`Fetching task information for handlaggningId: ${handlaggningId} from backend URL: ${backendUrl}`);

    try {
        const response = await fetch(backendUrl, {
            method: "GET",
            headers: {
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
            },
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Backend error: ${errorText}`);
            throw new Error('backend-error'); 
        }

        const data = await response.json();
        return res.json(transformBackendResponse(data));
    } catch (err) {
        console.error("Error during fetch from backend:", err);
        return res.status(500).json({ error: "Internal server error", message: err instanceof Error ? err.message : String(err) });
    }
});

app.post("/api/patchErsattningar", async (req, res) => {
    const { handlaggningId, ersattningar } = req.body;
    const backendBaseUrl = process.env.BE_RTF_MANUELL_URL ?? "";
    const backendRuleUrl = process.env.BE_RULE_PATH ?? "";
    const backendDoneUrl = `${backendBaseUrl}/${backendRuleUrl}/${handlaggningId}/done`;

    try {
        const patchUrl = `${backendBaseUrl}/${backendRuleUrl}/${handlaggningId}`;
        const patchResponse = await fetch(patchUrl, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
            },
            body: JSON.stringify({
                ersattningar: ersattningar.map((item: any) => ({
                    ersattningId: item.ersattningId,
                    beslutsutfall: item.beslutsutfall,
                    avslagsanledning: item.avslagsanledning,
                    signernad: true,
                })),
            }),
        });

        if (!patchResponse.ok) {
            const errorText = await patchResponse.text();
            console.error(`Backend PATCH error: ${errorText}`);
            return res.status(502).json({ error: `Failed to patch ersattningar` });
        }

        const doneResponse = await fetch(backendDoneUrl, {
            method: 'POST',
            headers: {
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
            }
        });

        if (!doneResponse.ok) {
            const errorText = await doneResponse.text();
            console.error(`Backend done error: ${errorText}`);
            throw new Error('backend-error');
        }

        return res.status(204).end();
    } catch (error) {
        console.error("Error patching ersattning:", error);
        return res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

//Endpoint för att hämta uppgiftsbeskrivning via BFF. Route: POST /api/uppgiftsbeskrivning

app.post("/api/uppgiftsbeskrivning", async (req, res) => {
    const { uppgiftstyp } = req.body;
    // Adjust backend URL as needed for your environment
    const backendBaseUrl = process.env.BE_RTF_MANUELL_URL ?? "";
    const backendRuleUrl = process.env.BE_RULE_PATH ?? "";
    const backendUrl = `${backendBaseUrl}/${backendRuleUrl}/utokadUppgiftsbeskrivning`;

    try {
        const response = await fetch(backendUrl, { method: 'GET' });
        if (!response.ok) {
            const errorText = await response.text();
            return res.status(response.status).json({ error: "Failed to fetch from backend", details: errorText });
        }
        const data = await response.json();
        res.json(data);
    } catch (error) {
        res.status(502).json({ error: "Backend service unavailable", message: error instanceof Error ? error.message : String(error) });
    }
});


app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});