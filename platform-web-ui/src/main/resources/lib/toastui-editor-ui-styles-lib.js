import toastuiEditorUiStyleStrings from '@toast-ui/editor/toastui-editor-only.css';

// Ensure the latest `prosemirror-view` (see package-lock.json) styles are attached on top of older
// `@toast-ui/editor` packaged ones.
import prosemirrorViewStyleStrings from 'prosemirror-view/style/prosemirror.css';
import { createStyleModule } from './tg-style-utils.js';

// `toastui-editor-only.css` carries the editor UI rules, that is the toolbar, popups, mode switch and such.
// Together with `toastuiEditorContentsStyles` it reconstitutes `toastui-editor.css`.
// The one thing it leaves out is the stale `prosemirror-view` copy bundled into `toastui-editor.css`.
// That copy is superseded by `prosemirrorViewStyleStrings` anyway, so dropping it changes nothing.
createStyleModule('toastui-editor-ui-styles', toastuiEditorUiStyleStrings, prosemirrorViewStyleStrings);

import { html } from '@polymer/polymer/lib/utils/html-tag.js';

/// Styles for the editor UI, that is the toolbar, the popups and the editable surface.
/// These complement `toastuiEditorContentsStyles`, which styles the rich text itself.
/// The module id is inlined into the template, because the html tag function security forbids interpolating it.
///
export const toastuiEditorUiStyles = html`<style include='toastui-editor-ui-styles'></style>`;
