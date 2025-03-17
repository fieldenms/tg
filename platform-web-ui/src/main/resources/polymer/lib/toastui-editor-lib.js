import toastuiEditorStyleStrings from '../@toast-ui/editor/dist/toastui-editor.css.js';
import prosemirrorViewStyleStrings from '../prosemirror-view/style/prosemirror.css.js';
import { createStyleModule } from './tg-style-utils.js';
import { html } from '../@polymer/polymer/lib/utils/html-tag.js';
export { default as purify } from '../dompurify/dist/purify.es.mjs.js';
import { Editor as ToastUIEditor } from '../@toast-ui/editor/dist/esm/index.js';
export { Editor as default } from '../@toast-ui/editor/dist/esm/index.js';

createStyleModule('toastui-editor-styles', toastuiEditorStyleStrings, prosemirrorViewStyleStrings);
const toastuiEditorStyles = html`<style include='toastui-editor-styles'></style>`; // can't use 'const ... = 'toastui-editor-styles'', because of html tag function stringent security

export { toastuiEditorStyles };
