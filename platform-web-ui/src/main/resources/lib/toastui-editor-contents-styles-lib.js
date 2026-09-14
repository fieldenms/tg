import toastuiEditorContentsStyleStrings from '@toast-ui/editor/toastui-editor-viewer.css';
import { createStyleModule } from './tg-style-utils.js';

// `toastui-editor-viewer.css` carries only the rendered markup rules, that is `.toastui-editor-contents ...`.
// It is the half of `toastui-editor.css` that a read-only consumer such as `tg-tooltip` needs.
// Keeping it in a separate entry lets such consumers style rich text without pulling in the editor.
createStyleModule('toastui-editor-contents-styles', toastuiEditorContentsStyleStrings);

import { html } from '@polymer/polymer/lib/utils/html-tag.js';

/// Styles for rendered rich text markup, without any editor UI.
/// Include these when rich text is displayed read-only, as `tg-tooltip` does.
/// The module id is inlined into the template, because the html tag function security forbids interpolating it.
///
export const toastuiEditorContentsStyles = html`<style include='toastui-editor-contents-styles'></style>`;
