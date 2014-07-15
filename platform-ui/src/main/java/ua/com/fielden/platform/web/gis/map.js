			var progress = document.getElementById('progress');
			var progressBar = document.getElementById('progress-bar');

			// var iteration = 1;

			function updateProgressBar(processed, total, elapsed, layersArray) {
				log("updateProgressBar(processed = " + processed + ", total = " + total + ", elapsed = " + elapsed + ",layersArray);");

				if (elapsed > 0) { // 1000
					// if it takes more than a second to load, display the progress bar:
					progress.style.display = 'block';
					progressBar.style.width = Math.round(processed/total*100) + '%';

					// if (elapsed > 500) {
					// 	map.fitBounds(markersClusterGroup.getBounds());												
					// }

					// if (elapsed > 1500 * iteration) {
					// 	map.fitBounds(markersClusterGroup.getBounds());						
					// 	iteration = iteration + 1;
					// }
				}

				if (processed && (processed === total)) {
					// all markers processed - hide the progress bar:
					progress.style.display = 'none';

					map.fitBounds(markersClusterGroup.getBounds());
					
					// iteration = 1;
				}
			}

			var osmLink = '<a href="http://openstreetmap.org">OpenStreetMap</a>',
				thunLink = '<a href="http://thunderforest.com/">Thunderforest</a>';

			var osmUrl = 'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
				osmAttrib = '&copy; ' + osmLink + ' Contributors',
				landUrl = 'http://{s}.tile.thunderforest.com/landscape/{z}/{x}/{y}.png',
				thunAttrib = '&copy; ' + osmLink + ' Contributors & ' + thunLink;

			// initialise different layers for different tile providers
			var osmMap = L.tileLayer(osmUrl, {
				    maxZoom: 19,
				    minZoom: 0,
					attribution: osmAttrib
				});
			var	landMap = L.tileLayer(landUrl, {
				    maxZoom: 18,
				    minZoom: 0,
					attribution: thunAttrib
				});
			// var bingMap = new L.BingLayer("YOUR_BING_API_KEY"); -- need an API key to use it 
			// http://stackoverflow.com/questions/14442055/use-bing-maps-tiles-with-leaflet
			var	googleRoadMap = new L.Google('ROADMAP', {
				    maxZoom: 21,
				    minZoom: 0
				});
			var	googleSatelliteMap = new L.Google('SATELLITE', {
				    maxZoom: 19,
				    minZoom: 0
				});
			var	googleHybridMap = new L.Google('HYBRID', {
				    maxZoom: 19,
				    minZoom: 0
				});
			var	googleTerrainMap = new L.Google('TERRAIN', {
				    maxZoom: 15,
				    minZoom: 0
				});
			var yandexRoadMap = new L.Yandex('map', {
				    maxZoom: 18,
				    minZoom: 0
				});
			var yandexHybridMap = new L.Yandex('hybrid', {
				    maxZoom: 19,
				    minZoom: 0
				});
			
			var ytraffic = new L.Yandex("null", {
				traffic:true, 
				opacity:0.8, 
				overlay:true
			});
 
			var map = L.map('map', {
				layers: [osmMap], // only add one!
				zoomControl: false,
                // Tell the map to use a loading control
                loadingControl: false
			})
			.setView([49.841919, 24.0316], 18); // Lviv (Rynok Sq) has been centered
 
			map.fire('dataloading');

			var baseLayers = {
				"OpenStreetMap": osmMap,
				"Landscape": landMap,
				// "Bing": bingMap,				
				"Yandex Roadmap": yandexRoadMap,
				"Yandex Hybrid": yandexHybridMap,
				"Google Roadmap": googleRoadMap,
				"Google Sattelite": googleSatelliteMap,
				"Google Hybrid": googleHybridMap,
				"Google Terrain": googleTerrainMap				
			};

			// ADD REGULAR LAYER BASED ON SIMPLE PRIMITIVES
			var gpsTracksOverlay = new L.LayerGroup();
			L.marker([49.836163,24.067783])
				.bindPopup('Te Papa').addTo(gpsTracksOverlay),
			L.marker([49.839817,24.050446])
				.bindPopup('Embassy Theatre').addTo(gpsTracksOverlay),
			L.marker([49.838488,24.035339])
				.bindPopup('Michael Fowler Centre').addTo(gpsTracksOverlay),
			L.marker([49.844134,24.025555])
				.bindPopup('Leuven Belgin Beer Cafe').addTo(gpsTracksOverlay);

			var polyline = L.polyline([
				[49.844134,24.025555],
				[49.838488,24.035339],
				[49.839817,24.050446],
				[49.836163,24.067783]
			], {
				weight: 15,
				opacity: 1.0,
				//noClip: true,
				//clickable: true,
				fill: true,
				//"pointer-events": "none"
			});

			polyline.on('mouseover', function() {
            	log("polyline mouseover (entered):");
          	});
			polyline.on('click', function() {
            	log("polyline click:");
           	});

           	polyline.bindPopup('Polyline popup.');

			polyline.addTo(gpsTracksOverlay);

			// ADD REGULAR LAYER BASED ON GEOJSON FEATURES
			// if (!geoJsonFeatures) {
			// var geoJsonFeatures = [{
			// 	    "type": "LineString",
			// 	    "coordinates": [
			// 	    	[24.03, 49.835],
			// 			[24.02, 49.84 ],
			// 			[24.05, 49.845]
			// 		],
			// 		"properties": {
			// 			"popupContent": "FIRST FEATURE"	
			// 		}					
			// 	}, {
			// 	    "type": "LineString",
			// 	    "coordinates": [
			// 	    	[24.04, 49.835],
			// 			[24.03, 49.84],
			// 			[24.06, 49.845]
			//  	   ],
			// 		"properties": {
			// 	 	    "popupContent": "SECOND FEATURE"
			// 	 	}
			//  	}
			// ];
			// }

			// var geoJsonStyle = {
			//     "color": "blue",
			//     "weight": 5,
			//     "opacity": 0.65
			// };

			function getColor(feature) {
				if (feature && feature.geometry && feature.geometry.type) {
					if (feature.properties && feature.properties.what && feature.properties.what === "circle") {
						return "#FF4500";	 //  orange
					} else if (feature.geometry.type === "LineString") {
						return "blue";	
					} else if (feature.geometry.type === "Point") {
						if (feature.properties && feature.properties.vectorSpeed) {
							return (feature.properties.vectorSpeed > 0) ? "blue" : "red";	
						} 
					} else if (feature.geometry.type === "Polygon") {
						return "purple";	
					}
					return "white";
				}
			}

			function geoJsonStyle(feature) {
 			    return {
        			// fillColor: getColor(feature),
        			weight: 5,
			        opacity: 0.65,
        			color: getColor(feature)
			        // dashArray: '3',
			        // fillOpacity: 0.7
    			};
			}

			// var ArrowIcon = L.Icon.extend({
   // 				options: {
   //      			// shadowUrl: 'leaf-shadow.png',
   //      			// iconSize:     [38, 95],
   //      			// shadowSize:   [50, 64],
   //      			// iconAnchor:   [22, 94],
   //      			iconAnchor:   [12, 12],
   //      			// shadowAnchor: [4, 62],
   //      			// popupAnchor:  [-3, -76]
   //      			popupAnchor:  [0, 0]        			
   //  			}
			// });

			// var arrowIcon = new ArrowIcon({
			// 	iconUrl: 'arrow-blue.png'
			// });

			var arrowIcon = new L.Icon({
				iconUrl: 'arrow-blue.png',
				iconAnchor:   [12, 12],
				popupAnchor:  [0, 0]        			
			});

			var arrowIconSelected = new L.Icon({
				iconUrl: 'arrow-green.png',
				iconAnchor:   [12, 12],
				popupAnchor:  [0, 0]        			
			});

			var CircleIcon = L.Icon.extend({
   				options: {
        			iconAnchor:   [12, 12],
        			popupAnchor:  [0, 0]        			
    			}
			});

			var TriangleIcon = L.Icon.extend({
   				options: {
        			iconAnchor:   [12, 12],
        			popupAnchor:  [0, 0]        			
    			}
			});

			var circleIcon = new CircleIcon({
				iconUrl: 'circle-red.png'
			});
			var circleIconSelected = new CircleIcon({
				iconUrl: 'circle-orange.png'
			});
			var triangleIcon = new TriangleIcon({
				iconUrl: 'triangle.png'
			});

			var CircleMarker = L.Marker.extend({
				options: {
					icon: circleIcon,
					title: "BlaBla",
					riseOnHover: true,
					riseOffset: 1000,
					zIndexOffset: 750 // high value to make the circles always on top				
				}
			});

			var ArrowMarker = L.RotatedMarker.extend({
				options: {
		    		icon: arrowIcon,
		    		title: "BlaBla",
					riseOffset: 1000,
		    		riseOnHover: true
				}
			});

			var CoordMarker = L.Marker.extend({
				options: {
					icon: triangleIcon,
					title: "BlaBla",
					riseOnHover: true,
					riseOffset: 1000,
					zIndexOffset: 750 // high value to make the circles always on top				
				}
			});

			var markersClusterGroup = L.markerClusterGroup({ 
				chunkedLoading: true, 
				chunkProgress: updateProgressBar,
				disableClusteringAtZoom: 17, // 17
				maxClusterRadius: function(zoom) {
					return 60;
				},
				iconCreateFunction: function(cluster) {
					// log("Creating cluster:");
					// log(cluster);
     				// return new L.DivIcon({ html: '<b>' + cluster.getChildCount() + '</b>' });
					var markers = cluster.getAllChildMarkers();					
					var chosenMarker; // = markers[0];
					var count = markers.length;
					for (var i = 0; i < count; i++) {
						if (markers[i] instanceof ArrowMarker) { // arrow marker
					
						} else { // circle marker
							// chosenMarker = markers[i]; // use first found circle marker as the marker for the cluster
							// return circleIcon; // new CircleMarker(chosenMarker.getLatLng());

							return L.divIcon({ // TODO !!!!!!!!!!!!!!! circle-red.png circle-purple.png circle-orange.png
								html: '<div class="img-overlay"><img src="circle-purple.png" /><div class="overlay" >&#x2194</div></div>',
								className: 'gps-marker-cluster',
								iconSize: new L.Point(24, 24)
							});
						}
					}
					if (!chosenMarker) {
						chosenMarker = markers[Math.floor(count / 2)]; // use middle arrow marker as the marker for the cluster when no circle markers exist
					}
					// var arrowMarker = new ArrowMarker(chosenMarker.getLatLng()); // L.divIcon({ html: n, className: 'mycluster', iconSize: L.point(40, 40) });
					// log("arrowMarker: ");					
					// log(arrowMarker);					

					// var m = new L.Marker(getRandomLatLng(map), { 
					// 	icon: L.divIcon({
					// 		html: '<img src="marker-icon.png" style="-webkit-transform: rotate(39deg)" />'
					// 	})
					// });

					// return arrowIcon; // arrowMarker;

					return L.divIcon({
						html: '<div class="img-overlay"><img src="arrow-blue.png" style="-webkit-transform: rotate(' + 
					 					chosenMarker.options.angle + 
					 					'deg); " /><div class="overlay" style="-webkit-transform: rotate(' + 
					 					(chosenMarker.options.angle + 90) + 
					 					'deg); ">&#x2194</div></div>', // ' + cluster.getChildCount() + ' &#x21FF
						className: 'gps-marker-cluster',
						iconSize: new L.Point(24, 24)
					});

					// return L.divIcon({ // TODO consider crossbrowser rotation issues (see Marker.Rotate.js for more details)
					// 	html: '<img src="arrow-blue.png" style="-webkit-transform: rotate(' + 
					// 				chosenMarker.options.angle + 
					// 		'deg); position:absolute; top:-6px; left:-6px;" />',
					// 	className: 'gps-marker-cluster',
					// 	iconSize: new L.Point(80, 80)
					// }); 
    			}
			});

			// var myIcon = L.divIcon({
   //          	// Specify a class name we can refer to in CSS.
   //          	className: 'count-icon',
   //          	// Define what HTML goes in each marker.
	  //           html: i,
   //  	        // Set a markers width and height.
   //          	iconSize: [40, 40]
   //      	});

			// var geojsonMarkerOptions = {
			// 	icon: myIcon,
  	// 			radius: 8,
			//     fillColor: "#ff7800",
			//     color: "#000",
			//     weight: 1,
			//     opacity: 1,
			//     fillOpacity: 0.8
			// };

			var featureToLeafletIds = {};
			var prevId;

			function log(object) {
				//java.println(object);
				console.log(object);
			}

			function deselectVisuallyWithoutEventFiring(featureId) {
				var leafletId = featureToLeafletIds["" + featureId + ""];
				var layer = geoJsonOverlay.getLayer(leafletId);
				log("deselectVisuallyWithoutEventFiring: featureId = " + featureId);
				log(layer);
				if (featureId === "track-line-string-id") {
					// do nothing
				} else {
					if (layer instanceof ArrowMarker) {
						layer.setIcon(arrowIcon);
						layer.setZIndexOffset(0); // return to previous zIndexOffset which make the marker of default priority based on the latitude
					} else if (layer instanceof CircleMarker) {
						layer.setIcon(circleIcon);
						layer.setZIndexOffset(750); // return to previous zIndexOffset which make the marker of high priority (zero speed)
					} else {
					}					
				}
			}

			function selectVisuallyWithoutEventFiring(featureId) {
				var leafletId = featureToLeafletIds["" + featureId + ""];
				var layer = geoJsonOverlay.getLayer(leafletId);
				log("selectVisuallyWithoutEventFiring: feautureId = " + featureId);
				log(layer);

				if (featureId === "track-line-string-id") {
					// do nothing
				} else {
					if (layer instanceof ArrowMarker) {
						map.panTo(layer.getLatLng()); // centering of the marker (fitToBounds is not needed)	
						layer.setIcon(arrowIconSelected);
						layer.setZIndexOffset(1000); // selected marker has the highest priority
					} else if (layer instanceof CircleMarker) {
						map.panTo(layer.getLatLng()); // centering of the marker (fitToBounds is not needed)	
						layer.setIcon(circleIconSelected);
						layer.setZIndexOffset(1000); // selected marker has the highest priority
					} else if (layer instanceof L.Polygon) {
						map.fitBounds(layer.getBounds());
						// map.panTo(layer.getBounds().getCenter()); // fitToBounds?
					} else {
					}
				}
			}
	
			function selectTabularlyWithoutEventFiring(featureId) {
				log("selectTabularlyWithoutEventFiring: feautureId = " + featureId);
				// java.selectTabularlyWithoutEventFiring(featureId);
			}

			function selectById(featureId) {
				log("selectById(" + featureId + ");");
				if (prevId) { // at the moment of selecting the feature - there has been other previously selected feature (or, perhaps, the same) 
					deselectVisuallyWithoutEventFiring(prevId);
				}
				selectVisuallyWithoutEventFiring(featureId);
				selectTabularlyWithoutEventFiring(featureId);
				prevId = featureId;
			}

			var geoJsonOverlay = L.geoJson([], {
			    style: geoJsonStyle,

			    pointToLayer: function (feature, latlng) {
			    	log(feature, latlng);
			    	if (feature.properties && feature.properties.vectorSpeed) { 
				    	return new ArrowMarker(latlng, {
				    		angle: ((feature.properties && feature.properties.vectorAngle) ? (feature.properties.vectorAngle - 180) : 0)
				    	});
			    	} else if (feature.properties && feature.properties.stuff) { 
			    		var coordMarker = new CoordMarker(latlng) // , {
			    		// 	opacity: 0.0
			    		// });
			        	// coordMarker.setZIndexOffset(-1000);
				    	return coordMarker;
			    	} else {
			    		return new CircleMarker(latlng);
			    	}
			    },

// 		        pointToLayer: function (feature, latlng) {
//     			    return L.marker(latlng, {
// 						icon: L.divIcon({
//             				// Specify a class name we can refer to in CSS.
//             				className: 'count-icon',
//             				// Define what HTML goes in each marker.
// 	            			// html: ((feature.properties && feature.properties.popupContent) ? feature.properties.popupContent : "UNKNOWN"),
// 	            			html: "<svg height=&quot;210&quot; width=&quot;500&quot;>
//   <polygon points=&quot;100,10 40,180 190,60 10,60 160,180&quot;
//   style=&quot;fill:lime;stroke:purple;stroke-width:5;fill-rule:nonzero;&quot; />
// </svg> "
//     	        			// Set a markers width and height.
//             				iconSize: [20, 20]
//         				}),
//   						radius: 8,
// 			    		fillColor: "#ff7800",
// 			    		color: "#000",
// 			    		weight: 1,
// 			    		opacity: 1,
// 			    		fillOpacity: 0.8
// 					}); // circleMarker
//     			},

			    onEachFeature: function (feature, layer) {
					var featureId = feature.id;
   	            	featureToLeafletIds["" + featureId + ""] = geoJsonOverlay.getLayerId(layer);	

   	            	log("onEachFeature featureId = " + featureId + " ");	            
  	            	log(layer);	            

  	            	layer.on('mouseover', function() {
    	            	log("mouseover (entered):");
    	            	log(layer); 	            		
  	            	});

  	            	layer.on('mouseout', function() {
    	            	log("mouseout (leaved):");
    	            	log(layer); 	            		
  	            	});

		            layer.on('click', function() { // dblclick
    	            	// alert("Hi!");
    	            	log("clicked:");
    	            	log(layer);

						selectById(featureId);
	            	});
	            	// if (layer instanceof CoordMarker) {
	            	// 	layer.setOpacity(0.0);
	            	// }

 			    	// does this feature have a property named popupContent?
				    if (feature.properties && feature.properties.popupContent) {
	       				layer.bindPopup(feature.properties.popupContent);	        			
			    	}
				}
			});
			log(geoJsonFeatures);
			geoJsonOverlay.addData(geoJsonFeatures);
			markersClusterGroup.addLayer(geoJsonOverlay);
			map.addLayer(markersClusterGroup);

			// firebug control
