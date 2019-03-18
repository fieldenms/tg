import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';

import '/resources/polymer/@polymer/paper-checkbox/paper-checkbox.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import "/resources/polymer/@polymer/paper-styles/element-styles/paper-material-styles.js";
import '/resources/polymer/@polymer/paper-progress/paper-progress.js';
import '/resources/polymer/@polymer/paper-styles/color.js';

import '/app/tg-app-config.js';
import '/app/tg-reflector.js';
import '/resources/serialisation/tg-serialiser.js';
import '/resources/images/tg-icons.js';

import '/resources/actions/tg-ui-action.js';
import '/resources/egi/tg-secondary-action-button.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgTooltipBehavior } from '/resources/components/tg-tooltip-behavior.js';
import { TgDragFromBehavior } from '/resources/components/tg-drag-from-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import '/resources/reflection/tg-polymer-utils.js';
import '/resources/reflection/tg-date-utils.js';

const template = html`
    <style>
        :host {
            @apply --layout-vertical;
        }
        .paper-material {
            @apply --layout-vertical;
        }
        .grid-toolbar {
            position: relative;
            overflow: hidden;
            @apply --layout-horizontal;
            @apply --layout-wrap;
        }
        paper-progress {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            width: auto;
        }
        paper-progress.uploading {
            --paper-progress-active-color: var(--paper-light-green-500);
        }
        paper-progress.processing {
            --paper-progress-active-color: var(--paper-orange-500);
        }
        .grid-toolbar-content {
            @apply --layout-horizontal;
            @apply --layout-center;
        }
        .grid-toolbar-content::slotted(*) {
            margin-top: 8px;
        }
    </style>
    <custom-style>
        <style include="paper-material-styles"></style>
    </custom-style>
    <!--configuring slotted elements-->
    <slot id="column_selector" name="property-column" hidden></slot>
    <slot id="primary_action_selector" name="primary-action" hidden></slot>
    <slot id="secondary_action_selector" name="secondary-action" hidden></slot>
    <slot id="insertion_point_action_selector" name="insertion-point-action" hidden></slot>
    <!--utility elements-->
    <tg-app-config id="appConfig"></tg-app-config>
    <tg-reflector id="reflector"></tg-reflector>
    <tg-serialiser id="serialiser"></tg-serialiser>
    <!--EGI template-->
    <div class="paper-material" elevation="1">
        <!--Table toolbar-->
        <div class="grid-toolbar">
            <paper-progress id="progressBar" hidden$="[[!_showProgress]]"></paper-progress>
            <div class="grid-toolbar-content">
                <slot id="top_action_selctor" name="entity-specific-action"></slot>
            </div>
            <div class="grid-toolbar-content" style="margin-left:auto">
                <slot name="standart-action"></slot>
            </div>
        </div>
    </div>`;

Polymer({

    _template: template,

    is: 'tg-entity-grid-inspector',
    
    properties: {
        /**
         * The property that determines whether progress bar is visible or not.
         */
        _showProgress: {
            type: Boolean
        },
    }


});