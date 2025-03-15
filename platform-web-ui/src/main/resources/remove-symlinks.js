const fs = require('fs');
const path = require('path');

function removeSymlinks(dir) {
    fs.readdirSync(dir, { withFileTypes: true }).forEach(entry => {
        const fullPath = path.join(dir, entry.name);
        if (entry.isSymbolicLink()) {
            console.log(`\tRemoving symlink [${fullPath}].`);
            fs.unlinkSync(fullPath);
        }
        else if (entry.isDirectory()) {
            removeSymlinks(fullPath);
        }
    });
}

console.log('Removing symlinks...');
removeSymlinks(process.argv[2]);
console.log('Removing symlinks...done');