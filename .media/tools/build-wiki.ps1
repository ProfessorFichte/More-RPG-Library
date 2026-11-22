<#
.SYNOPSIS
  Fills the spell tables of the GitHub wiki pages in <repo>\.media\wiki from the mod's own data.

.EXAMPLE
  powershell -File build-wiki.ps1 -RepoDir . -RawBase https://raw.githubusercontent.com/ProfessorFichte/Witcher/1.21.1

.DESCRIPTION
  Wiki pages live in <repo>\.media\wiki\**\*.md, subfolders included (Home.md, _Sidebar.md, ✨Spells.md ...). A page gets a spell table by containing
      <!-- SPELLS:<group> --><!-- /SPELLS:<group> -->
  The groups are defined in <repo>\.media\wiki\spells.json:
    {
      "namespace": "witcher_rpg",
      "groups": {
        "signs":   { "tag": "spell_book/signs", "label": "Sign" },
        "weapons": { "spells": ["witcher_rpg:defensive_witcher_mechanics"], "label": "Skill",
                     "foundOn": { "witcher_rpg:defensive_witcher_mechanics": "All Witcher Swords" } }
      },
      "descriptions": { "witcher_rpg:aard": "Short text without numbers." },
      "icons": { "witcher_rpg:some_spell": "spell/other_icon.png" }
    }
  - "tag": spell tag in data/<namespace>/tags/spell/<tag>.json (generated or resources), so new spells show up automatically
  - names come from the en_us lang file (spell.<ns>.<path>.name), icons default to textures/spell/<path>.png
  - a spell without an entry in "descriptions" uses the lang description without numbers, plus a warning
  Image placeholders (<!-- IMAGE: .media/images/x.png | Alt -->) are expanded like in build-pages.ps1.
#>
param(
    [string]$RepoDir = ".",
    [string]$RawBase = ""
)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "media-common.ps1")

$repo = Resolve-Full $RepoDir
$wikiDir = Join-Path $repo ".media\wiki"
if (-not (Test-Path -LiteralPath $wikiDir)) { Write-Host "   kein .media\wiki Ordner"; return }
$warn = New-Object System.Collections.Generic.List[string]

$cfgPath = Join-Path $wikiDir "spells.json"
$cfg = $null
if (Test-Path -LiteralPath $cfgPath) { $cfg = Read-Json $cfgPath }

function Find-ResourceFile([string]$rel) {
    foreach ($root in @("common\src\main\generated", "common\src\main\resources", "src\main\generated", "src\main\resources")) {
        $p = Join-Path (Join-Path $repo $root) $rel
        if (Test-Path -LiteralPath $p) { return $p }
    }
    return $null
}

$lang = @{}
if ($cfg) {
    $ns = [string]$cfg.namespace
    $langFile = Find-ResourceFile "assets\$ns\lang\en_us.json"
    if (-not $langFile) { throw "Lang-Datei fuer $ns nicht gefunden" }
    $langObj = Read-Json $langFile
    foreach ($p in $langObj.PSObject.Properties) { $lang[$p.Name] = [string]$p.Value }
}

