import { TgReflector } from '/app/tg-reflector.js';
import { TgAppConfig } from '/app/tg-app-config.js';

import {TgConfirmationDialog} from '/resources/components/tg-confirmation-dialog.js';

import moment from '/resources/polymer/lib/moment-lib.js';

import '/resources/polymer/@polymer/paper-styles/color.js';

let appConfig;
let confirmationDialog;

/**
 * Generates the unique identifier.
 */
export function generateUUID () {
    return crypto.randomUUID(); // this API is only present in secure (https://) contexts (or at localhost)
};

/**
 * Returns the first entity type and it's property path that lies on path of property name and entity
 */
export function getFirstEntityTypeAndProperty (entity, propertyName) {
    if (entity && propertyName) {
        const reflector = new TgReflector();
        const entityType = entity.constructor.prototype.type.call(entity);
        let currentProperty = propertyName;
        let currentType = entityType.prop(propertyName).type();
        if (currentType instanceof reflector._getEntityTypePrototype() && currentType.isUnionEntity() && entity.get(propertyName)) {
            currentProperty += "." + entity.get(propertyName)._activeProperty();
            currentType = entityType.prop(currentProperty).type();
        }
        while (!(currentType instanceof reflector._getEntityTypePrototype())) {
            const lastDotIndex = currentProperty.lastIndexOf(".");
            currentProperty = lastDotIndex >= 0 ? currentProperty.substring(0, lastDotIndex) : "";
            currentType = currentProperty ? entityType.prop(currentProperty).type() : entityType;
        }
        return [currentType, currentProperty]; 
    } else if (entity) {
        return [entity.constructor.prototype.type.call(entity), propertyName];
    }
};

/**
 * Returns the first entity type that lies on path of property name and entity.
 */
export function getFirstEntityType (entity, propertyName) {
    return getFirstEntityTypeAndProperty(entity, propertyName)[0];
};

/**
 * Removes all Light DOM children from Polymer 'element'.
 */
export function _removeAllLightDOMChildrenFrom (element) {
    while (element.firstChild) {
        element.removeChild(element.lastChild);
    }
};

/**
 * Returns the x and y coordinates relatively to specified container
 */
export function getRelativePos (x, y, container) {
    let reference = container;
    let newPos = {
        x: x,
        y: y
    }
    while (reference) {
        newPos.x -= reference.offsetLeft;
        newPos.y -= reference.offsetTop;
        reference = reference.offsetParent;
    }
    return newPos;
};

/**
 * This method prevents event from further bubbling.
 */
export function tearDownEvent (e) {
    if (e) {
        if (e.stopPropagation) e.stopPropagation();
        if (e.preventDefault) e.preventDefault();
        e.cancelBubble = true;
        e.returnValue = false;
    }
};

/**
 * Makes color lighter or darker depending on whether lum parameter is greater or less then 0.
 */
export function shadeColor (hex, lum) {
    if (hex != null) {
        hex = String(hex).replace(/[^0-9a-f]/gi, '');
        if (hex.length < 6) {
            hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
        }
        lum = lum || 0;
        var c, i, rgb = "#";
        for (i = 0; i < 3; i++) {
            c = parseInt(hex.substr(i * 2, 2), 16);
            c = Math.round(Math.min(Math.max(0, c + (c * lum)), 255)).toString(16);
            rgb += ("00" + c).substr(c.length);
        }
        return rgb;
    } else {
        return hex;
    }
};

/**
 * Returns true if the descendant has the parent as an ancestor, otherwise returns false.
 */
export function isInHierarchy (parent, descendant) {
    let current = descendant;
    while (current && current !== parent) {
        current = current.parentElement || current.getRootNode().host;
    }
    return !!current;
};

export function getParentAnd(element, predicate) {
    let current = element;
    while (current && !predicate(current)) {
        current = current.parentElement || current.getRootNode().host;
    }
    return current;
}

export function getActiveParentAnd(predicate) {
    return getParentAnd(deepestActiveElement(), predicate);
}

