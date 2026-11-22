import toastuiEditorContentsStyleStrings from '../@toast-ui/editor/dist/toastui-editor-viewer.css.js';
import { createStyleModule } from './tg-style-utils.js';
import { html } from '../@polymer/polymer/lib/utils/html-tag.js';

// `toastui-editor-viewer.css` carries only the rendered markup rules, that is `.toastui-editor-contents ...`.
// It is the half of `toastui-editor.css` that a read-only consumer such as `tg-tooltip` needs.
// Keeping it in a separate entry lets such consumers style rich text without pulling in the editor.
createStyleModule('toastui-editor-contents-styles', toastuiEditorContentsStyleStrings);

/// Styles for rendered rich text markup, without any editor UI.
/// Include these when rich text is displayed read-only, as `tg-tooltip` does.
/// The module id is inlined into the template, because the html tag function security forbids interpolating it.
///
const toastuiEditorContentsStyles = html`<style include='toastui-editor-contents-styles'></style>`;

export { toastuiEditorContentsStyles };
