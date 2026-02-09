import express from 'express';
import path from "path";
import { fileURLToPath } from "node:url";
import { proxyWithFallback } from '#proxyWithFallback.js';
import * as mockRTFDataService from '#mockRTFDataService.js';

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


app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});