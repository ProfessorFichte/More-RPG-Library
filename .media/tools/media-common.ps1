<#
  Shared helpers for build-pages.ps1, build-wiki.ps1 and update-all.ps1 (dot-sourced, not run directly).
#>

$script:Utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Read-Text([string]$path) {
    return [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
}

function Write-Text([string]$path, [string]$text) {
    [System.IO.File]::WriteAllText($path, $text, $script:Utf8NoBom)
}

function Read-Json([string]$path) {
    return (Read-Text $path) | ConvertFrom-Json
}

function Resolve-Full([string]$p, [string]$base = (Get-Location).Path) {
    if ([System.IO.Path]::IsPathRooted($p)) { return [System.IO.Path]::GetFullPath($p) }
    return [System.IO.Path]::GetFullPath((Join-Path $base $p))
}

function Get-Prop($o, [string]$n) {
    if ($null -ne $o -and $o.PSObject.Properties[$n]) { return $o.$n }
    return $null
}

<#
  Image placeholders:  <!-- IMAGE: .media/images/file.png | Alt text -->
  If the file exists in the repo, the placeholder is replaced by a centered image that points to $RawBase/<path>.
  If not, the placeholder stays (invisible on every platform) and a warning lists it.
  Also warns about $RawBase/.media/... images that are referenced but missing locally.
#>
function Expand-Images([string]$text, [string]$repoDir, [string]$rawBase, $warn) {
    if (-not $rawBase) { return $text }
    $rawBase = $rawBase.TrimEnd('/')
    $pattern = '(?:picture_placeholder[ \t]*)?<!--\s*IMAGE:\s*([^|>]+?)\s*\|\s*(.*?)\s*-->'
    $text = [regex]::Replace($text, $pattern, [System.Text.RegularExpressions.MatchEvaluator]{
        param($m)
        $rel = $m.Groups[1].Value.Trim().Replace('\', '/')
        $alt = $m.Groups[2].Value.Trim()
        $local = Join-Path $repoDir $rel
        if (Test-Path -LiteralPath $local) {
            $altEnc = [System.Net.WebUtility]::HtmlEncode($alt)
            return "<div align=`"center`" style=`"text-align:center`">`n<img src=`"$rawBase/$rel`" alt=`"$altEnc`">`n</div>"
        }
        $warn.Add("Bild fehlt noch: $rel ($alt)")
        return $m.Value
    })
    $escaped = [regex]::Escape($rawBase + '/')
    foreach ($m in [regex]::Matches($text, $escaped + '(\.media/[^"\)\s]+)')) {
        $rel = [System.Uri]::UnescapeDataString($m.Groups[1].Value)
        if (-not (Test-Path -LiteralPath (Join-Path $repoDir $rel))) { $warn.Add("Verlinktes Bild nicht im Repo: $rel") }
    }
    return $text
}
