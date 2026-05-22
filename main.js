const { app, BrowserWindow, protocol } = require("electron")
const { spawn } = require("child_process")
const path = require("path")
const fs = require("fs")

let backendProcess

function writeLog(msg) {
  const logFile = path.join(app.getPath("userData"), "app.log")
  const line = `[${new Date().toISOString()}] ${msg}\n`
  fs.appendFileSync(logFile, line)
}

function startBackend() {

  // When packaged, backend is unpacked from asar into app.asar.unpacked/
  const basePath = app.isPackaged
    ? path.join(process.resourcesPath, "app.asar.unpacked")
    : __dirname

  const jarPath = path.join(
    basePath,
    "backend",
    "springer.jar"
  )

  // Use bundled JRE so the client doesn't need Java installed
  const jreBasePath = app.isPackaged
    ? path.join(process.resourcesPath, "app.asar.unpacked")
    : __dirname
  const javaBin = path.join(jreBasePath, "jre", "bin", "java.exe")
  const javaExe = fs.existsSync(javaBin) ? javaBin : "java"

  writeLog(`Starting backend. JAR path: ${jarPath}`)
  writeLog(`JAR exists: ${fs.existsSync(jarPath)}`)
  writeLog(`Java binary: ${javaExe}`)

  backendProcess = spawn(
    javaExe,
    ["-jar", jarPath]
  )

  backendProcess.stdout.on("data", (data) => {
    writeLog(`Backend: ${data}`)
    console.log(`Backend: ${data}`)
  })

  backendProcess.stderr.on("data", (data) => {
    writeLog(`Backend Error: ${data}`)
    console.error(`Backend Error: ${data}`)
  })

  backendProcess.on("error", (err) => {
    writeLog(`Failed to start backend: ${err.message}`)
  })

  backendProcess.on("exit", (code) => {
    writeLog(`Backend exited with code: ${code}`)
  })
}

function createWindow() {

  const win = new BrowserWindow({
    width: 1400,
    height: 900,
    autoHideMenuBar: true
  })

  win.loadFile(
    path.join(__dirname, "frontend", "index.html")
  )

  win.webContents.openDevTools()
}

app.whenReady().then(() => {

  // Intercept file:// protocol to remap root-absolute asset paths
  // (e.g. /kanini.png, /assets/index.js) to the frontend directory.
  // Vite builds with absolute paths by default; this fixes them for Electron.
  protocol.interceptFileProtocol("file", (request, callback) => {
    let url = decodeURIComponent(request.url)
    // Strip the file:// scheme
    url = url.replace(/^file:\/\//, "")
    // On Windows strip leading slash before drive letter (e.g. /D:/... → D:/...)
    if (process.platform === "win32") {
      url = url.replace(/^\/([A-Za-z]:)/, "$1")
    }

    if (!fs.existsSync(url)) {
      // The path doesn't exist — it's likely a root-absolute Vite asset path.
      // Strip any leading drive+path prefix down to the bare filename/subpath
      // and resolve it relative to the frontend folder.
      const frontendDir = path.join(__dirname, "frontend")
      // Remove everything up to and including the first path segment that
      // looks like a drive root, leaving just the relative portion.
      const relative = url.replace(/^[A-Za-z]:[\\\/]/, "").replace(/^[\\\/]/, "")
      const remapped = path.join(frontendDir, relative)
      if (fs.existsSync(remapped)) {
        callback({ path: remapped })
        return
      }
    }

    callback({ path: url })
  })

  startBackend()

  setTimeout(() => {
    createWindow()
  }, 5000)

})