del "package-lock.json" && ^
npm install && ^
del node_modules\stacky && ^
xcopy lib node_modules\ /E /I /Q && ^
rollup --config && ^
rmdir /S /Q polymer & ^
rename node_modules polymer && ^
rmdir /S /Q polymer\@google-web-components & ^
rmdir /S /Q polymer\@polymer & ^
rmdir /S /Q polymer\@webcomponents & ^
rmdir /S /Q polymer\web-animations-js & ^
rmdir /S /Q polymer\lib & ^
rmdir /S /Q polymer\antlr4 & ^
rmdir /S /Q polymer\moment & ^
rmdir /S /Q polymer\moment-timezone & ^
rmdir /S /Q _virtual & ^
move build\node_modules\@google-web-components polymer\ && ^
move build\node_modules\@polymer polymer\ && ^
move build\node_modules\@webcomponents polymer\ && ^
move build\node_modules\web-animations-js polymer\ && ^
move build\node_modules\lib polymer\ && ^
move build\node_modules\antlr4 polymer\ && ^
move build\node_modules\moment polymer\ && ^
move build\node_modules\moment-timezone polymer\ && ^
move build\_virtual .\ && ^
for /r _virtual %%F in (*) do powershell -Command "(Get-Content %%F) -replace 'node_modules', 'polymer' | Set-Content %%F" && ^
rmdir /S /Q build & ^
forfiles /p polymer /s /m package.json /c "cmd /c del @path" && ^
