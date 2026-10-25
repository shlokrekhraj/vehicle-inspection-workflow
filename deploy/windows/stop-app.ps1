# Stops the app started by start-app.ps1 (if it is still running).
param([Parameter(Mandatory = $true)] [string] $WorkDir)

$pidFile = Join-Path $WorkDir "app.pid"
if (-not (Test-Path $pidFile)) {
    Write-Host "No PID file in $WorkDir - nothing to stop."
    exit 0
}
$appId = [int](Get-Content $pidFile)
$process = Get-Process -Id $appId -ErrorAction SilentlyContinue
if ($process) {
    Stop-Process -Id $appId -Force
    Start-Sleep -Seconds 2
    Write-Host "Stopped application process $appId."
} else {
    Write-Host "Process $appId has already stopped."
}
Remove-Item $pidFile -Force
exit 0
