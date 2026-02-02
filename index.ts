import express from 'express';
import path from "path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 9002;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
    res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");
    if (req.method === "OPTIONS") {
        res.sendStatus(200);
    } else {
        next();
    }
});

app.get("/api/health", (req, res) => {
    res.json({ status: "ok", timestamp: new Date().toISOString() });
});

// Endpoint för att hämta uppgiftinformation via BFF. Route: /api/regel/{regeltyp}/{kundbehovsflodeId}
app.get("/api/:regeltyp/:kundbehovsflodeId", async (req, res) => {
    try {
        const { regeltyp, kundbehovsflodeId } = req.params;
        const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
        const backendUrl = `${backendBaseUrl}/api/${regeltyp}/${kundbehovsflodeId}`;
        
        const response = await fetch(backendUrl, {
            method: "GET", //Invänta information från FK om hur den här ska se ut
            headers: {
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
            },
        });
        
        if (!response.ok) {
            return res.status(response.status).json({ error: "Failed to fetch task information from the backend." });
        }
        
        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error("Error fetching from backend:", error);
        res.status(500).json({ error: "Internal server error" });
    }
});

// Endpoint för att markera uppgift som klar via BFF. Route: POST /api/regel/{regeltyp}/{kundbehovsflodeId}/klar
app.post("/api/regel/:regeltyp/:kundbehovsflodeId/klar", async (req, res) => {
    try {
        const { regeltyp, kundbehovsflodeId } = req.params;
        const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
        const backendUrl = `${backendBaseUrl}/api/${regeltyp}/${kundbehovsflodeId}/klar`;
        
        const response = await fetch(backendUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}),
            },
            body: JSON.stringify(req.body),
        });
        
        if (!response.ok) {
            return res.status(response.status).json({ error: "Backend responded with an error when marking as complete." });
        }
        
        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error("Error posting to backend:", error);
        res.status(500).json({ error: "Internal server error" });
    }
});


app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});