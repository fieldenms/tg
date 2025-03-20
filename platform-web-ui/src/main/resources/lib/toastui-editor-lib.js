import toastuiEditorStyleStrings from '@toast-ui/editor/toastui-editor.css';
import prosemirrorViewStyleStrings from 'prosemirror-view/style/prosemirror.css'; // ensure the latest `prosemirror-view` (1.37.2, see package-lock.json) styles are attached on top of older `@toast-ui/editor` packaged one
import { createStyleModule } from './tg-style-utils.js';
createStyleModule('toastui-editor-styles', toastuiEditorStyleStrings, prosemirrorViewStyleStrings);

import { html } from '@polymer/polymer/lib/utils/html-tag.js';
export const toastuiEditorStyles = html`<style include='toastui-editor-styles'></style>`; // can't use 'const ... = 'toastui-editor-styles'', because of html tag function stringent security

// Preserve dompurify explicitly.
// Because otherwise it is tree shaken due to being "unused" in '@toast-ui/editor/dist/esm/index.js'.
// They decided to insert dompurify 2.3.3 into the source.
export { default as purify } from 'dompurify';

import Editor from '@toast-ui/editor';
export default Editor;