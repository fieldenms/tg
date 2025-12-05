
import { html, PolymerElement } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { mixinBehaviors } from '/resources/polymer/@polymer/polymer/lib/legacy/class.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronOverlayManager } from '/resources/polymer/@polymer/iron-overlay-behavior/iron-overlay-manager.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { tearDownEvent, isMobileApp} from '/resources/reflection/tg-polymer-utils.js';
import { checkLinkAndOpen } from '/resources/components/tg-link-opener.js';

import Editor from '/resources/polymer/lib/toastui-editor-lib.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js';

import '/resources/polymer/@polymer/paper-styles/paper-styles.js';

import { tgRichTextStyles } from '/resources/components/rich-text/tg-rich-text-styles.js';
import '/resources/components/tg-link-dialog.js';
import '/resources/components/tg-color-picker-dialog.js';
import '/resources/images/editor-icons.js';
import '/resources/egi/tg-responsive-toolbar.js';

/**
 * Defines plugin for fake selection.
 * Fake selection allows to keep text look like it is selected, even if the editor loses focus.
 * Such fake selection helps to highlight the text that is about to change its state (e.g. colour) or become a hyperlink.
 * 
 * @param {Object} context 
 * @param {Object} options 
 * @returns Object
 */
function fakeSelection(context, options) {

    return {
        wysiwygCommands: {
            fakeSelect: ({from, to}, { tr, schema }, dispatch) => {
                const mark = schema.marks.mark.create();
                tr.addMark(from, to, mark).setMeta("addToHistory", false);
                dispatch(tr);
                return true;
            },
            fakeUnselect: ({from, to}, state, dispatch) => {
                const markType = state.schema.marks.mark;
                if (dispatch) {
                    let tr = state.tr;
                    const has = state.doc.rangeHasMark(from, to, markType);
                    if (has) {
                        tr.removeMark(from, to, markType).setMeta("addToHistory", false);
                    }
                    dispatch(tr);
                }
                return true;
            },
        },
        toHTMLRenderers: {
            htmlInline: {
                mark(node, { entering }) {
                    return entering
                        ? { type: 'openTag', tagName: 'mark', attributes: node.attrs }
                        : { type: 'closeTag', tagName: 'mark' };
                },
            },
        },
    }
}

/**
 * Plugin that allows user to change the text colour or reset it.
 * 
 * @param {Object} context 
 * @param {Object} options 
 * @returns Object
 */
function colorTextPlugin(context, options) {
    //The following method was copied from prosemirror-commands module
    function markApplies(doc, ranges, type) {
        for (let i = 0; i < ranges.length; i++) {
            let { $from, $to } = ranges[i];
            let can = $from.depth == 0 ? doc.type.allowsMarkType(type) : false;
            doc.nodesBetween($from.pos, $to.pos, node => {
                if (can) {
                    return false;
                }
                can = node.inlineContent && node.type.allowsMarkType(type);
            });
            if (can) {
                return true;
            }
        }
        return false
    }

    return {
        wysiwygCommands: {
            color: ({ selectedColor }, { tr, selection, schema }, dispatch) => {
                if (selectedColor) {
                    const { from, to } = selection;
                    const attrs = { htmlAttrs: { style: `color: ${selectedColor} !important` } };
                    const mark = schema.marks.span.create(attrs);

                    tr.addMark(from, to, mark);
                    dispatch(tr.scrollIntoView());

                    return true;
                }
                return false;
            },
            clearColor: (payload, state, dispatch) => {
                //The following logic was copied from prosemirror-commands module and tuned to requirements of this method
                const markType = state.schema.marks.span;
                let { empty, $cursor, ranges } = state.selection;
                if ((empty && !$cursor) || !markApplies(state.doc, ranges, markType)) {
                    return false;
                } 
                if (dispatch) {
                    if ($cursor) {
                        if (markType.isInSet(state.storedMarks || $cursor.marks())) {
                            dispatch(state.tr.removeStoredMark(markType));
                        } 
                    } else {
                        let has = false, tr = state.tr;
                        for (let i = 0; !has && i < ranges.length; i++) {
                            let { $from, $to } = ranges[i];
                            has = state.doc.rangeHasMark($from.pos, $to.pos, markType);
                        }
                        for (let i = 0; i < ranges.length; i++) {
                            let { $from, $to } = ranges[i]
                            if (has) {
                                tr.removeMark($from.pos, $to.pos, markType);
                            }
                        }
                        dispatch(tr);
                    }
                }
                return true;
            },
        },
        toHTMLRenderers: {
            htmlInline: {
                span(node, context) {
                    const colorStyles = node.attrs.style &&node.attrs.style.split(";")
                        .map(styleParam => styleParam.split(":"))
                        .filter(stylePair => stylePair[0].trim() === 'color')
                        .map(stylePair => `${stylePair[0]}:${stylePair[1]}`)
                        .join(";");
                    return context.entering
                        ? { type: 'openTag', tagName: 'span', attributes: {style: colorStyles} }
                        : { type: 'closeTag', tagName: 'span' };
                },
            },
        },
    };
}

/****************************Tooltip trigger related code*********************************/
let currentTooltipElement = null;
function mouseOverHandler(e) {
    const a = findParentBy.bind(this)(e.target, isLink);
    if (a) {
        if (currentTooltipElement !== a) {
            this._hideTooltip();
        }
        this.showTooltip(`${a.getAttribute('href')}<br>Ctrl+Click, &#x2318;+Click or long touch to open link`);
    } else {
        this._hideTooltip();
    }
    currentTooltipElement = a;
};
/****************************************************************************************/

/**
 * Predicate that determines whether specified element is link or not.
 * 
 * @param {Object} element element to test
 * @returns 
 */
function isLink(element) {
    return element.hasAttribute && element.hasAttribute('href')
}

