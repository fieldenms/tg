del "package-lock.json" && ^
npm install --no-bin-links && ^
del node_modules\stacky && ^
xcopy lib node_modules\ /E /I /Q && ^
rollup --config && ^
rmdir /S /Q polymer & ^
rename node_modules polymer && ^
for /d %%D in (build\node_modules\*) do (
    rmdir /S /Q "polymer\%%~nxD"
    move "%%D" polymer\
)
rmdir /S /Q polymer\dompurify & ^
rmdir /S /Q _virtual & ^
move build\_virtual .\ && ^
for /r _virtual %%F in (*) do powershell -Command "(Get-Content %%F) -replace 'node_modules', 'polymer' | Set-Content %%F" && ^
rmdir /S /Q build & ^
forfiles /p polymer /s /m package.json /c "cmd /c del @path" && ^
