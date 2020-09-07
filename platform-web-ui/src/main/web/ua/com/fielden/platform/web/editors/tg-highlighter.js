/**
 * Escapes the special RegExp characters to convert the specified string 
 * to a form, suitable for matching 'as is'.
 *
 * This enables usage of characters like +([. to be used for exact matching.
 * See more: http://stackoverflow.com/questions/3115150/how-to-escape-regular-expression-special-characters-using-javascript
 *           https://github.com/benjamingr/RegExp.escape
 */
RegExp.escape = function (text) {
    return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
};

function _searchRegExp (str) {
    const pattern = RegExp.escape(str).replace(/%/gi, ".*");
    return new RegExp(pattern, 'gi');
};

/* 
    * Method that identifies matching parts in the str for the searchQuery.
    * Returns an array of objects representing matched and non-matched parts of the str in sequential order.
    * It is used to identify those parts that needs to be highlighted during rendering.
    */
export function matchedParts (str, searchQuery) {
    const safeStr = str ? str : '';
    const parts = [];
    const text = safeStr.toUpperCase();
    // if all is matched then return a single value
    if ("%" == searchQuery) {
        parts.push({
            part: safeStr,
            matched: false
        });
        return parts;
    } else if ('' == searchQuery) {
        return parts;
    }

    // otherwise split 
    const searchExp = _searchRegExp(searchQuery);
    let match;
    let startIndex = 0;
    while ((match = searchExp.exec(text)) !== null) {
        if (match.index > startIndex) { // match is not from the start, so need to record thing before as not matched
            const part = {
                part: safeStr.substring(startIndex, match.index),
                matched: false
            };
            parts.push(part);
        }

        // record the matched part
        const part = {
            part: safeStr.substring(match.index, searchExp.lastIndex),
            matched: true
        };
        parts.push(part);

        startIndex = searchExp.lastIndex;

    }
    // check if there is an unmatched part at the end
    if (startIndex < safeStr.length) {
        const part = {
            part: safeStr.substring(startIndex, text.length),
            matched: false
        };
        parts.push(part);
    }
    // return the result
    return parts;
}