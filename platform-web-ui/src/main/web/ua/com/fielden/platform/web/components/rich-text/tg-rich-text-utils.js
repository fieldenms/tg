/**
 * List of available converters
 */
const RichTextConverter = {
    'BR': convertBreak,
    'H1': convertHeader,
    'H2': convertHeader,
    'H3': convertHeader,
    'P': convertParagraph,
    'UL': convertUnorderedList,
    'OL': convertOrderedList,
    'LI': convertListItem
}

/**********************Converters***************************************************/
function convertBreak(breakElement) {
    return null;
}

function convertHeader(header) {
    const bold = document.createElement('b');
    moveChildren(header, bold).appendChild(document.createTextNode(' '));
    return bold;
}

function convertParagraph(paragraph) {
    const fragment = document.createElement('span');
    moveChildren(paragraph, fragment).appendChild(document.createTextNode(' '));
    return fragment;
}

function convertUnorderedList(unorderedList) {
    const unorderedSpan = document.createElement('span');
    unorderedSpan.classList.add('unordered-list');
    return moveChildren(unorderedList, unorderedSpan);
}

function convertOrderedList(orderedList) {
    const orderedSpan = document.createElement('span');
    orderedSpan.classList.add('ordered-list');
    return moveChildren(orderedList, orderedSpan);
}

function convertListItem(listItem) {
    const spanItem = document.createElement('span');
    if (listItem.parentElement.tagName === 'UL') {
        if (listItem.classList.contains('task-list-item')) {
            [...listItem.classList].forEach(classItem => spanItem.classList.add(classItem));
        } else {
            spanItem.classList.add('unordered-list-item');
        }
    } else if (listItem.parentElement.tagName === 'OL') {
        spanItem.classList.add('ordered-list-item');
    }
    return moveChildren(listItem, spanItem);
}
/***********************************************************************************/

function moveChildren(from, to) {
    while(from.hasChildNodes()) {
        to.appendChild(from.firstChild);
    }
    return to;
}

function convertNode(root) {
    [...root.childNodes].forEach(child => {
        const convertedNode = convertNode(child);
        if (convertedNode) {
            root.replaceChild(convertedNode, child);
        } else {
            root.removeChild(child);
        }
    });
    if (root.tagName && RichTextConverter[root.tagName]) {
        return RichTextConverter[root.tagName](root);
    }
    return root;
}

/**
 * Simplification should convert HTML rich text into single line HTML text including lists, paragraphs, breaks etc.
 * 
 * @param {String} richText - HTML text to simplify.
 * @returns 
 */
export function simplifyRichText(richText) {
    const root = document.createElement('div');
    root.innerHTML = richText;
    return convertNode(root).innerHTML;
}