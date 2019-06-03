import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/polymer/lib/elements/dom-bind.js';
import '/resources/polymer/@polymer/iron-media-query/iron-media-query.js'
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

import '/resources/components/tg-subheader.js';

import '/app/tg-app-config.js'

import { beforeNextRender } from "/resources/polymer/@polymer/polymer/lib/utils/render-status.js";
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const template = html`
    <style>
        :host(.debug), :host(.debug) *, :host(.debug) ::slotted(*) {
            border: 1px dashed red !important;
        }
        .hidden-with-subheader {
            display: none !important;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <tg-app-config id="appConfig"></tg-app-config>
    <iron-media-query query="[[_calcMobileQuery()]]" on-query-matches-changed="_mobileChanged"></iron-media-query>
    <iron-media-query query="[[_calcTabletQuery()]]" on-query-matches-changed="_tabletChanged"></iron-media-query>
    <iron-media-query query="[[_calcDesktopQuery()]]" on-query-matches-changed="_desktopChanged"></iron-media-query>`;

template.setAttribute('strip-whitespace', '');

(function () {
    const countDots = function (path) {
        return (path.match(/\./g) || []).length;
    };
    const stampTemplate = function (template, model) {
        if (template && model) {
            for (let prop in model) {
                template[prop] = model[prop];
            }
        }
    };
    const forEachPropValue = function (obj, callback) {
        for (let propName in obj) {
            const elements = obj[propName] || [];
            elements.forEach(element => callback(element));
        }
    };
    const wrapWithDiv = function (element) {
        const div = document.createElement("div");
        div.appendChild(element);
        return div;
    };
    const keyWords = {
            "skip": function (selectedElements, orderedElements, layoutElem) {
                return document.createElement('div');
            },
            "select": function (selectedElements, orderedElements, layoutElem) {
                const selectCondition = layoutElem.split(':')[1].split('=');
                const attribute = selectCondition[0].trim();
                if (selectedElements.length > 0 && selectedElements[0].hasAttribute(attribute) && selectedElements[0].getAttribute(attribute) === selectCondition[1].trim()) {
                    return createSlotFor(selectedElements.splice(0, 1)[0]);
                }
            },
            "subheader": function (selectedElements, orderedElements, layoutElem) {
                return createSubheader.bind(this)(layoutElem, false, false);
            },
            "subheader-open": function (selectedElements, orderedElements, layoutElem) {
                return createSubheader.bind(this)(layoutElem, true, false);
            },
            "subheader-closed": function (selectedElements, orderedElements, layoutElem) {
                return createSubheader.bind(this)(layoutElem, true, true);
            },
            "html": function (selectedElements, orderedElements, layoutElem) {
                this._htmlElements = this._htmlElements || {};
                const elements = this._htmlElements[layoutElem];
                let elemToReturn = elements && elements.find(element => element.parentElement === null);
                if (elemToReturn) {
                    return wrapWithDiv(elemToReturn);
                } else {
                    const templateElem = document.createElement('template');
                    templateElem.innerHTML = layoutElem.split(':').slice(1).join(':').trim();
                    elemToReturn = document.createElement('dom-bind');
                    elemToReturn.appendChild(templateElem);
                    stampTemplate(elemToReturn, this.context);
                    if (elements) {
                        elements.push(elemToReturn);
                    } else {
                        this._htmlElements[layoutElem] = [elemToReturn];
                    }
                    return wrapWithDiv(elemToReturn);
                }
            }
        };

    const createSlotFor= function (elem) {
        const slotElement = document.createElement("slot");
        if (elem.hasAttribute("slot")) {
            slotElement.setAttribute("name", elem.getAttribute("slot"));
        }
        return slotElement;
    }

    const createSubheader = function (layoutElem, collapsible, closed) {
        const elemToReturn = document.createElement('tg-subheader');
        elemToReturn.subheaderTitle = layoutElem.split(':')[1].trim();
        if (collapsible) {
            elemToReturn.collapsible = true;
            elemToReturn.closed = closed;
        }
        return elemToReturn;
    };
    const setLayout = function (layout) {
        if (!Array.isArray(layout)) {
            throw "The layout must be an array";
        }
        if (this.currentLayout !== layout) {
            if (!this.componentsToLayout) {
                this.componentsToLayout = [];
                this.slottedElements = {};
                Array.from(this.children).forEach((item, idx) => {
                    item.setAttribute("slot", "layout_element_" + idx);
                    this.componentsToLayout.push(item);
                    this.slottedElements["layout_element_" + idx] = item;
                });
            }
            //First of all clear all appended elements
            if (this.appendedElements) {
                this.appendedElements.forEach(element => this.shadowRoot.removeChild(element));
            }
            this.appendedElements  = [];
            //Clear sub headers and remove styles and set default one those might be overriden later.
            resetSubheaderComponents.bind(this)();
            removeStylesAndClasses.bind(this)(this);
            this.toggleClass("layout", true);
            this.toggleClass("vertical", true);
            
            const selectedElements = [];
            const orderedElements = this.componentsToLayout.slice();
            splitElements.bind(this)(selectedElements, orderedElements, layout);
            let subheader = null;
            layout.forEach((function (layoutElem) {
                subheader = createFlexCell.bind(this)(this, layoutElem, selectedElements, orderedElements, subheader, true);
            }).bind(this));
            this._setCurrentLayout(layout);
            this.fire('layout-finished', this);
        }
    };
    const resetSubheaderComponents = function () {
        this._subheaders.forEach(function (subheader) {
            subheader.removeAllRelatedComponents();
        });
        this._subheaders = [];
    };
    const splitElements = function (selectedElements, orderedElements, layout) {
        if (hasArray(layout)) {
            layout.forEach(function (element) {
                if (Array.isArray(element)) {
                    splitElements.bind(this)(selectedElements, orderedElements, element);
                }
            }.bind(this));
        } else {
            const selectIndex = getSelectIndex(layout);
            if (selectIndex >= 0) {
                const selectedElement = spliceSelectedElement(orderedElements, layout[selectIndex]);
                if (selectedElement) {
                    selectedElements.push(selectedElement);
                }
            }
        }
    };
    const spliceSelectedElement = function (orderedElements, layoutElement) {
        const colonIndex = layoutElement.indexOf(':');
        const equalSignIndex = layoutElement.indexOf('=');
        if (colonIndex >= 0 && equalSignIndex >= 0 && colonIndex < equalSignIndex) {
            const selectCondition = layoutElement.split(':')[1].split('=');
            const attribute = selectCondition[0].trim();
            const value = selectCondition[1].trim();
            for (let elementIndex = 0; elementIndex < orderedElements.length; elementIndex += 1) {
                if (orderedElements[elementIndex].hasAttribute(attribute) && orderedElements[elementIndex].getAttribute(attribute) === value) {
                    return orderedElements.splice(elementIndex, 1)[0];
                }
            }
        } else {
            throw "Syntax error: the select condition must have ':' and '=' signs for example: 'select:propName=key.prop'";
        }
        return null;
    };
    const removeStylesAndClasses = function (element) {
        if (element.stylesToRemove) {
            element.stylesToRemove.forEach(function (style) {
                element.style.removeProperty(style);
            });
            delete element.stylesToRemove;
        }
        if (element.classesToRemove) {
            element.classesToRemove.forEach(function (className) {
                this.toggleClass(className, false, element);
            }.bind(this));
            delete element.classesToRemove;
        }
        this.toggleClass("hidden-with-subheader", false, element);
    };
    const createFlexCell = function (container, layoutElem, selectedElements, orderedElements, subheader, horizontal) {
        let newSubheader = subheader;
        if (typeof layoutElem === "string") {
            const trimmedLayoutElement = layoutElem.trim();
            const elemToSetStyles = container.tagName === "SLOT" ? this.slottedElements[container.getAttribute("name")] : container;
            if (layoutElem.indexOf(':') >= 0) {
                const styleValues = layoutElem.split(":");
                if (!keyWords[styleValues[0].trim()]) {
                    elemToSetStyles.style[styleValues[0].trim()] = styleValues[1].trim();
                    elemToSetStyles.stylesToRemove = elemToSetStyles.stylesToRemove || [];
                    elemToSetStyles.stylesToRemove.push(styleValues[0].trim());
                }
            } else {
                elemToSetStyles.classesToRemove = elemToSetStyles.classesToRemove || [];
                elemToSetStyles.classesToRemove.push(trimmedLayoutElement);
                if (trimmedLayoutElement === "horizontal" || trimmedLayoutElement === "vertical") {
                    this.toggleClass(horizontal ? "vertical" : "horizontal", false, elemToSetStyles);
                }
                this.toggleClass(trimmedLayoutElement, true, elemToSetStyles);
            }
        } else if (Array.isArray(layoutElem)) {
            let rowElement;
            if (!hasArray(layoutElem)) {
                rowElement = getNextElement.bind(this)(selectedElements, orderedElements, layoutElem);
                if (rowElement.tagName === "SLOT") {
                    removeStylesAndClasses.bind(this)(this.slottedElements[rowElement.getAttribute("name")]);
                } else {
                    removeStylesAndClasses.bind(this)(rowElement);
                }
            } else {
                rowElement = document.createElement("div");
                this.toggleClass("layout", true, rowElement);
                if (layoutElem.indexOf("horizontal") < 0 && layoutElem.indexOf("vertical") < 0) {
                    this.toggleClass(horizontal ? "horizontal" : "vertical", true, rowElement);
                }
            }
            if (rowElement.tagName === 'TG-SUBHEADER') {
                newSubheader = rowElement;
                this._subheaders.push(newSubheader)
            } else if (newSubheader) {
                newSubheader.addRelativeElement(rowElement);
            }
            layoutElem.forEach((function (columnLayout) {
                newSubheader = createFlexCell.bind(this)(rowElement, columnLayout, selectedElements, orderedElements, newSubheader, !horizontal);
            }).bind(this));
            if (container === this) {
                this.shadowRoot.appendChild(rowElement);
                this.appendedElements.push(rowElement);
            } else {
                container.appendChild(rowElement);
            }
        }
        return newSubheader;
    };
    const getNextElement = function (selectedElements, orderedElements, layoutElem) {
        let elemToReturn = findKeyWordElement.bind(this)(selectedElements, orderedElements, layoutElem);
        if (!elemToReturn) {
            if (orderedElements.length > 0) {
                elemToReturn = createSlotFor(orderedElements.splice(0, 1)[0]);
            } else {
                elemToReturn = document.createElement('div');
                elemToReturn.innerHTML = "element not found!";
            }
        }
        return elemToReturn;
    };
    const findKeyWordElement = function (selectedElements, orderedElements, layoutElem) {
        for (let elemIndex = 0; elemIndex < layoutElem.length; elemIndex++) {
            if (typeof layoutElem[elemIndex] === 'string') {
                const keyWord = layoutElem[elemIndex].split(':')[0].trim();
                if (keyWords[keyWord]) {
                    return keyWords[keyWord].bind(this)(selectedElements, orderedElements, layoutElem[elemIndex]);
                }
            }
        }
    };
    const getSelectIndex = function (layoutElem) {
        for (let elemIndex = 0; elemIndex < layoutElem.length; elemIndex++) {
            if (typeof layoutElem[elemIndex] === 'string' && layoutElem[elemIndex].indexOf("select") >= 0) {
                return elemIndex;
            }
        }
        return -1;
    };
    /**
     * Determines whether layout has array as one of it's element.
     */
    const hasArray = function (layoutElem) {
        for (let elemIndex = 0; elemIndex < layoutElem.length; elemIndex++) {
            if (Array.isArray(layoutElem[elemIndex])) {
                return true;
            }
        }
        return false;
    };
    Polymer({
        _template: template,

        is: "tg-flex-layout",

        properties: {
            whenDesktop: Array,
            whenTablet: Array,
            whenMobile: Array,
            debug: {
                type: Boolean,
                value: false,
                reflectToAttribute: true,
                observer: "_debugChanged"
            },
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
            })
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
        _setLayout: function (layout) {
            beforeNextRender(this, () => {
                setLayout.bind(this)(layout);
            });
        },
        _handleMobileScreen: function (whenMobile, whenTablet, whenDesktop, mobileScreen, contentLoaded) {
            const layout = whenMobile || whenTablet || whenDesktop;
            if (contentLoaded && mobileScreen && layout) {
                this._setLayout(layout);
            }
        },
        _handleTabletScreen: function (whenTablet, whenMobile, whenDesktop, tabletScreen, contentLoaded) {
            const layout = whenTablet || whenMobile || whenDesktop;
            if (contentLoaded && tabletScreen && layout ) {
                this._setLayout(layout);
            }
        },
        _handleDesktopScreen: function (whenDesktop, whenTablet, whenMobile, desktopScreen, contentLoaded) {
            const layout = whenDesktop || whenTablet || whenMobile;
            if (contentLoaded && desktopScreen && layout) {
                this._setLayout(layout);
            }
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
        _debugChanged: function (newValue, oldValue) {
            this.toggleClass("debug", newValue);
        },
        _calcMobileQuery: function () {
            return "max-width: " + (this.$.appConfig.minTabletWidth - 1) + "px";
        },
        _calcTabletQuery: function () {
            return "(min-width: " + this.$.appConfig.minTabletWidth + "px) and (max-width: " + (this.$.appConfig.minDesktopWidth - 1) + "px)";
        },
        _calcDesktopQuery: function () {
            return "min-width: " + this.$.appConfig.minDesktopWidth + "px";
        },
        _contextChanged: function (changeRecord) {
            this._htmlElements = this._htmlElements || {};
            const dotCount = countDots(changeRecord.path);
            if (changeRecord.path === "context") {
                forEachPropValue(this._htmlElements, element => stampTemplate(element, changeRecord.value));
            } else if (dotCount === 1) {
                const propName = changeRecord.path.substr(changeRecord.path.lastIndexOf(".") + 1);
                forEachPropValue(this._htmlElements, element => element[propName] = changeRecord.value);
            } else {
                const propPath = changeRecord.path.substr(changeRecord.path.indexOf(".") + 1);
                forEachPropValue(this._htmlElements, element => element.notifyPath(propPath, changeRecord.value));
            }
        }
    });
})();