/**
 * Predicate that determines whether specified element is span with specified color or not.
 * 
 * @param {Object} element element to test
 * @returns 
 */
function isColoredSpan(element) {
    return element.tagName && element.tagName === 'SPAN' && element.style.color
}

/**
 * Finds element that is equal to specified one or is parent to specified one and satisfies the specified predicate.
 * 
 * @param {Object} element - an element for which parent element should be found
 * @param {Function} predicate - determines whether selected parent is sufficient or not.
 * @returns 
 */
function findParentBy(element, predicate) {
    let parent = element;
    while (parent && !predicate(parent) && parent !== this._getEditableContent() ) {
        parent = parent.parentElement;
    }
    return parent && predicate(parent) ? parent : null;
}

/*******************************link click action related logic************************/
let mouseTimer = null;
let longPress = false;
let shortPress = false;

function runLinkIfPossible(el) {
    const a = findParentBy.bind(this)(el, isLink);
    if (a) {
        checkLinkAndOpen(a.getAttribute('href'));
    }
}

function mouseDownHandler(e) {
    if (e.button == 0 || e.type.startsWith("touch")) {
        longPress = false;
        shortPress = true;
        const el = e.target;
        mouseTimer = setTimeout(() => {
            longPress = true;
            shortPress = false;
            setTimeout( () => {runLinkIfPossible.bind(this)(el)}, 1);
        }, 1000);
        //This prevents selection of node and making it draggable
        if (e.button === 0 && (e.ctrlKey ||  e.metaKey)) {
            tearDownEvent(e);
        }
    }
}

function mouseUpHandler(e) {
    if (e.button == 0 || e.type.startsWith("touch")) {
        if (mouseTimer) {
            clearTimeout(mouseTimer);
        }
        if (shortPress && !longPress && (e.ctrlKey || e.metaKey)) {
            const el = e.target;
            setTimeout(() => {runLinkIfPossible.bind(this)(el)}, 150);
        }
        longPress = false;
        shortPress = false;
        mouseTimer = null;
    }
}
/****************************************************************************************/

/**
 * Finds the element to edit.
 * This element can be determined by caret position or by selection.
 * 
 * @param {Function} predicate 
 * @param {Function} extractor 
 * @returns 
 */
function getElementToEdit(predicate, extractor) {
    const selection = this._getSelection();
    if (selection) {
        if (selection[0] === selection[1]) {
            //It means that only caret postion was set (no selection). Then take text and url from dom at caret position if it exists
            const node = this._editor.wwEditor.view.domAtPos(selection[0], 1).node;
            const element = findParentBy.bind(this)(node, predicate);
            if (element && element.pmViewDesc) {
                const text = this._editor.getSelectedText(element.pmViewDesc.posAtStart, element.pmViewDesc.posAtEnd);
                return {pos: [element.pmViewDesc.posAtStart, element.pmViewDesc.posAtEnd], text: text, detail: extractor(element)};
            }
        } else {
            //This branch indicates that user has selected some text or even nodes, therefore the text should be taken from selection
            // and url from the first <a> tag in selection
            const text = this._editor.getSelectedText(selection[0], selection[1]);
            const nodes = [];
            for (let i = selection[0]; i <= selection[1]; i++) {
                const node = this._editor.wwEditor.view.domAtPos(i, selection[1] - i).node;
                if (node) {
                    nodes.push(node);
                }
            }
            const element = nodes.map(node => findParentBy.bind(this)(node, predicate)).find(a => a);
            return (element && {text: text, detail: extractor(element)}) || {text: text, detail: ''};
        }
    }
}

/**
 * Determines whether all elements selected by the user satisfy the given predicate. 
 * 
 * @param {Function} predicate - function that determines whether the next element being considered meets defined requirements
 * @returns 
 */
function allElementsSatisfies(predicate) {
    const selection = this._getSelection();
    if (selection) {
        if (selection[0] === selection[1]) {
            //It means that only caret postion was set (no selection). 
            //Then return true if the caret is positioned within an element that satisfies the predicate. Otherwise it returns false.
            const node = this._editor.wwEditor.view.domAtPos(selection[0], 1).node;
            return !!findParentBy.bind(this)(node, predicate);
        } else {
            //This branch indicates that user has selected some text. 
            //Then return true if all elements satisfy the predicate or false if they don't.
            const nodes = [];
            for (let i = selection[0]; i <= selection[1]; i++) {
                const node = this._editor.wwEditor.view.domAtPos(i, selection[1] - i).node;
                if (node && node !== this._getEditableContent()) {
                    nodes.push(node);
                }
            }
            return !nodes.some(node => !findParentBy.bind(this)(node, predicate));
        }
    }
}

/**
 * Determines whether the specified mouse click event happened on a task list checkbox.
 * 
 * @param {Object} e - - an event object generated by mouse click.
 * @returns 
 */
function isOnTaskListItem(e) {
    const pos = this._editor.wwEditor.view.posAtCoords({left:e.clientX, top:e.clientY});
    const node = pos && this._editor.wwEditor.view.domAtPos(pos.pos, pos.inside);
    if (node && node.node.hasAttribute && node.node.hasAttribute('data-task')) {
        const style = getComputedStyle(node.node, ':before');
        if (isPositionInBox(style, e.offsetX, e.offsetY)) {
            return true;
        }
    }
    return false;
}

/**
 * Tear downs the mouse event if it happened on a task list checkbox, while the editor was disabled.
 * 
 * @param {Object} e - an event object generated on mouse click.
 */
function handleTaskListItemClickWhenDisabled (e) {
    if (isOnTaskListItem.bind(this)(e) && this.disabled) {
        tearDownEvent(e);
    }
}

/**
 * Handles click on task list item so that it triggers revalidation.
 * 
 * @param {Object} e - an event object generated on mouse click.
 */
