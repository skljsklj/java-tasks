Param()
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Write-Host "[build] Cleaning and compiling Java sources..."
if (Test-Path .\out) { Remove-Item -Recurse -Force .\out }
New-Item -ItemType Directory .\out | Out-Null

$sources = Get-ChildItem -Recurse .\src\main\java -Filter *.java | ForEach-Object FullName
if (-not $sources -or $sources.Count -eq 0) {
  Write-Error "[build] No Java sources found under src\main\java"
}

& javac -encoding UTF-8 -d .\out @sources
if ($LASTEXITCODE -ne 0) { throw "[build] Compilation failed (exit $LASTEXITCODE)" }

if (-not (Test-Path .\out\eindex\server\Server.class)) {
  Write-Warning "[build] Server class not found after compile."
}

Write-Host "[build] Done. Classes in .\out"

