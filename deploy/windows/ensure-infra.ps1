# Makes sure the shared Docker infrastructure exists (idempotent):
#   network vi-net, local registry vi-registry (localhost:5000), MySQL vi-mysql.
param([string] $Network = "vi-net")
# Note: no "Stop" error preference - in Windows PowerShell that turns harmless
# docker stderr output (e.g. "not found") into terminating errors.
if (-not $env:DB_PASSWORD) { throw "DB_PASSWORD is not set" }

function Exists([string] $kind, [string] $name) {
    if ($kind -eq "network") { $found = docker network ls --filter "name=^${name}$" --format "{{.Name}}" }
    else { $found = docker ps -a --filter "name=^${name}$" --format "{{.Names}}" }
    return [bool]$found
}
function Ensure-Running([string] $name) {
    $state = docker inspect -f '{{.State.Running}}' $name
    if ($state -ne "true") { docker start $name | Out-Null; Write-Host "Started existing container $name" }
}

if (-not (Exists "network" $Network)) { docker network create $Network | Out-Null; Write-Host "Created network $Network" }
else { Write-Host "Network $Network already exists" }

if (-not (Exists "container" "vi-registry")) {
    docker run -d --name vi-registry --restart unless-stopped -p 5000:5000 -v vi-registry-data:/var/lib/registry registry:2 | Out-Null
    Write-Host "Started local registry vi-registry on localhost:5000"
} else { Ensure-Running "vi-registry"; Write-Host "Registry vi-registry already running" }

if (-not (Exists "container" "vi-mysql")) {
    docker run -d --name vi-mysql --restart unless-stopped --network $Network `
        -e "MYSQL_ROOT_PASSWORD=$env:DB_PASSWORD" -v vi-mysql-data:/var/lib/mysql mysql:8.4 | Out-Null
    Write-Host "Started MySQL container vi-mysql"
} else { Ensure-Running "vi-mysql"; Write-Host "MySQL container vi-mysql already running" }

Write-Host "Waiting for MySQL to accept connections..."
for ($i = 0; $i -lt 60; $i++) {
    # the container knows its own root password, so nothing secret goes on the command line
    docker exec vi-mysql sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysqladmin ping -h 127.0.0.1 -uroot --silent' 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { Write-Host "MySQL is ready"; exit 0 }
    Start-Sleep -Seconds 3
}
throw "MySQL did not become ready in time"
