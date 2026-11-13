# Stop on npm older than 11 (see 'engines' in package.json).
$npmVersion = npm --version
if ([int]($npmVersion.Split('.')[0]) -lt 11) {
    Write-Host "ERROR: install-polymer requires npm 11 or newer, but npm $npmVersion is installed." -ForegroundColor Red
    Write-Host "npm 10 and older silently ignore the overrides nested under 'wct-browser-legacy' in package.json." -ForegroundColor Red
    Write-Host "Excluded web test dependencies would then return to package-lock.json." -ForegroundColor Red
    Write-Host "Update npm with: npm install -g npm@11" -ForegroundColor Red
    exit 1
}
del "package-lock.json"
npm install --no-bin-links
node remove-symlinks.js node_modules
mkdir node_modules\lib
xcopy lib node_modules\lib /E /I /Q
rollup --config
rm polymer -r -Force
Move-Item build\node_modules polymer
rm _virtual -r -Force
move build\_virtual .\
Get-ChildItem -Path "_virtual" -Recurse -File | ForEach-Object {
    (Get-Content $_.FullName) -replace 'node_modules', 'polymer' | Set-Content $_.FullName
}
rm build -r -Force
rm node_modules -r -Force