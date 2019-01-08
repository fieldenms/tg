npm install
cd node_modules/@polymer
git clone https://github.com/PolymerElements/iron-test-helpers
cd ../..
polymer build
rm -r -f polymer
mv node_modules polymer
cd polymer
rm -r -f @polymer
cd ..
mv build/tg-custom-build/node_modules/@polymer polymer/@polymer
rm -r -f build