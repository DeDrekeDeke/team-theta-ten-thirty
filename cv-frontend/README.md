# CV Frontend

React + TypeScript frontend for the educational CV Manager starter application.

## Run Locally

Start the backend first, then run:

```bash
npm install
npm run dev
```

The frontend runs at:

```text
http://localhost:5173
```

The API base URL defaults to:

```text
http://localhost:8080
```

Override it with:

```text
VITE_API_BASE_URL=http://localhost:8080
```

## GitHub Pages

GitHub Pages serves only the built frontend. It cannot run the Spring Boot
backend, and `localhost` in a deployed page points to the visitor's computer.

Deploy the backend somewhere else, then build and publish the frontend with the
backend origin:

```powershell
$env:VITE_API_BASE_URL = "https://your-backend-host.example.com"
npm run deploy
```

Do not include `/api` in `VITE_API_BASE_URL`; request paths already include it.

## Authentication Notes

- The frontend keeps the authenticated user and bearer token in memory only for the current browser tab.
- The token is not stored in `localStorage`, `sessionStorage`, or cookies.
- This reduces exposure to token theft through persistent browser storage, but a page refresh clears the session and requires the user to log in again.
- Unauthorized or expired sessions are cleared automatically and the user is sent back through the login flow with the backend error message.
