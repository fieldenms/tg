import '/resources/polymer/@polymer/polymer/lib/elements/dom-module.js';
import '/resources/components/rich-text/tg-rich-text-input-styles.js';
import '/resources/polymer/@polymer/paper-styles/typography.js';

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
                @apply --paper-font-subhead;
            }
            .toastui-editor-toolbar {
                display: none;
            }
            .toastui-editor-defaultUI .ProseMirror {
                padding: 0 !important;
            }
            .toastui-editor-contents {
                font-family: inherit !important;
                font-size: 16px !important; /*this font size was taken from paper-style typography. It was taken from there because there is no other way to inherit or specify a proper font-size*/
            }
            .toastui-editor-contents li {
                padding: 2px 0;
            }
            .toastui-editor-contents h1, .toastui-editor-contents h2 {
                border-bottom: none !important;
            }
            .toastui-editor-contents a {
                cursor: pointer !important;
            }
            .toastui-editor-contents .task-list-item {
                cursor: pointer; /*Need to set this style to display pointer cursor on checkboxes of task lins on Safari*/
            }
            .toastui-editor-contents .task-list-item * {
                cursor: auto; /*Set automatic cusrsor  on any element of task list to make pointer cursor available only on checkbox*/
            }
            .toastui-editor-contents .task-list-item:before {
                top: 2px !important; /*should add 2px top because of padding for li element that also has 2px on top*/
            }
            .toastui-editor-contents :not(table){
                line-height: normal;
            }
            del a span, del a {
                text-decoration: line-through underline !important;
            }
            del span {
                text-decoration: line-through !important;  
            }
            a span {
                text-decoration: underline !important;
            }
        </style>
    </template>
`;
styleElement.register('rich-text-enhanced-styles');