Param([
  switch]$Quiet
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (Test-Path .\out) {
  Remove-Item -Recurse -Force .\out
  if (-not $Quiet) { Write-Host "[clean-out] Removed .\\out" }
} else {
  if (-not $Quiet) { Write-Host "[clean-out] .\\out not found" }
}

