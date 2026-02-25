/* main-app related resources */
// This import is required to ensure the SSE initialisation upon loading of a web client
import '/resources/components/tg-event-source.js';
// Load styles.
import '/resources/polymer/@polymer/paper-styles/paper-styles.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
// Load components.
// IMPORTANT: The following import for rich-text styles must always occur before any other component import.
//            This prevents early style processing and caching by the `dom-bind` element.
import '/resources/components/rich-text/tg-rich-text-styles.js';
import '/resources/images/tg-reference-hierarchy.js';
import '/resources/components/tg-qr-code-scanner.js';
import '/app/tg-app.js';

import '/app/application-startup-resources.js';