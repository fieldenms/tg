rm -f package-lock.json
npm install
rm -f node_modules/stacky
polymer build
rm -r -f polymer
mv node_modules polymer
rm -r -f polymer/@polymer
rm -r -f polymer/@google-web-components
mv build/tg-custom-build/node_modules/@polymer polymer/
mv build/tg-custom-build/node_modules/@google-web-components polymer/
rm -r -f build
find polymer -type f -name "package.json" -delete
