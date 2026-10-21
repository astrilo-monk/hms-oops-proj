$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$patch = Join-Path $root 'hms-frontend.patch'
Set-Location $root
if (-not (Test-Path $patch)) { throw "patch not found at $patch, place hms-frontend.patch in the repository root first" }
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'git not found on PATH' }
Write-Host 'checking patch; this patch only adds new files' -ForegroundColor Cyan
& git apply --check $patch
if ($LASTEXITCODE -ne 0) { throw 'patch check failed; nothing was applied' }
& git apply $patch
if ($LASTEXITCODE -ne 0) { throw 'patch application failed' }
Write-Host 'patch applied. existing files were not deleted or overwritten.' -ForegroundColor Green
Write-Host 'review the changes with: git status --short'
