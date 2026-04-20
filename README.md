# Rimfrost RTF Manuell BFF

Backend-for-Frontend for the RTF Manuell rule. Proxies requests from the RTF Manuell micro-frontend to the backend service.

## Quick Start

```bash
npm install
npm run dev   # starts on port 9002 with hot reload
```

## Scripts

| Command | Description |
|---|---|
| `npm run dev` | Hot-reload dev server via tsx |
| `npm run build` | Compile TypeScript to `dist/` |
| `npm run start` | Run compiled output |
| `npm run type-check` | TypeScript check without emit |
| `npm run lint` | ESLint |

## Environment Variables

Copy `.env.example` to `.env` and fill in values:

```bash
cp .env.example .env
```

| Variable | Default | Description |
|---|---|---|
| `PORT` | `9002` | Port the server listens on |
| `BE_RTF_MANUELL_URL` | — | Base URL of the RTF Manuell backend service |
| `BE_RULE_PATH` | `regel/rtf-manuell` | Rule path appended to the backend URL |

## API

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/health` | Health check |
| `GET` | `/api/task/:handlaggningId` | Fetch task data |
| `POST` | `/api/:handlaggningId/patchErsattningar` | Patch ersättningar and mark done |
| `GET` | `/api/uppgiftsbeskrivning/:uppgiftstyp` | Fetch task description |

## Docker

```bash
docker build -t rimfrost-rtf-manuell-bff .
docker run -p 9002:9002 \
  -e BE_RTF_MANUELL_URL=https://rtf-manuell.internal.example.com \
  -e BE_RULE_PATH=regel/rtf-manuell \
  rimfrost-rtf-manuell-bff
```

Environment variables are read from the container environment — do **not** bake them into the image.