function handleTaskListItemStatusChange(e) {
    if (!this.disabled && isOnTaskListItem.bind(this)(e)) {
        this.changeEventHandler();
        if (this.shadowRoot.activeElement && isMobileApp()) {
            const pos = this._editor.wwEditor.view.posAtCoords({left:e.clientX, top:e.clientY});
            const node = pos && this._editor.wwEditor.view.domAtPos(pos.pos, pos.inside);
            this._applySelection(node.node.pmViewDesc.posAtStart, node.node.pmViewDesc.posAtStart);
        }
    }
}

function isPositionInBox(style, offsetX, offsetY) {
    const left = parseInt(style.left, 10);
    const top = parseInt(style.top, 10);
    const width = parseInt(style.width, 10) + parseInt(style.paddingLeft, 10) + parseInt(style.paddingRight, 10);
    const height = parseInt(style.height, 10) + parseInt(style.paddingTop, 10) + parseInt(style.paddingBottom, 10);
  
    return offsetX >= left && offsetX <= left + width && offsetY >= top && offsetY <= top + height;
}

/**
 * Focuses rich text editor on keyboard event that doesn't includes any functional key except the SHIFT key.
 * 
 * @param {Object} event keyboard event
 */
function focusOnKeyDown(event) {
    if (!this.disabled
        && (event.keyCode === 13 || (event.key.length === 1 && event.keyCode !== 32 /*space*/&& !event.ctrlKey && !event.altKey && !event.metaKey))
        && !this.shadowRoot.activeElement) {
        this._editor.moveCursorToStart(true);
        if (event.key.length === 1) {
            setTimeout(() => {this._editor.insertText(event.key)}, 1);
        }
    }
}

/**
 * Prevents the propagation of click events when triggered on link elements.
 * 
 * @param {Event} event - click event object 
 */
function preventLinkClick(event) {
    const a = findParentBy.bind(this)(event.composedPath()[0], isLink);
    if (a) {
        tearDownEvent(event);
    }
}

/**
 * Selects element that is about to change it's state and returns additional information needed for editing it in external  dialog.
 * 
 * @param {Function} predicate - determines which element to edit
 * @param {Function} extractor - extracts the information from element that can be edited
 * @returns element to edit
 */
function editElement(predicate, extractor) {
    const element = getElementToEdit.bind(this)(predicate, extractor);
    if (element) {
        if (element.pos) {
            this._applySelection(element.pos[0], element.pos[1]);
        }
        return element;
    }
}

/**
 * Converts rgb/rgba notation into color with hash notation. 
 * 
 * @param {String} rgbString RGB string with "rgb[a](..,..,..)"
 * @returns HTML color notation
 */
function rgbToHex(rgbString) {
    return "#" + rgbString
        .split("(")[1]
        .split(")")[0]
        .split(",")
        .map(colorComponent => {            
            const parsedColor = parseInt(colorComponent).toString(16); //Convert to a base16 string
            return (parsedColor.length === 1) ? "0" + parsedColor : parsedColor;
        })
        .join("");
}

/**
 * Handles some key events before they get handled by Toast UI editor.
 * 
 * @param {Object} event keyboard event
 */
function handleKeyEventsBeforeEditor(event) {
    const selection = this._getSelection();
    if (event.keyCode === 13 /*Enter*/ && selection && selection[0] === 0 /*Text is selected from beginning*/) {
        this._editor.insertText("\n");
        tearDownEvent(event);
    } else if ((event.ctrlKey || event.metaKey) &&  event.keyCode === 65/*a*/) {
        const from = 1, to = this._editor.wwEditor.view.state.tr.doc.content.size - 1;
        this._editor.setSelection(from, to);
        tearDownEvent(event);
    } else if (event.keyCode === 32 /*space*/) {
        this._editor.wwEditor.view.state.schema.marks.link.spec.inclusive = false;
    }
}

/**
 * Handles some key events after they were handled by Toast UI editor.
 * 
 * @param {Event} event keyboard event
 */
function handleEditorKeyEvents(event) {
    if (event.keyCode === 13 && getElementToEdit.bind(this)(el => el.tagName && el.tagName === 'LI', el => el)) {
        setTimeout(() => {scrollIntoView.bind(this)()}, 0);
    }
}

function handleKeyup(e) {
    if (e.keyCode === 32 /*space*/) {
        this._editor.wwEditor.view.state.schema.marks.link.spec.inclusive = true;
    }
}

function handleBeforeinput(e) {
    if (e.data === ' ') {
        this._editor.wwEditor.view.state.schema.marks.link.spec.inclusive = false;
    }
}

function handleInput(e) {
    if (e.data === ' ') {
        setTimeout(() => this._editor.wwEditor.view.state.schema.marks.link.spec.inclusive = true, 0);
    }
}

/**
 * Returns a html text from the editor.
 * Removes fake selection if present.
 * 
 * @returns converted text
 */
function getEditorHTMLText() {
    // Check if the editor's value is empty using `getMarkdown`, 
    // since `getHtml` returns '<p><br></p>' and `getText` returns '\n' for an empty value. 
    // If not empty, remove the fake selection element from the HTML before returning.
    if (this._editor.getMarkdown().length > 0) {
        const html = this._editor.getHTML();
        return this._fakeSelection ? html.replace(/<mark>(.*?)<\/mark>/g, '$1') : html;
    }
    // Otherwise, return "".
    // This is necessary because an empty value for the editing field should remain "" to ensure the refresh cycle works correctly.
    return "";
}

/**
 * Applies fake selection, specified by array `selection`, to the text.
 * 
 * @param {Array} selection - array of indices for which fake selection should be applied.
 */
function applyFakeSelection(selection) {
    if (selection && selection.length == 2) {
        this._editor.exec('fakeSelect', {from: selection[0], to: selection[1]});
    }
}

