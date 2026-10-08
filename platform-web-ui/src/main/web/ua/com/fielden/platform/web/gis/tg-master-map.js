import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';
import '/resources/egi/tg-responsive-toolbar.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { TgMasterComponentBehavior } from '/resources/master/tg-master-component-behavior.js';

const template = html`
    <style>
        :host {
            display: flex;
            flex-direction: column;
        }
        .caption {
            min-height: 24px;
            line-height: 24px;
            font-size: 12px;
            color: var(--paper-input-container-color, var(--secondary-text-color));
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            cursor: default;
        }
        .caption[hidden] {
            display: none;
        }
        /* the toolbar under the caption, which moves the actions that do not fit into a dropdown, as the toolbar of the rich text editor */
        .toolbar {
            flex-shrink: 0;
            padding-bottom: 4px;
            /* the sizes of property actions, which the toolbar can move beyond the styles of the master */
            --tg-ui-action-icon-button-width: 24px;
            --tg-ui-action-icon-button-height: 24px;
            --tg-ui-action-icon-button-padding: 4px;
            --tg-ui-action-spinner-width: 20px;
            --tg-ui-action-spinner-height: 20px;
            --tg-ui-action-spinner-min-width: 20px;
            --tg-ui-action-spinner-min-height: 20px;
            --tg-ui-action-spinner-max-width: 20px;
            --tg-ui-action-spinner-max-height: 20px;
            --tg-ui-action-spinner-padding: 0px;
            --tg-ui-action-spinner-margin-left: 0;
            --tg-responsove-toolbar-expand-button: {
                padding: 4px;
                width: 24px;
                height: 24px;
                color: var(--paper-input-container-color, var(--secondary-text-color));
            };
            --tg-responsove-toolbar-dropdown-content: {
                padding: 4px;
            };
        }
        .toolbar[hidden] {
            display: none;
        }
        .map-container {
            flex: 1;
            min-height: 0;
            position: relative;
        }
        .map {
            /* the map is out of flow, so that its content, which is unstyled until the Leaflet styles apply in a later frame, never changes the size of the component */
            position: absolute;
            inset: 0;
            /* keeps the z-indices of map panes and controls within the map, below the blocking pane of the component */
            isolation: isolate;
        }
    </style>
    <div class="caption" hidden$="[[!propTitle]]" tooltip-text$="[[propDesc]]">[[propTitle]]</div>
    <tg-responsive-toolbar id="toolbar" class="toolbar custom-responsive-toolbar" hidden$="[[!_hasToolbar(_hasPropertyActions, labelsToggle)]]">
        <paper-icon-button slot="entity-specific-action" class="entity-specific-action" style$="[[_labelsToggleStyle(_labelsShown)]]" icon="icons:label" toggles active="{{_labelsShown}}" hidden$="[[!labelsToggle]]" tooltip-text="Show / hide map labels."></paper-icon-button>
        <slot slot="entity-specific-action" class="entity-specific-action" name="property-action"></slot>
    </tg-responsive-toolbar>
    <div class="map-container">
        <div id="map" class="map"></div>
    </div>
`;

/**
 * A map component for entity masters, which draws the master entity, an entity it refers to, or the entities of its collectional property.
 * It is declared in the Entity Master DSL, bound to the entity or in place of the editor of a property:
 *
 *     .addComponent("gis/tg-master-map")
 *         .withAttr("gis-component-uri", "gis/location/tg-location-gis-master-component")
 *         .withAttr("gis-component-name", "LocationGisMasterComponent")
 *         .withAttr("entity-path", "location.gisInfo")
 *
 * The entities are drawn by the GIS component of the application, a subclass of 'MasterGisComponent' exported as 'gis-component-name' from module 'gis-component-uri'.
 * What is drawn is the value at 'property-name' for a component in place of an editor, the value at 'entity-path' for a component bound to the entity,
 * and the entity itself if there is no path; the value is a single entity or a collection of entities, which must be fetched with the master entity.
 *
 * The map is created once it has a size, and is released when the component is detached.
 * The component fills the height of its layout cell, so that it grows with the master in a flexible row of the master layout, such as 'FLEXIBLE_ROW' of 'LayoutComposer'.
 * Its height is not less than 'min-height', which is also its height in a row laid out to the heights of its content.
 *
 * The title of the map is followed by a responsive toolbar, which moves the actions that do not fit into a dropdown.
 * The toolbar has the property actions, and a toggle that shows and hides the labels of map features, as the labels toggle of centre maps, if attribute 'labels-toggle' is set.
 * A property action reaches the map as 'action.masterMap', wherever the toolbar places the action, and the GIS component as 'action.masterMap.gisComponent' once the map is created.
 */
