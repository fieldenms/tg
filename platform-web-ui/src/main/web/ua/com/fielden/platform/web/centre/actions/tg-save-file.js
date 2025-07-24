import { saveAs } from '/resources/polymer/lib/file-saver-lib.js';

/**
 * Saves data from `functionalEntity` to a local file.
 * Its implementation depends on the contract that the underlying functional entity has properties:
 *   - mime -- a MIME type for the data being exported
 *   - fileName -- a file name including file extension where the data should be saved
 *   - data -- base64 string representing a binary array
 *
 * Clears EGI selection by default (if performed in Entity Centre context).
 */
export function saveFile(functionalEntity, self) {
    const byteCharacters = atob(functionalEntity.data);
    const byteNumbers = new Uint8Array(byteCharacters.length);
    for (let index = 0; index < byteCharacters.length; index++) {
         byteNumbers[index] = byteCharacters.charCodeAt(index);
    }
    const data = new Blob([byteNumbers], {type: functionalEntity.mime});
    saveAs(data, functionalEntity.fileName);
    if (self.$.egi && self.$.egi.clearPageSelection) {
        self.$.egi.clearPageSelection();
    }
}