/**
 * Removes the fake selection, specified by array `selection`, from the text.
 * 
 * @param {Array} selection - array of indices of the fake selection start and finish.
 */
function applyFakeUnselection(selection) {
    if (selection && selection.length == 2) {
        this._editor.exec('fakeUnselect', {from: selection[0], to: selection[1]});
    }
}

function initLinkEditing() {
    return editElement.bind(this)(isLink, el => el.getAttribute('href'));
}

function initColorEditing() {
    return editElement.bind(this)(isColoredSpan, el => rgbToHex(el.style.color));
}

/**
 * Creates a hyperlink for the specified `text` or removes it, if `url` is empty.
 * 
 * @param {String} url - hyperlink text
 * @param {String} text - URL description
 */
function toggleLink(url, text) {
    const selection = this._getSelection();
    if (selection && selection[0] !== selection[1] && !url) {
        this._editor.exec('toggleLink');
    } else {
        this._editor.exec('addLink', { linkUrl: url, linkText: text });
    }
    this.changeEventHandler();
}

/**
 * Applies colour to selected text if `selectedColor` is specified or removes color from selection if `selectedColor` is empty.
 * 
 * @param {String} selectedColor - color with hash to apply.
 */
function applyColor(selectedColor) {
    this.focusInput();
    if (selectedColor) {
        this._editor.exec("color", {selectedColor: selectedColor});
    } else {
        this._editor.exec('clearColor');
    }
    this.changeEventHandler();
}

/**
 * Set the position of the dialog at the middle-bottom side of the selected text, specified with the `pos` array.
 * The position might be adjusted if dialog doesn't fit into the browser window.
 * 
 * @param {HTMLElement} dialog 
 * @param {Array} pos 
 */
function setDialogPosition(dialog, pos) {
    const dialogWidth = parseInt(dialog.style.width);
    const dialogHeight = parseInt(dialog.style.height);

    const wWidth = getWindowWidth();
    const wHeight = getWindowHeight();

    let x = (pos[0].left + pos[1].left) / 2 - dialogWidth / 2;
    let y = Math.max(pos[0].bottom, pos[1].bottom);

    if (x < 0) {
        x = 0; 
    } else if (x + dialogWidth > wWidth) {
        x = wWidth - dialogWidth;
    }

    if (y < 0) {
        y = 0;
    } else if (y + dialogHeight > wHeight) {
        const yAboveTheText = Math.min(pos[0].top, pos[1].top) - dialogHeight;
        y = Math.min(wHeight - dialogHeight, yAboveTheText);
    }

    dialog.horizontalOffset =  x;
    dialog.verticalOffset = y;
}

function repositionOpenedDialog () {
    let openedDialog = null;
    if (this.$.linkDropdown.opened) {
        openedDialog = this.$.linkDropdown;
    } else if (this.$.colorDropdown.opened) {
        openedDialog = this.$.colorDropdown;
    }
    if (openedDialog) {
        let position = getSelectionCoordinates.bind(this)();
        if (position) {
            setDialogPosition(openedDialog, position);
        }
    }
}

function getWindowWidth () {
    return window.visualViewport.width;
}

function getWindowHeight () {
    return window.visualViewport.height;
}

/**
 * Calculates the position of the selected text.
 * The returned value is an array containing the positions of the first and last characters of the selected text.
 * 
 * @returns Array of the selection position
 */
function getSelectionCoordinates() {
    if (this._editor && this._getSelection()) {
        const view = this._editor.wwEditor.view;
        const selection = this._getSelection();
        return [view.coordsAtPos(selection[0]), view.coordsAtPos(selection[1])];
    }
}

/**
 * Scrolls caret position into view.
 */
function scrollIntoView() {
    this._editor.wwEditor.view.dispatch(this._editor.wwEditor.view.state.tr.scrollIntoView());
}

/*********************** Dialog related event handlers *********************/
function handleCancelEvent(e) {
    if (e.composedPath()[0].tagName == "IRON-DROPDOWN") {
        const dropDownContent = e.composedPath()[0].$.content.assignedNodes()[0];
        if (dropDownContent && dropDownContent.cancel) {
            dropDownContent.cancel(e.detail);
        }
    }
    if (!(e.detail instanceof MouseEvent)) {
        tearDownEvent(e.detail);
        this.focusInput();
    }
}

function handleAcceptEvent(e) {
    const currentOverlay = IronOverlayManager.currentOverlay();
    if (e.keyCode === 13 && currentOverlay && currentOverlay.tagName == "IRON-DROPDOWN") {
        const dropDownContent = currentOverlay.$.content.assignedNodes()[0];
        if (dropDownContent && dropDownContent.okCallback){
            dropDownContent.okCallback(e);
        }
    }
}
/***********************************************************************/

/**
 * Adds event listener to document to handle situation when mouse was pressed on editor but released on another element.
 * Such behavior creates problem for user editing.
 * 
 * @param {Event} e - mouse event
 */
function mouseEventRetranslator(e) {
    document.addEventListener("mouseup",  this._mouseUp);
    document.addEventListener("mousemove", this._mouseMove);
}

/**
 * Catches 'mouse up' event and sends it to the editor to handle it as if it happened on it.
 * It is necessary to handle the entire sequence of mouse events on the editor, even if the mouse moves outside of the editor.
 * 
 * @param {Event} e mouse event
 */
function mouseUp (e) {
    if (this._editor.wwEditor.view.input.mouseDown && !e.composedPath().includes(this._getEditableContent())) {
        this._editor.wwEditor.view.input.mouseDown.up(e);
    }
    removeMouseEventHandlersFromDocument.bind(this)();
}