Polymer({
    _template: template,

    is: 'tg-master-map',

    behaviors: [TgMasterComponentBehavior],

    properties: {

        /**
         * The path of the module with the GIS component, relative to '/resources/' and without the '.js' extension.
         */
        gisComponentUri: {
            type: String
        },

        /**
         * The name under which the module exports the GIS component.
         */
        gisComponentName: {
            type: String
        },

        /**
         * The dot-notated path from the entity to the entity or entities to draw, for a component bound to the entity; the entity itself is drawn if empty.
         */
        entityPath: {
            type: String,
            value: ''
        },

        /**
         * The property whose entity or entities are drawn, for a component in place of an editor.
         */
        propertyName: {
            type: String,
            value: ''
        },

        /**
         * The zoom for a single point, and the greatest zoom for fitting several features.
         */
        zoom: {
            type: Number,
            value: 14
        },

        /**
         * The least height of the component, as a CSS length.
         */
        minHeight: {
            type: String,
            value: '200px',
            observer: '_minHeightChanged'
        },

        /**
         * Indicates whether the toolbar has a toggle that shows and hides the labels of map features.
         */
        labelsToggle: {
            type: Boolean,
            value: false
        },

        /**
         * The GIS component that draws the map, or null while the map is not created.
         */
        gisComponent: {
            type: Object,
            value: null,
            readOnly: true
        },

        _labelsShown: {
            type: Boolean,
            value: true,
            observer: '_labelsShownChanged'
        },

        _hasPropertyActions: {
            type: Boolean,
            value: false
        }
    },

    observers: [
        '_entitiesChanged(entity, entityPath, propertyName)'
    ],

    ready: function () {
        this._hasPropertyActions = this._propertyActions.length > 0;
        // the toolbar moves the actions that do not fit into its dropdown, so the parent element of an action is not always this map
        this._propertyActions.forEach(action => action.masterMap = this);
    },

    attached: function () {
        this._gisComponentClass = this._gisComponentClass || this._loadGisComponentClass();
        this._resizeObserver = new ResizeObserver(() => this._sizeChangedAsync());
        this._resizeObserver.observe(this.$.map);
    },

    detached: function () {
        this._resizeObserver.disconnect();
        this._resizeObserver = null;
        cancelAnimationFrame(this._sizeChangedFrame);
        if (this.gisComponent) {
            this.gisComponent.remove();
            this._setGisComponent(null);
        }
    },

    _minHeightChanged: function (minHeight) {
        this.style.minHeight = minHeight;
    },

    _labelsShownChanged: function (labelsShown) {
        if (this.gisComponent) {
            this.gisComponent.showLabels(labelsShown);
        }
    },

    _hasToolbar: function (_hasPropertyActions, labelsToggle) {
        return _hasPropertyActions || labelsToggle;
    },

    /**
     * The style of the labels toggle, which is inline, as the toolbar can move the toggle into its dropdown, beyond the styles of this component.
     * The toggle has the size of property actions, and a border while the labels are shown, as the labels toggle of centre maps.
     */
    _labelsToggleStyle: function (labelsShown) {
        return `width:24px;height:24px;padding:2px;box-sizing:border-box;border-radius:50%;border:2px solid ${labelsShown ? 'currentColor' : 'transparent'};`;
    },

    _entitiesChanged: function (entity, entityPath, propertyName) {
        this._entities = this._entitiesToShow(entity, propertyName || entityPath);
        if (this.gisComponent) {
            this.gisComponent.show(this._entities);
        }
    },

    /**
     * Returns the entities at 'path' of 'entity', or 'entity' itself for an empty path, as an array.
     */
    _entitiesToShow: function (entity, path) {
        if (!entity) {
            return [];
        }
        const value = path ? entity.get(path) : entity;
        if (value === null || typeof value === 'undefined') {
            return [];
        }
        return Array.isArray(value) ? value : [value];
    },

    /**
     * Handles a change of size of the map in the next animation frame, as creating or updating the map changes the DOM,
     * which must not happen in a 'ResizeObserver' callback to avoid error 'ResizeObserver loop completed with undelivered notifications'.
     */
    _sizeChangedAsync: function () {
        cancelAnimationFrame(this._sizeChangedFrame);
        this._sizeChangedFrame = requestAnimationFrame(() => this._sizeChanged());
    },

    /**
     * Creates the GIS component once the map has a size, and updates the map after later changes of size; the map has no size while hidden.
     * The toolbar is resized with the map, as they have the same width.
     */
    _sizeChanged: function () {
        const mapDiv = this.$.map;
        if (mapDiv.offsetWidth === 0 || mapDiv.offsetHeight === 0) {
            return;
        }
        if (!this.$.toolbar.hidden) {
            this.$.toolbar.notifyResize();
        }
        if (this.gisComponent) {
            this.gisComponent.invalidateSize();
        } else if (!this._creatingGis) {
            this._creatingGis = true;
            this._gisComponentClass.then(GisComponentClass => {
                this._creatingGis = false;
                if (GisComponentClass && this.isConnected && !this.gisComponent) {
                    // the GIS component is kept as soon as it creates the map, so that an error in drawing never leads to a second map in the same element
                    this._setGisComponent(new GisComponentClass(mapDiv, this));
                    this.gisComponent.showLabels(this._labelsShown);
                    this.gisComponent.show(this._entities || []);
                }
            });
        }
    },

    /**
     * Loads the GIS component class, resolving to null if it cannot be loaded, in which case the component shows no map.
     */
    _loadGisComponentClass: function () {
        const uri = this.gisComponentUri, name = this.gisComponentName;
        if (!uri || !name) {
            console.error(`tg-master-map: attributes 'gis-component-uri' and 'gis-component-name' are required, but are [${uri}] and [${name}].`);
            return Promise.resolve(null);
        }
        return import(`/resources/${uri}.js`).then(module => {
            if (typeof module[name] !== 'function') {
                console.error(`tg-master-map: module [/resources/${uri}.js] does not export GIS component [${name}].`);
                return null;
            }
            return module[name];
        }).catch(error => {
            console.error(`tg-master-map: module [/resources/${uri}.js] with GIS component [${name}] cannot be loaded.`, error);
            return null;
        });
    }
});
