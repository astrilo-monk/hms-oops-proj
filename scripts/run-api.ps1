$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'javac not found. install a JDK and ensure javac is on PATH.' }
New-Item -ItemType Directory -Force -Path 'out' | Out-Null
$files = Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
if (-not $files) { throw 'no Java source files found under src' }
Write-Host 'compiling existing Java classes and the new HTTP API...' -ForegroundColor Cyan
& javac --add-modules jdk.httpserver -d out $files
if ($LASTEXITCODE -ne 0) { throw 'compilation failed, see errors above' }
Write-Host 'starting local API at http://127.0.0.1:8080/api' -ForegroundColor Green
& java --add-modules jdk.httpserver -cp out api.HotelApi
