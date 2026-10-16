$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not (Get-Command flutter -ErrorAction SilentlyContinue)) { throw '請先安裝 Flutter stable 並加入 PATH，然後執行 flutter doctor。' }
# Generate toolchain files in a temporary project so custom sources are never overwritten.
$scaffold = Join-Path ([System.IO.Path]::GetTempPath()) ('music_bridge_' + [guid]::NewGuid().ToString('N'))
& flutter create --platforms=android --android-language=kotlin --project-name music_bridge $scaffold
if ($LASTEXITCODE -ne 0) { throw 'flutter create failed' }
$generatedAndroid = Join-Path $scaffold 'android'
# Copy only missing scaffold files. Keep our manifest, Kotlin, Gradle app config and layouts.
Get-ChildItem -LiteralPath $generatedAndroid -Recurse -File | ForEach-Object {
    $relative = $_.FullName.Substring($generatedAndroid.Length + 1)
    $destination = Join-Path (Join-Path $PSScriptRoot 'android') $relative
    if (-not (Test-Path -LiteralPath $destination)) {
        New-Item -ItemType Directory -Force -Path (Split-Path $destination) | Out-Null
        Copy-Item -LiteralPath $_.FullName -Destination $destination
    }
}
& flutter pub get
if ($LASTEXITCODE -ne 0) { throw 'flutter pub get failed' }
Write-Host "初始化完成。暫存專案保留於 $scaffold"
Write-Host '接著執行 flutter analyze 與 flutter build apk --debug'
