# Polls the Spring Boot health endpoint until it reports UP.
param(
    [Parameter(Mandatory = $true)] [string] $Url,
    [int] $TimeoutSeconds = 120,
    [string] $ErrorLog = ""
)
$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
while ((Get-Date) -lt $deadline) {
    try {
        $response = Invoke-RestMethod -Uri $Url -TimeoutSec 5
        if ($response.status -eq "UP") {
            Write-Host "Health check passed: $Url -> UP"
            $response | ConvertTo-Json -Depth 6
            exit 0
        }
        Write-Host "Health status is $($response.status), waiting..."
    } catch {
        Write-Host "Not ready yet ($($_.Exception.Message))"
    }
    Start-Sleep -Seconds 3
}
Write-Host "Health check FAILED: $Url did not report UP within $TimeoutSeconds seconds."
if ($ErrorLog -and (Test-Path $ErrorLog)) { Get-Content $ErrorLog -Tail 60 }
exit 1
