npm install
git clone https://github.com/PolymerElements/iron-test-helpers node_modules/@polymer/iron-test-helpers
polymer build
rm -r -f polymer
mv node_modules polymer
rm -r -f polymer/@polymer
rm -r -f polymer/wct-browser-legacy
rm -r -f polymer/@google-web-components
mv build/tg-custom-build/node_modules/@polymer polymer/
mv build/tg-custom-build/node_modules/wct-browser-legacy polymer/
mv build/tg-custom-build/node_modules/@google-web-components polymer/
rm -r -f build
rm -f package-lock.json
