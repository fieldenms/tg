import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-list/iron-list.js';
import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js'

/*import '/resources/images/tg-icons.js';*/
import '/resources/editors/tg-highlighting-behavior.js'
import '/resources/editors/tg-dom-stamper.js'

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgEditorBehavior , createEditorTemplate } from '/resources/editors/tg-editor-behavior.js';

const template = html`
    <style>
        :host {
            @apply(--layout-vertical);
            @apply(--layout-flex);
        }
        /* tg-editor {
            --tg-editor-paper-input-container-mixin: {
                @apply(--layout-vertical);
                @apply(--layout-flex);
            };
            --tg-editor-input-content-mixin: {
                @apply(--layout-start);
                height: 100%;
            };
            --tg-editor-label-and-input-container-mixin: {
                height: 100%;
            };
            --tg-editor-main-container-mixin: {
                height: 100%;
            };
            --tg-editor-input-container-mixin: {
                @apply(--layout-vertical);
                height: 100%;
            };
        } */
        .noselect {
            -webkit-touch-callout: none;
            /* iOS Safari */
            -webkit-user-select: none;
            /* Safari */
            -khtml-user-select: none;
            /* Konqueror HTML */
            -moz-user-select: none;
            /* Firefox */
            -ms-user-select: none;
            /* Internet Explorer/Edge */
            user-select: none;
            /* Non-prefixed version, currently
                                  supported by Chrome and Opera */
        }
        iron-list {
            overflow: auto;
            -webkit-overflow-scrolling: touch;
        }
        .item-disabled {
            pointer-events: none;
        }
        
        .item {
            @apply(--layout-horizontal);
            @apply(--layout-center);
            padding: 16px 22px 16px 0;
            border-bottom: 1px solid #DDD;
        }
        
        .item:hover {
            background-color: var(--google-grey-100);
        }
        
        .item:focus,
        .item.selected:focus {
            outline: 0;
        }
        .item:hover > .resizing-box {
            visibility: visible;
        }
        .resizing-box:hover {
            cursor: move; /* fallback if grab cursor is unsupported */
            cursor: grab;
            cursor: -moz-grab;
            cursor: -webkit-grab;
        }
        .resizing-box:active { 
            cursor: grabbing;
            cursor: -moz-grabbing;
            cursor: -webkit-grabbing;
        }
        .resizing-box {
            visibility: hidden;
            margin: 0 2px;
            color: var(--paper-light-blue-700);
            min-width: 32px;
            min-height: 32px;
        }
        .dummy-box {
            background-color: transparent;
            border: 1px solid var(--paper-light-blue-500);
        }
        .dragging-item > .resizing-box{
            visibility: visible;
        }
        paper-checkbox {
            --paper-checkbox-checked-color: var(--paper-light-blue-700);
            --paper-checkbox-checked-ink-color: var(--paper-light-blue-700);
            --paper-checkbox-unchecked-color: var(--paper-grey-900:);
            --paper-checkbox-unchecked-ink-color: var(--paper-grey-900:);
        }
        .item.selected {
            background-color: var(--google-grey-100);
        }
        
        .ordering-number {
            font-size: 8pt;
            width: 1rem;
        }
        
        .pad {
            padding-left: 14px;
            overflow: hidden;
            @apply(--layout-vertical);
        }
        .without-pad {
            overflow: hidden;
            @apply(--layout-vertical);
        }
        .primary {
            font-size: 10pt;
            padding-bottom: 3px;
        }
        .secondary {
            font-size: 8pt;
        }
        .inherited-primary {
            font-weight: bolder;
        }
        .inherited-secondary {
            font-weight: bolder;
        }
        .dim {
            color: gray;
        }
        .truncate {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .sorting-group {
            cursor: pointer;
            @apply(--layout-horizontal);
        }
        .sorting-invisible {
            visibility: hidden;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>`;