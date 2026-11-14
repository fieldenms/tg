#!/usr/bin/env bash
set -e
# Stop on npm older than 11 (see 'engines' in package.json).
npm_version=$(npm --version)
if [ "${npm_version%%.*}" -lt 11 ]; then
    echo "ERROR: install-polymer requires npm 11 or newer, but npm $npm_version is installed." >&2
    echo "npm 10 and older silently ignore the overrides nested under 'wct-browser-legacy' in package.json." >&2
    echo "Excluded web test dependencies would then return to package-lock.json." >&2
    echo "Update npm with: npm install -g npm@11" >&2
    exit 1
fi
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
