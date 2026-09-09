// This entry pulls in the whole `@toast-ui/editor` JS, so only real editor consumers may import from it.
// Editor styles live in 'toastui-editor-styles-lib.js'.
// Their read-only subset lives in 'toastui-editor-contents-styles-lib.js'.

// Preserve dompurify explicitly.
// Because otherwise it is tree shaken due to being "unused" in '@toast-ui/editor/dist/esm/index.js'.
// They decided to insert dompurify 2.3.3 into the source.
export { default as purify } from 'dompurify';

import Editor from '@toast-ui/editor';
export default Editor;
