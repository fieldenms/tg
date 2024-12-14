del "package-lock.json" && ^
npm install && ^
del node_modules\stacky && ^
polymer build && ^
rmdir /S /Q polymer & ^
rename node_modules polymer && ^
rmdir /S /Q polymer\@google-web-components & ^
rmdir /S /Q polymer\@polymer & ^
rmdir /S /Q polymer\@webcomponents & ^
rmdir /S /Q polymer\web-animations-js & ^
move build\tg-custom-build\node_modules\@google-web-components polymer\ && ^
move build\tg-custom-build\node_modules\@polymer polymer\ && ^
move build\tg-custom-build\node_modules\@webcomponents polymer\ && ^
move build\tg-custom-build\node_modules\web-animations-js polymer\ && ^
rmdir /S /Q build & ^
forfiles /p polymer /s /m package.json /c "cmd /c del @path" && ^
