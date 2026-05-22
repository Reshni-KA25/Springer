# Springer App — Electron + Spring Boot

A desktop application built with **Electron** (frontend) and **Spring Boot** (backend JAR). The backend runs as a local Java process spawned by Electron, and the frontend is a Vite/React build served via `file://` protocol.

---

## Project Structure

```
Electron Springer app/
├── main.js                  # Electron main process — starts backend JAR, creates window
├── package.json             # Electron + electron-builder config
├── EligibilityRule.json     # App configuration/rules file
├── frontend/                # Pre-built Vite/React frontend (dist output)
│   ├── index.html
│   ├── assets/              # JS, CSS bundles
│   └── *.png / *.svg        # Static image assets
├── backend/
│   └── springer.jar         # Spring Boot fat JAR
└── jre/
    └── bin/
        └── java.exe         # Bundled Java 21 JRE (so clients need no Java install)
```

---

## How It Works

1. **Electron starts** → `main.js` runs
2. **Backend launch** → Electron spawns `jre/bin/java.exe -jar backend/springer.jar`
3. **Spring Boot starts** on its configured port (e.g. `http://localhost:8080`)
4. **After 5 seconds**, Electron opens a `BrowserWindow` loading `frontend/index.html`
5. **Frontend** (React/Vite) communicates with the backend via REST API calls to `localhost`
6. **File protocol fix** → A custom `file://` protocol interceptor remaps absolute Vite asset paths to the correct `frontend/` directory

---

## Prerequisites (Development)

| Tool | Version |
|------|---------|
| Node.js | 18+ |
| npm | 9+ |
| Java JDK | 21 (to build the Spring Boot JAR) |

> **For running the packaged app** — no Java installation needed; JRE 21 is bundled in the `jre/` folder.

---

## Setup & Run (Development)

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd "Electron Springer app"
```

### 2. Install Node dependencies

```bash
npm install
```

### 3. Add required files (not tracked in Git)

These files are too large for Git and must be added manually after cloning:

- **`backend/springer.jar`** — Build from the Spring Boot source project:
  ```bash
  cd <spring-boot-project>
  mvn clean package -DskipTests
  # Copy target/springer-*.jar → Electron Springer app/backend/springer.jar
  ```

- **`jre/`** — Download Java 21 JRE (zip) from https://adoptium.net/temurin/releases/?version=21
  - Choose: Windows → x64 → JRE → `.zip`
  - Extract and place contents directly inside the `jre/` folder so that `jre/bin/java.exe` exists

### 4. Start the app

```bash
npm start
```

---

## Build Distributable (Windows .exe)

> **Note:** Run PowerShell as Administrator, or enable Windows Developer Mode  
> (Settings → Privacy & Security → For Developers → Developer Mode ON)  
> to avoid symlink permission errors during build.

```powershell
$env:CSC_IDENTITY_AUTO_DISCOVERY="false"; npm run dist
```

Output will be in the `dist/` folder:
- `dist/win-unpacked/` — Portable version (zip and share the whole folder)
- `dist/Springer App Setup 1.0.0.exe` — Installer (if NSIS succeeds)

### Sharing the app

Share the entire `dist/win-unpacked/` folder (zipped). The recipient:
1. Extracts the zip
2. Runs `Springer App.exe` from inside the folder
3. No Java installation needed — JRE 21 is bundled

---

## Logs

App logs are written to:
```
C:\Users\<username>\AppData\Roaming\Springer App\app.log
```
Check this file to debug backend startup issues on client machines.

---

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `ERR_FILE_NOT_FOUND` for assets | Vite built with absolute paths | Already handled by protocol interceptor in `main.js` |
| `UnsupportedClassVersionError` | JRE version too old for the JAR | Ensure `jre/` contains Java 21 |
| `Unable to access jarfile` | JAR not found at path | Ensure `backend/springer.jar` exists |
| `javax.management.MBeanRegistration` not found | Stripped/minimal JRE missing modules | Use a full JRE zip from Adoptium, not a jlink-stripped one |
| Backend not connected | JAR failed to start | Check `app.log` for details |
