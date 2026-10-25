# Runs an Ansible playbook inside the controller container and saves the output.
#   $env:DB_PASSWORD = "<mysql root password>"
#   powershell -ExecutionPolicy Bypass -File ansible\run-ansible.ps1 -Playbook site.yml -LogFile docs\evidence\ansible-site.log -ExtraArgs "-e fresh_node=true"
param(
    [Parameter(Mandatory = $true)] [string] $Playbook,
    [string] $LogFile = "",
    [string] $ExtraArgs = "",
    [string] $AppVersion = ""
)
if (-not $env:DB_PASSWORD) { throw "Set `$env:DB_PASSWORD first." }
if ($AppVersion) { $env:APP_VERSION = $AppVersion }
$repo = Split-Path $PSScriptRoot -Parent
Push-Location $repo
try {
    $cmd = "docker compose -f ansible/docker-compose.yml run --rm -T -e ANSIBLE_FORCE_COLOR=0 ansible ansible-playbook playbooks/$Playbook $ExtraArgs"
    Write-Host "> $cmd"
    if ($LogFile) {
        New-Item -ItemType Directory -Force -Path (Split-Path $LogFile) | Out-Null
        "> $cmd`n" | Set-Content $LogFile
        cmd /c "$cmd 2>&1" | Tee-Object -FilePath $LogFile -Append
    } else {
        cmd /c "$cmd 2>&1"
    }
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
