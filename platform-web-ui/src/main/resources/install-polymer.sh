rm -f package-lock.json
npm install
rm -f node_modules/stacky
cp -r lib node_modules/
rollup --config
rm -r -f polymer
mv node_modules polymer
rm -r -f polymer/@google-web-components
rm -r -f polymer/@polymer
rm -r -f polymer/@webcomponents
rm -r -f polymer/web-animations-js
rm -r -f polymer/lib
rm -r -f polymer/antlr4
rm -r -f polymer/moment
rm -r -f polymer/moment-timezone
rm -r -f _virtual
mv build/node_modules/@google-web-components polymer/
mv build/node_modules/@polymer polymer/
mv build/node_modules/@webcomponents polymer/
mv build/node_modules/web-animations-js polymer/
mv build/node_modules/lib polymer/
mv build/node_modules/antlr4 polymer/
mv build/node_modules/moment polymer/
mv build/node_modules/moment-timezone polymer/
mv build/_virtual ./
find _virtual -type f -exec sed -i 's|node_modules|polymer|g' {} \;
rm -r -f build
find polymer -type f -name "package.json" -delete
