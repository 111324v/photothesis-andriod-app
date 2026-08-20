[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$ErrorActionPreference = "SilentlyContinue"

$mainDir = "e:\mobile-master (1)\andriod-app\app\src\main"
$svgDir = Join-Path $mainDir "assets\svg"
$drawableDir = Join-Path $mainDir "res\drawable-nodpi"
$rawDir = Join-Path $mainDir "res\raw"

New-Item -ItemType Directory -Force -Path $svgDir | Out-Null
New-Item -ItemType Directory -Force -Path $drawableDir | Out-Null
New-Item -ItemType Directory -Force -Path $rawDir | Out-Null

$assetsParent = "e:\mobile-master (1)\mobile-master\assets"
$subdirs = Get-ChildItem $assetsParent -Directory

# Find cut dir (has most files) and video dir (has mp4)
$srcCut = ($subdirs | Sort-Object { (Get-ChildItem $_.FullName -File).Count } -Descending)[0]
$srcVideo = $subdirs | Where-Object { (Get-ChildItem $_.FullName -Filter "*.mp4" -File).Count -gt 0 } | Select-Object -First 1

Write-Host "Cut dir: $($srcCut.FullName) ($((Get-ChildItem $srcCut.FullName -File).Count) files)"
Write-Host "Video dir: $($srcVideo.FullName)"

# Bulk copy ALL SVGs from cut dir
$svgFiles = Get-ChildItem $srcCut.FullName -Filter "*.svg" -File
Write-Host "`nCopying $($svgFiles.Count) SVG files..."
$svgFiles | Copy-Item -Destination $svgDir -Force

# Bulk copy ALL PNGs from cut dir (exclude subdirs)
$pngFiles = Get-ChildItem $srcCut.FullName -Filter "*.png" -File
Write-Host "Copying $($pngFiles.Count) PNG files..."
$pngFiles | Copy-Item -Destination $drawableDir -Force

# Copy ALL MP4s from video dir
if ($srcVideo) {
    $mp4Files = Get-ChildItem $srcVideo.FullName -Filter "*.mp4" -File
    Write-Host "Copying $($mp4Files.Count) MP4 files..."
    $mp4Files | Copy-Item -Destination $rawDir -Force
}

Write-Host "`n=== DONE ==="
Write-Host "SVG count: $((Get-ChildItem $svgDir -File).Count)"
Write-Host "Drawable count: $((Get-ChildItem $drawableDir -File).Count)"
Write-Host "Raw count: $((Get-ChildItem $rawDir -File).Count)"
Write-Host "`nSVG files:"
Get-ChildItem $svgDir -File | ForEach-Object { Write-Host "  $($_.Name)" }
Write-Host "`nDrawable files:"
Get-ChildItem $drawableDir -File | ForEach-Object { Write-Host "  $($_.Name)" }
Write-Host "`nRaw files:"
Get-ChildItem $rawDir -File | ForEach-Object { Write-Host "  $($_.Name)" }
