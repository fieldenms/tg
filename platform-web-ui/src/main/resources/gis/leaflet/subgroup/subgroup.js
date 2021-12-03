/**
 * This is GeoJSON variation for https://github.com/ghybs/Leaflet.FeatureGroup.SubGroup plugin.
 * The only change (beyond GeoJSON extension instead of FeatureGroup) was in 'initialize' method.
 */
L.GeoJSON.SubGroup = L.GeoJSON.extend({ // was L.FeatureGroup.SubGroup = L.FeatureGroup.extend

    /**
     * Instantiates a SubGroup.
     */
    initialize: function (parentGroup, geojson, options) { // was (parentGroup, layersArray)
        L.GeoJSON.prototype.initialize.call(this, geojson, options); // was (this, layersArray)

        this.setParentGroup(parentGroup);
    },

    /**
     * Changes the parent group into which child markers are added to /
     * removed from.
     * @param parentGroup (L.LayerGroup)
     * @returns {SubGroup} this
     */
    setParentGroup: function (parentGroup) {
        var pgInstanceOfLG = parentGroup instanceof L.LayerGroup;

        this._parentGroup = parentGroup;

        // onAdd
        this.onAdd =
            pgInstanceOfLG ?
                (
                    typeof parentGroup.addLayers === "function" ?
                        this._onAddToGroupBatch :
                        this._onAddToGroup
                ) :
                this._onAddToMap;

        // onRemove
        this.onRemove =
            pgInstanceOfLG ?
                (
                    typeof parentGroup.removeLayers === "function" ?
                        this._onRemoveFromGroupBatch :
                        this._onRemoveFromGroup
                ) :
                this._onRemoveFromMap;

        // addLayer
        this.addLayer = pgInstanceOfLG ?
            this._addLayerToGroup :
            this._addLayerToMap;

        // removeLayer
        this.removeLayer = pgInstanceOfLG ?
            this._removeLayerFromGroup :
            this._removeLayerFromMap;

        return this;
    },

    /**
     * Removes the current sub-group from map before changing the parent
     * group. Re-adds the sub-group to map if it was before changing.
     * @param parentGroup (L.LayerGroup)
     * @returns {SubGroup} this
     */
    setParentGroupSafe: function (parentGroup) {
        var map = this._map;

        if (map) {
            map.removeLayer(this);
        }

        this.setParentGroup(parentGroup);

        if (map) {
            map.addLayer(this);
        }

        return this;
    },

    /**
     * Returns the current parent group.
     * @returns {*}
     */
    getParentGroup: function () {
        return this._parentGroup;
    },


    // For parent groups with batch methods (addLayers and removeLayers)
    // like MarkerCluster.
    _onAddToGroupBatch: function (map) {
        var layersArray = this.getLayers();

        this._map = map;
        this._parentGroup.addLayers(layersArray);
    },

    _onRemoveFromGroupBatch: function () {
        var layersArray = this.getLayers();

        this._parentGroup.removeLayers(layersArray);
        this._map = null;
    },


    // For other parent layer groups.
    _onAddToGroup: function (map) {
        var parentGroup = this._parentGroup;

        this._map = map;
        this.eachLayer(parentGroup.addLayer, parentGroup);
    },

    _onRemoveFromGroup: function () {
        var parentGroup = this._parentGroup;

        this.eachLayer(parentGroup.removeLayer, parentGroup);
        this._map = null;
    },


    // Defaults to standard FeatureGroup behaviour when parent group is not
    // specified or is not a type of LayerGroup.
    _onAddToMap: L.FeatureGroup.prototype.onAdd,
    _onRemoveFromMap: L.FeatureGroup.prototype.onRemove,


    _addLayerToGroup: function (layer) {
        if (this.hasLayer(layer)) {
            return this;
        }

        layer.addEventParent(this);

        var id = this.getLayerId(layer);

        this._layers[id] = layer;

        if (this._map) {
            // Add to parent group instead of directly to map.
            this._parentGroup.addLayer(layer);
        }

        return this.fire("layeradd", {layer: layer});
    },

    _removeLayerFromGroup: function (layer) {
        // If unknown layer, skip.
        if (!this.hasLayer(layer)) {
            return this;
        }

        // Retrieve the layer id.
        var id = layer in this._layers ? layer : this.getLayerId(layer);

        // Retrieve the layer from this._layer.
        layer = this._layers[id];

        // Unregister from events parent.
        layer.removeEventParent(this);

        if (this._map && layer) {
            // Remove from parent group instead of directly from map.
            this._parentGroup.removeLayer(layer);
        }

        delete this._layers[id];

        return this.fire("layerremove", {layer: layer});
    },

    // Defaults to standard FeatureGroup behaviour when parent group is not
    // specified or is not a type of LayerGroup.
    _addLayerToMap: L.FeatureGroup.prototype.addLayer,
    _removeLayerFromMap: L.FeatureGroup.prototype.removeLayer

});