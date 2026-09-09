// Registers 'toastui-editor-contents-styles', which the full editor styles include ahead of the UI ones.
import './toastui-editor-contents-styles-lib.js';

import toastuiEditorUiStyleStrings from '@toast-ui/editor/toastui-editor-only.css';

// Ensure the latest `prosemirror-view` (see package-lock.json) styles are attached on top of older
// `@toast-ui/editor` packaged ones.
import prosemirrorViewStyleStrings from 'prosemirror-view/style/prosemirror.css';
import { createStyleModule } from './tg-style-utils.js';

// `toastui-editor-only.css` carries the editor UI rules, that is the toolbar, popups, mode switch and such.
// Together with 'toastui-editor-contents-styles' it reconstitutes `toastui-editor.css`.
// The one thing it leaves out is the stale `prosemirror-view` copy bundled into `toastui-editor.css`.
// That copy is superseded by `prosemirrorViewStyleStrings` anyway, so dropping it changes nothing.
createStyleModule('toastui-editor-ui-styles', toastuiEditorUiStyleStrings, prosemirrorViewStyleStrings);

import { html } from '@polymer/polymer/lib/utils/html-tag.js';

/// Full editor styles, that is the rendered markup rules followed by the editor UI ones.
/// Included modules are applied in the order listed, which reproduces the cascade of `toastui-editor.css`.
/// The module ids are inlined into the template, because the html tag function security forbids interpolating them.
///
export const toastuiEditorStyles = html`<style include='toastui-editor-contents-styles toastui-editor-ui-styles'></style>`;