function Get-TagSpells([string]$tagNs, [string]$tagPath, [System.Collections.Generic.HashSet[string]]$seen) {
    $file = Find-ResourceFile ("data\$tagNs\tags\spell\" + $tagPath.Replace('/', '\') + ".json")
    if (-not $file) { $warn.Add("Spell-Tag nicht gefunden: $tagNs`:$tagPath"); return @() }
    $out = @()
    foreach ($v in (Read-Json $file).values) {
        $id = if ($v -is [string]) { $v } else { [string]$v.id }
        if ($id.StartsWith("#")) {
            $sub = $id.Substring(1).Split(':')
            if ($seen.Add($id)) { $out += Get-TagSpells $sub[0] $sub[1] $seen }
        } else { $out += $id }
    }
    return $out
}

function Get-ShortDescription([string]$id) {
    $d = Get-Prop $cfg.descriptions $id
    if ($d) { return [string]$d }
    $parts = $id.Split(':')
    $raw = $lang["spell.$($parts[0]).$($parts[1]).description"]
    if (-not $raw) { $warn.Add("Keine Beschreibung fuer $id"); return "" }
    $warn.Add("Beschreibung fuer $id aus der Lang-Datei uebernommen - kurze Version in spells.json eintragen")
    $t = $raw -replace '%%', '%'
    $t = [regex]::Replace($t, '\s*(for|by|of|up to|lasting for)?\s*\{[^}]+\}\s*(seconds|second|secs|sec\.?|s\b)?', ' ')
    $t = [regex]::Replace($t, '\s+([.,;:])', '$1')
    return ([regex]::Replace($t, '\s{2,}', ' ')).Trim()
}

function New-SpellTable([string]$groupName) {
    $g = Get-Prop $cfg.groups $groupName
    if (-not $g) { $warn.Add("Gruppe '$groupName' fehlt in spells.json"); return "" }
    $ids = @()
    $tag = [string](Get-Prop $g "tag")
    if ($tag) {
        $tagNs = $cfg.namespace; $tagPath = $tag
        if ($tag.Contains(':')) { $tagNs, $tagPath = $tag.Split(':') }
        $ids += Get-TagSpells $tagNs $tagPath (New-Object System.Collections.Generic.HashSet[string])
    }
    $extra = Get-Prop $g "spells"
    if ($extra) { $ids += @($extra) }
    $hide = @(Get-Prop $g "hide")
    $ids = @($ids | Where-Object { $hide -notcontains $_ } | Select-Object -Unique)

    $label = [string](Get-Prop $g "label"); if (-not $label) { $label = "Spell" }
    $foundOn = Get-Prop $g "foundOn"
    $assetBase = if ($RawBase) { $RawBase.TrimEnd('/') + "/common/src/main/resources/assets" } else { "" }

    $head = "| Icon | $label | ID |"; $sep = "|---|---|---|"
    if ($foundOn) { $head += " Found on |"; $sep += "---|" }
    $head += " Description |"; $sep += "---|"
    $lines = @($head, $sep)
    foreach ($id in $ids) {
        $ns, $path = $id.Split(':')
        $name = $lang["spell.$ns.$path.name"]
        if (-not $name) { $warn.Add("Kein Name in der Lang-Datei fuer $id"); $name = $path }
        $icon = [string](Get-Prop $cfg.icons $id)
        if (-not $icon) { $icon = "spell/$path.png" }
        if (-not (Find-ResourceFile "assets\$ns\textures\$($icon.Replace('/', '\'))")) { $warn.Add("Icon fehlt: assets/$ns/textures/$icon") }
        $img = "<img src=`"$assetBase/$ns/textures/$icon`" width=`"32`">"
        $row = "| $img | $name | ``$id`` |"
        if ($foundOn) {
            $f = [string](Get-Prop $foundOn $id)
            if (-not $f) { $f = [string](Get-Prop $foundOn "*") }
            $row += " $f |"
        }
        $row += " $(Get-ShortDescription $id) |"
        $lines += $row
    }
    return ($lines -join "`n")
}

foreach ($page in Get-ChildItem -LiteralPath $wikiDir -Filter *.md -Recurse) {
    $text = (Read-Text $page.FullName) -replace "`r`n", "`n"
    $new = Expand-Images $text $repo $RawBase $warn
    if ($cfg) {
        $new = [regex]::Replace($new, '(?s)<!-- SPELLS:([\w\-/]+) -->.*?<!-- /SPELLS:\1 -->', [System.Text.RegularExpressions.MatchEvaluator]{
            param($m)
            $name = $m.Groups[1].Value
            "<!-- SPELLS:$name -->`n" + (New-SpellTable $name) + "`n<!-- /SPELLS:$name -->"
        })
    }
    if ($new -ne $text) { Write-Text $page.FullName $new; Write-Host "   Wiki: $($page.Name) aktualisiert" -ForegroundColor Green }
}
foreach ($w in ($warn | Sort-Object -Unique)) { Write-Warning $w }
