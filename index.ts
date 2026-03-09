import validateErsattningArray from '#utils/validateErsattningArray.js';
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

// Endpoint för att hämta uppgiftinformation via BFF. Route: /api/:regel/:regeltyp/:handlaggningId
app.get("/api/:regel/:regeltyp/:handlaggningId", async (req, res) => {
    const { regel, regeltyp, handlaggningId } = req.params;
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
    const backendUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${handlaggningId}`;

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
        return res.json(data);
    } catch (err) {
        console.error("Error during fetch from backend:", err);
        return res.status(500).json({ error: "Internal server error", message: err instanceof Error ? err.message : String(err) });
    }
});

app.post("/api/:regel/:regeltyp/:handlaggningId/patchErsattning", async (req, res) => {
    const { regel, regeltyp, handlaggningId } = req.params;
    const { ersattning } = req.body;
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
    const backendDoneUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${handlaggningId}/done`;

    if (!validateErsattningArray(ersattning)) {
        return res.status(400).json({ error: "Invalid ersattning array format" });
    }

    try {
        for (const item of ersattning) {
            const patchUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${handlaggningId}/ersattning/${item.ersattningId}`;
            const patchResponse = await fetch(patchUrl, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
                },
                body: JSON.stringify({
                    beslutsutfall: item.beslutsutfall,
                    avslagsanledning: item.avslagsanledning,
                }),
            })

            if (!patchResponse.ok) {
                console.error(`Failed to patch ersattningId ${item.ersattningId}`);
                return res.status(502).json({ error: `Failed to patch ersattningId ${item.ersattningId}` });
            }
        }

        const response = await fetch(backendDoneUrl, {
                method: 'POST',
                headers: {
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
                }
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error(`Backend error: ${errorText}`);
                throw new Error('backend-error');
            }

        return res.json({ message: "Ersättningar uppdaterade och postDone anropat" });
    } catch (error) {
        console.error("Error patching ersattning:", error);
        return res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

//Endpoint för att hämta uppgiftsbeskrivning via BFF. Route: /api/uppgiftsbeskrivning/:uppgiftstyp

app.get("/api/uppgiftsbeskrivning/:uppgiftstyp", async (req, res) => {
    const { uppgiftstyp } = req.params;
    // Adjust backend URL as needed for your environment
    const backendUrl = `http://localhost:8890/regel/rtf-manuell/utokadUppgiftsbeskrivning`;

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