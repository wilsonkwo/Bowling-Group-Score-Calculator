<#
Stops whatever is listening on the frontend (5173) and backend (8080) dev ports.
#>

$ports = @(8080, 5173)

foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if (-not $conns) {
        Write-Host "Port ${port}: nothing listening"
        continue
    }

    foreach ($procId in ($conns.OwningProcess | Select-Object -Unique)) {
        $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "Port ${port}: stopping $($proc.ProcessName) (PID $procId)"
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    }
}
