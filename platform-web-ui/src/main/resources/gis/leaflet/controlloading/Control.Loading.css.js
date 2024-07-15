export default String.raw`
.leaflet-control-loading:empty {
    /* Spinner via ajaxload.info, base64-encoded */
    background-image: url(resources/gis/leaflet/controlloading/images/control-loading.gif); /* TG change: replace base64 image with locally downloaded version; this eliminates the need to change Content Security Policy for customers with no 'img-src https: data:;' */
    background-repeat: no-repeat;
}

.leaflet-control-loading,
.leaflet-control-zoom a.leaflet-control-loading,
.leaflet-control-zoomslider a.leaflet-control-loading,
.leaflet-control-layer-container {
    display: none;
}

.leaflet-control-loading.is-loading,
.leaflet-control-zoom a.leaflet-control-loading.is-loading,
.leaflet-control-zoomslider a.leaflet-control-loading.is-loading,
.leaflet-control-layer-container.is-loading {
    display: block;
}

/* Necessary for display consistency in Leaflet >= 0.6 */
.leaflet-bar-part-bottom {
    border-bottom: medium none;
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
}
`;