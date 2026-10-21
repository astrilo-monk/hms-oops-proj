$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$frontend = Join-Path $root 'frontend'
Set-Location $frontend
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) { throw 'npm not found. install Node.js LTS first.' }
if (-not (Test-Path 'node_modules')) { npm install; if ($LASTEXITCODE -ne 0) { throw 'npm install failed' } }
npm run dev
