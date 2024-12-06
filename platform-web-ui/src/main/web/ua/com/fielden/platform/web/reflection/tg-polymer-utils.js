import { TgReflector } from '/app/tg-reflector.js';

/**
 * Generates the unique identifier.
 */
export function generateUUID () {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
    return uuid;
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