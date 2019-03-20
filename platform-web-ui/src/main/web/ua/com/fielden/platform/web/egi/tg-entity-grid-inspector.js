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
            border-radius: 2px;
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
        #baseContainer {
            min-height: 0;
            @apply --layout-vertical;
            @apply --layout-flex;
            @apply --layout-relative;
        }
        .table-header-row {
            font-size: 0.9rem;
            font-weight: 400;
            color: #757575;
            height: 3rem;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: fit-content;
            @apply --layout-horizontal;
        }
        .table-data-row {
            font-size: 1rem;
            font-weight: 400;
            color: #212121;
            height: var(--egi-row-height, 1.5rem);
            border-top: 1px solid #e3e3e3;
            -webkit-font-smoothing: antialiased;
            text-rendering: optimizeLegibility;
            min-width: fit-content;
            @apply --layout-horizontal;
        }
        .dummy-drag-box {
            width: 1.5rem;
            height: 1.5rem;
        }
        .table-cell,
        .table-data-cell {
            @apply --layout-horizontal;
            @apply --layout-center;
            @apply --layout-center-justified
            @apply --layout-relative;
            padding: 0 0.6rem;
        }
        .action-cell {
            @apply --layout-horizontal;
            @apply --layout-center;
            width: 20px;
            padding: 0 0.3rem;
        }
        .drag-anchor {
            --iron-icon-width: 1.5rem;
            --iron-icon-height: 1.5rem;
            @apply(--layout-self-center);
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
        <div id="baseContainer">
            <!-- Table header -->
            <div class="table-header-row">
                <div class="dummy-drag-box"></div>
                <div class="table-cell">
                    <paper-checkbox></paper-checkbox>
                </div>
                <div class="action-cell">
                    <!--Primary action stub header goes here-->
                </div>
                <template is="dom-repeat" items="[[columns]]">
                    <div class="table-cell">
                        <div class="truncate" style="width:100%">[[item.columnTitle]]</div>
                    </div>
                </template>
                <div class="action-cell">
                    <!--Secondary actions header goes here-->
                </div>
            </div>
            <template is="dom-repeat" items="[[egiModel]]" as="egiEntity" index-as="entityIndex" >
                <div class="table-data-row" selected$="[[egiEntity.selected]]">
                    <iron-icon draggable="true" class="drag-anchor" icon="tg-icons:dragVertical"></iron-icon>
                    <div class="table-cell">
                        <paper-checkbox></paper-checkbox>
                    </div>
                    <div class="action-cell">
                    </div>
                    <template is="dom-repeat" items="[[columns]]" as="column">
                        <div class="table-data-cell">
                            <div class="fit" style$="[[_calcBackgroundRenderingHintsStyle(egiEntity.renderingHints.*, entityIndex, column.property)]]"></div>
                            <iron-icon class="table-icon" hidden$="[[!_isBooleanProp(egiEntity.entity, column)]]" style$="[[_calcValueRenderingHintsStyle(egiEntity.renderingHints.*, entityIndex, column.property, 'true')]]" icon="[[_getBooleanIcon(egiEntity.entity, column.property)]]"></iron-icon>
                            <a class="truncate" hidden$="[[!_isHyperlinkProp(egiEntity.entity, column)]]" href$="[[_getBindedValue(egiEntity.entity.*, column.property, column.type)]]" style$="[[_calcValueRenderingHintsStyle(egiEntity.renderingHints.*, entityIndex, column.property, 'false')]]">[[_getBindedValue(egiEntity.entity.*, column.property, column.type)]]</a>
                            <div class="truncate relative" hidden$="[[!_isNotBooleanOrHyperlinkProp(egiEntity.entity, column)]]" style$="[[_calcValueRenderingHintsStyle(egiEntity.renderingHints.*, entityIndex, column.property, 'false')]]">[[_getBindedValue(egiEntity.entity.*, column.property, column.type)]]</div>
                        </div>
                    </template>
                    <div class="action-cell layout horizontal center no-flexible">
                        <template is="dom-if" if="[[_isOnlyOneSecondaryActions(secondaryActions)]]">
                            <tg-ui-action class="action" show-dialog="[[secondaryActions.0.showDialog]]" current-entity="[[egiEntity.entity]]" short-desc="[[secondaryActions.0.shortDesc]]" long-desc="[[secondaryActions.0.longDesc]]" icon="[[secondaryActions.0.icon]]" component-uri="[[secondaryActions.0.componentUri]]" element-name="[[secondaryActions.0.elementName]]" action-kind="[[secondaryActions.0.actionKind]]" number-of-action="[[secondaryActions.0.numberOfAction]]" attrs="[[secondaryActions.0.attrs]]" create-context-holder="[[secondaryActions.0.createContextHolder]]" require-selection-criteria="[[secondaryActions.0.requireSelectionCriteria]]" require-selected-entities="[[secondaryActions.0.requireSelectedEntities]]" require-master-entity="[[secondaryActions.0.requireMasterEntity]]" pre-action="[[secondaryActions.0.preAction]]" post-action-success="[[secondaryActions.0.postActionSuccess]]" post-action-error="[[secondaryActions.0.postActionError]]" should-refresh-parent-centre-after-save="[[secondaryActions.0.shouldRefreshParentCentreAfterSave]]" ui-role="[[secondaryActions.0.uiRole]]" icon-style="[[secondaryActions.0.iconStyle]]"></tg-ui-action>
                        </template>
                        <template is="dom-if" if="[[!_isOnlyOneSecondaryActions(secondaryActions)]]">
                            <tg-secondary-action-button class="action" current-entity="[[egiEntity.entity]]" actions="[[secondaryActions]]"></tg-secondary-action-button>
                        </template>
                    </div>
                </div>
            </template>
        </div>
    </div>`;

Polymer({

    _template: template,

    is: 'tg-entity-grid-inspector',
    
    properties: {
        //Determines whether entities can be dragged from this EGI.
        canDragFrom: {
            type: Boolean,
            value: false
        },
        //The property that determines whether progress bar is visible or not.
        _showProgress: {
            type: Boolean
        },
    }


});