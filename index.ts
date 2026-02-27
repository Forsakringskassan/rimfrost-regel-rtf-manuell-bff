import express from 'express';
import path from "path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);

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

// Endpoint för att hämta uppgiftinformation via BFF. Route: /api/:regel/:regeltyp/:kundbehovsflodeId
app.get("/api/:regel/:regeltyp/:kundbehovsflodeId", async (req, res) => {
    const { regel, regeltyp, kundbehovsflodeId } = req.params;
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
    const backendUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${kundbehovsflodeId}`;

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
    }
});

app.post("/api/:regel/:regeltyp/:kundbehovsflodeId/patchErsattning", async (req, res) => {
    const { regel, regeltyp, kundbehovsflodeId } = req.params;
    const { ersattning } = req.body;
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";

    try {
        for (const item of ersattning) {
            console.log(`Patching ersattningId ${item.ersattningId} with beslutsutfall: ${item.beslutsutfall} and avslagsanledning: ${item.avslagsanledning}`);
            const patchUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${kundbehovsflodeId}/ersattning/${item.ersattningId}`;
            const patchResponse = await fetch(patchUrl, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
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

        const doneUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${kundbehovsflodeId}/done`;
        const doneResponse = await fetch(doneUrl, {
            method: "POST",
        });

        if (!doneResponse.ok) {
            console.error(`Failed to mark as done for kundbehovsflodeId ${kundbehovsflodeId}`);
            return res.status(502).json({ error: `Failed to mark as done for kundbehovsflodeId ${kundbehovsflodeId}` });
        }
    } catch (error) {
        console.error("Error patching ersattning:", error);
        return res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
}
)

app.post("/api/:regel/:regeltyp/:kundbehovsflodeId/done", async (req, res) => {
        const { regel, regeltyp, kundbehovsflodeId } = req.params;
        const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
        const backendUrl = `${backendBaseUrl}/${regel}/${regeltyp}/${kundbehovsflodeId}/done`;
        try {
            const response = await fetch(backendUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
                },
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error(`Backend error: ${errorText}`);
                throw new Error('backend-error');
            }
    } catch (error) {
        console.error("Error posting to backend:", error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});