export function allDefined (args) {
    const convertedArgs = [...args];
    for (let i = 0; i < convertedArgs.length; i++) {
        if (convertedArgs[i] === undefined) {
            return false;
        }
    }
    return true;
}

/**
 * Finds deepest active element including those inside Shadow root children of custom web components starting lookup from non-empty 'element'.
 * 
 * @param element -- non-empty active element (sometimes <body> or 'null' if there is no focused element), that could be activeElement
 *  on its own or its shadowRoot can contain more granular activeElement inside.
 */
const _deepestActiveElementOf = function (element) {
    if (element && element.shadowRoot && element.shadowRoot.activeElement) {
        return _deepestActiveElementOf(element.shadowRoot.activeElement);
    } else {
        return element;
    }
};

/**
 * Finds deepest active element including those inside Shadow root children of custom web components starting lookup from document.activeElement.
 */
export const deepestActiveElement = function () {
    return _deepestActiveElementOf(document.activeElement);
};

/**
 * Returns a pair of { short: ..., extended: ... } messages, associated with the 'result'.
 * The 'result' may be of type Result, Warning or Informative (java).
 * 
 * 'result' message should never be 'null' (see Result.getMessage()), except the case where java's NPE was causing failure Result -- in this case we return 'Null pointer exception' for both short and extended messages.
 * If Result was constructed with single message, it will be used for both short and extended messages.
 * Otherwise we do splitting by <extended/> part (and try to be smart if there are missing parts before or after <extended/>).
 */
export const resultMessages = function (result) {
    if (result.message === null) {
        const npeMsg = 'Null pointer exception';
        return {
            short: npeMsg,
            extended: npeMsg
        };
    }
    if (result.message) { // non-empty string
        const messages = result.message.split('<extended/>');
        const shortMessage = messages[0] || messages[1]; // return exact message before <extended/>, only if non-empty; otherwise return ext message; or whole message if there is no <extended/> part
        return {
            short: shortMessage,
            extended: messages[1] || shortMessage // return exact message after <extended/>, only if non-empty; otherwise return short message; or whole message if there is no <extended/> part
        };
    }
    // only '' value are possible here (inside Result / Exception deserialised instances);
    // however, the method may be used for some artificial values from Web UI;
    // just return the same empty '' or undefined in both short / extended parts -- delegate further
    return { 
        short: result.message,
        extended: result.message
    };
};

/**
 * The selector for focusable elements.
 */
export const FOCUSABLE_ELEMENTS_SELECTOR = 'a[href], area[href], input, select, textarea, button, iframe, object, embed, [tabindex="0"], [contenteditable]';

/**
 * The class that can be used like an entity for entity grid inspector or any other place.
 */
export class EntityStub {

    constructor(id) {
        this['id'] = id;
    }

    get(prop) {
        if (prop === '') { // empty property name means 'entity itself'
            return this;
        }
        const dotIndex = prop.indexOf(".");
        if (dotIndex > -1) {
            var first = prop.slice(0, dotIndex);
            var rest = prop.slice(dotIndex + 1);
            var firstVal = this[first];
            if (firstVal === null) {
                return null;
            }
            return firstVal.get(rest);
        }
        return this[prop];
    }

    set(property, value) {
        this[property] = value;
    }

    propType (name) {
        return null;
    }

    type() {
        const self = this;
        return {
            prop: (name) => {
                return {
                    type: () => self.propType(name),
                    scale: () => 0,
                    trailingZeros: () => true,
                    displayAs: () => ""
                }
            }
        }
    }

    prop(propName) {
        return {
            validationResult: () => null
        }
    }
};

/**
 * Finds the closest parent to startFrom element which can be focused.
 * 
 * @param {HTMLElement} startFrom - the element to start search from.
 * @param {HTMLElement} orElse - the element to which is returned if key event target wasn't found.
 * @param {Function} doDuringSearch - custom function that allows to perform some tasks during search it receives currently inspected HTMLElement. 
 * @returns The closest parent to startFrom element with tabindex equal to 0. 
 */
