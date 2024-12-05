const RichTextConverter = {
    'H1': convertHeader,
    'H2': convertHeader,
    'H3': convertHeader,
    'P': convertParagraph,
    'UL': converUnorderedList,
    'LI': convertListItem
}

function convertHeader(header) {
    const bold = document.createElement('b');
    moveChildren(header, bold).appendChild(document.createTextNode(' '));
    return bold;
}

function convertParagraph(paragraph) {
    const fragment = document.createDocumentFragment();
    moveChildren(paragraph, fragment).appendChild(document.createTextNode(' '));
    return fragment;
}

function converUnorderedList(unorderdList) {
    const unorderedSpan = document.createElement('span');
    unorderedSpan.classList.add('unordered-list');
    return moveChildren(unorderdList, unorderedSpan);
}

function convertListItem(listItem) {
    let spanItem;
    if (listItem.parentElement.tagName === 'UL') {
        spanItem = document.createElement('span');
        if (listItem.classList.contains('task-list-item')) {
            [...listItem.classList].forEach(classItem => spanItem.classList.add(classItem));
        } else {
            spanItem.classList.add('unordered-list-item');
        }
    } else if (listItem.parentElement.tagName === 'OL') {
        //TODO generate list item for ordered list
    }
    return moveChildren(listItem, spanItem);
}

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

export function simplifyRichText(richText) {
    const root = document.createElement('div');
    root.innerHTML = richText;
    return convertNode(root).innerHTML;
}