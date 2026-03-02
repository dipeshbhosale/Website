#!/usr/bin/env pwsh
# ================================================================
# Skill Nest — Start All Backend Services
# Run from: skill-nest-backend/
# Usage:  .\start-all.ps1
# ================================================================

$services = @(
    @{ name = "api-gateway";        port = 8080; dir = "api-gateway"        },
    @{ name = "auth-service";       port = 8081; dir = "auth-service"       },
    @{ name = "course-service";     port = 8082; dir = "course-service"     },
    @{ name = "user-service";       port = 8083; dir = "user-service"       },
    @{ name = "enrollment-service"; port = 8084; dir = "enrollment-service" },
    @{ name = "calendar-service";   port = 8085; dir = "calendar-service"   }
)

# Load .env file if it exists
if (Test-Path ".env") {
    Get-Content ".env" | ForEach-Object {
        if ($_ -match "^\s*([^#][^=]+)=(.+)$") {
            [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
        }
    }
    Write-Host "✅ Loaded .env"
}

Write-Host ""
Write-Host "╔══════════════════════════════════════════╗"
Write-Host "║   Skill Nest Backend — Starting Services ║"
Write-Host "╚══════════════════════════════════════════╝"
Write-Host ""

$jobs = @()
foreach ($svc in $services) {
    Write-Host "▶ Starting $($svc.name) on port $($svc.port)..."
    $job = Start-Job -ScriptBlock {
        param($dir, $name)
        Set-Location $using:PSScriptRoot
        Set-Location $dir
        mvn spring-boot:run -q 2>&1 | ForEach-Object { "[$name] $_" }
    } -ArgumentList $svc.dir, $svc.name
    $jobs += $job
    Start-Sleep -Seconds 2
}

Write-Host ""
Write-Host "All services starting. Press Ctrl+C to stop all."
Write-Host ""
Write-Host "Service        URL"
Write-Host "─────────────────────────────────────────────────"
foreach ($svc in $services) {
    Write-Host "$($svc.name.PadRight(22)) http://localhost:$($svc.port)"
}
Write-Host ""

try {
    Receive-Job -Job $jobs -Wait
} finally {
    $jobs | Stop-Job
    $jobs | Remove-Job
    Write-Host "All services stopped."
}