/*			var firebugControl = new L.easyButton(
				"fa-fire",
				function() {
					if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}
				},
				"Firebug",
				map
			);
*/
			// fitToBounds control
			var fitToBoundsControl = new L.easyButton(
				"fa-compress",
				function() {
					map.fitBounds(markersClusterGroup.getBounds());
				},
				"Fit to bounds",
				map
			);

		    // Add our zoom control manually where we want to
            var zoomControl = L.control.zoom({
                position: 'topleft'
            });
            map.addControl(zoomControl);

            // Add our loading control in the same position and pass the
            // zoom control to attach to it
            var loadingControl = L.Control.loading({
                position: 'topleft',
                zoomControl: zoomControl
            });
            map.addControl(loadingControl);

  			// scale control on the left bottom of the map
			var scaleControl = L.control.scale({
			    imperial: false,
			    position: 'bottomleft'
			});
			map.addControl(scaleControl);

			// ADD LEAFLET.DRAW
			// var drawnItems = new L.FeatureGroup();
			// map.addLayer(drawnItems);

			var drawControl = new L.Control.Draw({
				position: 'bottomleft',

				edit: {
					featureGroup: markersClusterGroup // drawnItems
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
			map.addControl(drawControl);

			map.on('draw:created', function (e) {
				var type = e.layerType,
					layer = e.layer;
				markersClusterGroup.addLayer(layer);
			});

			// GATHER ALL LAYERS
			var overlays = {
				"GEO-json": markersClusterGroup,
				"GPS-tracks": gpsTracksOverlay,
				"Traffic": ytraffic
			};
 			
			L.control.layers(baseLayers, overlays).addTo(map); 

			map.fire('dataload');
