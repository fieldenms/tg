import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { L } from '/resources/gis/leaflet/leaflet-lib.js';

import { controlLoading } from '/resources/gis/leaflet/controlloading/leaflet-controlloading-lib.js';
import { LeafletDraw } from '/resources/gis/leaflet/draw/leaflet-draw-lib.js';

const template = html`
    <!--style include="leaflet-styles leaflet-draw-styles leaflet-controlloading-styles"></style-->
    <div class="card">
        <div id="map" style="width: 900px; height: 500px"></div>
    </div>
`;

Polymer({
    _template: template,

    is: 'tg-gis-example-component',

    ready: function () {
        const leafletStyles = document.createElement('custom-style');
        const st = document.createElement('style');
        st.setAttribute('include', 'leaflet-styles leaflet-draw-styles leaflet-controlloading-styles');
        leafletStyles.appendChild(st);
        this.shadowRoot.appendChild(leafletStyles);
        // this.updateStyles();

        const myMap = L.map(this.$.map, {
            center: [51.505, -0.09],
            zoom: 13
        });
        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
            id: 'mapbox.streets'
        }).addTo(myMap);

        const zoomControl = L.control.zoom({
            position: 'topleft'
        });
        myMap.addControl(zoomControl);
        const loadingControl = controlLoading({
            position: 'topleft',
            zoomControl: zoomControl
        });
        myMap.addControl(loadingControl);

        const drawnItems = L.featureGroup().addTo(myMap);
        // leaflet draw controls
        const drawControl = new LeafletDraw({
            position: 'bottomleft',

            edit: {
                featureGroup: drawnItems
            },

            draw: {
                polygon: {
                    shapeOptions: {
                        color: 'purple'
                    },
                    allowIntersection: false,
                    drawError: {
                        color: 'orange',
                        timeout: 1000
                    },
                    showArea: true,
                    metric: true
                }
            }
        });
        myMap.addControl(drawControl);
    }
});