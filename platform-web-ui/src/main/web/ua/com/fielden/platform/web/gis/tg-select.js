import { L } from '/resources/gis/leaflet/leaflet-lib.js';
import { _isEntity } from '/app/tg-reflector.js';

export const Select = function (_map, _getLayerById, _markerFactory, tgMap, findEntityBy, getLayerByGlobalId) {
    const self = this;

    self._map = _map;
    self._getLayerById = _getLayerById;
    self.findEntityBy = findEntityBy;
    self._markerFactory = _markerFactory;

    self._featureToLeafletIds = {};
    self._prevId = null;
    self.outsideTapDeselectionListener = () => {
        if (self._prevId !== null) {
            self.deselect(self._prevId);
        }
    };
    self._map.on('click', self.outsideTapDeselectionListener);

    /**
     * Indicates whether previously selected EGI entity should be deselected in case where some different map marker gets selected.
     */
    self.deselectPrevEgiEntityOnLayerSelection = false;
    self._tgMap = tgMap;
    self.getLayerIdByEntity = entity => 
        entity.properties && entity.properties.layerId 
            ? entity.properties.layerId
            : (typeof entity.arcGisId !== 'undefined' ? getLayerByGlobalId(entity.get('arcGisId'))._leaflet_id : null);

    self._centreSelectionHandler = function (newSelection) {
        if (newSelection.entities.length == 1) {
            const selectionEntity = newSelection.entities[0];
            const layerId = self.getLayerIdByEntity(selectionEntity.entity);
            if (selectionEntity.select) {
                self._silentlySelectById(layerId);
            } else {
                self._silentlyDeselectById(layerId);
            }
        }
    };
    self._tgMap.centreSelectionHandler = self._centreSelectionHandler;
};

/**
 * Deselects layer with 'layerId'.
 * Also manages EGI selection based on 'deselectPrevEgiEntityOnLayerSelection' option.
 */
Select.prototype.deselect = function (layerId) {
    if (this._prevId !== null && this._prevId === layerId) {
        const details = [];
        const prevId = this._prevId;
        this._prevId = null;
        this._silentlyDeselect(prevId);
        const deselectionDetailOpt = this._generateDeselectionDetailOpt(prevId);
        if (deselectionDetailOpt) {
            details.push(deselectionDetailOpt);
        }
        this._generateSelectionEvent(details, false); // do not scroll EGI when unselection of the entity is performed
    }
};

/**
 * Selects layer with 'layerId'. Performs deselection of previously selected layer, if any.
 * Also manages EGI selection based on 'deselectPrevEgiEntityOnLayerSelection' option.
 */
Select.prototype.select = function (layerId) {
    if (this._prevId !== layerId) {
        const details = [];
        const prevId = this._prevId;
        //selecting entity.
        this._silentlySelect(layerId);
        this._prevId = layerId;
        const entity = this.findEntityBy(this._getLayerById(layerId).feature);
        if (_isEntity(entity)) {
            details.push({
                entity: entity,
                select: true
            });
        }
        
        if (prevId !== null) {
            this._silentlyDeselect(prevId);
            const deselectionDetailOpt = this._generateDeselectionDetailOpt(prevId);
            if (deselectionDetailOpt) {
                details.push(deselectionDetailOpt);
            }
        }
        this._generateSelectionEvent(details, true); // scroll EGI to new selected entity
    }
};

/**
 * Creates optional deselection detail of 'tg-entity-selected' event for EGI deselection.
 * Deselection occurs only in case of 'deselectPrevEgiEntityOnLayerSelection' option.
 */
Select.prototype._generateDeselectionDetailOpt = function (prevId) {
    if (this.deselectPrevEgiEntityOnLayerSelection) {
        const prevEntity = this.findEntityBy(this._getLayerById(prevId).feature);
        if (_isEntity(prevEntity)) {
            return {
                entity: prevEntity,
                select: false
            };
        }
    }
    return null;
};

/**
 * Generates and dispatches 'tg-entity-selected' event from selection / unselection 'details' with option 'shouldScrollToSelected'.
 */
Select.prototype._generateSelectionEvent = function (details, shouldScrollToSelected) {
    if (details.length > 0) {
        const event = new CustomEvent('tg-entity-selected', {
            detail: {
                shouldScrollToSelected: shouldScrollToSelected,
                entities: details
            },
            bubbles: true,
            composed: true,
            cancelable: true
        });
        this._tgMap.getCustomEventTarget().dispatchEvent(event);
    }
};

Select.prototype._silentlySelectById = function (layerId) {
    if (this._prevId !== layerId) {
        const prevId = this._prevId;
        this._silentlySelect(layerId);
        this._prevId = layerId;

        if (prevId !== null) { // at the moment of selecting the feature - there has been other previously selected feature (or, perhaps, the same) 
            this._silentlyDeselect(prevId);
        }
    }
}

Select.prototype._silentlyDeselectById = function (layerId) {
    if (this._prevId === layerId) {
        this._prevId = null;
    }
    this._silentlyDeselect(layerId);
}

Select.prototype._silentlyDeselect = function (layerId) {
    const layer = this._getLayerById(layerId);
    if (layer instanceof this._markerFactory.ArrowMarker || layer instanceof this._markerFactory.CircleMarker) {
        layer.setSelected(false);
    }
}

Select.prototype._silentlySelect = function (layerId) {
    const layer = this._getLayerById(layerId);
    if (layer instanceof this._markerFactory.ArrowMarker || layer instanceof this._markerFactory.CircleMarker) {
        this._map.panTo(layer.getLatLng(), { animate: true, duration: 2.25 }); // centering of the marker (fitToBounds is not needed)
        layer.setSelected(true);
    } else if (layer instanceof L.Polygon) {
        this._map.fitBounds(layer.getBounds());
        // map.panTo(layer.getBounds().getCenter()); // fitToBounds?
    }
}