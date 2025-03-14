rm -f package-lock.json
npm install --no-bin-links
rm -f node_modules/stacky
cp -r lib node_modules/
rollup --config
rm -r -f polymer
mv node_modules polymer
for dir in build/node_modules/*; do
    name=$(basename "$dir")
    rm -rf "polymer/$name"
    mv "$dir" polymer/
done
rm -r -f _virtual
mv build/_virtual ./
find _virtual -type f -exec sed -i 's|node_modules|polymer|g' {} \;
rm -r -f build
find polymer -type f -name "package.json" -delete
