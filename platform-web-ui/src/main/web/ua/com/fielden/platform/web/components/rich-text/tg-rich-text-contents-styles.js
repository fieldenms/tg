import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';

import { toastuiEditorContentsStyles } from '/resources/polymer/lib/toastui-editor-contents-styles-lib.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

// TG overrides for the rendered rich text markup.
// They are held apart from the editor UI overrides in 'tg-rich-text-styles.js' on purpose.
// That way a read-only consumer such as `tg-tooltip` can style rich text without depending on the editor.
createStyleModule('tg-rich-text-contents-styles', `
    .toastui-editor-contents {
        text-align: initial;
        font-family: inherit !important;
        font-size: 16px !important; /*this font size was taken from paper-style typography. It was taken from there because there is no other way to inherit or specify a proper font-size*/
    }
    .toastui-editor-contents h1, .toastui-editor-contents h2 {
        border-bottom: none !important;
    }
    .toastui-editor-contents h1 {
        margin: 12px 0 5px !important;
    }
    .toastui-editor-contents h2 {
        margin: 10px 0 3px !important;
    }
    .toastui-editor-contents h3 {
        margin: 8px 0 2px !important;
    }
    .toastui-editor-contents a {
        cursor: pointer !important;
    }
    .toastui-editor-contents .task-list-item:before {
        top: 2px !important; /* ~(2px or 3px depending on vertical align) = 24px(lineheight)/2 - 18px(checkbox height)/2 */
    }
    .toastui-editor-contents ul>li:before {
        margin-top: 0 !important;
        top: 9px !important; /* ~(10px or 9px depending on vertical align) = 24px(lineheight)/2 - 5px(point height)/2 */
    }
    .toastui-editor-contents[contenteditable="false"] .task-list-item,
    .toastui-editor-contents[contenteditable="false"] .task-list-item:before {
        cursor: default; /*Need to set this style to dispaly default cursor for task list item in disabled editor*/
    }
    .toastui-editor-contents .task-list-item {
        cursor: pointer; /*Need to set this style to display pointer cursor on checkboxes of task lins on Safari*/
    }
    .toastui-editor-contents .task-list-item>p {
         cursor: text; /*Set automatic cusrsor  on any element of task list to make pointer cursor available only on checkbox*/
    }
    .toastui-editor-contents :not(table){
        line-height: 24px;
    }
    del a span, del a {
        text-decoration: line-through underline !important;
    }
    del span {
        text-decoration: line-through !important;
    }
    a span {
        text-decoration: underline !important;
    }
`);

/// Rendered rich text markup styles, that is the toast-ui contents styles plus the TG overrides for them.
/// Include these when rich text is displayed read-only, as `tg-tooltip` does.
/// Use `tgRichTextStyles` instead when the editor UI is displayed as well.
///
export const tgRichTextContentsStyles = html`
    ${toastuiEditorContentsStyles}
    <style include='tg-rich-text-contents-styles'></style>
`;
