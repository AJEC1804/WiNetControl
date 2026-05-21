Add-Type -AssemblyName System.IO.Compression.FileSystem
$docPath = "C:\Juanes\Guia Proyecto Aulas Semestre 3  (1).docx"
$zip = [System.IO.Compression.ZipFile]::OpenRead($docPath)
$entry = $zip.GetEntry("word/document.xml")
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$xml = $reader.ReadToEnd()
$reader.Close()
$stream.Close()
$zip.Dispose()

$xml = $xml -replace '<w:p[ >]', "`n<w:p>"
$matches = [regex]::Matches($xml, '<w:t.*?>([^<]*)</w:t>')
$text = $matches | ForEach-Object { $_.Groups[1].Value }
$text -join ""
