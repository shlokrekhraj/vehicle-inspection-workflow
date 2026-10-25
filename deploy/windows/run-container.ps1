# Runs one app container on vi-net and waits until Docker reports it healthy.
param(
    [Parameter(Mandatory = $true)] [string] $Image,
    [Parameter(Mandatory = $true)] [string] $Name,
    [Parameter(Mandatory = $true)] [string] $DbName,
    [string] $Environment = "docker",
    [int]    $HostPort = 0,
    [string] $Network = "vi-net",
    [int]    $TimeoutSeconds = 180
)
if (-not $env:DB_PASSWORD) { throw "DB_PASSWORD is not set" }

docker rm -f $Name 2>$null | Out-Null
$runArgs = @("run", "-d", "--name", $Name, "--network", $Network, "--restart", "unless-stopped",
    "-e", "DB_URL=jdbc:mysql://vi-mysql:3306/${DbName}?createDatabaseIfNotExist=true",
    "-e", "DB_USERNAME=root", "-e", "DB_PASSWORD=$env:DB_PASSWORD",
    "-e", "APP_ENV=$Environment")
if ($HostPort -gt 0) { $runArgs += @("-p", "${HostPort}:8080") }
$runArgs += $Image
docker @runArgs | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Host "docker run failed"; exit 1 }
Write-Host "Started $Name from $Image (env=$Environment, db=$DbName, port=$HostPort)"

$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
while ((Get-Date) -lt $deadline) {
    $health = docker inspect -f '{{.State.Health.Status}}' $Name 2>$null
    $running = docker inspect -f '{{.State.Running}}' $Name 2>$null
    if ($health -eq "healthy") { Write-Host "$Name is healthy"; exit 0 }
    if ($running -ne "true") { break }
    Start-Sleep -Seconds 4
}
Write-Host "$Name did not become healthy - last log lines:"
docker logs --tail 60 $Name
exit 1
