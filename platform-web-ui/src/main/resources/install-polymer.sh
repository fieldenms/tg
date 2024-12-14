rm -f package-lock.json
npm install
rm -f node_modules/stacky
polymer build
rm -r -f polymer
mv node_modules polymer
rm -r -f polymer/@google-web-components
rm -r -f polymer/@polymer
rm -r -f polymer/@webcomponents
rm -r -f polymer/web-animations-js
mv build/tg-custom-build/node_modules/@google-web-components polymer/
mv build/tg-custom-build/node_modules/@polymer polymer/
mv build/tg-custom-build/node_modules/@webcomponents polymer/
mv build/tg-custom-build/node_modules/web-animations-js polymer/
rm -r -f build
find polymer -type f -name "package.json" -delete