export const getKeyEventTarget = function (startFrom, orElse, doDuringSearch) {
    let parent = startFrom;
    while (parent && parent.getAttribute('tabindex') !== '0') {
        if (typeof doDuringSearch === 'function') {
            doDuringSearch(parent);
        }
        parent = parent.parentElement || parent.getRootNode().host;
    }
    return parent || orElse;
}

/**
 * Returns true if specified text contains html tags which are not allowed to be inserted as html text. 
 *  
 */
export const containsRestrictedTags = function (htmlText) {
    const offensiveTag = new RegExp('<html|<body|<script|<img', 'mi');
    return offensiveTag.exec(htmlText) !== null;
}

/**
 * Returns 'true' if client application was loaded on mobile device, 'false' otherwise (see AbstractWebResource and DeviceProfile for more details).
 * 
 * It is recommended to use word "Mobi" for mobile device detection, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Browser_detection_using_the_user_agent for more info.
 * 
 * It is very important not to confuse this function with MOBILE / TABLET / DESKTOP layouts (tg-tile-layout, tg-flex-layout).
 * These three layout modes can be used in 'desktop' application when resizing application window.
 * Two of these modes can be used for 'mobile' application: MOBILE / TABLET.
 * TABLET is activated commonly when landscape orientation is used for mobile device.
 */
export const isMobileApp = function () {
    return window.navigator.userAgent.includes('Mobi'); // consistent with AbstractWebResource.calculateDeviceProfile method
};

/**
 * Determines whether iPhone specific browser is used for rendering this client application. This could be Safari, Chrome for iOS, Opera Mini (iOS WebKit), Firefox for iOS.
 * See https://deviceatlas.com/blog/mobile-browser-user-agent-strings for more details.
 */
export const isIPhoneOs = function () {
    return window.navigator.userAgent.includes('iPhone OS');
};

export const doWhenDimentionsAttainedAnd = function (self, conditionFun, doFun, time) {
    conditionFun.bind(self);
    doFun.bind(self);
    self.async(function () {
        if (self.offsetWidth && self.offsetHeight && conditionFun()) {
            doFun();
        } else {
            doWhenDimentionsAttainedAnd(self, conditionFun, doFun, 100);
        }
    }, time);
};

/**
 * Replaces all triangular brackets with appropriate html sign.
 * 
 * @param {String} text - the text with html tags 
 * @returns 
 */
export const escapeHtmlText = function(text) {
    const searchFor = [/</g, />/g];
    const replaceWith = ['&lt;', '&gt;'];
    let escapedStr = text;
    searchFor.forEach((search, i) => {
        escapedStr = escapedStr.replace(search, replaceWith[i]);
    });
    return escapedStr;
};

/**
 * Returns name of currently authenticated user.
 */
const _userName = function () {
    const appTemplate = document.body.querySelector('tg-app-template');
    return appTemplate && appTemplate.menuConfig && appTemplate.menuConfig.userName;
};

/**
 * Returns generated key for local storage and specified subject to save and retrieve data.
 * 
 * @param {String} subject - subject that should be appended to user name to create key for local storage data.
 * @returns 
 */
export const localStorageKey = function (subject) {
    return `${_userName()}_${subject}`;
};

/**
 * Creates simple dummy entity to bind it to entity master
 */
export const createDummyBindingEntity = function (customPropObject, propDefinition) {
    const reflector = new TgReflector();
    const fullEntityType = reflector.getEntityPrototype();
    fullEntityType.compoundOpenerType = () => null;

    const fullEntity = reflector.newEntityEmpty();

    fullEntity.get = prop => {
        if (prop === '') { // empty property name means 'entity itself'
            return fullEntity;
        }
        return fullEntity[prop];
    };
    fullEntity._type = fullEntityType;
    fullEntity.id = -1;
    fullEntity.version = 0;
    Object.keys(customPropObject).forEach(key => {
        fullEntity[key] = customPropObject[key].value;
    });
    
    const bindingView = reflector.newEntityEmpty();
    bindingView['id'] = -1;
    bindingView['version'] = 0;
    bindingView['@@touchedProps'] = {
        names: [],
        values: [],
        counts: []
    };
    bindingView['@@origin'] = fullEntity;
    Object.keys(customPropObject).forEach(key => {
        bindingView[key] = customPropObject[key].value;
        bindingView[`@${key}_editable`] = customPropObject[key].editable;
    });
    bindingView.get = prop => {
        if (prop === '') { // empty property name means 'entity itself'
            return bindingView;
        }
        return bindingView[prop];
    };
    const bindingViewType = reflector.getEntityPrototype();
    bindingViewType.prop = propDefinition;
    bindingView._type = bindingViewType;
    return bindingView;
};

