import '/resources/element_loader/tg-element-loader.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { TgMasterComponentBehavior } from '/resources/master/tg-master-component-behavior.js';
import { generateUUID } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            display: flex;
            flex-direction: column;
            --tg-grid-container: {
                @apply --layout-flex;
                min-height:0;
            };
        }
        .caption {
            display: flex;
            align-items: center;
            min-height: 24px;
            font-size: 12px;
            color: var(--paper-input-container-color, var(--secondary-text-color));
        }
        .caption[hidden] {
            display: none;
        }
        .caption-title {
            flex: 1;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            cursor: default;Ï
        }
        #loader {
            flex: 1;
            min-height: 0;
            display: flex;
            flex-direction: column;
            /* the containing block of the views of the embedded centre, which are positioned absolutely to fill the nearest positioned ancestor, so that they stay below the caption */
            position: relative;
        }
        /* the embedded centre, which the loader inserts as its child */
        #loader > * {
            flex: 1;
            min-height: 0;
        }
    </style>
    <div class="caption" hidden$="[[!_hasCaption(propTitle, _hasPropertyActions)]]">
        <span class="caption-title" tooltip-text$="[[propDesc]]">[[propTitle]]</span>
        <slot name="property-action"></slot>
    </div>
    <tg-element-loader id="loader" import="[[_importUri(miType)]]" element-name="[[_elementName(miType)]]" attrs="[[_centreAttrs]]" context="[[_getMasterEntity]]" context-property="getMasterEntity"></tg-element-loader>
`;

/**
 * A component for entity masters that embeds an entity centre, whose query depends on the master entity, as the embedded centres of compound masters do.
 * It is declared in the Entity Master DSL, usually bound to the entity:
 *
 *     .addComponent("centre/tg-master-centre")
 *         .withTitle("Stock on Hand")
 *         .withAttr("mi-type", MiWorkActivityExpendableMaster_StockOnHand.class.getName())
 *
 * The centre is that of menu item type 'mi-type', registered in the application, which is loaded from '/centre_ui/<mi-type>' as element 'tg-<simple name of mi-type>-centre'.
 * Its context includes the master entity, with the changes that are not yet saved, so that its query enhancer with 'context().withMasterEntity()' can restrict the query by the master entity.
 * The centre runs once the master entity is available, and runs again each time the master entity changes, such as after a change of a property, which is validated by the master.
 * It does not run while the component is blocked.
 *
 * The centre has its own uuid, so that its actions do not refresh the master.
 * The component fills the height of its layout cell, so that it grows with the master in a flexible row of the master layout, such as 'FLEXIBLE_ROW' of 'LayoutComposer'.
 * Its height is not less than 'min-height', which is also its height in a row laid out to the heights of its content.
 */
Polymer({
    _template: template,

    is: 'tg-master-centre',

    behaviors: [TgMasterComponentBehavior],

    properties: {

        /**
         * The fully-qualified name of the menu item type of the embedded centre.
         */
        miType: {
            type: String
        },

        /**
         * The least height of the component, as a CSS length.
         */
        minHeight: {
            type: String,
            value: '250px',
            observer: '_minHeightChanged'
        },

        /**
         * The function that provides the master entity as the context of the embedded centre, which the centre calls when it runs.
         * A new function is assigned for every change of the master entity, which makes the centre run again.
         */
        _getMasterEntity: {
            type: Function
        },

        _centreAttrs: {
            type: Object,
            value: function () {
                return {
                    embedded: true,
                    uuid: generateUUID()
                };
            }
        },

        _hasPropertyActions: {
            type: Boolean,
            value: false
        }
    },

    observers: [
        '_contextChanged(entity, createContextHolder, blocked)'
    ],

    ready: function () {
        this._hasPropertyActions = this._propertyActions.length > 0;
    },

    _minHeightChanged: function (minHeight) {
        this.style.minHeight = minHeight;
    },

    _hasCaption: function (propTitle, _hasPropertyActions) {
        return !!propTitle || _hasPropertyActions;
    },

    _importUri: function (miType) {
        return `/centre_ui/${miType}`;
    },

    _elementName: function (miType) {
        return `tg-${miType.substring(miType.lastIndexOf('.') + 1)}-centre`;
    },

    /**
     * Provides the current master entity as the context of the centre, which loads the centre the first time and runs it again afterwards.
     */
    _contextChanged: function (entity, createContextHolder, blocked) {
        if (entity && createContextHolder && !blocked) {
            this._getMasterEntity = () => this.masterEntityContext();
        }
    }
});
