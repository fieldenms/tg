import '/resources/polymer/@polymer/polymer/lib/elements/dom-module.js';
import '/resources/components/rich-text/tg-rich-text-input-styles.js';

const styleElement = document.createElement('dom-module');
styleElement.innerHTML = `
    <template>
        <style include='rich-text-styles'>
            :host {
                position: relative
                @apply --layout-vertical;
            }
            .toastui-editor-defaultUI {
                border: none !important;
            }
            .toastui-editor-toolbar {
                display: none;
            }
            .toastui-editor-defaultUI .ProseMirror {
                padding: 0 !important;
            }
            .toastui-editor-contents {
                font-size: inherit !important;
            }
            .toastui-editor-contents h1, .toastui-editor-contents h2 {
                border-bottom: none !important;
            }
            .toastui-editor-contents a {
                cursor: pointer !important;
            }
            .toastui-editor-contents .task-list-item:before {
                top: calc(50% - 9px) !important;
            }
            .toastui-editor-contents ul>li:before {
                margin-top: 0 !important;
                top: calc(50% - 2.5px);
            }
        </style>
    </template>
`;
styleElement.register('rich-text-enhanced-styles');