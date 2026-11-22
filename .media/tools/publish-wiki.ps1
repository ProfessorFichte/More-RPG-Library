<#
.SYNOPSIS
  Copies <repo>\.media\wiki\**\*.md (subfolders kept) into the GitHub wiki repository (<owner>/<repo>.wiki.git) and commits it.

.EXAMPLE
  powershell -File publish-wiki.ps1 -RepoDir . -GitHub ProfessorFichte/Witcher          # commit only, shows the diff
  powershell -File publish-wiki.ps1 -RepoDir . -GitHub ProfessorFichte/Witcher -Push    # commit + push

.NOTES
  The wiki clone lives in %LOCALAPPDATA%\mod-wikis\<repo>. GitHub only creates the wiki repository after the
  first page was saved in the browser (Wiki tab -> "Create the first page"), so do that once per new wiki.
  Pages that exist online but not in .media\wiki are kept and listed as a warning; delete them in the browser if needed.
#>
param(
    [string]$RepoDir = ".",
    [Parameter(Mandatory = $true)][string]$GitHub,
    [switch]$Push,
    [string]$Message = "Update wiki"
)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "media-common.ps1")

$repo = Resolve-Full $RepoDir
$src = Join-Path $repo ".media\wiki"
if (-not (Test-Path -LiteralPath $src)) { throw "Kein .media\wiki Ordner in $repo" }

$cacheRoot = Join-Path $env:LOCALAPPDATA "mod-wikis"
$name = $GitHub.Split('/')[1]
$clone = Join-Path $cacheRoot $name
$env:GIT_TERMINAL_PROMPT = "0"
$env:GCM_INTERACTIVE = "never"

if (-not (Test-Path -LiteralPath (Join-Path $clone ".git"))) {
    New-Item -ItemType Directory -Force $cacheRoot | Out-Null
    git clone -q "https://github.com/$GitHub.wiki.git" $clone
    if ($LASTEXITCODE -ne 0) { throw "Wiki-Repo $GitHub.wiki.git nicht gefunden. Erst im Browser die erste Wiki-Seite anlegen." }
} else {
    git -C $clone pull -q --ff-only
    if ($LASTEXITCODE -ne 0) { throw "git pull im Wiki-Klon fehlgeschlagen: $clone" }
}

$srcPages = @(Get-ChildItem -LiteralPath $src -Filter *.md -Recurse)
$srcRel = @{}
foreach ($p in $srcPages) {
    if ($srcRel.ContainsKey($p.Name)) { throw "Seitenname doppelt in .media\wiki (GitHub-Wiki braucht eindeutige Namen): $($p.Name)" }
    $srcRel[$p.Name] = $p.FullName.Substring($src.Length).TrimStart('\')
}
# GitHub wikis resolve pages by file name, so a page moved into a subfolder must not keep its old copy.
$gitDir = Join-Path $clone ".git"
foreach ($p in (Get-ChildItem -LiteralPath $clone -Filter *.md -Recurse | Where-Object { -not $_.FullName.StartsWith($gitDir) })) {
    $rel = $p.FullName.Substring($clone.Length).TrimStart('\')
    if (-not $srcRel.ContainsKey($p.Name)) { Write-Warning "Nur online vorhanden (nicht in .media\wiki): $rel" }
    elseif ($srcRel[$p.Name] -ne $rel) { Remove-Item -LiteralPath $p.FullName -Force }
}
foreach ($p in $srcPages) {
    $dest = Join-Path $clone $srcRel[$p.Name]
    New-Item -ItemType Directory -Force (Split-Path -Parent $dest) | Out-Null
    Copy-Item -LiteralPath $p.FullName -Destination $dest -Force
}

git -C $clone add -A
git -C $clone diff --cached --quiet
if ($LASTEXITCODE -eq 0) { Write-Host "   Wiki: keine Aenderungen"; return }
git -C $clone diff --cached --stat
git -C $clone commit -q -m $Message
Write-Host "   Wiki committed ($clone)" -ForegroundColor Green
if ($Push) {
    git -C $clone push -q
    if ($LASTEXITCODE -ne 0) { throw "git push fuer das Wiki fehlgeschlagen" }
    Write-Host "   Wiki pushed" -ForegroundColor Green
} else {
    Write-Host "   Nicht gepusht (mit -Push hochladen)"
}
