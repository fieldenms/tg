/**
 * List of available converters
 */
const RichTextConverter = {
    'DIV': remainTheSame,
    'H1': convertHeader,
    'H2': convertHeader,
    'H3': convertHeader,
    'P': convertParagraph,
    'EM': remainTheSame,
    'STRONG': remainTheSame,
    'DEL': remainTheSame,
    'SPAN': remainTheSame,
    'A': remainTheSame,
    'UL': convertUnorderedList,
    'OL': convertOrderedList,
    'LI': convertListItem,
    'BR': convertBreak
}

/**********************Converters***************************************************/
function defaultConverter(element) {
    const code = document.createElement('code');
    code.innerText = `[${element.tagName.toLowerCase()}]`;
    return code;
}

function remainTheSame(element) {
    return element;
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
    unorderedSpan.classList.add('list');
    unorderedSpan.classList.add('unordered-list');
    return moveChildren(unorderedList, unorderedSpan);
}

function convertOrderedList(orderedList) {
    const orderedSpan = document.createElement('span');
    orderedSpan.classList.add('list');
    orderedSpan.classList.add('ordered-list');
    return moveChildren(orderedList, orderedSpan);
}

function convertListItem(listItem) {
    const spanItem = document.createElement('span');
    spanItem.classList.add("list-item");
    if (listItem.classList.contains('task-list-item')) {
        [...listItem.classList].forEach(classItem => spanItem.classList.add(classItem));
    } else if (listItem.parentElement.tagName === 'UL') {
        spanItem.classList.add('unordered-list-item');
    } else if (listItem.parentElement.tagName === 'OL') {
        spanItem.classList.add('ordered-list-item');
    }
    return moveChildren(listItem, spanItem);
}

function convertBreak(breakElement) {
    return null;
}
/***********************************************************************************/

function moveChildren(from, to) {
    while(from.hasChildNodes()) {
        to.appendChild(from.firstChild);
    }
    return to;
}

function convertNode(root) {
    //Convert root' children first
    [...root.childNodes].forEach(child => {
        const convertedNode = convertNode(child);
        if (convertedNode) {
            root.replaceChild(convertedNode, child);
        } else {
            root.removeChild(child);
        }
    });
    //And then try to convert root element if it has a tagName (i.e. is HTMLElement)
    if (root.tagName) {
        //If converter is present then convert it.
        if (RichTextConverter[root.tagName]) {
            return RichTextConverter[root.tagName](root);
        }
        //Otherwise use default converter.
        return defaultConverter(root);
    }
    //Otherwise return root element if it is a node (i.e. doesn't have tagName)
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