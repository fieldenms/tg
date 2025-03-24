del "package-lock.json"
npm install --no-bin-links
node remove-symlinks.js node_modules
mkdir node_modules\lib
xcopy lib node_modules\lib /E /I /Q
rollup --config
rm polymer -r -Force
Rename-Item node_modules polymer
Get-ChildItem -Path "build\node_modules" -Directory | ForEach-Object {
    rm -Path "polymer\$($_.Name)" -r -Force
    Move-Item -Path $_.FullName -Destination "polymer\"
}
rm _virtual -r -Force
move build\_virtual .\
Get-ChildItem -Path "_virtual" -Recurse -File | ForEach-Object {
    (Get-Content $_.FullName) -replace 'node_modules', 'polymer' | Set-Content $_.FullName
}
rm build -r -Force
forfiles /p polymer /s /m package.json /c "cmd /c del @path"