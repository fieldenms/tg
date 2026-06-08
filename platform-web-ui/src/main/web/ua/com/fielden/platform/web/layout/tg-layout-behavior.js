import { html } from '/resources/polymer/@polymer/polymer/polymer-element.js';
import { beforeNextRender } from "/resources/polymer/@polymer/polymer/lib/utils/render-status.js";

import '/app/tg-app-config.js'

const countDots = function (path) {
    return (path.match(/\./g) || []).length;
};

export const stampLayoutTemplate = function (template, model) {
    if (template && model) {
        Object.keys(model).forEach(prop => {
            template[prop] = model[prop];
        });
    }
};

const forEachPropValue = function (obj, callback) {
    for (let propName in obj) {
        const elements = obj[propName] || [];
        elements.forEach(element => callback(element));
    }
};

export const TgLayoutBehavior = {

    properties: {
        whenDesktop: Object,
        whenTablet: Object,
        whenMobile: Object,
        desktopScreen: {
            type: Boolean,
            readOnly: true
        },
        tabletScreen: {
            type: Boolean,
            readOnly: true
        },
        mobileScreen: {
            type: Boolean,
            readOnly: true
        },
        currentLayout: {
            type: Boolean,
            readOnly: true
        },
        contentLoaded: {
            type: Boolean,
            readOnly: true,
            observer: "_handleContentLoading",
            value: false
        },
        context: {
            type: Object
        },
        // Checks whether an element with `filterable` attribute should be visible.
        // Returns true if the element should be visible; otherwise false.
        filter: {
            type: Function,
            value: null,
            observer: "_filterChanged"
        },
        _subheaders: {
            type: Array
        },
        _htmlElements: {
            type: Object
        }
    },

    observers: [
        "_handleDesktopScreen(whenDesktop, whenTablet, whenMobile, desktopScreen, contentLoaded)",
        "_handleTabletScreen(whenTablet, whenMobile, whenDesktop, tabletScreen, contentLoaded)",
        "_handleMobileScreen(whenMobile, whenTablet, whenDesktop, mobileScreen, contentLoaded)",
        "_contextChanged(context.*)"],

    ready: function () {
        this._subheaders = [];
            
        this._editorErrorHandler = this._editorErrorHandler.bind(this);
        this.addEventListener('editor-error-appeared', this._editorErrorHandler);
    },

    attached: function () {
        beforeNextRender(this, () => {
            this._setContentLoaded(true);
        });
    },

   _editorErrorHandler: function (e) {
        const subheader = e.detail.$$relativeSubheader$$;
        if (this._subheaders.indexOf(subheader) >= 0) {
            const subheaderHierarchy = this._findSubheaderHierarchy(subheader);
            if (subheaderHierarchy.length > 0 && subheaderHierarchy[subheaderHierarchy.length - 1].offsetParent) {
                subheaderHierarchy.forEach(function (subheader) {
                    subheader.open();
                });
            }
        }
    },

    _findSubheaderHierarchy: function (subheader) {
        const subheaderHierarchy = [];
        while (subheader) {
            subheaderHierarchy.push(subheader);
            subheader = subheader.parentElement && subheader.parentElement.$$relativeSubheader$$;
        }
        return subheaderHierarchy;
    },

    _handleDesktopScreen: function (whenDesktop, whenTablet, whenMobile, desktopScreen, contentLoaded) {
        const layout = whenDesktop || whenTablet || whenMobile;
        if (contentLoaded && desktopScreen && layout) {
            this._setLayout(layout);
        }
    },

    _handleTabletScreen: function (whenTablet, whenMobile, whenDesktop, tabletScreen, contentLoaded) {
        const layout = whenTablet || whenMobile || whenDesktop;
        if (contentLoaded && tabletScreen && layout ) {
            this._setLayout(layout);
        }
    },

    _handleMobileScreen: function (whenMobile, whenTablet, whenDesktop, mobileScreen, contentLoaded) {
        const layout = whenMobile || whenTablet || whenDesktop;
        if (contentLoaded && mobileScreen && layout) {
            this._setLayout(layout);
        }
    },

     _calcDesktopQuery: function () {
        return "min-width: " + this.$.appConfig.minDesktopWidth + "px";
    },

    _calcTabletQuery: function () {
        return "(min-width: " + this.$.appConfig.minTabletWidth + "px) and (max-width: " + (this.$.appConfig.minDesktopWidth - 1) + "px)";
    },

    _calcMobileQuery: function () {
        return "max-width: " + (this.$.appConfig.minTabletWidth - 1) + "px";
    },

    _handleContentLoading: function (contentLoaded) {
        if (contentLoaded && !this.whenDesktop && !this.whenTablet && !this.whenMobile) {
            this.fire('layout-finished', this);
        }
    },

    _mobileChanged: function (e, detail) {
        this._setMobileScreen(detail.value);
    },

    _tabletChanged: function (e, detail) {
        this._setTabletScreen(detail.value);
    },

    _desktopChanged: function (e, detail) {
        this._setDesktopScreen(detail.value);
    },

    _contextChanged: function (changeRecord) {
        this._htmlElements = this._htmlElements || {};
        const dotCount = countDots(changeRecord.path);
        if (changeRecord.path === "context") {
            forEachPropValue(this._htmlElements, element => stampLayoutTemplate(element, changeRecord.value));
        } else if (dotCount === 1) {
            const propName = changeRecord.path.substr(changeRecord.path.lastIndexOf(".") + 1);
            forEachPropValue(this._htmlElements, element => element[propName] = changeRecord.value);
        } else {
            const propPath = changeRecord.path.substr(changeRecord.path.indexOf(".") + 1);
            forEachPropValue(this._htmlElements, element => element.notifyPath(propPath, changeRecord.value));
        }
    },

    _setLayoutBeforeNextRender: function (layout) {
        beforeNextRender(this, () => {
            this._setLayout(layout);
        });
    },

    _filterChanged: function (newFilter, oldFilter) {
        this._filterLayout(newFilter);
    },

    /**
     * Sets the give layout. Override this to initiate container with given laytout.
     * 
     * @param {Object} layout - layout toe set.
     */
    _setLayout: function (layout) {
        
    },

    /**
     * Applies filter to current layout. Override this to provide filtering logic for laytout.
     * 
     * @param {Function} filter - filter function to apply for the current layout.
     */
    _filterLayout: function (filter) {

    }
};

export const layoutMediaQueryTemplate = html`
    <tg-app-config id="appConfig"></tg-app-config>
    <iron-media-query query="[[_calcMobileQuery()]]" on-query-matches-changed="_mobileChanged"></iron-media-query>
    <iron-media-query query="[[_calcTabletQuery()]]" on-query-matches-changed="_tabletChanged"></iron-media-query>
    <iron-media-query query="[[_calcDesktopQuery()]]" on-query-matches-changed="_desktopChanged"></iron-media-query>`; 