/**
 * Catches the 'mouse move' event and sends it to the editor to handle it as if it happened on it.
 * It is necessary to handle the entire sequence of mouse events on the editor, even if the mouse moves outside of the editor.
 * 
 * @param {Event} e mouse event
 */
function mouseMove (e) {
    if (this._editor.wwEditor.view.input.mouseDown && !e.composedPath().includes(this._getEditableContent())) {
        this._editor.wwEditor.view.input.mouseDown.move(e);
    }
    removeMouseEventHandlersFromDocument.bind(this)();
}

/**
 * Removes mouse event listeners from document after the corresponding event was handled.
 */
function removeMouseEventHandlersFromDocument() {
    if (!this._editor.wwEditor.view.input.mouseDown) {
        document.removeEventListener("mouseup",  this._mouseUp);
        document.removeEventListener("mousemove", this._mouseMove);
    }
}

const template = html`
    ${tgRichTextStyles}
    <style>
        :host {
            @apply --layout-vertical;
        }
        ::selection {
            color: currentcolor;
            background-color: rgba(31,  176, 255, 0.3);
        }
        mark {
            background-color: rgba(31,  176, 255, 0.3);
            color: inherit;
        }
        del a mark {
            text-decoration: line-through underline !important;
        }
        del mark {
            text-decoration: line-through;  
        }
        a mark {
            text-decoration: underline;
        }
        .custom-responsive-toolbar {
            overflow: hidden;
            padding: 8px 0 8px 0;
            flex-shrink: 0;
            position: relative;
            left: -5px;
            --tg-responsove-toolbar-expand-button: {
                padding: 0;
                width: 22px;
                height: 22px;
                color:var(--paper-input-container-color, var(--secondary-text-color));
            };
            --tg-responsove-toolbar-dropdown-content: {
                padding: 8px 0 8px 8px;
            }
        }
    </style>
    <iron-dropdown id="linkDropdown" style="width:300px;height:160px;" vertical-align="top" horizontal-align="left" always-on-top on-iron-overlay-closed="_dialogClosed" on-iron-overlay-opened="_dialogOpened">
        <tg-link-dialog id="linkDialog" class="dropdown-content" slot="dropdown-content" cancel-callback="[[_cancelLinkInsertion]]" ok-callback="[[_acceptLink]]" toaster="[[toaster]]"></tg-link-dialog>
    </iron-dropdown>
    <iron-dropdown id="colorDropdown" style="width:300px;height:160px;" vertical-align="top" horizontal-align="left" always-on-top on-iron-overlay-closed="_dialogClosed" on-iron-overlay-opened="_dialogOpened">
        <tg-color-picker-dialog id="colorDialog" class="dropdown-content" slot="dropdown-content" cancel-callback="[[_cancelColorAction]]" ok-callback="[[_acceptColor]]" toaster="[[toaster]]"></tg-color-picker-dialog>
    </iron-dropdown>
    <tg-responsive-toolbar class="custom-responsive-toolbar editor-toolbar" disabled$="[[disabled]]" hidden$="[[isReadonly]]">
        <iron-icon slot="entity-specific-action" style$="[[_getSingleLetterActionStyle()]]" class="entity-specific-action" icon="editor:format-bold" tooltip-text="Bold, Ctrl+B, &#x2318;+B" on-down="_stopMouseEvent" on-tap="_applyBold"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getSingleLetterActionStyle()]]" class="entity-specific-action" icon="editor:format-italic" tooltip-text="Italic, Ctrl+I, &#x2318;+I" on-down="_stopMouseEvent" on-tap="_applyItalic"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getSingleLetterActionStyle()]]" class="entity-specific-action" icon="editor:strikethrough-s" tooltip-text="Strikethrough" on-down="_stopMouseEvent" on-tap="_applyStrikethough"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getSingleLetterActionStyle()]]" class="entity-specific-action" icon="editor:format-color-text" tooltip-text="Font colour" on-down="_applyFakeSelect" on-tap="_changeTextColor"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:insert-link" tooltip-text="Insert link" on-down="_applyFakeSelect" on-tap="_toggleLink"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-list-bulleted" tooltip-text="Bulletpoints, Ctrl+U, &#x2318;+U" on-down="_stopMouseEvent" on-tap="_createBulletList"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor:format-list-numbered" tooltip-text="Numbering, Ctrl+O, &#x2318;+O" on-down="_stopMouseEvent" on-tap="_createOrderedList"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor-icons:list-checkbox" tooltip-text="Task list" on-down="_stopMouseEvent" on-tap="_createTaskList"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor-icons:header-1" tooltip-text="Header 1" on-down="_stopMouseEvent" on-tap="_applyHeader1"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor-icons:header-2" tooltip-text="Header 2" on-down="_stopMouseEvent" on-tap="_applyHeader2"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="editor-icons:header-3" tooltip-text="Header 3" on-down="_stopMouseEvent" on-tap="_applyHeader3"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="icons:undo" tooltip-text="Undo, Ctrl+Z, &#x2318;+Z" on-down="_stopMouseEvent" on-tap="_undo"></iron-icon>
        <iron-icon slot="entity-specific-action" style$="[[_getActionStyle()]]" class="entity-specific-action" icon="icons:redo" tooltip-text="Redo, Ctrl+Y, &#x2318;+Y" on-down="_stopMouseEvent" on-tap="_redo"></iron-icon>
    </tg-responsive-toolbar>
    <div id="editor"></div>`; 

class TgRichTextInput extends mixinBehaviors([IronResizableBehavior, IronA11yKeysBehavior, TgTooltipBehavior], PolymerElement) {

    static get template() { 
        return template;
    }

