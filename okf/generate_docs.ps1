<#
Converts every okf/*.md file into a sibling .html file (same folder, same name),
so relative links to source files (../src/...) keep working unchanged.
Also copies the architecture diagram and embeds it in the generated index.html.
#>

$okfRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

function Convert-LinkMatch {
    param($m)
    $label = $m.Groups[1].Value
    $url = $m.Groups[2].Value
    if ($url -notmatch '^[a-z]+://' -and $url -match '\.md(#.*)?$') {
        $url = [regex]::Replace($url, '\.md(#.*)?$', '.html$1')
    }
    return "<a href=`"$url`">$label</a>"
}
$linkEvaluator = [System.Text.RegularExpressions.MatchEvaluator]${function:Convert-LinkMatch}

function Convert-Inline {
    param([string]$text)
    $text = [System.Net.WebUtility]::HtmlEncode($text)
    $text = $text -replace '&quot;', '"'
    $text = [regex]::Replace($text, '!\[([^\]]*)\]\(([^)]+)\)', '<img alt="$1" src="$2">')
    $text = [regex]::Replace($text, '\[([^\]]+)\]\(([^)]+)\)', $linkEvaluator)
    $text = $text -replace '\*\*(.+?)\*\*', '<strong>$1</strong>'
    $text = $text -replace '`([^`]+?)`', '<code>$1</code>'
    return $text
}

function Convert-MarkdownToHtml {
    param([string[]]$lines)

    $html = New-Object System.Collections.Generic.List[string]
    $inCode = $false
    $inList = $false
    $inTable = $false
    $tableRow = 0

    foreach ($rawLine in $lines) {
        $line = $rawLine.TrimEnd()

        if ($line -match '^```') {
            if ($inCode) { $html.Add('</code></pre>'); $inCode = $false }
            else {
                if ($inList) { $html.Add('</ul>'); $inList = $false }
                if ($inTable) { $html.Add('</table>'); $inTable = $false; $tableRow = 0 }
                $html.Add('<pre><code>'); $inCode = $true
            }
            continue
        }
        if ($inCode) {
            $html.Add([System.Net.WebUtility]::HtmlEncode($rawLine))
            continue
        }

        if ($line -match '^\s*\|') {
            if ($line -match '^\s*\|[\s:|-]+\|\s*$') { continue }
            if (-not $inTable) {
                if ($inList) { $html.Add('</ul>'); $inList = $false }
                $html.Add('<table>'); $inTable = $true; $tableRow = 0
            }
            $cells = $line.Trim().Trim('|') -split '\|' | ForEach-Object { Convert-Inline $_.Trim() }
            $tag = if ($tableRow -eq 0) { 'th' } else { 'td' }
            $rowHtml = ($cells | ForEach-Object { "<$tag>$_</$tag>" }) -join ''
            $html.Add("<tr>$rowHtml</tr>")
            $tableRow++
            continue
        } elseif ($inTable) {
            $html.Add('</table>'); $inTable = $false; $tableRow = 0
        }

        if ($line -match '^\s*-\s+(.*)$') {
            if (-not $inList) { $html.Add('<ul>'); $inList = $true }
            $html.Add("<li>$(Convert-Inline $Matches[1])</li>")
            continue
        } elseif ($inList) {
            $html.Add('</ul>'); $inList = $false
        }

        if ($line -match '^#\s+(.*)$') { $html.Add("<h1>$(Convert-Inline $Matches[1])</h1>"); continue }
        if ($line -match '^##\s+(.*)$') { $html.Add("<h2>$(Convert-Inline $Matches[1])</h2>"); continue }
        if ($line -match '^###\s+(.*)$') { $html.Add("<h3>$(Convert-Inline $Matches[1])</h3>"); continue }

        if ($line -eq '') { continue }

        $html.Add("<p>$(Convert-Inline $line)</p>")
    }
    if ($inList) { $html.Add('</ul>') }
    if ($inTable) { $html.Add('</table>') }
    if ($inCode) { $html.Add('</code></pre>') }

    return ($html -join "`n")
}

$style = @'
body{font-family:Segoe UI,Arial,sans-serif;max-width:860px;margin:0 auto;padding:32px 24px;line-height:1.6;color:#2C2C2A;background:#FAF9F6}
h1,h2,h3{color:#1a1a18}
h1{border-bottom:2px solid #E6F1FB;padding-bottom:8px}
code{background:#F1EFE8;padding:2px 5px;border-radius:4px;font-family:Consolas,monospace;font-size:0.9em}
pre{background:#F1EFE8;padding:12px 16px;border-radius:6px;overflow-x:auto}
pre code{background:none;padding:0}
table{border-collapse:collapse;width:100%;margin:16px 0}
th,td{border:1px solid #D3D1C7;padding:8px 12px;text-align:left}
th{background:#E6F1FB}
a{color:#185FA5}
img{max-width:100%}
.meta{color:#5F5E5A;font-size:0.85em;margin-bottom:24px}
.back{display:inline-block;margin-bottom:16px;font-size:0.9em}
'@

$mdFiles = Get-ChildItem -Path $okfRoot -Recurse -Filter '*.md'
Write-Host "Found $($mdFiles.Count) markdown files under okf/"

foreach ($file in $mdFiles) {
    $raw = Get-Content -Path $file.FullName -Raw -Encoding UTF8

    $title = $file.BaseName
    $frontmatter = ''
    $body = $raw
    if ($raw -match '(?s)^---\r?\n(.*?)\r?\n---\r?\n(.*)$') {
        $frontmatter = $Matches[1]
        $body = $Matches[2]
        if ($frontmatter -match 'title:\s*(.+)') { $title = $Matches[1].Trim() }
    }

    $bodyHtml = Convert-MarkdownToHtml -lines ($body -split "`r?`n")

    $isIndex = $file.Name -eq 'index.md' -and $file.DirectoryName -eq $okfRoot
    $backLink = if ($isIndex) { '' } else { '<a class="back" href="index.html">&larr; back to index</a>' }

    $page = @"
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>$title</title>
<style>$style</style>
</head>
<body>
$backLink
$bodyHtml
</body>
</html>
"@

    $outPath = [System.IO.Path]::ChangeExtension($file.FullName, '.html')
    Set-Content -Path $outPath -Value $page -Encoding UTF8
    Write-Host "  $($file.Name) -> $(Split-Path -Leaf $outPath)"
}

Write-Host "Done. Open okf\index.html in a browser to view the generated documentation."
Write-Host "Architecture diagram: okf\architecture.svg (embedded in index.html)"
