import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js'
import '/resources/polymer/@polymer/iron-input/iron-input.js';
import '/resources/polymer/@polymer/iron-dropdown/iron-dropdown.js'

import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { TgEditorBehavior,  TgEditorBehaviorImpl, createEditorTemplate} from '/resources/editors/tg-editor-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js'

const additionalTemplate = html`
    <style>
        #input.upper-case {
            text-transform: uppercase;
        }
        #input[disabled] {
            cursor: text;
        }
        .colour-box {
            position: relative;
            min-width: 40px;
            min-height: 40px;
        }
        .dummy-box {
            position: absolute;
            top: -3px;
            left: -3px;
            min-width: 40px;
            min-height: 40px;
            border: 3px solid white;
            background-color: none;
            box-shadow: 0px 2px 6px #ccc;
            z-index: 1;
            display: none;
            cursor: pointer;
        }
        .icon {
            min-width: 40px;
            min-height: 40px;
            color: #757575;
        }
        .small-icon {          
            display: block;
            max-width: 14px;
            max-height: 14px;
            color: #757575;
        }
        .hidden-icon {
            visibility: hidden;
        }
        .colour-box:hover > .dummy-box {
            display: block;
        }
        .small-box {
            max-width: 14px;
            max-height: 14px;
            background-color: #FAFAFA;
            border: 2px solid #BDBDBD;
            border-radius: 2px;
            cursor: pointer;
        }
        iron-dropdown {
            background-color: #FAFAFA;
            box-shadow: 0px 2px 6px #ccc;
        }
        .prefix {
            font-family: 'Roboto', 'Noto', sans-serif;
            margin-left: 10px;
            font-size: 1rem;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>`;
const customPrefixTemplate = html`
    <div class="layout horizontal prefix-custom-attributes" style$="[[_calcDecoratorPartStyle(_disabled)]]">
        <div on-tap="_openColourPicker" class="small-box prefix-custom-attributes" style$="[[_calcColourBoxStyle(_editingValue)]]">
            <iron-icon class$="[[_calcColourSmallIconClass(_editingValue)]]" icon="close"></iron-icon>
        </div>
        <div class="prefix prefix-custom-attributes" style$="[[_calcColourPrefixStyle(_editingValue)]]">#</div>
    </div>`;
const customInputTemplate = html`
    <iron-input bind-value="{{_editingValue}}" allowed-pattern="[0-9, A-F, a-f]" class="custom-input-wrapper colour-input">
        <input
            id="input"
            class="custom-input"
            style$="[[_calcColourTextStyle(_editingValue)]]"
            maxlength="6"
            on-change="_onChange"
            on-input="_onInput"
            on-tap="_onTap"
            on-mousedown="_onTap"
            on-keydown="_onKeydown"
            disabled$="[[_disabled]]"
            tooltip-text$="[[_getTooltip(_editingValue)]]"
            autocomplete="off"/>
    </iron-input>`;
const propertyActionTemplate = html`<slot name="property-action"></slot>`;

