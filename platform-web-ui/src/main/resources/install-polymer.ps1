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