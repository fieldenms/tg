import toastuiEditorStyleStrings from '@toast-ui/editor/toastui-editor.css';
import { createStyleModule } from './tg-style-utils.js';
createStyleModule('toastui-editor-styles', toastuiEditorStyleStrings);

import { html } from '@polymer/polymer/lib/utils/html-tag.js';
export const toastuiEditorStyles = html`<style include='toastui-editor-styles'></style>`; // can't use 'const ... = 'toastui-editor-styles'', because of html tag function stringent security

import Editor from '@toast-ui/editor';
export default Editor;