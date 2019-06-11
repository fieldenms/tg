npm install && ^
git clone https://github.com/PolymerElements/iron-test-helpers node_modules\@polymer\iron-test-helpers && ^
polymer build && ^
rmdir /S /Q polymer & ^
rename node_modules polymer && ^
rmdir /S /Q polymer\@polymer & ^
rmdir /S /Q polymer\wct-browser-legacy & ^
rmdir /S /Q polymer\@google-web-components & ^
move build\tg-custom-build\node_modules\@polymer polymer\ && ^
move build\tg-custom-build\node_modules\wct-browser-legacy polymer\ && ^
move build\tg-custom-build\node_modules\@google-web-components polymer\ && ^
rmdir /S /Q build & ^
del "package-lock.json"
