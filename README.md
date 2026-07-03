# rimfrost-regel-rtf-manuell-bff

Backend-for-frontend for the RTF manuell micro-frontend. Proxies and transforms task data from the RTF manuell rule service.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:9002/q/dev/>.

The application runs on port **9002** by default (matches the old TypeScript BFF).

To run the full build locally (mirrors CI, skips Docker):

```shell script
./mvnw verify -Dquarkus.container-image.build=false
```

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `BACKEND_URL` | `http://localhost:8080/regel/rtf-manuell` | Full base URL of the RTF manuell backend, including the rule path. Replaces the old `BE_RTF_MANUELL_URL` + `BE_RULE_PATH` combination. |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:3030` | Comma-separated list of allowed CORS origins. |

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

Docker image build is disabled by default (`quarkus.container-image.build=false` in `application.properties`), so this command only produces the JAR.

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Packaging and running as Docker

Build a Docker image _rimfrost/rimfrost-regel-rtf-manuell-bff:latest_:

```shell script
./mvnw clean package -Dquarkus.container-image.build=true
```

Launch container:

```shell script
docker run -p 9002:9002 \
  -e BACKEND_URL=http://host.docker.internal:8080/regel/rtf-manuell \
  rimfrost/rimfrost-regel-rtf-manuell-bff
```

## REST endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/task/{handlaggningId}` | Fetches and transforms task data from the backend. Passes the `Authorization` header through. |
| `POST` | `/api/{handlaggningId}/patchErsattningar` | Patches ersättningar on the backend (PATCH), then marks the task as done (POST to `/done`). |
| `GET` | `/api/uppgiftsbeskrivning/{uppgiftstyp}` | Fetches task description from the backend (`/utokadUppgiftsbeskrivning`). |
| `GET` | `/q/health` | Health check provided by `quarkus-smallrye-health`. |

Health: <http://localhost:9002/q/health>
