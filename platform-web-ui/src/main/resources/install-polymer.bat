del "package-lock.json" && ^
npm install && ^
polymer build && ^
rmdir /S /Q polymer & ^
rename node_modules polymer && ^
rmdir /S /Q polymer\@polymer & ^
rmdir /S /Q polymer\@google-web-components & ^
move build\tg-custom-build\node_modules\@polymer polymer\ && ^
move build\tg-custom-build\node_modules\@google-web-components polymer\ && ^
rmdir /S /Q build & ^
