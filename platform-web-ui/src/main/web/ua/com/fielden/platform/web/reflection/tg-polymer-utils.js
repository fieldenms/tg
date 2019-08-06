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
 * Removes all Light DOM children from Polymer 'element'.
 *
 * The need for such utility method arose from the fact that Polymer (currently 1.4 version) returns
 * from Polymer.dom(element).childNodes, Polymer.dom(element).firstChild, Polymer.dom(element).firstElementChild methods
 * not only Light DOM children, but also Local DOM children, including the elements in the template and whitespace
 * nodes in the template.
 *
 * Please, note that Polymer.dom().flush() call is needed to be done manually after this method has been used.
 * The intention was made for the cases, where some additional DOM manipulation is needed, and in such cases
 * flush() call could be efficiently done after all DOM manipulation once.
 */
export function _removeAllLightDOMChildrenFrom (element) {
    const childNodes = element.childNodes;
    while (element.firstChild) {
        element.removeChild(element.firstChild);
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
 * Returns true if the descendant is has parent as ancestor, otherwise returns false.
 */
export function isInHierarchy (parent, descendant) {
    let current = descendant;
    while (current && current !== parent) {
        current = current.parentElement || current.getRootNode().host;
    }
    return !!current;
};

/**
 * Converts short collectional property with string value
 */
export function generateShortCollection (entity, property, typeObject) {
    const collectionValue = entity.get(property);
    const containerPropertyValue = property.lastIndexOf('.') >= 0 ? entity.get(property.substr(0, property.lastIndexOf('.'))) : entity;
    const keys = typeObject.compositeKeyNames();
    return collectionValue.map(function (subEntity) {
        const key = keys.find(function (key) {
            if (subEntity.get(key) !== containerPropertyValue) {
                return key;
            }
        });
        return subEntity.get(key);
    });
};

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
export function deepestActiveElement () {
    return _deepestActiveElementOf(document.activeElement);
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

    type() {
        return {
            prop: (prop) => {
                return {
                    scale: () => 0,
                    trailingZeros: () => true
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

const calculateNodesCount = function (parents) {
    let count = parents.length;
    for (let i = 0; i < parents.length; i++) {
        if (parents[i].shadowRoot) {
            count += calculateNodesCount(parents[i].shadowRoot.querySelectorAll('*'));
        }
    }
    return count;
};

/**
 * Calculates count of nodes for 'element' including sub-nodes in its Shadow DOM (if it has Shadow DOM).
 */
export const calculateNodesForElement = function (element) {
    const elements = element.querySelectorAll('*');
    const result = [element];
    elements.forEach(elem => {
        if (elem.assignedNodes)  {
            result.push(...elem.assignedNodes().flatMap(node => [node, ...node.querySelectorAll('*')]));
        } else {
            result.push(elem);
        }
    })
    return calculateNodesCount(result);
};