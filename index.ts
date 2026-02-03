import express from 'express';
import path from "path";
import { fileURLToPath } from "node:url";

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

// Endpoint för att hämta uppgiftinformation via BFF. Route: /api/regel/{regeltyp}/{kundbehovsflodeId}
app.get("/api/:regeltyp/:kundbehovsflodeId", async (req, res) => {
    try {
        const { regeltyp, kundbehovsflodeId } = req.params;
        const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
        const backendUrl = `${backendBaseUrl}/${regeltyp}/${kundbehovsflodeId}`;
        
        console.log(`Proxying GET request to: ${backendUrl}`);
        
        const response = await fetch(backendUrl, {
            method: "GET", 
            headers: {
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}), //Invänta information från FK om hur den här ska se ut
            },
        });
        
        console.log(`Backend response status: ${response.status}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Backend error: ${errorText}`);
            return res.status(response.status).json({ error: "Failed to fetch task information from the backend.", details: errorText });
        }
        
        const contentType = response.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
            const text = await response.text();
            console.error(`Backend returned non-JSON content: ${text.substring(0, 200)}`);
            return res.status(502).json({ error: "Backend returned non-JSON response" });
        }
        
        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error("Error fetching from backend:", error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});

// Endpoint för att markera uppgift som klar via BFF. Route: PATCH /api/regel/{regeltyp}/{kundbehovsflodeId}
app.patch("/api/:regeltyp/:kundbehovsflodeId", async (req, res) => {
    try {
        const { regeltyp, kundbehovsflodeId } = req.params;
        const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8890";
        const backendUrl = `${backendBaseUrl}/${regeltyp}/${kundbehovsflodeId}`;
        
        console.log(`Proxying PATCH request to: ${backendUrl}`);
        console.log(`Request body:`, req.body);
        
        const response = await fetch(backendUrl, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                ...(req.headers.authorization ? { authorization: req.headers.authorization } : {}), //Invänta information från FK om hur den här ska se ut
            },
            body: JSON.stringify(req.body),
        });
        
        console.log(`Backend response status: ${response.status}`);
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error(`Backend error: ${errorText}`);
            return res.status(response.status).json({ error: "Backend responded with an error when marking as complete.", details: errorText });
        }
        
        const contentType = response.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
            const text = await response.text();
            console.error(`Backend returned non-JSON content: ${text.substring(0, 200)}`);
            return res.status(502).json({ error: "Backend returned non-JSON response" });
        }
        
        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error("Error posting to backend:", error);
        res.status(500).json({ error: "Internal server error", message: error instanceof Error ? error.message : String(error) });
    }
});


app.listen(PORT, () => {
    console.log(`BFF server running on port ${PORT}`);
});