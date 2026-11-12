#!/usr/bin/env bash
set -e
rm -f package-lock.json
npm install --no-bin-links
node remove-symlinks.js node_modules
cp -R lib node_modules/
rollup --config
rm -r -f polymer
mv build/node_modules polymer
rm -r -f _virtual
mv build/_virtual ./
find _virtual -type f -exec perl -i -pe 's|node_modules|polymer|g' {} +
rm -r -f build
rm -r -f node_modules
