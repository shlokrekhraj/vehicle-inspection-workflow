# Runs Nginx (in Docker) as the public entry point, reverse-proxying to the app.
param(
    [Parameter(Mandatory = $true)] [string] $Upstream,   # e.g. host.docker.internal:9090
    [Parameter(Mandatory = $true)] [string] $ConfigDir,  # folder that will hold default.conf.template
    [int] $Port = 8088,
    [string] $Name = "vi-nginx",
    [string] $Network = ""
)

New-Item -ItemType Directory -Force -Path $ConfigDir | Out-Null
Copy-Item (Join-Path $PSScriptRoot "..\nginx\default.conf.template") $ConfigDir -Force

docker rm -f $Name 2>$null | Out-Null

$runArgs = @("run", "-d", "--name", $Name, "--restart", "unless-stopped",
    "-p", "${Port}:80",
    "-e", "APP_UPSTREAM=$Upstream",
    "-v", "${ConfigDir}:/etc/nginx/templates:ro")
if ($Network) { $runArgs += @("--network", $Network) }
else { $runArgs += @("--add-host", "host.docker.internal:host-gateway") }
$runArgs += "nginx:1.27-alpine"

docker @runArgs
if ($LASTEXITCODE -ne 0) { throw "docker run for Nginx failed" }
Write-Host "Nginx '$Name' listening on http://localhost:$Port -> $Upstream"
