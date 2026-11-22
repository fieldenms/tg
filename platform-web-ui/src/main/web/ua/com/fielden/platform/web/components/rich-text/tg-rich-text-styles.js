import '/resources/polymer/@polymer/paper-styles/typography.js';
import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';

import { tgRichTextContentsStyles } from '/resources/components/rich-text/tg-rich-text-contents-styles.js';
import { toastuiEditorUiStyles } from '/resources/polymer/lib/toastui-editor-ui-styles-lib.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

// TG overrides for the editor UI.
// The overrides for the rendered markup live in 'tg-rich-text-contents-styles.js'.
createStyleModule('tg-rich-text-ui-styles', `
    :host {
        position: relative
        @apply --layout-vertical;
    }
    .toastui-editor-defaultUI {
        border: none !important;
        @apply --paper-font-subhead;
    }
    .toastui-editor-toolbar {
        display: none;
    }
    .toastui-editor-defaultUI .ProseMirror {
        padding: 0 !important;
    }
`);

/// Full rich text styles, that is the rendered markup styles followed by the editor UI ones.
/// Include these in an editor, and `tgRichTextContentsStyles` where rich text is displayed read-only.
/// The module id is inlined into the template, because the html tag function security forbids interpolating it.
///
export const tgRichTextStyles = html`
    ${tgRichTextContentsStyles}
    ${toastuiEditorUiStyles}
    <style include='tg-rich-text-ui-styles'></style>
`;
