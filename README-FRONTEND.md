# hms frontend

A minimal dark React/Vite staff dashboard connected to a dependency-free Java HTTP API. The API calls the existing `Hotel`, `Room`, `Booking`, `Invoice`, `Guest` and payment classes. Both the API and the UI use in-memory state; restarting the Java process resets demo data.

## Requirements
- JDK 17 or newer
- Node.js LTS and npm

## Run
Open two PowerShell terminals at the repository root.

Terminal 1:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-api.ps1
```

Terminal 2:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-frontend.ps1
```

Open the local URL printed by Vite, normally `http://localhost:5173`.

## API endpoints
- `GET /api/health`
- `GET /api/dashboard`
- `GET /api/rooms`
- `GET /api/bookings`
- `GET /api/guests`
- `POST /api/guests`
- `POST /api/bookings`
- `POST /api/maintenance`
- `POST /api/checkout/{bookingId}`
- `POST /api/pay/{invoiceId}`

The payment methods simulate validation only. Do not send real card details. The API binds to loopback (`127.0.0.1`) and is intended for local demonstration, not production or public deployment. The JSON request parsing is intentionally small and is not a general-purpose JSON parser.

## Safe patching
The patch only adds files. It does not modify or delete existing Java files, backups, the journal, or Git history. Inspect it with `git apply --stat .\hms-frontend.patch` before applying. If you need to revert after applying, remove only the newly added paths listed by the patch, after checking `git status`.
