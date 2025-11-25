# SSH Helper Functions for DPW Server Deployment
# This script provides functions to execute commands on the remote server

$Global:ServerIP = "102.210.149.40"
$Global:AdminUser = "admin"
$Global:AdminPassword = "DPw!@2025"
$Global:RootPassword = "DPW!@2025!"

function Invoke-SSHCommand {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Command,
        
        [Parameter(Mandatory=$false)]
        [switch]$AsRoot,
        
        [Parameter(Mandatory=$false)]
        [string]$Description
    )
    
    if ($Description) {
        Write-Host "`n>>> $Description" -ForegroundColor Cyan
    }
    
    Write-Host "Executing: $Command" -ForegroundColor Yellow
    
    if ($AsRoot) {
        # Execute as root using su
        $fullCommand = "echo '$Global:RootPassword' | su -c `"$Command`" root"
        $sshCmd = "ssh -o StrictHostKeyChecking=no $Global:AdminUser@$Global:ServerIP `"$fullCommand`""
    } else {
        # Execute as admin user
        $sshCmd = "ssh -o StrictHostKeyChecking=no $Global:AdminUser@$Global:ServerIP `"$Command`""
    }
    
    # Use plink if available (comes with PuTTY), otherwise use OpenSSH
    if (Get-Command plink -ErrorAction SilentlyContinue) {
        $output = echo y | plink -ssh -pw $Global:AdminPassword $Global:AdminUser@$Global:ServerIP $Command
    } else {
        # For OpenSSH on Windows
        Write-Host "Note: You may need to enter password: $Global:AdminPassword" -ForegroundColor Yellow
        Invoke-Expression $sshCmd
    }
    
    Write-Host "Done.`n" -ForegroundColor Green
}

function Copy-ToServer {
    param(
        [Parameter(Mandatory=$true)]
        [string]$LocalPath,
        
        [Parameter(Mandatory=$true)]
        [string]$RemotePath,
        
        [Parameter(Mandatory=$false)]
        [string]$Description
    )
    
    if ($Description) {
        Write-Host "`n>>> $Description" -ForegroundColor Cyan
    }
    
    Write-Host "Uploading: $LocalPath -> $RemotePath" -ForegroundColor Yellow
    
    # Use pscp if available (comes with PuTTY), otherwise use scp
    if (Get-Command pscp -ErrorAction SilentlyContinue) {
        pscp -pw $Global:AdminPassword $LocalPath "$Global:AdminUser@$Global:ServerIP`:$RemotePath"
    } else {
        Write-Host "Note: You may need to enter password: $Global:AdminPassword" -ForegroundColor Yellow
        scp $LocalPath "$Global:AdminUser@$Global:ServerIP`:$RemotePath"
    }
    
    Write-Host "Upload complete.`n" -ForegroundColor Green
}

function Get-FromServer {
    param(
        [Parameter(Mandatory=$true)]
        [string]$RemotePath,
        
        [Parameter(Mandatory=$true)]
        [string]$LocalPath,
        
        [Parameter(Mandatory=$false)]
        [string]$Description
    )
    
    if ($Description) {
        Write-Host "`n>>> $Description" -ForegroundColor Cyan
    }
    
    Write-Host "Downloading: $RemotePath -> $LocalPath" -ForegroundColor Yellow
    
    if (Get-Command pscp -ErrorAction SilentlyContinue) {
        pscp -pw $Global:AdminPassword "$Global:AdminUser@$Global:ServerIP`:$RemotePath" $LocalPath
    } else {
        Write-Host "Note: You may need to enter password: $Global:AdminPassword" -ForegroundColor Yellow
        scp "$Global:AdminUser@$Global:ServerIP`:$RemotePath" $LocalPath
    }
    
    Write-Host "Download complete.`n" -ForegroundColor Green
}

# Export functions
Export-ModuleMember -Function Invoke-SSHCommand, Copy-ToServer, Get-FromServer
