# Step 11 - Docker image and container lifecycle demo.
# Runs every lifecycle command in order and writes a timestamped log that can be
# submitted as the "Docker command log" deliverable.
#
#   mvn -B package -DskipTests
#   $env:DB_PASSWORD = "<your MySQL root password>"
#   powershell -ExecutionPolicy Bypass -File docker\container-lifecycle.ps1

param(
    [string] $Version = "1.2.0",
    [int]    $Port = 8090,
    [string] $LogFile = "docs\evidence\docker-lifecycle.log"
)

$ErrorActionPreference = "Continue"
$image = "vehicle-inspection-workflow"
$name  = "vi-lifecycle-demo"
$net   = "vi-lifecycle-net"
$db    = "vi-lifecycle-mysql"

if (-not $env:DB_PASSWORD) { throw "Set `$env:DB_PASSWORD first." }
New-Item -ItemType Directory -Force -Path (Split-Path $LogFile) | Out-Null
"# Docker container lifecycle - $(Get-Date -Format s)" | Set-Content $LogFile

function Step([string] $title, [string] $command) {
    $header = "`n===== $title`n> $command"
    Write-Host $header -ForegroundColor Cyan
    Add-Content $LogFile $header
    $output = Invoke-Expression "$command 2>&1" | Out-String
    Write-Host $output
    Add-Content $LogFile $output.TrimEnd()
}

Step "Docker version"              "docker version --format '{{.Server.Version}}'"
Step "1. Build image"              "docker build -t ${image}:${Version} --build-arg APP_VERSION=$Version ."
Step "2. Tag image (latest)"       "docker tag ${image}:${Version} ${image}:latest"
Step "3. List images"              "docker images $image"
Step "4. Image details"            "docker image inspect ${image}:${Version} --format 'Id={{.Id}} Size={{.Size}} Created={{.Created}} Labels={{json .Config.Labels}}'"
Step "5. Image history (layers)"   "docker history ${image}:${Version}"

Step "6. Create network"           "docker network create $net"
Step "7. Start MySQL container"    "docker run -d --name $db --network $net -e MYSQL_ROOT_PASSWORD=$env:DB_PASSWORD -e MYSQL_DATABASE=vehicle_inspection_db mysql:8.4"
Write-Host "Waiting for MySQL..." ; $ok = $false
for ($i = 0; $i -lt 40 -and -not $ok; $i++) {
    # the container knows its own root password, so nothing secret goes on the command line
    docker exec $db sh -c 'MYSQL_PWD=$MYSQL_ROOT_PASSWORD mysqladmin ping -h 127.0.0.1 -uroot --silent' 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ok = $true } else { Start-Sleep 3 }
}

Step "8. Run app container (port mapping $Port->8080)" ("docker run -d --name $name --network $net -p ${Port}:8080 " +
    "-e DB_URL=jdbc:mysql://${db}:3306/vehicle_inspection_db -e DB_USERNAME=root -e DB_PASSWORD=*** ${image}:${Version}").Replace("***", $env:DB_PASSWORD)
Write-Host "Waiting for the app to become healthy..."
for ($i = 0; $i -lt 40; $i++) {
    $h = docker inspect -f '{{.State.Health.Status}}' $name 2>$null
    if ($h -eq "healthy") { break }; Start-Sleep 3
}
Step "9. Running containers"       "docker ps --filter name=vi-lifecycle"
Step "10. Port mapping"            "docker port $name"
Step "11. Health status"           "docker inspect -f '{{.State.Status}} / health={{.State.Health.Status}}' $name"
Step "12. HTTP check via mapped port" "Invoke-RestMethod http://localhost:$Port/actuator/health | ConvertTo-Json -Depth 6"
Step "13. Container logs (last 25 lines)" "docker logs --tail 25 $name"
Step "14. Resource usage"          "docker stats --no-stream $name"
Step "15. Stop container"          "docker stop $name"
Step "16. State after stop"        "docker ps -a --filter name=$name --format '{{.Names}} {{.Status}}'"
Step "17. Start container again"   "docker start $name"
Step "18. Restart container"       "docker restart $name"
Start-Sleep 5
Step "19. State after restart"     "docker ps --filter name=$name --format '{{.Names}} {{.Status}}'"
Step "20. Remove container (force)" "docker rm -f $name"
Step "21. Clean up demo MySQL and network" "docker rm -f $db; docker network rm $net"
Step "22. Containers left"         "docker ps -a --filter name=vi-lifecycle"

Write-Host "`nLifecycle log written to $LogFile" -ForegroundColor Green
