<#
.SYNOPSIS
  Updates README.md, CURSEFORGE.md and the wiki pages of all mod repos in one call.

.EXAMPLE
  .\update-all.ps1                          # build everything (README, CURSEFORGE.md, wiki tables), nothing is uploaded
  .\update-all.ps1 -Only Witcher            # one repo (name from repos.json)
  .\update-all.ps1 -Only Witcher -Clipboard # ... and copy CURSEFORGE.md to the clipboard for the CurseForge editor
  .\update-all.ps1 -Commit -Push            # commit + push README.md, CURSEFORGE.md and .media (images + wiki) of every changed repo
  .\update-all.ps1 -Wiki -Push              # also copy .media\wiki into the GitHub wikis and push them
  .\update-all.ps1 -Modrinth                # also upload README.md as Modrinth description (needs $env:MODRINTH_TOKEN)
  .\update-all.ps1 -Root D:\Repos                     # other PC with a different repo folder

.NOTES
  repos.json lists every repo: "folder" (relative to "root"), "name" (as in banners.json), "github" and "branch".
  "root" is relative to this tools folder, so the same file works on every PC as long as the folder layout matches;
  otherwise pass -Root. Repos whose README has no BANNERS marker yet are skipped (not migrated to the new layout).
  Modrinth token: modrinth.com -> Settings -> Personal access tokens, scope "Write projects".
  Set it once per PowerShell window:  $env:MODRINTH_TOKEN = "mrp_..."
  CurseForge has no API for descriptions: paste CURSEFORGE.md into the Markdown editor of the project page.
#>
param(
    [switch]$Commit,
    [switch]$Push,
    [switch]$Wiki,
    [switch]$Modrinth,
    [switch]$Clipboard,
    [string]$Only = "",
    [string]$Root = "",
    [string]$Message = "Update README, CurseForge page and wiki"
)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "media-common.ps1")

$builder = Join-Path $PSScriptRoot "build-pages.ps1"
$wikiBuilder = Join-Path $PSScriptRoot "build-wiki.ps1"
$wikiPublisher = Join-Path $PSScriptRoot "publish-wiki.ps1"
$bannersFile = Resolve-Full "..\small_banners\banners.json" $PSScriptRoot
$cfg = Read-Json (Join-Path $PSScriptRoot "repos.json")
$banners = Read-Json $bannersFile
$rootDir = if ($Root) { Resolve-Full $Root } else { Resolve-Full $cfg.root $PSScriptRoot }

if ($Modrinth -and -not $env:MODRINTH_TOKEN) { throw "MODRINTH_TOKEN ist nicht gesetzt" }

function Send-Modrinth([string]$slug, [string]$readme) {
    $json = @{ body = $readme } | ConvertTo-Json -Compress
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($json)
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-RestMethod -Method Patch -Uri "https://api.modrinth.com/v2/project/$slug" -Body $bytes `
        -ContentType "application/json; charset=utf-8" `
        -Headers @{ Authorization = $env:MODRINTH_TOKEN; "User-Agent" = "mod-pages-update-all/1.0" } | Out-Null
}

$ok = 0; $skipped = @(); $failed = @()
foreach ($r in $cfg.repos) {
    if ($Only -and $r.name -ne $Only -and $r.folder -ne $Only) { continue }
    $dir = Join-Path $rootDir $r.folder
    $readme = Join-Path $dir "README.md"
    if (-not (Test-Path -LiteralPath $readme)) { Write-Warning "Uebersprungen (keine README.md): $($r.name)"; $skipped += $r.name; continue }
    if ((Read-Text $readme) -notmatch '<!-- BANNERS:') { Write-Warning "Uebersprungen (README noch im alten Format): $($r.name)"; $skipped += $r.name; continue }

    Write-Host "== $($r.name)" -ForegroundColor Cyan
    $rawBase = "https://raw.githubusercontent.com/$($r.github)/$($r.branch)"
    Push-Location -LiteralPath $dir
    try {
        & $builder -Banners $bannersFile -Exclude $r.name -RawBase $rawBase
        & $wikiBuilder -RepoDir $dir -RawBase $rawBase

        $branch = (git branch --show-current)
        if ($branch -ne $r.branch) { Write-Warning "Ausgecheckter Branch ist '$branch', Bilder-Links zeigen auf '$($r.branch)'" }

        if ($Clipboard) { Set-Clipboard -Value (Read-Text (Join-Path $dir "CURSEFORGE.md")); Write-Host "   CURSEFORGE.md in der Zwischenablage" }

        if ($Commit) {
            $paths = @("README.md", "CURSEFORGE.md") + @(".media" | Where-Object { Test-Path -LiteralPath (Join-Path $dir $_) })
            git add -- $paths
            git diff --cached --quiet
            if ($LASTEXITCODE -ne 0) {
                git commit -q -m $Message
                Write-Host "   committed" -ForegroundColor Green
                if ($Push) { git push -q; Write-Host "   pushed" -ForegroundColor Green }
            } else { Write-Host "   keine Aenderungen" }
        }

        if ($Wiki -and (Test-Path -LiteralPath (Join-Path $dir ".media\wiki"))) {
            & $wikiPublisher -RepoDir $dir -GitHub $r.github -Push:$Push
        }

        if ($Modrinth) {
            $entry = @($banners.own) | Where-Object { $_.name -eq $r.name } | Select-Object -First 1
            $slug = [string](Get-Prop $r "mr")
            if (-not $slug) { $slug = [string](Get-Prop $entry "mr") }
            if (-not $slug) { Write-Warning "Kein Modrinth-Slug fuer $($r.name) in repos.json oder banners.json" }
            else { Send-Modrinth $slug (Read-Text $readme); Write-Host "   Modrinth-Beschreibung aktualisiert ($slug)" -ForegroundColor Green }
        }
        $ok++
    } catch {
        Write-Warning "Fehler in $($r.name): $($_.Exception.Message)"; $failed += $r.name
    } finally {
        Pop-Location
    }
}
Write-Host "`nFertig: $ok ok, $($skipped.Count) uebersprungen, $($failed.Count) Fehler" -ForegroundColor Green
if ($skipped) { Write-Host "Uebersprungen: $($skipped -join ', ')" }
if ($failed)  { Write-Host "Fehler: $($failed -join ', ')" -ForegroundColor Red }
