import 'dotenv/config';
import express from 'express';
import path from "path";
import { fileURLToPath } from "node:url";
import { proxyWithFallback } from './proxyWithFallback.js';
import * as mockRTFDataService from './mockDataService.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 9002;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Loggning av alla inkommande requests
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

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

// Endpoint för att hämta uppgiftsbeskrivning. Route: /api/uppgiftsbeskrivning/:uppgiftstyp
app.get("/api/uppgiftsbeskrivning/:uppgiftstyp", async (req, res) => {
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
    const backendUrl = `${backendBaseUrl}/regel/rtf-manuell/utokadUppgiftsbeskrivning`;
    
    await proxyWithFallback(req, res, {
        targetUrl: backendUrl,
        method: 'GET',
        fallbackData: { beskrivning: "Fallback: Beskrivning kunde inte laddas från backend." }
    });
});

// Endpoint för att hämta uppgiftinformation via BFF. Route: /api/:regel/:regeltyp/:kundbehovsflodeId
app.get("/api/:regel/:regeltyp/:kundbehovsflodeId", async (req, res) => {
    const { regel, regeltyp, kundbehovsflodeId } = req.params;
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
    
    await proxyWithFallback(req, res, {
        targetUrl: `${backendBaseUrl}/${regel}/${regeltyp}/${kundbehovsflodeId}`,
        method: 'GET',
        fallbackData: mockRTFDataService.getUppgiftData(kundbehovsflodeId),
        onSuccess: (data) => {
            return data;
        }
    })
});

// Endpoint för att markera uppgift som klar via BFF. Route: PATCH /api/:regel/:regeltyp/:kundbehovsflodeId
app.patch("/api/:regel/:regeltyp/:kundbehovsflodeId", async (req, res) => {
    const { regel, regeltyp, kundbehovsflodeId } = req.params;
    const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
    
    await proxyWithFallback(req, res, {
        targetUrl: `${backendBaseUrl}/${regel}/${regeltyp}/${kundbehovsflodeId}`,
        method: 'PATCH',
        body: req.body,
        fallbackData: mockRTFDataService.updateUppgiftStatus(kundbehovsflodeId, req.body)
    })
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