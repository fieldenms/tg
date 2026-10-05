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
            cursor: default;
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
    <div class="caption" hidden$="[[!_hasCaption(propTitle, _hasPropertyActions)]]">
        <span class="caption-title" tooltip-text$="[[propDesc]]">[[propTitle]]</span>
        <slot name="property-action"></slot>
    </div>
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
        if (this._gis) {
            this._gis.remove();
            this._gis = null;
        }
    },

    _minHeightChanged: function (minHeight) {
        this.style.minHeight = minHeight;
    },

    _hasCaption: function (propTitle, _hasPropertyActions) {
        return !!propTitle || _hasPropertyActions;
    },

    _entitiesChanged: function (entity, entityPath, propertyName) {
        this._entities = this._entitiesToShow(entity, propertyName || entityPath);
        if (this._gis) {
            this._gis.show(this._entities);
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
     */
    _sizeChanged: function () {
        const mapDiv = this.$.map;
        if (mapDiv.offsetWidth === 0 || mapDiv.offsetHeight === 0) {
            return;
        }
        if (this._gis) {
            this._gis.invalidateSize();
        } else if (!this._creatingGis) {
            this._creatingGis = true;
            this._gisComponentClass.then(GisComponentClass => {
                this._creatingGis = false;
                if (GisComponentClass && this.isConnected && !this._gis) {
                    this._gis = new GisComponentClass(mapDiv, this);
                    this._gis.show(this._entities || []);
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
