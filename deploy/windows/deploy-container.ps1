# Deploys a new image as vi-app-<env>. If the new container is not healthy,
# it automatically rolls back to the image that was running before.
param(
    [Parameter(Mandatory = $true)] [string] $Image,
    [Parameter(Mandatory = $true)] [string] $Environment,
    [int] $HostPort = 8085,
    [string] $StateDir = "C:\vehicle-inspection-deploy"
)
$name = "vi-app-$Environment"
$stateFile = Join-Path $StateDir "$Environment-current-image.txt"
New-Item -ItemType Directory -Force -Path $StateDir | Out-Null

$previous = $null
if (Test-Path $stateFile) { $previous = (Get-Content $stateFile).Trim() }
Write-Host "Currently deployed: $(if ($previous) { $previous } else { 'nothing' })"
Write-Host "Deploying:          $Image"

docker pull $Image
& (Join-Path $PSScriptRoot "run-container.ps1") -Image $Image -Name $name -Environment $Environment `
    -DbName "vehicle_inspection_$Environment" -HostPort $HostPort
if ($LASTEXITCODE -eq 0) {
    Set-Content $stateFile $Image
    if ($previous) { Set-Content (Join-Path $StateDir "$Environment-previous-image.txt") $previous }
    Write-Host "DEPLOYED $Image as $name"
    exit 0
}

Write-Host "New release is unhealthy."
if ($previous -and $previous -ne $Image) {
    Write-Host "ROLLING BACK to $previous"
    & (Join-Path $PSScriptRoot "run-container.ps1") -Image $previous -Name $name -Environment $Environment `
        -DbName "vehicle_inspection_$Environment" -HostPort $HostPort
    if ($LASTEXITCODE -eq 0) { Write-Host "Rollback succeeded - $previous is serving traffic" }
}
exit 1