/**
 * Loads a specified resource into new or existing browsing context (see https://developer.mozilla.org/en-US/docs/Web/API/Window/open).
 * Logs an error in case if resource opening was blocked by popup blocker or some other problem prevented it.
 *
 * Unspecified 'target' means '_blank' i.e. most likely to be opened in a new tab (or window with special user options).
 */
const openLink = function (url, target, windowFeatures) {
    const newWindow = window.open(url, target, windowFeatures);
    if (newWindow) {
        // Always prevent tabnapping.
        // I.e. prevent ability by new tab / window to rewrite 'location' of original tab / window through 'opener' property.
        newWindow.opener = null;

        if (newWindow.focus) {
            // Create an asynchronous request to bring to view a newly opened window / tab.
            // In most cases this will work without 'focus()' call.
            // However, tapping on original tab / window may prevent this behaviour.
            newWindow.focus();
        }
    } else {
        // The window wasn't allowed to open.
        // This is likely caused by built-in or external popup blockers.
        // Log this to both server and user.
        throw new Error(`Link [${url}] blocked. Target: [${target}], windowFeatures: [${windowFeatures}].`);
    }
};

/**
 * Determines whether link is etarnal to this application or not.
 * 
 * @param {String} url - A URL string to check
 * @returns 
 */
export function isExternalURL(url) {
    return new URL(url).hostname !== window.location.hostname;
}

/**
 * Displays a confirmation dialog asking whether the link should be opened, and saves additional settings to avoid showing this message again for the same link or host.
 * 
 * @param {String} url - url text of the link to check.
 * @param {String} target target attribute for that is passed to openLink finction
 * @param {Object} windowFeatures - window feature object that is passed to openLink function
 */
export const checkLinkAndOpen = function (url, target, windowFeatures) {
    const hostName = new URL(url).hostname;
    appConfig = appConfig || new TgAppConfig();
    confirmationDialog = confirmationDialog || new TgConfirmationDialog();
    
    const isAllowedSite = function() {
        return appConfig.allowedSites.indexOf(hostName) >= 0;
    }

    const wasAcceptedByUser = function () {
        return (localStorage.getItem(url) !== null && moment().diff(moment(localStorage.getItem(url)), 'days') < appConfig.daysUntilSitePermissionExpires) ||
                (localStorage.getItem(hostName) !== null && moment().diff(moment(localStorage.getItem(hostName)), 'days') < appConfig.daysUntilSitePermissionExpires);
    }
    
    if (!isAllowedSite() && !wasAcceptedByUser()) {
        const text = `The link is taking you to another site.<br>Are you sure you would like to continue?<br>
                        <pre style="line-break:anywhere;max-width:500px;white-space:normal;color:var(--paper-light-blue-500);">${url}</pre>`
        const options = ["Don't show this again for this link", "Don't show this again for this site"];
        const buttons = [{ name: 'Cancel' }, { name: 'Continue', confirm: true, autofocus: true, classes: "red" }];
        confirmationDialog.showConfirmationDialog(text, buttons, {single: true, options}, "Double-check this link").then(opt => {
            if (opt[options[0]]) {
                localStorage.setItem(url, new Date());
            }
            if (opt[options[1]]) {
                localStorage.setItem(hostName, new Date());
            }
            openLink(url, target, windowFeatures);
        });
    } else {
        openLink(url, target, windowFeatures);
    }
}