rm -f package-lock.json
npm install --no-bin-links
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
rm -r -f polymer/@toast-ui
rm -r -f polymer/dompurify
rm -r -f polymer/prosemirror-commands
rm -r -f polymer/prosemirror-history
rm -r -f polymer/prosemirror-inputrules
rm -r -f polymer/prosemirror-keymap
rm -r -f polymer/prosemirror-model
rm -r -f polymer/prosemirror-state
rm -r -f polymer/prosemirror-view
rm -r -f polymer/orderedmap
rm -r -f polymer/prosemirror-transform
rm -r -f polymer/rope-sequence
rm -r -f polymer/w3c-keyname
rm -r -f _virtual
mv build/node_modules/@google-web-components polymer/
mv build/node_modules/@polymer polymer/
mv build/node_modules/@webcomponents polymer/
mv build/node_modules/web-animations-js polymer/
mv build/node_modules/lib polymer/
mv build/node_modules/antlr4 polymer/
mv build/node_modules/moment polymer/
mv build/node_modules/moment-timezone polymer/
mv build/node_modules/@toast-ui polymer/
mv build/node_modules/dompurify polymer/
mv build/node_modules/prosemirror-commands polymer/
mv build/node_modules/prosemirror-history polymer/
mv build/node_modules/prosemirror-inputrules polymer/
mv build/node_modules/prosemirror-keymap polymer/
mv build/node_modules/prosemirror-model polymer/
mv build/node_modules/prosemirror-state polymer/
mv build/node_modules/prosemirror-view polymer/
mv build/node_modules/orderedmap polymer/
mv build/node_modules/prosemirror-transform polymer/
mv build/node_modules/rope-sequence polymer/
mv build/node_modules/w3c-keyname polymer/
mv build/_virtual ./
find _virtual -type f -exec sed -i 's|node_modules|polymer|g' {} \;
rm -r -f build
find polymer -type f -name "package.json" -delete
