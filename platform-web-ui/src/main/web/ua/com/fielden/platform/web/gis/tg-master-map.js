import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { TgMasterComponentBehavior } from '/resources/master/tg-master-component-behavior.js';

const template = html`
    <style>
        :host {
            display: flex;
            flex-direction: column;
            height: var(--tg-master-map-height, 400px);
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
        .map {
            flex: 1;
            min-height: 0;
            /* keeps the z-indices of map panes and controls within the map, below the blocking pane of the component */
            isolation: isolate;
        }
    </style>
    <div class="caption" hidden$="[[!_hasCaption(propTitle, _hasPropertyActions)]]">
        <span class="caption-title" tooltip-text$="[[propDesc]]">[[propTitle]]</span>
        <slot name="property-action"></slot>
    </div>
    <div id="map" class="map"></div>
`;

/**
 * A map component for entity masters, which draws the master entity, an entity it refers to, or the entities of its collectional property.
 * It is declared in the Entity Master DSL, bound to the entity or in place of the editor of a property:
 *
 *     .addComponent("gis/tg-master-map")
 *         .withAttr("gis-component-uri", "gis/location/tg-location-master-gis-component")
 *         .withAttr("gis-component-name", "LocationMasterGisComponent")
 *         .withAttr("entity-path", "location")
 *
 * The entities are drawn by the GIS component of the application, a subclass of 'MasterGisComponent' exported as 'gis-component-name' from module 'gis-component-uri'.
 * What is drawn is the value at 'property-name' for a component in place of an editor, the value at 'entity-path' for a component bound to the entity,
 * and the entity itself if there is no path; the value is a single entity or a collection of entities, which must be fetched with the master entity.
 *
 * The map is created once it has a size, and is released when the component is detached.
 * The height of the component is '--tg-master-map-height', 400px by default, as editors in a master are laid out to the heights of their content.
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
        this._resizeObserver = new ResizeObserver(() => this._sizeChanged());
        this._resizeObserver.observe(this.$.map);
    },

    detached: function () {
        this._resizeObserver.disconnect();
        this._resizeObserver = null;
        if (this._gis) {
            this._gis.remove();
            this._gis = null;
        }
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
