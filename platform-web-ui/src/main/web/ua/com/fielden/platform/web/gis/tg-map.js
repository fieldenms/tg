import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { allDefined } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            display: inline-block;
            width: 100%;
            height: 100%;
        }
        .map {
            width: 100%;
            height: 100%;
        }
        .progress {
            display: none;
            position: absolute;
            z-index: 1000;
            left: 400px;
            top: 300px;
            width: 200px;
            height: 20px;
            margin-top: -20px;
            margin-left: -100px;
            background-color: #fff;
            background-color: rgba(255, 255, 255, 0.7);
            border-radius: 4px;
            padding: 2px;
        }
        .progress-bar {
            width: 0;
            height: 100%;
            background-color: #76A6FC;
            border-radius: 4px;
        }
        .map-container {
            height: 100%;
            width: 100%;
        }
    </style>
    <div class="map-container">
        <div class="progress">
            <div class="progress-bar"></div>
        </div>
        <div class="map"></div>
    </div>
`;

Polymer({
    _template: template,

    is: 'tg-map',

    properties: {
        /**
         * Binding functional entity. This tg-map component is embedded into entity master with this entity as '_currBindingEntity'.
         */
        entity: {
            type: Object
        },

        /**
         * The entities retrieved when running parent centre of this map embedded into insertion point.
         */
        retrievedEntities: {
            type: Array
        },

        /**
         * Last entity centre selection.
         */
        centreSelection: {
            type: Object,
        },

        /**
         * The event target for custom events.
         */
        customEventTarget: {
            type: Object
        },

        /**
         * The function to map column properties of the entity to the form [{ dotNotation: 'prop1.prop2', value: '56.67'}, ...]. The order is 
         * consistent with the order of columns.
         *
         * @param entity -- the entity to be processed with the mapper function
         */
        columnPropertiesMapper: {
            type: Function,
            notify: true
        },

        /**
         * Property indicating that leaflet map component has been fully initialised.
         * The data should be promoted only when '_initialised' becomes true.
         */
        _initialised: {
            type: Boolean
        }
    },

    observers: [
        '_entityChanged(entity, _initialised)',
        '_retrievedEntitiesChanged(retrievedEntities, _initialised)',
        '_centreSelectionChanged(centreSelection, _initialised)',
        '_columnPropertiesMapperChanged(columnPropertiesMapper, _initialised)'
    ],

    /**
     * Initialises custom map component 'ConcreteGisComponent' if not initialised yet; invalidates map component size otherwise (needed in case where parent component size has been changed).
     */
    initialiseOrInvalidate: function (ConcreteGisComponent) {
        if (this._initialised === true) {
            this._gisComponent._map.invalidateSize();
        } else {
            this._waitForDimensionsAndInit(ConcreteGisComponent);
        }
    },

    /**
     * Waits for dimensions to be initialised and initialises 'ConcreteGisComponent' after that.
     */
    _waitForDimensionsAndInit: function (ConcreteGisComponent) {
        // console.debug('tg-map: _waitForDimensionsAndInit');
        if (this.offsetWidth !== 0 && this.offsetHeight !== 0) {
            new ConcreteGisComponent(this.shadowRoot.querySelector('.map'), this.shadowRoot.querySelector('.progress'), this.shadowRoot.querySelector('.progress-bar'), this);
            this._initialised = true;
        } else {
            this.async(this._waitForDimensionsAndInit.bind(this, ConcreteGisComponent), 100);
        }
    },

    _centreSelectionChanged: function (centreSelection, _initialised) {
        if (!allDefined(arguments)) {
            return;
        }
        this.centreSelectionHandler(centreSelection);
    },

    _columnPropertiesMapperChanged: function (columnPropertiesMapper, _initialised) {
        if (!allDefined(arguments)) {
            return;
        }
        // console.debug('tg-map: columnPropertiesMapper = ', columnPropertiesMapper);
        this.columnPropertiesMapperHandler(columnPropertiesMapper);
    },

    _entityChanged: function (entity, _initialised) {
        if (!allDefined(arguments)) {
            return;
        }
        this.entityHandler(entity);
    },

    entityHandler: function (newEntity) {
    },

    _retrievedEntitiesChanged: function (retrievedEntities, _initialised) {
        if (!allDefined(arguments)) {
            return;
        }
        // console.debug('tg-map: retrievedEntities = ', retrievedEntities);
        this.retrivedEntitiesHandler(retrievedEntities);
    },

    retrivedEntitiesHandler: function (newRetrievedEntities) {
    },

    centreSelectionHandler: function (newCentreSelection) {
    },

    columnPropertiesMapperHandler: function (newColumnPropertiesMapper) {
    },

    getCustomEventTarget: function () {
        return this.customEventTarget || this;
    }
});