const colourLuminance = function (hex, lum) {
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

const rgbToHex = function (colourString) {
    if (colourString === null||colourString.length === 3 || colourString.length === 6) {
        return colourString.toUpperCase();
    }
    var nums = /(.*?)rgb\((\d+),\s*(\d+),\s*(\d+)\)/i.exec(colourString);
    var r = parseInt(nums[2], 10).toString(16);
    var g = parseInt(nums[3], 10).toString(16);
    var b = parseInt(nums[4], 10).toString(16);    
    return "#" + ((r.length == 1 ? "0" + r : r) +(g.length == 1 ? "0" + g : g) + (b.length == 1 ? "0" + b : b));
};

Polymer({
    _template: createEditorTemplate(additionalTemplate, customPrefixTemplate, customInputTemplate, html``, html``, propertyActionTemplate),

    is: 'tg-colour-picker',
    
    behaviors: [ TgEditorBehavior ],
    
    ready: function () {
        this._calcColourTextStyle = (function (editingValue) {
            if (editingValue === "" || editingValue.length === 3 || editingValue.length === 6) {
                return "margin-left: 1px; color: #212121";
            } else {
                return "margin-left: 1px; color: #DD2C00";
            }
        }).bind(this);  
    },

    convertToString: function (value) {
        if (value === null) {
            return "";
        } else {
            return value.hashlessUppercasedColourValue;
        }
    },

    convertFromString: function (strValue) {
        if (strValue.length !== 3 && strValue.length !== 6 && strValue !== "") {
            throw "The entered value [ #" + strValue + "] is not a valid colour (use only [0-9; A-F], 3 or 6 characters).";
        } else {
            return {
                hashlessUppercasedColourValue: strValue
            };
        }
    },
    
    _openColourPicker: function () {
        if (this._disabled === false) {
            if (!this._colourPalette) {
                this._colourPalette = this._createDialog();
                this.shadowRoot.appendChild(this._colourPalette);
            }
            this._colourPalette.open();
        }
    },

    _createDialog: function () {
        var self = this;
        var domBind = document.createElement('dom-bind');
        
        domBind.colourSelected = function (e, detail) {
            this._acceptColourBind();
            var target = e.target || e.srcElement;
            self._editingValue = ((rgbToHex(target.style['background-color'])).substring(1)).toUpperCase();
            this.$.dropdown.close();
        }.bind(domBind);

        domBind.nonColourSelected = function () {
            this._acceptColourBind();
            self._editingValue = "";
            this.$.dropdown.close();

        }.bind(domBind);

        domBind._acceptColourBind = function () {
            self._isAcceptingColourFromPicker = true;
        }.bind(domBind);

        domBind.open = function () {
            this.$.dropdown.open();
        }.bind(domBind);
        
        domBind._colorPickerClosed = function () {
            self.decoratedInput().focus();
        }.bind(domBind);
        
        domBind._colorPickerOpened = function (event) {
            tearDownEvent(event);
        }.bind(domBind);

        var template = document.createElement('template');
        template.innerHTML =
            '<iron-dropdown id="dropdown" on-iron-overlay-closed="_colorPickerClosed" on-iron-overlay-opened="_colorPickerOpened">' + // tabindex="0" on-iron-overlay-opened="_colorPickerOpened" 
            '<div slot="dropdown-content" class="dropdown-content">' +
            '<div class="layout vertical" style="margin:10px 10px 10px 10px;">' +
            '<div class="layout horizontal">' +
            '<div class="colour-box" on-tap="nonColourSelected" style="background-color:#F5F5F5">' +
            '<div class="dummy-box" style="background-color:#F5F5F5">' +
            '<iron-icon class="icon" icon="icons:close"></iron-icon>' +
            '</div>' +
            '<iron-icon class="icon" icon="icons:close"></iron-icon>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#F44336">' +
            '<div class="dummy-box" style="background-color:#F44336"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#FF9800">' +
            '<div class="dummy-box" style="background-color:#FF9800"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#FFEB3B">' +
            '<div class="dummy-box" style="background-color:#FFEB3B"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#4CAF50;">' +
            '<div class="dummy-box" style="background-color:#4CAF50;"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#009688">' +
            '<div class="dummy-box" style="background-color:#009688"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#00BCD4">' +
            '<div class="dummy-box" style="background-color:#00BCD4"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#3F51B5">' +
            '<div class="dummy-box" style="background-color:#3F51B5"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#9C27B0">' +
            '<div class="dummy-box" style="background-color:#9C27B0"></div>' +
            '</div>' +
            '</div>' +
            '<div class="layout horizontal">' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#D7CCC8">' +
            '<div class="dummy-box" style="background-color:#D7CCC8"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#EF9A9A">' +
            '<div class="dummy-box" style="background-color:#EF9A9A"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#FFCC80">' +
            '<div class="dummy-box" style="background-color:#FFCC80"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#FFF59D">' +
            '<div class="dummy-box" style="background-color:#FFF59D"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#A5D6A7;">' +
            '<div class="dummy-box" style="background-color:#A5D6A7;"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#80CBC4">' +
            '<div class="dummy-box" style="background-color:#80CBC4"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#80DEEA">' +
            '<div class="dummy-box" style="background-color:#80DEEA"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#9FA8DA">' +
            '<div class="dummy-box" style="background-color:#9FA8DA"></div>' +
            '</div>' +
            '<div class="colour-box" on-tap="colourSelected" style="background-color:#CE93D8">' +
            '<div class="dummy-box" style="background-color:#CE93D8"></div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</iron-dropdown>';

        domBind.appendChild(template);
        return domBind;
    },
    
    _editingValueChanged: function (newValue, oldValue) {
        TgEditorBehaviorImpl._editingValueChanged.call(this, newValue, oldValue);
        if (this._isAcceptingColourFromPicker) {
            this._isAcceptingColourFromPicker = false;
            this.commit();
        }
    },

    _calcColourBoxStyle: function (editingValue) {
        if (editingValue.length === 3 || editingValue.length === 6) {
            return "border-color: " + colourLuminance('#' + editingValue, -0.1) + ";background-color: " + '#' + editingValue;
        } 
        return "";
    },

    _calcColourSmallIconClass: function (editingValue) {
        if (editingValue.length === 3 || editingValue.length === 6) {
            return "hidden-icon";
        } else  {
            return "small-icon";
        }
    },

    _calcColourPrefixStyle: function (editingValue) {
        if (editingValue === "") {
            return "visibility: hidden;";
        }else if (editingValue.length === 3 || editingValue.length === 6) {
            return "color: #212121;";
        } else {
            return "color: #DD2C00;";
        }
    },
    _calcDecoratorPartStyle: function (_disabled) {
        var style = "";
        if (_disabled === true) {
            style += "opacity: 1;"
        }
        return style;
    },
    _formatText: function (value) {
        return value && '#' + value;
    }
});