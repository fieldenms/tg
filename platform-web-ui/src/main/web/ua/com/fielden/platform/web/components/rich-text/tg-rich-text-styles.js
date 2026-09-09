import '/resources/polymer/@polymer/paper-styles/typography.js';
import { createStyleModule } from '/resources/polymer/lib/tg-style-utils.js';

// Registers 'tg-rich-text-contents-styles', which the full rich text styles include ahead of the UI ones.
import '/resources/components/rich-text/tg-rich-text-contents-styles.js';

import { toastuiEditorStyles } from '/resources/polymer/lib/toastui-editor-styles-lib.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

// TG overrides for the editor UI.
// The overrides for the rendered markup live in 'tg-rich-text-contents-styles.js'.
createStyleModule('tg-rich-text-styles', `
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
/// Included modules are applied in the order listed, so the editor UI overrides come last.
///
export const tgRichTextStyles = html`
    ${toastuiEditorStyles}
    <style include='tg-rich-text-contents-styles tg-rich-text-styles'></style>
`;
