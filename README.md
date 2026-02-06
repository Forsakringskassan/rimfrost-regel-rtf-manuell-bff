# RTF Manual Rule BFF (Backend for Frontend)

This is the **Rule BFF** for RTF manual rules, serving data to the RTF manual micro-frontend in the Rimfrost task management system.

## System Architecture

### Communication Flow

```
[Host FE] ←→ [Portal BFF] ←→ [Backend Services (port 8889)]
    ↓
[Micro FE] ←→ [Rule BFF (This)] ←→ [Backend Services (port 8890)]
```

**Responsibilities:**

- Serves RTF manual rule data to micro-frontend
- Handles rule-specific business logic and transformations
- Provides fallback data for RTF manual rules
- Manages rule completion/status updates

### Unified Fallback System

**Environment-Driven Configuration:**

```env
BACKEND_BASE_URL=http://localhost:8890
FALLBACK_MODE=auto    # auto | always | never
FALLBACK_TIMEOUT_MS=5000
PORT=9002
```

**Fallback Strategy:**

- `auto`: Attempt backend, fallback to mock on failure (development)
- `always`: Always serve mock RTF data (offline development)
- `never`: Fail fast without fallback (production)

## API Endpoints

### Core Rule Endpoints

| Method  | Path                                       | Description        | Fallback           |
| ------- | ------------------------------------------ | ------------------ | ------------------ |
| `GET`   | `/api/:regel/:regeltyp/:kundbehovsflodeId` | Fetch rule data    | Mock RTF data      |
| `PATCH` | `/api/:regel/:regeltyp/:kundbehovsflodeId` | Update rule status | Mock status update |

**Example Usage:**

```bash
# Fetch RTF manual rule data
GET /api/regel/rtf-manuell/b82166da-9c98-4088-a90d-7f3f06acf48a

# Mark rule as completed
PATCH /api/regel/rtf-manuell/b82166da-9c98-4088-a90d-7f3f06acf48a
Content-Type: application/json
{"status": "Klar"}
```

### Health Check

| Method | Path          | Description           |
| ------ | ------------- | --------------------- |
| `GET`  | `/api/health` | Service health status |

## Development

### Quick Start

```bash
# Install dependencies
npm install

# Start development server with hot reload
npx tsx --watch index.ts
```

**Service URLs:**

- Development: `http://localhost:9002`
- Health check: `http://localhost:9002/api/health`

### Environment Setup

Create `.env` file:

```env
PORT=9002
BACKEND_BASE_URL=http://localhost:8890
FALLBACK_MODE=auto
FALLBACK_TIMEOUT_MS=5000
```

### Testing Fallback

**Test with backend running:**

```bash
# Should proxy to real backend
curl http://localhost:9002/api/regel/rtf-manuell/test-id
```

**Test with backend down:**

```bash
# Should return mock data
FALLBACK_MODE=always npm run dev
curl http://localhost:9002/api/regel/rtf-manuell/test-id
```

## Production Deployment

```bash
# Build TypeScript
npm run build

# Set production environment
NODE_ENV=production
FALLBACK_MODE=never
BACKEND_BASE_URL=https://production-backend.example.com

# Start service
node dist/index.js
```

## Integration

**Frontend Integration:**
Micro-frontend at `rimfrost-regel-rtf-manuell-fe` consumes this BFF

**Backend Integration:**
Proxies to backend services for RTF manual rule processing

**Fallback Integration:**
Consistent patterns with Portal BFF for unified development experience