    static get properties() {
        return {
            value: {
                type: String,
                observer: "_valueChanged",
                notify: true,
            },

            changeEventHandler: {
                type: Function,
                value: null
            },

            keyDownHandler: {
                type:Function,
                value: null
            },

            height: {
                type: String,
                value: '100%',
                observer: "_heightChanged"
            },

            minHeight: {
                type: String,
                value: '25px',
                observer: "_minHeightChanged"
            },

            disabled: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },

            /** Determines whether this editor is in read-only mode. */
            isReadonly: {
                type: Boolean,
                value: true,
                reflectToAttribute: true
            },

            toaster: Object,

            _editor: Object,
            _fakeSelection: {
                type: Array,
            },

            _cancelLinkInsertion: Function,
            _acceptLink: Function,

            _cancelColorAction: Function,
            _acceptColor: Function
        }
    }

    static get observers() {
        return ["_disabledChanged(disabled, _editor)"]
    }

    ready() {
        super.ready();
        // Bind some functions to use it for event handlers.
        this._handleAcceptEvent = handleAcceptEvent.bind(this);
        this._mouseUp = mouseUp.bind(this)
        this._mouseMove = mouseMove.bind(this)
        // Initialise link and color dialogs.
        this.$.linkDropdown.positionTarget = document.body;
        this.$.linkDropdown.addEventListener('iron-overlay-canceled', handleCancelEvent.bind(this));
        this._cancelLinkInsertion = function (e) {
            this.$.linkDropdown.cancel();
        }.bind(this);
        this._acceptLink = function (e) {
            if (this.$.linkDialog.accept(e)) {
                const link = initLinkEditing.bind(this)();
                if ((link && link.detail) || this.$.linkDialog.url) {
                    toggleLink.bind(this)(this.$.linkDialog.url, (link && link.text) || this.$.linkDialog.url);
                } else {
                    this.focusInput();
                }
                this.$.linkDropdown.close();
            }
            tearDownEvent(e);
        }.bind(this);
        this.$.colorDropdown.positionTarget = document.body;
        this.$.colorDropdown.addEventListener('iron-overlay-canceled', handleCancelEvent.bind(this));
        this._cancelColorAction = function(e) {
            this.$.colorDropdown.cancel();
        }.bind(this);
        this._acceptColor = function(e) {
            if (this.$.colorDialog.accept(e)) {
                const textColorObj = initColorEditing.bind(this)();
                if ((textColorObj && textColorObj.detail) || this.$.colorDialog.color) {
                    applyColor.bind(this)(this.$.colorDialog.color);
                } else {
                    this.focusInput();
                }
                this.$.colorDropdown.close();
            }
            tearDownEvent(e);
        }.bind(this);
        // Create editor.
        this._editor = new Editor({
            el: this.$.editor,
            height: this.height,
            minHeight: '0',
            initialEditType: 'wysiwyg',
            events: {
                change: this._htmlContentChanged.bind(this),
                blur: this._focusLost.bind(this),
                focus: this._focusGain.bind(this),
                keydown: (viewType, event) => this.keyDownHandler(event)
            },
            plugins: [colorTextPlugin, fakeSelection],
            linkAttributes: {target: "_blank"},
            useCommandShortcut: false,
            usageStatistics: false,
            toolbarItems: [],
            hideModeSwitch: true,
            autofocus: false
        });
        // Trigger tooltips manually.
        this.triggerManual = true;
        // The following code is needed to preserve whitespaces after loading html into editor.
        this._editor.wwEditor.schema.cached.domParser.rules.forEach(r => r.preserveWhitespace = "full");
        // Make link inclusive which means that link will be active widget when cursor is placed at the end of it.
        this._editor.wwEditor.view.state.schema.marks.link.spec.inclusive = true;
        // Make editable container not tabbable.
        // It will remain focusable with mouse pointer.
        this._getEditableContent().setAttribute('tabindex', '-1');
        // Add event listeners for tooltips on editor.
        this._getEditableContent().addEventListener("mouseover", mouseOverHandler.bind(this));
        // Add event listener to handle case when clicking on task list checkbox.
        this._getEditableContent().addEventListener("mousedown", handleTaskListItemClickWhenDisabled.bind(this), true);
        this._getEditableContent().addEventListener("mousedown", handleTaskListItemStatusChange.bind(this));
        // Add event listeners to make link clickable and with proper cursor.
        this._getEditableContent().addEventListener("mousedown", mouseDownHandler.bind(this), true);
        this._getEditableContent().addEventListener("mouseup", mouseUpHandler.bind(this));
        this._getEditableContent().addEventListener("touchstart", mouseDownHandler.bind(this));
        this._getEditableContent().addEventListener("touchend", mouseUpHandler.bind(this));
        // Add mouse down handler to handle case when user presses mouse button on editor and moves it outside of the editor,
        // which later prevents invocation of the mouse up handler.
        this._getEditableContent().addEventListener("mousedown", mouseEventRetranslator.bind(this), true);
        this._getEditableContent().addEventListener("keydown", handleKeyEventsBeforeEditor.bind(this), true);
        this._getEditableContent().addEventListener("keydown", handleEditorKeyEvents.bind(this));
        this._getEditableContent().addEventListener("keyup", handleKeyup.bind(this));
        // Next two event handlers needed to make link mark uninclusive and inclusive again on mobile devices,
        // which generates input events instead of the keydown/keyup events.
        this._getEditableContent().addEventListener("beforeinput", handleBeforeinput.bind(this), true);
        this._getEditableContent().addEventListener("input", handleInput.bind(this));
        this._getEditableContent().addEventListener("scroll", this._editorScrolled.bind(this));
        // Initiate key binding and key event target.
        this.addOwnKeyBinding('ctrl+b meta+b', '_applyBold');
        this.addOwnKeyBinding('ctrl+i meta+i', '_applyItalic');
        this.addOwnKeyBinding('ctrl+x meta+x', '_cut');
        this.addOwnKeyBinding('ctrl+z meta+z', '_undo');
        this.addOwnKeyBinding('ctrl+y meta+y', '_redo');
        this.addOwnKeyBinding('ctrl+u meta+u', '_createBulletList');
        this.addOwnKeyBinding('ctrl+o meta+o', '_createOrderedList');
        this.addOwnKeyBinding('ctrl+right ctrl+left ctrl+up ctrl+down', '_skipNavigation');
        this.addOwnKeyBinding('esc', '_stopEditing');
        this.keyEventTarget = this._getEditableContent();
        // Adjust key event handler to be able to process events from _editor when event was prevented.
        const prevKeyBindingHandler = this._onKeyBindingEvent.bind(this);
        this._onKeyBindingEvent = function (keyBindings, event) {
            if (!this.disabled) {
                Object.defineProperty(event, 'defaultPrevented', {value: false});
                prevKeyBindingHandler(keyBindings, event);
            }
        };
        this.addEventListener('keydown', focusOnKeyDown.bind(this));
        // Add click event to prevent the browser from opening the link.
        this.addEventListener('click', preventLinkClick.bind(this));
    }

    connectedCallback () {
        super.connectedCallback();
        document.addEventListener("keydown", this._handleAcceptEvent, true);
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener("keydown", this._handleAcceptEvent, true);
    }

    notifyResize () {
        super.notifyResize();
        repositionOpenedDialog.bind(this)();
        this._editorScrolled();
    }

    /**
     * Changes the editable state of rich text input.
     * 
     * @param {Boolean} editable determines whether editor shoulb be editable or not
     */
    makeEditable(editable) {
        this._getEditableContent().setAttribute("contenteditable", editable + "");
    }

    getHeight() {
        return this._editor && this._editor.getHeight();
    }

    getText() {
        return this._getEditableContent().innerText;
    }

    focusInput() {
        if (this._editor) {
            this._editor.wwEditor.view.focus();
        }
    }

    clearHistory () {
        const state = this._editor.wwEditor.createState();
        state.doc = this._editor.wwEditor.view.state.doc;
        this._editor.wwEditor.view.updateState(state);
    }

    replaceText(text, start, end) {
        const adjustedStart = start || 0;
        const adjustedEnd = end || this._editor.wwEditor.view.state.tr.doc.content.size;
        this._editor.replaceSelection(text, adjustedStart, adjustedEnd);
        const increment = text.includes('\n') || adjustedStart === 0 || adjustedEnd === this._editor.wwEditor.view.state.tr.doc.content.size ? 1 : 0;
        const cursorPosition = adjustedStart + text.length + increment;
        this._applySelection(cursorPosition, cursorPosition);
    }

    insertText(text, where) {
        if (typeof where === 'undefined') {
            this._editor.moveCursorToEnd(false);
        } else if (where) {
            this._applySelection(where, where);
        }
        this._editor.insertText(text);
    }

    get selectionStart() {
        const selection = this._getSelection();
        return selection[0] === 1 ? 0 : selection[0];
    }

    set selectionStart(where) {
        const selectionEnd = this._getSelection()[1];
        this._applySelection(where, selectionEnd < where ? where : selectionEnd);
    }

    get selectionEnd() {
        return this._getSelection()[1];
    }

    set selectionEnd(where) {
        const selectionStart = this._getSelection()[0];
        this._applySelection(selectionStart > where ? where : selectionStart, where);
    }

    _editorScrolled(e) {
        let shadowStyle = "";
        // Add top shadow if `scrollTop` is defined and not 0.
        if (this._getEditableContent().scrollTop) {
            shadowStyle += "inset 0 6px 6px -6px rgba(0,0,0,0.7)";
        }
        // If shadow style is not empty then add shadow style to editor, otherwise remove shadow.
        if (shadowStyle) {
            this._getEditableContent().style.boxShadow = shadowStyle;
        } else {
            this._getEditableContent().style.removeProperty('box-shadow');
        }
    }

    _cut(event) {
        const selection = this._getSelection();
        if (selection && selection[0] !== selection[1]) {
            tearDownEvent(event.detail && event.detail.keyboardEvent);
            document.execCommand('cut');
        }
    }

    _stopMouseEvent(e) {
        e.preventDefault();
    }

    _applyHeader1(event) {
        if (!allElementsSatisfies.bind(this)(el => el.tagName === 'H1')) {
            this._editor.exec('heading', { level: 1 });
        } else {
            this._editor.exec('heading', { level: 0 });
        }
    }

    _applyHeader2(event) {
        if (!allElementsSatisfies.bind(this)(el => el.tagName === 'H2')) {
            this._editor.exec('heading', { level: 2 });
        } else {
            this._editor.exec('heading', { level: 0 });
        }
    }

    _applyHeader3(event) {
        if (!allElementsSatisfies.bind(this)(el => el.tagName === 'H3')) {
            this._editor.exec('heading', { level: 3 });
        } else {
            this._editor.exec('heading', { level: 0 });
        }
    }

    _applyBold(event) {
        this._editor.exec('bold');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _applyItalic(event) {
        this._editor.exec('italic');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _applyStrikethough(event) {
        this._editor.exec('strike');
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _changeTextColor(e) {
        scrollIntoView.bind(this)();
        const textColorObj = initColorEditing.bind(this)();
        if (textColorObj) {
            this.$.colorDialog.color = textColorObj.detail;
        }
        setDialogPosition(this.$.colorDropdown, getSelectionCoordinates.bind(this)());
        document.body.appendChild(this.$.colorDropdown);
        this.$.colorDropdown.open();
    }

    _toggleLink(e) {
        scrollIntoView.bind(this)();
        const link = initLinkEditing.bind(this)();
        if (link) {
            this.$.linkDialog.url = link.detail;
        }
        setDialogPosition(this.$.linkDropdown, getSelectionCoordinates.bind(this)());
        document.body.appendChild(this.$.linkDropdown);
        this.$.linkDropdown.open();
    }

    _undo(event) {
        this._editor.exec('undo');
        this.changeEventHandler();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _redo(event) {
        this._editor.exec('redo');
        this.changeEventHandler();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _createBulletList(e) {
        try {
            this._editor.exec('bulletList');
            tearDownEvent(e.detail && e.detail.keyboardEvent);
            scrollIntoView.bind(this)();
        } catch (e) {
            if (e.name === 'TransformError') {
                console.error(e);
            } else {
                throw e;
            }
        }
    }

    _createOrderedList(e) {
        try {
            this._editor.exec('orderedList');
            tearDownEvent(e.detail && e.detail.keyboardEvent);
            scrollIntoView.bind(this)();
        } catch (e) {
            if (e.name === 'TransformError') {
                console.error(e);
            } else {
                throw e;
            }
        }
    }

    _skipNavigation (e) {
        if (e.detail && e.detail.keyboardEvent) {
            e.detail.keyboardEvent.skipNavigation = true;
        }
    }

    _createTaskList(e) {
        try {
            this._editor.exec('taskList');
            scrollIntoView.bind(this)();
        } catch (e) {
            if (e.name === 'TransformError') {
                console.error(e);
            } else {
                throw e;
            }
        }
    }

    _stopEditing(event) {
        const selection = this._getSelection();
        const cursorPosition = selection ? selection[1] : 0;
        this._applySelection(cursorPosition, cursorPosition);
        this._editor.blur();
        this.focus();
        tearDownEvent(event.detail && event.detail.keyboardEvent);
    }

    _valueChanged(newValue) {
        if(this._editor && newValue !== getEditorHTMLText.bind(this)()) {
            this._editor.setHTML(newValue, false);
            // Reset `scrollTop` because Safari still scrolls to the bottom even after setting HTML with second parameter equal to `false`,
            // which means "do not move cursor to the end".
            this._getEditableContent().scrollTop = 0;
        }
    }

    _htmlContentChanged(e) {
        const htmlText = getEditorHTMLText.bind(this)();
        if (this.value !== htmlText) {
            this.value = htmlText;
        }
    }

    _applyFakeSelect () {
        if (!this._fakeSelection) {
            this._fakeSelection = this._editor.getSelection();
            applyFakeSelection.bind(this)(this._fakeSelection);
            this._editor.setSelection(this._fakeSelection[1], this._fakeSelection[1]); //clears selection
        }
    }

    _focusLost(e) {
        this.changeEventHandler(e);
    }
    
    _focusGain(e) {
        if (this._fakeSelection) {
            applyFakeUnselection.bind(this)(this._fakeSelection);
            this._editor.setSelection(this._fakeSelection[0], this._fakeSelection[1]); //restors editor selection
            delete this._fakeSelection;
        }
    }

    _getSelection() {
        if (this._fakeSelection) {
            return this._fakeSelection;
        }
        const selection = this._editor.getSelection();
        if (selection && selection[0] === 1 && selection[1] !== 1) {
            this._editor.setSelection(0, selection[1]);
            return this._editor.getSelection();
        }
        return selection;
    }

    _applySelection(from, to) {
        if (this._fakeSelection) {
            this._fakeSelection = [from, to];
            applyFakeUnselection.bind(this)(this._fakeSelection);
            applyFakeSelection.bind(this)(this._fakeSelection);
        } else {
            this._editor.setSelection(from, to);
        }
    }

    _heightChanged(newHeight) {
        if (this._editor) {
            this._editor.setHeight(newHeight);
            this.fire("iron-resize", {
                node: this,
                bubbles: true,
            });
        }
    }

    _minHeightChanged(newMinHeight) {
        if (parseInt(newMinHeight) >= 25) {
            this.$.editor.style.minHeight = newMinHeight;
        }
    }

    _disabledChanged(newDisabled, _editor) {
        if (_editor) {
            this.makeEditable(!newDisabled);
            const handlersToRemove = ["keydown", "keypress", "keyup"];
            const handlers = _editor.wwEditor.view.input.eventHandlers;
            handlersToRemove.forEach(handler => {
                if (handlers[handler]) {
                    if (newDisabled) {
                        this._getEditableContent().removeEventListener(handler, handlers[handler]);
                    } else {
                        this._getEditableContent().addEventListener(handler, handlers[handler]);
                    }
                }
                
            });
        }
    }

    _getEditableContent() {
        return this._editor.getEditorElements().wwEditor.children[0];
    }

    _dialogClosed(e) {
        if (e.composedPath()[0].tagName == "IRON-DROPDOWN") {
            const dropDownContent = e.composedPath()[0].$.content.assignedNodes()[0];
            if (dropDownContent && dropDownContent.resetState) {
                dropDownContent.resetState();
                document.body.removeChild(e.composedPath()[0]);
            }
        }
    }

    _dialogOpened(e) {
        if (e.composedPath()[0].tagName == "IRON-DROPDOWN") {
            const dropDownContent = e.composedPath()[0].$.content.assignedNodes()[0];
            if (dropDownContent && dropDownContent.focusDefaultEditor && !isMobileApp()) {
                dropDownContent.focusDefaultEditor();
            }
        }
    }

    _getSingleLetterActionStyle() {
        return this._getActionStyleFor(true);
    }

    _getActionStyle() {
        return this._getActionStyleFor(false);
    }

    _getActionStyleFor(singleLetterIcon) {
        const marginRight = singleLetterIcon ? 5 : 8;
        return `width:22px;height:22px;cursor:pointer;margin-right:${marginRight}px;color:var(--paper-input-container-color, var(--secondary-text-color));`;
    }
}

customElements.define('tg-rich-text-input', TgRichTextInput);