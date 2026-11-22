<#
.SYNOPSIS
  Builds CURSEFORGE.md from the master README.md (GitHub + Modrinth) and fills the banner grids from banners.json.

.EXAMPLE
  powershell -File build-pages.ps1 -Banners ..\small_banners\banners.json -Exclude "Bard" -RawBase https://raw.githubusercontent.com/ProfessorFichte/Bards/1.21.1

.DESCRIPTION
  1. Replaces <!-- IMAGE: .media/images/x.png | Alt --> with a centered image once the file exists in the repo (see media-common.ps1).
  2. Replaces everything between <!-- BANNERS:own --> ... <!-- /BANNERS:own --> (also :series, :others)
     with a grid from banners.json. README links go to Modrinth (fallback CurseForge).
  3. CURSEFORGE.md: <details><summary>..</summary>...</details> -> <div class="spoiler">...</div> on a single line
     without blank lines (CurseForge's Markdown mode only renders the spoiler as one HTML block),
     grids link to CurseForge, marker comments removed.
  4. Warns about tags CurseForge does not allow, Markdown tables, unbalanced <div> and missing images.
  -Exclude = name of the current mod (as in banners.json), hidden in the "own" grid.
#>
param(
    [string]$Source  = "README.md",
    [string]$Target  = "CURSEFORGE.md",
    [string]$Banners = (Join-Path $PSScriptRoot "..\small_banners\banners.json"),
    [string]$Exclude = "",
    [string]$RawBase = ""
)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "media-common.ps1")

$src = Resolve-Full $Source; $dst = Resolve-Full $Target; $bnr = Resolve-Full $Banners
foreach ($f in @($src, $bnr)) { if (-not (Test-Path -LiteralPath $f)) { throw "Datei nicht gefunden: $f" } }
$repoDir = Split-Path -Parent $src

$warn = New-Object System.Collections.Generic.List[string]
$data = Read-Json $bnr

function New-Grid($items, [int]$cols, [string]$platform) {
    $rows = New-Object System.Collections.Generic.List[string]
    for ($i = 0; $i -lt $items.Count; $i += $cols) {
        $cells = @()
        for ($j = $i; $j -lt [Math]::Min($i + $cols, $items.Count); $j++) {
            $it = $items[$j]
            $cf = [string](Get-Prop $it "cf"); $mr = [string](Get-Prop $it "mr")
            if ($platform -eq "mr" -and $mr) { $url = "https://modrinth.com/mod/$mr" }
            elseif ($cf) { $url = "https://www.curseforge.com/minecraft/mc-mods/$cf" }
            else { $url = "https://modrinth.com/mod/$mr" }
            $img = [string](Get-Prop $it "img")
            if (-not $img) { $img = $data.baseUrl + (Get-Prop $it "file") }
            $alt = [System.Net.WebUtility]::HtmlEncode([string]$it.name)
            $cells += "<td align=`"center`" style=`"text-align:center`"><a href=`"$url`"><img src=`"$img`" alt=`"$alt`"></a></td>"
        }
        $rows.Add("<tr>" + ($cells -join "") + "</tr>")
    }
    return "<table>`n<tbody>`n" + ($rows -join "`n") + "`n</tbody>`n</table>"
}

function Set-Banners([string]$text, [string]$platform, [bool]$keepMarkers) {
    foreach ($g in @("own", "series", "others")) {
        $items = @($data.$g)
        if ($g -eq "own" -and $Exclude) { $items = @($items | Where-Object { $_.name -ne $Exclude }) }
        $cols = [int]$data.columns.$g
        $grid = New-Grid $items $cols $platform
        $block = if ($keepMarkers) { "<!-- BANNERS:$g -->`n$grid`n<!-- /BANNERS:$g -->" } else { $grid }
        $pat = "(?s)<!-- BANNERS:$g -->.*?<!-- /BANNERS:$g -->"
        if (-not [regex]::IsMatch($text, $pat)) { $warn.Add("Marker BANNERS:$g nicht in der README gefunden"); continue }
        $text = [regex]::Replace($text, $pat, [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $block })
    }
    return $text
}

$text = (Read-Text $src) -replace "`r`n", "`n"

$master = Expand-Images $text $repoDir $RawBase $warn
$master = Set-Banners $master "mr" $true
if ($master -ne $text) { Write-Text $src $master }

$cfText = [regex]::Replace($master, '(?s)<details[^>]*>\s*<summary[^>]*>.*?</summary>(.*?)</details>', [System.Text.RegularExpressions.MatchEvaluator]{
    param($m)
    $inner = [regex]::Replace($m.Groups[1].Value, '(?s)<!--.*?-->', '')
    $out = ""
    foreach ($line in ($inner -split "`n")) {
        $l = $line.Trim()
        if (-not $l) { continue }
        if ($out -and -not ($out.EndsWith('>') -and $l.StartsWith('<'))) { $out += " " }
        $out += $l
    }
    '<div class="spoiler">' + $out + '</div>'
})
$cfText = Set-Banners $cfText "cf" $false
$cfText = [regex]::Replace($cfText, '(?m)^[ \t]*<!--\s*IMAGE:.*?-->[ \t]*\n', '')

$allowed = @('p','br','strong','b','em','i','u','strike','span','code','pre','sup','cite','abbr','acronym','del','ins',
    'ol','ul','li','dl','dt','dd','a','img','q','blockquote','div','section','article','h1','h2','h3','h4','h5','h6',
    'hr','table','tr','td','th','tbody','thead','iframe','embed','object')
$tags = [regex]::Matches($cfText, '</?([a-zA-Z][a-zA-Z0-9]*)') | ForEach-Object { $_.Groups[1].Value.ToLower() } | Sort-Object -Unique
foreach ($t in $tags) { if ($allowed -notcontains $t) { $warn.Add("Tag <$t> ist bei CurseForge nicht erlaubt") } }
if ($cfText -match '(?m)^\s*\|.*\|\s*$') { $warn.Add("Markdown-Tabelle gefunden - besser HTML-<table> verwenden") }
$open = ([regex]::Matches($cfText, '<div[\s>]')).Count; $close = ([regex]::Matches($cfText, '</div>')).Count
if ($open -ne $close) { $warn.Add("<div> nicht ausgeglichen: $open geoeffnet, $close geschlossen") }

Write-Text $dst $cfText
Write-Host "   README.md aktualisiert, CURSEFORGE.md geschrieben" -ForegroundColor Green
foreach ($w in ($warn | Sort-Object -Unique)) { Write-Warning $w }
