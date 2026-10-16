param(
    [string]$SourcePath = (Join-Path $PSScriptRoot '..\assets\icons\app_icon.png')
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

# Android 啟動圖示不是 Flutter 畫面元件；不同螢幕密度需要對應大小的 PNG。
# 保留同一個 ic_launcher 資源名稱，Manifest 的引用不需要跟著更改。
$resourceRoot = [System.IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot '..\android\app\src\main\res'))
$densitySizes = [ordered]@{
    'mdpi' = 48
    'hdpi' = 72
    'xhdpi' = 96
    'xxhdpi' = 144
    'xxxhdpi' = 192
}

$sourceImage = [System.Drawing.Image]::FromFile(
    [System.IO.Path]::GetFullPath($SourcePath))
try {
    foreach ($density in $densitySizes.Keys) {
        $size = $densitySizes[$density]
        $destination = Join-Path $resourceRoot "mipmap-$density"
        [void][System.IO.Directory]::CreateDirectory($destination)
        $bitmap = New-Object System.Drawing.Bitmap(
            $size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
            try {
                # 使用透明畫布與高品質縮放；等比例置中，避免非正方形來源被拉伸。
                $graphics.Clear([System.Drawing.Color]::Transparent)
                $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
                $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
                $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
                $ratio = [Math]::Min($size / $sourceImage.Width, $size / $sourceImage.Height)
                $width = [int][Math]::Round($sourceImage.Width * $ratio)
                $height = [int][Math]::Round($sourceImage.Height * $ratio)
                $bounds = New-Object System.Drawing.Rectangle(
                    [int](($size - $width) / 2), [int](($size - $height) / 2), $width, $height)
                $graphics.DrawImage($sourceImage, $bounds)
            } finally { $graphics.Dispose() }
            $bitmap.Save((Join-Path $destination 'ic_launcher.png'),
                [System.Drawing.Imaging.ImageFormat]::Png)
            Write-Output "Generated mipmap-$density/ic_launcher.png (${size}x${size})"
        } finally { $bitmap.Dispose() }
    }
} finally { $sourceImage.Dispose() }
