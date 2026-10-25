# Starts the Spring Boot jar in the background and records its PID.
# Used by the Jenkins Selenium stage (test instance) and the Deploy stage.
param(
    [Parameter(Mandatory = $true)] [string] $Jar,
    [Parameter(Mandatory = $true)] [string] $WorkDir,
    [int] $Port = 8080,
    [string] $Environment = "local",
    [string] $Version = "dev",
    [string] $DbName = "vehicle_inspection_db"
)
$ErrorActionPreference = "Stop"

New-Item -ItemType Directory -Force -Path $WorkDir | Out-Null
$pidFile = Join-Path $WorkDir "app.pid"

& (Join-Path $PSScriptRoot "stop-app.ps1") -WorkDir $WorkDir

$env:APP_PORT = "$Port"
$env:APP_ENV = $Environment
$env:APP_VERSION = $Version
$env:DB_URL = "jdbc:mysql://localhost:3306/${DbName}?createDatabaseIfNotExist=true"

$app = Start-Process -FilePath "java" `
    -ArgumentList "-jar", "`"$Jar`"" `
    -WorkingDirectory $WorkDir `
    -RedirectStandardOutput (Join-Path $WorkDir "app.log") `
    -RedirectStandardError (Join-Path $WorkDir "app-error.log") `
    -WindowStyle Hidden -PassThru

Set-Content -Path $pidFile -Value $app.Id
Write-Host "Started $Jar (env=$Environment, port=$Port, db=$DbName) with PID $($app.Id)"
