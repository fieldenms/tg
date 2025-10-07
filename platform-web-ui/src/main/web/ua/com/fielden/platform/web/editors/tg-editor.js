import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-icons/editor-icons.js';
import '/resources/polymer/@polymer/paper-input/paper-input-container.js';
import '/resources/polymer/@polymer/paper-input/paper-input-error.js';
import '/resources/polymer/@polymer/paper-input/paper-input-char-counter.js';

import '/resources/components/tg-confirmation-dialog.js';
import '/resources/images/tg-icons.js';

import {TgReflector} from '/app/tg-reflector.js';

import {PolymerElement, html} from '/resources/polymer/@polymer/polymer/polymer-element.js';
import {GestureEventListeners} from '/resources/polymer/@polymer/polymer/lib/mixins/gesture-event-listeners.js';

import { tearDownEvent, allDefined, resultMessages, deepestActiveElement, isInHierarchy } from '/resources/reflection/tg-polymer-utils.js';

let checkIconTimer = null;

let lastEditor = null;

let qrCodeScanner = null;

const timeoutCheckIcon = function (editor) {
    if (checkIconTimer) {
        clearTimeout(checkIconTimer);
        if(editor !== lastEditor) {
            hideCheckIcon();
            showCheckIcon(editor);
        }
    } else {
        showCheckIcon(editor);
    }
    checkIconTimer = setTimeout(function() {
        hideCheckIcon();
    }, 1000);
}

const showCheckIcon = function (editor) {
    lastEditor = editor;
    lastEditor.$.copyIcon.icon = "icons:check";
    lastEditor.addEventListener("mouseleave", hideCheckIconOnMouseLeave);
}

const hideCheckIcon = function () {
    lastEditor.$.copyIcon.icon = "icons:content-copy";
    lastEditor.removeEventListener("mouseleave", hideCheckIconOnMouseLeave);
    checkIconTimer = null;
    lastEditor = null;
}

const hideCheckIconOnMouseLeave = function () {
    if (!lastEditor.focused) {
        if (checkIconTimer) {
            clearTimeout(checkIconTimer);
            checkIconTimer = null;
        }
        hideCheckIcon();
    }
}

const defaultLabelTemplate = html`
    <label style$="[[_calcLabelStyle(_editorKind, _disabled)]]" disabled$="[[_disabled]]" tooltip-text$="[[_getTooltip(_editingValue, _scanAvailable)]]" slot="label">
        <span class="label-title" on-down="_labelDownEventHandler">[[propTitle]]</span>
        <iron-icon class="label-action" hidden$="[[noLabelFloat]]" id="copyIcon" icon="icons:content-copy" on-tap="_copyTap"></iron-icon>
        <iron-icon class="label-action" hidden$="[[!_scanAvailable]]" id="scanIcon" icon="tg-icons:qrcode-scan" on-down="_preventFocusOut" on-tap="_scanTap"></iron-icon>
    </label>`;

export function createEditorTemplate (additionalTemplate, customPrefixAttribute, customInput, inputLayer, customIconButtons, propertyAction, customLabelTemplate) {
    return html`
        <style>
            :host {
                --paper-input-container-input-color: var(--paper-grey-900);
                --paper-input-container-focus-color: var(--paper-indigo-500);
                --paper-input-container-color: var(--light-theme-secondary-color);
                --paper-input-container-disabled: {
                    opacity: 1;
                    pointer-events: auto;
                };
            }
            .custom-input-wrapper, .custom-input {
                @apply --paper-input-container-shared-input-style;
                font-weight: 500;
                text-align: left;
            }
            label {
                cursor: default;
                @apply --layout-horizontal;
                @apply --layout-center;
            }
            .label-title {
                margin-right: 4px;
            }
            #decorator[focused]:not([disabled]) .label-title {
                font-weight: 700;
            }
            #decorator[focused]:not([disabled]) .label {
                font-weight: 700;
            }
            .label-action {
                width: 18px;
                height: 18px;
                padding: 0 4px;
                cursor: pointer;
            }
            .input-layer {
                font-size: 16px;
                line-height: 24px;
                font-weight: 500;
                display: none;
                position: absolute;
                background-color: inherit;
                pointer-events: none;
                top: 0;
                bottom: 0;
                left: 0;
                right: 0;
            }

            .main-container {
                position:relative;
                @apply --layout-vertical;
            }

            .editor-prefix,
            .editor-suffix {
                @apply --layout-horizontal;
            }
            
            #decorator {
                --paper-input-container-input: {
                    font-weight: 500;
                }
                --paper-input-char-counter: {
                    position: absolute; /* to not extend the height of editor when paper-input-char-counter is present; similarly to paper-input-error default behaviour */
                    right: 0; /* push it to the end */
                }
            }
            
            #decorator[disabled] {
                --paper-input-container-underline-focus: {
                    visibility:hidden;
                }
            }
            #decorator[disabled] .input-layer {
                pointer-events: auto;
            }
            #decorator[has-layer][disabled] .input-layer,
            #decorator[has-layer]:not([focused]) .input-layer {
                display: var(--tg-editor-default-input-layer-display, inherit);
            }
            #decorator .input-layer {
                color: var(--paper-input-container-input-color, var(--primary-text-color));
            }
            #decorator[has-layer][disabled] .custom-input,
            #decorator[has-layer]:not([focused]) .custom-input {
                opacity: 0;
            }

            /* style requiredness */
            #decorator.required {
                --paper-input-container-color: #03A9F4;
                --paper-input-container-focus-color: #03A9F4;
            }

            /* style visible paper-input-error */
            #decorator[is-invalid] {
                --paper-input-error: {
                    cursor: pointer; /* cursor to indicate action presence */
                    z-index: 1; /* always above other editors (selection crit) to be able to tap */
                    max-width: 100%; /* ensure paper-input-error width as in its content */
                    right: auto !important; /* ensure paper-input-error width as in its content */
                }
            }

            /* style informative */
            #decorator[is-invalid].informative {
                --paper-input-container-color: #8E24AA;
                --paper-input-container-invalid-color: #8E24AA;
            }

            /* style warning */
            #decorator[is-invalid].warning {
                --paper-input-container-color: #FFA000;
                --paper-input-container-invalid-color: #FFA000;
            }

            /* style error */
            #decorator[is-invalid]:not(.warning):not(.informative) {
                --paper-input-container-color: var(--google-red-500);
            }
        </style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
        <tg-confirmation-dialog id="confirmationDialog"></tg-confirmation-dialog>
        ${additionalTemplate}
        <paper-input-container id="decorator" always-float-label no-label-float="[[noLabelFloat]]" has-layer$="[[_hasLayer]]" invalid="[[_invalid]]" is-invalid$="[[_invalid]]" disabled$="[[_disabled]]" focused$="[[focused]]">
            <!-- flex auto  for textarea! -->
            ${customLabelTemplate || defaultLabelTemplate}
            <div clss="editor-prefix" slot="prefix">
                ${customPrefixAttribute}
            </div>
            <div id="mainContainer" class="main-container" slot="input">
                ${customInput}
                ${inputLayer}
            </div>
            <div class="editor-suffix" slot="suffix">
                ${customIconButtons}
                ${propertyAction}
            </div>
            <!-- 'autoValidate' attribute for paper-input-container is 'false' -- all validation is performed manually and is bound to paper-input-error, which could be hidden in case of empty '_error' property -->
            <paper-input-error hidden$="[[!_error]]" disabled$="[[_disabled]]" tooltip-text$="[[_extendedError]]" slot="add-on" on-tap="_inputErrorTapHandler">[[_error]]</paper-input-error>
            <!-- paper-input-char-counter addon is updated whenever 'bindValue' property of child '#input' element is changed -->
            <paper-input-char-counter id="inputCounter" class="footer" hidden$="[[!_isMultilineText(_editorKind)]]" disabled$="[[_disabled]]" slot="add-on"></paper-input-char-counter>
        </paper-input-container>
       
        <template is="dom-if" if="[[debug]]">
            <p>_editingValue: <i>[[_editingValue]]</i>
            </p>
            <p>_commValue: <i>[[_commValue]]</i>
            </p>
            <p>_acceptedValue: <i>[[_acceptedValue]]</i>
            </p>
        </template>`;
};

export class TgEditor extends GestureEventListeners(PolymerElement) {

    static get properties() {
        return {
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            // These mandatory properties must be specified in attributes, when constructing <tg-*-editor>s.       //
            // No default values are allowed in this case.														   //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            /**
             * The title for this editor. It normally appears as the caption for the editor.
             */
            propTitle: {
                type: String
            },
    
            /**
             * The description for this editor.
             */
            propDesc: {
                type: String
            },
    
            /**
            * True if the input is in focus, otherwise false.
            */
            focused: {
                readOnly: true,
                type: Boolean,
                value: false,
                notify: true
            },

            /**
             * Determines whether label is floatable or not.
             */
            noLabelFloat: {
                type: Boolean,
                value: false,
            },

            /**
             * Determines whether the QR code scanner action in the title should be hidden. 
             */
            hideQrCodeScanner: {
                type: Boolean,
                value: false,
            },
    
            /**
             * This published property specifies to what binding entity this editor should be bound.
             */
            entity: {
                type: Object,
                observer: '_entityChanged',
                notify: true
            },
            
            /**
             * The entity that contains original binding values. This should be used to identify whether _editingValue is modified from original _editingValue during editing.
             */
            originalEntity: {
                type: Object,
                observer: '_originalEntityChanged',
                notify: true
            },
            
            /**
             * This published property specifies to what property this editor should be bound.
             */
            propertyName: {
                type: String
            },
    
            /**
             * This callback should be used for custom action after the '_acceptedValue' has been changed (for e.g. validation).
             */
            validationCallback: {
                type: Function
            },
            
            /**
             * The state for the editor (governed by external hosts, that hold this editor).
             *
             * The editor can be only in two states: EDIT and VIEW. The state EDIT
             * allows user to edit property.
             *
             * The state VIEW allows user to review the property.
             *
             * The initial state can be VIEW or EDIT.
             */
            currentState: {
                type: String
                // TODO why is this needed??? reflectToAttribute: true
            },
            
            /**
             * The action object that represents an action to be embedded as an icon button inside this editor.
             *
             * If the action attribute is 'null' -- no action button should be displayed.
             *
             * Action object and 'null' are the only permitted values.
             */
            action: {
                type: Object
            },

            /**
             * The property action index to show. The default value should be '-1' to hide all property actions. The index should be calculated on server. 
             */
            propertyActionIndex: {
                type: Number,
                value: -1,
                observer: '_propertyActionIndexChanged'
            },

            /**
             * The property actions embedded into this editor. Only one of these action will be visible that corresponds to propertyActionIndex property 
             */
            propertyActions: {
                type: Array,
                value: () => []
            },
    
            ////////////////////////////////////// SUBSECTION: NOT MANDATORY PROPERTIES //////////////////////////////////////
            /**
             * Controls rendering of debug information for an entity editor. 
             */
            debug: {
                type: Boolean
            },
            
            /**
             * This modif holder is needed for lazy value conversion.
             */
            previousModifiedPropertiesHolder: {
                type: Object
            },

            /**
             * The object that holds callbacks for showing toast.
             */
            toaster: {
                type: Object,
                value: null
            },

            /**
             * Defines standard invalid message for built-in validation of 'input'.
             * Should be empty in case if such validation is not supported.
             */
            builtInValidationMessage: {
                type: String,
                value: null
            },

            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////// INNER PROPERTIES ///////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
            //   prefix and default values specified in 'value' specificator of the property definition (or,       //
            //   alternatively, computing function needs to be specified). 									       //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            _hasLayer:{
                type: Boolean
            },
    
            _editorKind:{
                type: String
            },
    
            _disabled: {
                type: Boolean,
                computed: '_isDisabled(currentState, entity, propertyName)',
                observer: '_disabledChanged'
            },

            _scanAvailable: {
                type: Boolean,
                computed: '_canScan(hideQrCodeScanner, noLabelFloat, entity, propertyName)'
            },
    
            _invalid: {
                type: Boolean,
                value: false
            },
            
            /**
             * The message about the editor-specific validation. If 'null' -- the validation was successfull.
             */
            _editorValidationMsg: {
                type: String,
                value: null,
                observer: '_editorValidationMsgChanged'
            },
            
            /**
             * Returns 'true' in case where 'entity', 'propertyName' and '_editorValidationMsg' have been already defined, 'false' otherwise.
             */
            _validationComponentsDefined: {
                type: Boolean,
                value: false
            },
            
            /**
             * Indicates whether 'refresh cycle' has been initiated, which means that new entity has been arrived.  
             *   After the '_editingValue' has been populated -- this value should be immediately committed, but 
             *   without additional validation.
             *
             * 'true' and 'false' are the only permitted values.
             */
            _refreshCycleStarted: {
                type: Boolean, 
                value: false,
                observer: '_refreshCycleStartedChanged'
            },
            
            /**
             * The validation error message.
             */
            _error: {
                type: String,
                value: null
            },

            /**
             * The extended validation error message.
             */
            _extendedError: {
                type: String,
                value: null
            },
    
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
            //   prefix. 																				           //
            // Also, these properties are designed to be bound to children element properties -- it is necessary to//
            //   populate their default values in ready callback (to have these values populated in children)!     //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            /**
             * The value being edited (main editing capability). If there are other editing views in this editor -- maintain their editingValues separately.
             *
             * This value is of the data type for editing (main editing capability), most likely String.
             */
            _editingValue: {
                type: String,
                observer: '_editingValueChanged'
            },
            
            /**
             * The value being committed (main editing capability). If there are other editing views in this editor -- maintain their committedValues separately.
             *
             * This value is of the data type for editing (main editing capability), most likely String.
             */
            _commValue: {
                type: String,
                observer: '_commValueChanged'
            },
            
            /**
             * The value being accepted after the editing. The commit can be done using 'TAB off' or 'Enter key pressed'.
             *
             * This value is of the data type for concrete component, for e.g. for tg-datetime-picker it is Number, tg-textfield -- String etc.
             * The type strictly conforms to the type of 'bindTo' attribute.
             */
            _acceptedValue: {
                type: String,
                observer: '_acceptedValueChanged'
            },
            
            /**
             * The mouse tap event listener that selectes the text inside input when first time tapped.
             */
            _onMouseDown: {
                type: Function,
                value: function () {
                    return (function (event) {
                        if (this.shadowRoot.activeElement !== this.decoratedInput()) {
                            this.decoratedInput().select();
                            this._tearDownEventOnUp = true;
                        }
                    }).bind(this);
                }
            },

            _onMouseUp: {
                type: Function,
                value: function () {
                    return (function (event) {
                        if (this._tearDownEventOnUp) {
                            tearDownEvent(event);
                            delete this._tearDownEventOnUp;
                        }
                    }).bind(this);
                }
            },
            
            /**
             * This event is invoked after the component gained focus.
             *
             * Designated to be bound to child elements.
             */
            _onFocus: {
                type: Function,
                value: function () {
                    return (function (event) {
                        this._setFocused(true);
                        if (this._updateManuallyFocusedInputWith) {
                            this._updateManuallyFocusedInputWith(this._focusTarget(event.target));
                        }
                    }).bind(this);
                }
            },
        
            /**
             * This event is invoked after the component lost focus.
             *
             * Designated to be bound to child elements.
             */
            _outFocus: {
                type: Function,
                value: function () {
                    return (function (event) {
                        this._setFocused(false);
                        this._checkBuiltInValidation();
                        if (this._updateManuallyFocusedInputWith) {
                            this._updateManuallyFocusedInputWith(null);
                        }
                    }).bind(this);
                }
            },
            
            /**
             * This event is invoked after the component has been changed (it is invoked after the focus was lost). Provides value commit behaviour.
             *
             * Designated to be bound to child elements.
             */
            _onChange: {
                type: Function,
                value: function () {
                    return (function (event) {
                        // console.debug("_onChange:", event);
                        if (this['_onChange_handler']) {
                            clearTimeout(this['_onChange_handler']);
                        }
                        this['_onChange_handler'] = setTimeout(function() {
                            this.commitIfChanged();
                        }.bind(this), 50);
                    }).bind(this);
                    }
            },
            
            /**
             * This event is invoked after some key has been pressed. We are interested in 'Enter' key to provide value commit behaviour.
             *
             * Designated to be bound to child elements.
             */
            _onKeydown: {
                type: Function,
                value: function () {
                    return (function (event) {
                        // console.debug("_onKeydown:", event);
                        if (event.keyCode === 13) { // 'Enter' has been pressed
                            this._checkBuiltInValidation();
                            this.commitIfChanged();
                        } else if ((event.keyCode === 38 || event.keyCode === 40) 
                                    && (event.altKey || event.ctlKey || event.metaKey || event.shiftKey)) {
                            tearDownEvent(event);
                        }
                        this._handleCopy(event);

                    }).bind(this);
                }
            },
            
            /**
             * This event is invoked after some key has been pressed.
             *
             * Designated to be bound to child elements.
             */
            _onInput: {
                type: Function,
                value: function () {
                    return (function (event) {
                        // console.debug("_onInput:", event);
                    }).bind(this);
                }
            }
            
            /* The following functions will potentially be needed. In this case, use the appropriate form of 'function-property' definition (to be able to bind to child elements). */
    
            /* _onInput: function (event) {
                console.log("_onInput:", event);
            },
            
            _onBlur: function (event) {
                console.log("focus lost: _onBlur:", event);
            },
            
            _onFocus: function (event) {
                console.log("focus got: _onFocus:", event);
            }, */
        };
    }

    static get observers() {
        return [
            '_recordDefinition(entity, propertyName, _editorValidationMsg)',
            '_identifyModification(_editingValue, entity)',
            '_editedPropsChanged(entity.@editedProps)'
        ]
    }

    constructor () {
        super();
        this._reflector = new TgReflector();
        //////////// INNER PROPERTIES, THAT GOVERN CHILDREN: default values population ////////////
        this._editingValue = this._defaultEditingValue();
        // The following 'commit' call synchronises '_commValue' with '_editingValue' after default editing value population.
        //  Please, also note that this call also triggers '_acceptedValue' population, as per '_commValueChanged' method.
        this.commit();
    }

    ready () {
        super.ready();
        this._ensureAttribute('selectable-elements-container', true);
        this.decorator().labelVisible = false;
        this._ensureAttribute('tg-editor', true);
        if (!this._editorKind) {
            this._editorKind = 'NOT_MULTILINETEXT_OR_BOOLEAN';
        }
        //Initialising multi actions
        this.propertyActions = (this.$.actionSlot && [...this.$.actionSlot.assignedNodes({flatten: true})]) || [];
        this.propertyActions.forEach(action => {
            action.setAttribute('hidden', '');
        });
    }

    /**
     * Returns a concrete element in `_onFocus` `target` to be stored for further focusing.
     */
    _focusTarget (target) {
        return target;
    }

    isInWarning () {
        return this.$.decorator.classList.contains("warning");
    }

    isWithInformative() {
        return this.$.decorator.classList.contains("informative");
    }

    reflector () {
        return this._reflector;
    }

    decorator () {
        return this.$.decorator;
    }

    _handleCopy (event) {
        if (event.keyCode === 67 && event.altKey && (event.ctrlKey || event.metaKey)) { //(CTRL/Meta) + ALT + C
            this.commitIfChanged();
            this._copyTap();
        }
    }
    
    _isMultilineText (editorKind) {
        return 'MULTILINE_TEXT' === editorKind;
    }

    /**
     * Calculates the style for container's label.
     */
    _calcLabelStyle (editorKind, _disabled) {
        var style = "";
        if ("BOOLEAN" === editorKind) {
            style += "visibility: hidden;"
        } else if ("COLLECTIONAL" === editorKind) {
            style += "display: none;"
        }
        if (_disabled === true) {
            style += "opacity: 1;"
        }
        return style;
    }
    
    /**
     * Calculates the style for decorator inner parts, based on '_disabled' property.
     */
    _calcDecoratorPartStyle (_disabled) {
        var style = "min-width: 0px;";
        if (_disabled === true) {
            style += "opacity: 1;"
        }
        return style;
    }

    /**
     * The observer to the '_disabled' property, which maintains appropriately the class list of the decorator (regarding the class 'decorator-disabled').
     */
    _disabledChanged (newValue, oldValue) {
        if (newValue === true) {
            this.$.decorator.classList.add("decorator-disabled");
        } else {
            this.$.decorator.classList.remove("decorator-disabled");
        }
        this.updateStyles();
    }
    
    _recordDefinition (entity, propertyName, _editorValidationMsg) {
        if (this._validationComponentsDefined === false) {
            this._bindMessages(entity, propertyName, _editorValidationMsg);
            this._validationComponentsDefined = true;
        }
    }
    
    _identifyModification(_editingValue, entity) {
        if (this.reflector().isEntity(entity)) {
            const _originalEditingValue = entity ? this._extractOriginalEditingValue(entity, this.propertyName) : _editingValue;
            // console.debug('_bindingEntity (_identifyModification) self = ', this.is, '_editingValue', _editingValue, '_originalEditingValue', _originalEditingValue);
            const prevEditedProps = entity['@editedProps'];
            const newEditedProps = {};
            for (var prop in prevEditedProps) {
                if (prevEditedProps.hasOwnProperty(prop)) {
                    newEditedProps[prop] = prevEditedProps[prop];
                }
            }
            if (!this._equalToOriginalValue(this._editingValue, _originalEditingValue)) {
                newEditedProps[this.propertyName] = true;
            } else {
                delete newEditedProps[this.propertyName];
            }
            this.set('entity.@editedProps', newEditedProps);
            // if ('some_name' === this.propertyName) { console.error('_identifyModification: prop [', this.propertyName, '] originalEditingValue = [', _originalEditingValue, '] editingValue = [', _editingValue, '] newEditedProps [', newEditedProps, ']'); }
        }
    }

    /**
     * Returns 'true' in case if '_editingValue' is equal to original during typing (no validation request phase).
     *   This method may be overridden in case if some fine tuning for SAVE disablement logic is needed.
     *   E.g. some third party component (Toast UI in Rich Text editor) may provide some transformations to _editingValue ('hello world' => '<p>hello world</p>'), which we consider insignificant and don't want to show to the user.
     */
    _equalToOriginalValue (_editingValue, _originalEditingValue) {
        return this.reflector().equalsEx(_editingValue, _originalEditingValue);
    }

    /**
     * Observer for 'refresh cycle' stage changes.
     *   This method may be overridden in case if some fine tuning is required to be performed when 'refresh cycle' started / completed.
     */
    _refreshCycleStartedChanged (newValue, oldValue) {}

    /**
     * Extracts 'original' version of editing value taking into account the erroneous properties.
     *
     * Please note that 'original' notion has nothing to do with 'entity.getOriginal() values' or 'originalBindingEntity'.
     * We just care about 'original' editing value that we started with immediately after validation request has bee completed 
     * and before next consequent validation request; aka during editing without value committing.
     *
     * Erroneous values are considered here too. Binding entities creation contains such logic.
     */
    _extractOriginalEditingValue (bindingEntity, propertyName) {
        return this.convertToString(this.reflector().tg_getBindingValue.bind(this.reflector())(bindingEntity, propertyName));
    }
    
    _editedPropsChanged (editedProps) {
    }

    /**
     * This function returns the tooltip for this editor.
     */
    _getTooltip (value, _scanAvailable) {
        var tooltip = this._formatTooltipText(value);
        tooltip += this.propDesc && (tooltip ? '<br><br>' : '') + this.propDesc;
        tooltip += (tooltip ? '<br><br>' : '') + this._getActionTooltip(_scanAvailable);
        return tooltip;
    }

    /**
     * Returns tooltip for action
     */
    _getActionTooltip (_scanAvailable) {
        const actionTooltips = [];
        actionTooltips.push(this._getCopyActionTooltip());
        actionTooltips.push(this._getScanActionTooltip(_scanAvailable));
        const filteredActionTooltips = actionTooltips.filter(tooltip => !!tooltip);
        return `<div style='display:flex;'>
            <div style='margin-right:10px;'>${filteredActionTooltips.length > 1 ? "With actions:" : "With action:"} </div>
            <div style='flex-grow:1;'>${filteredActionTooltips.join("<br><br>")}</div>
            </div>`
    }

    _getCopyActionTooltip() {
        return "<b>Copy</b><br>Copy content"
    }

    _getScanActionTooltip(_scanAvailable) {
        return _scanAvailable ? "<b>Scan</b><br>Scan QR or Bar code" : "";
    }
    
    /**
     * This method returns a default value for '_editingValue', which is used 
     *  for representing the value when no entity was bound to this editor yet.
     *
     * Please, override this method in case where empty string is not applicable (for example in boolean editor 'true' or 'false' values are applicable only).
     */
    _defaultEditingValue () {
        return '';
    }
    
    decoratedInput () {
        return this.$.input;
    }
    
    /**
     * Returns 'true' if the editor is disabled, 'false' otherwise (based on the editor's state and 'editable' meta-state for the property).
     */
    _isDisabled (currentState, bindingEntity, propertyName) {
        if (!allDefined(arguments)) {
            return true;
        } else if (currentState === 'VIEW') {
            return true;
        } else if (currentState === 'EDIT') {
            if (this.reflector().isEntity(bindingEntity)) {
                return this.reflector().isDotNotated(propertyName) ? true : (!(bindingEntity["@" + propertyName + "_editable"]));
            } else {
                return true;
            }
        } else {
            throw "Unsupported state exception: " + currentState + ".";
        }
    }

    /**
     * This method is called during editing.
     *
     * IMPORTANT: please do override this method if needed, but only with this.super([oldValue, newValue]); invoked!
     */
    _editingValueChanged (newValue, oldValue) {
        // console.debug("_editingValueChanged", oldValue, newValue, "_refreshCycleStarted ==", this._refreshCycleStarted);
        
        // TODO provide alternative?
        // TODO provide alternative?
        // TODO provide alternative?
        // TODO provide alternative?
        // TODO provide alternative?
        // TODO provide alternative?
        // this.decorator().updateLabelVisibility(this._editingValue);

        if (this._refreshCycleStarted === true) {
            this.commit();
        }
    }

    /**
     * Converts the value of `property` and assigns the result to `bindingEntity[property]`.
     *
     * All non-dot-notated properties are converted here.
     * 
     * Also, this method converts root properties for dot-notated properties.
     * This is necessary to guarantee that a root property value, assigned in a definer of another property,
     * will not get lost on subsequent validation/saving cycles.
     * Such loss was happening when a value, from which root property was defined, got removed as part of some domain logic (e.g., a definer).
     * This method implements an automatic conversion to include cases where a root property is not on the master and not in the 'isNecessaryForConversion' list.
     *
     * @param original -- whether to use the original value of `property` or the current one
     * @param customFullEntity -- if specified, then `property` is read from this entity
     */
    _convertPropertyValue (bindingEntity, property, original, customFullEntity) {
        if (!this.reflector().isDotNotated(property)) {
            const fullEntity = customFullEntity || this.reflector().tg_getFullEntity(bindingEntity);
            if (original) {
                this.reflector().tg_convertOriginalPropertyValue(bindingEntity, property, fullEntity);
            } else {
                this.reflector().tg_convertPropertyValue(bindingEntity, property, fullEntity, this.previousModifiedPropertiesHolder);
            }
        } else {
            this._convertPropertyValue(bindingEntity, property.substring(0, property.lastIndexOf('.')), original, customFullEntity);
        }
    }

    _originalEntityChanged (newValue, oldValue) {
        if (this.reflector().isEntity(newValue)) {
            // lazy conversion of original property value performs here (previusly it was done for all properties inside tg-entity-binder-behavior)
            this._convertPropertyValue(newValue, this.propertyName, true /* original? */);
        }
    }

    /**
     * This method is called once the entity was changed from the outside of the component.
     *
     * IMPORTANT: please do override this method if needed, but only with this.super([oldValue, newValue]); invoked!
     */
    _entityChanged (newValue, oldValue) {
        // console.log("_entityChanged", newValue, oldValue, "still _refreshCycleStarted ==", this._refreshCycleStarted);
        if (this.reflector().isEntity(newValue)) {
            // IMPORTANT: Initiate 'refresh cycle' -- in new logic refresh cycle is also mandatory after 'validation' has been performed,
            // not only after master's 'save' / 'refresh' or centre's 'run', 'save' or 'discard'
            // (to be precise it is done for every case where _currBindingEntity is changed for this editor)
            this._refreshCycleStarted = true;
            
            // lazy conversion of property value performs here (previusly it was done for all properties inside tg-entity-binder-behavior)
            this._convertPropertyValue(newValue, this.propertyName, false /* original? */);
            
            const convertedValue = this.reflector().tg_getBindingValue.bind(this.reflector())(newValue, this.propertyName);
            const newEditingValue = this.convertToString(convertedValue);
            if (newEditingValue === this._editingValue) {
                this._refreshCycleStarted = false;
                this._updateMessagesForEntity(newValue);
            } else {
                const editingValueAfterPreviousRefreshCycleChanged = oldValue 
                    ? this.convertToString(this.reflector().tg_getBindingValue.bind(this.reflector())(oldValue, this.propertyName)) !== newEditingValue 
                    : true;
                
                if (!editingValueAfterPreviousRefreshCycleChanged) {
                    if (!newValue["@" + this.propertyName + "_editable"]) {
                        this._editingValue = newEditingValue;
                        this._updateMessagesForEntity(newValue);
                    } else {
                        this._refreshCycleStarted = false;
                    }
                } else {
                    this._editingValue = newEditingValue;
                    this._updateMessagesForEntity(newValue);
                }
            }
        } else {
            // console.debug("_entityChanged: Not yet initialised _currBindingEntity, from which to get binding value!");
            this._updateMessagesForEntity(newValue);
            this._editingValue = this._defaultEditingValue();
            this.commit();
        }
        this._tryFireErrorMsg(this._error);
    }

    _labelDownEventHandler (event) {
        // Select text inside editor and focus it, if it is enabled and not yet focused.
        // Selection of the text on-focus is consistent with on-tap action in the editor or focus gain logic when tabbing between editors.
        if (this.shadowRoot.activeElement !== this.decoratedInput() && !this._disabled) {
            this.decoratedInput().select();
            this.decoratedInput().focus();
        }
        // Need to tear down the event for the editor to remain focused.
        tearDownEvent(event);
    }

    _copyTap () {
        // copy to clipboard should happen only if there is something to copy
        if (navigator.clipboard && this._editingValue) {
            navigator.clipboard.writeText(this._editingValue);
            this._showCheckIconAndToast(this._editingValue);
        } else if (this.toaster) {
            this.toaster.openToastWithoutEntity("Nothing to copy", true, "There was nothing to copy.", false);
        }
    }

    /*************************QR Code Scanner related methods****************************************/
    _preventFocusOut(e) {
        tearDownEvent(e);
    }

    _scanTap () {
        if (qrCodeScanner === null) {
            qrCodeScanner = document.getElementById("qrScanner");
        }
        if (qrCodeScanner) {
            qrCodeScanner.toaster = this.toaster;
            qrCodeScanner.closeCallback = this._closeScanner(this.focused);
            qrCodeScanner.applyCallback = this._applyScannerValue.bind(this);
            qrCodeScanner.open();
        } else {
            throw new Error("QR code scanner is not present in DOM. Please add it with 'qrScanner' id");
        }
    }

    _closeScanner (wasFocused) {
        return () => {
            if (qrCodeScanner) {
                qrCodeScanner.toaster = null;
                qrCodeScanner.closeCallback = null;
                qrCodeScanner.applyCallback = null;
                if (wasFocused) {
                    this._labelDownEventHandler();
                }
            }
        }
    }

    _applyScannerValue (value) {
        this._editingValue = value;
        this._checkBuiltInValidation();
        this.commitIfChanged();
    }

    _canScan (hideQrCodeScanner, noLabelFloat, entity, propertyName) {
        if (allDefined(arguments)) {
            const metaPropEditable = this.reflector().isEntity(entity) && !this.reflector().isDotNotated(propertyName)
                                 ? entity["@" + propertyName + "_editable"]
                                 : false;
            return !hideQrCodeScanner && !noLabelFloat && metaPropEditable
        }
        return false;
    }
    /*********************************** QR Code Scanner related methods end******************************/

    _showCheckIconAndToast (text) {
        if (this.toaster) {
            this.toaster.openToastWithoutEntity("Copied!", true, text, false);
        }
        timeoutCheckIcon(this);
    }
    
    _updateMessagesForEntity (newEntity) {
        if (this._validationComponentsDefined === true) {
            this._bindMessages(newEntity, this.propertyName, this._editorValidationMsg);
        }
    }
    
    _assignConvertedValue (propValue) {
        const newEditingValue = this.convertToString(propValue);
        if (newEditingValue === this._editingValue && (this._refreshCycleStarted === true) ) {
            this._refreshCycleStarted = false;
        }
        this._editingValue = newEditingValue;
    }
    
    assignValue (entity, propertyName, tg_getBindingValueFromFullEntity) {
        const convertedValue = tg_getBindingValueFromFullEntity(entity, propertyName);
        this._assignConvertedValue(convertedValue);
    }
    
    assignConcreteValue (value, converter) {
        const convertedValue = converter(value);
        this._assignConvertedValue(convertedValue);
    }

    /**
     * This method is called once the the accepted value was changed after the editor has commited its value.
     *
     * IMPORTANT: please do not override this method.
     */
    _commValueChanged (newValue, oldValue) {
        // console.log("_commValueChanged", oldValue, newValue, "_refreshCycleStarted ==", this._refreshCycleStarted);
        try {
            const _refreshCycleStarted = this._refreshCycleStarted;
            this._acceptedValue = this.convertFromString(newValue);
            // if built-in 'input' validation is not supported, clear _editorValidationMsg on successful value conversion;
            // if built-in 'input' validation not supported, clear for the case if Refresh button pressed or in the case where the editor validation message not equals to built-in one
            if (!this.builtInValidationMessage || _refreshCycleStarted === true || this._editorValidationMsg !== this.builtInValidationMessage) {
                this._editorValidationMsg = null;
            }
        } catch (error) {
            console.log("_commValueChanged catched", error, this);
            this._editorValidationMsg = error;
        }
    }

    /**
     * This method is called once the the accepted value was changed after the editor has commited its value.
     *
     * IMPORTANT: please do not override this method. This method have some additional customisation points:
     * _shouldInvokeValidation() and _skipValidationAction().
     */
    _acceptedValueChanged (newValue, oldValue) {
        // console.log("_acceptedValueChanged", oldValue, newValue, "_refreshCycleStarted ==", this._refreshCycleStarted);

        if (this._refreshCycleStarted) {
            this._refreshCycleStarted = false;
            // console.log("_acceptedValueChanged should become false. _refreshCycleStarted ==", this._refreshCycleStarted);
        } else {
            // The following logic shouldn't be executed if binding entity is not fully initialised (i.e. this.entity wasn't defined or _entityChanged wasn't executed).
            // This is due to the problem that property change is not atomic in Polymer 3.
            // The problem manifests itself on an early stage of element lifecycle, before the element is attached.
            // All observers and ready callback are postponed until the element will be inserted into DOM (https://github.com/Polymer/polymer/issues/4526).
            if (this.reflector().isEntity(this.entity) && typeof this.entity[this.propertyName] !== 'undefined') {
                this.entity.setAndRegisterPropertyTouch(this.propertyName, newValue);
                
                if (this._shouldInvokeValidation()) {
                    this.validationCallback();
                } else {
                    this._skipValidationAction();
                }
            }
        }
    }
    
    /**
     * Commits recently edited value (_editingValue) skipping validation cycle.
     *
     * This method performs all editor-driven approximations, commits value to _commValue, _acceptedValue and into binding entity that is bound into this editor.
     */
    commitWithoutValidation () {
        // turn validation off
        const _shouldInvokeValidation = this._shouldInvokeValidation;
        this._shouldInvokeValidation = function () { return false; };
        // perform regular 'commit' with all (custom to concrete editor implementation) approximations, property touching logic etc.
        this.commit();
        // turn validation on again
        this._shouldInvokeValidation = _shouldInvokeValidation;
    }
    
    /**
     * Please override this method in case where no validation should occur after _acceptedValueChanged.
     */
    _shouldInvokeValidation () {
        return true;
    }

    /**
     * Please override this method in case where some custom action is needed if _shouldInvokeValidation() returns 'false' after _acceptedValueChanged.
     */
    _skipValidationAction () {}

    /**
     * Converts the value into string representation (which is used in editing / comm values). Override this method in descendant editor to get some specific behavior.
     */
    convertToString (value) {
        return this.reflector().tg_toString(value, this.entity.type(), this.propertyName, { bindingValue: true });
    }

    /**
     * Converts the value from string representation (which is used in editing / comm values) into concrete type of this editor component. Please implement this method in descendant editor.
     */
    convertFromString (strValue) {
        // return strValue;
        throw "Conversion from string into entity property type is not specified for this editor.";
    }

    /**
     * Commits editing value.
     */
    commit () {
        // console.debug('COMMIT: start.');
        if (this.reflector().isEntity(this.entity)) {
            if (typeof this.entity["@" + this.propertyName + "_uppercase"] !== 'undefined') {
                var upperCased = this._editingValue.toLocaleUpperCase();
                console.debug('COMMIT (value should be uppercased): current editingValue = [', this._editingValue, '] upperCased = [', upperCased, ']');
                if (!this.reflector().equalsEx(upperCased, this._editingValue)) {
                    console.debug('COMMIT (value should be uppercased): change editingValue to [', upperCased, ']');
                    this._editingValue = upperCased;
                }
            }
            this._commitForDescendants();
        }
        // console.debug("COMMIT: [", this._editingValue, "] value.");
        this._commValue = this._editingValue;
    }

    /**
     * Commits editing value '_editingValue' in case if it is changed from previously committed value '_commValue'.
     */
    commitIfChanged () {
        if (!this.reflector().equalsEx(this._editingValue, this._commValue)) {
            this.commit();
        }
    }
    
    /**
     * Please, override this method (in descendant editors) in case where some custom '_editingValue' preprocessing is needed. 
     */
    _commitForDescendants () {
    }
    
    _bindMessages (entity, propertyName, _editorValidationMsg) {
        // console.log("_bindMessages: ", entity, propertyName, _editorValidationMsg);
        if (_editorValidationMsg !== null) {
            this._bindError(resultMessages({ message: _editorValidationMsg }));
        } else if (this.reflector().isEntity(entity)) {
            // please, note that dot-notated property will not have any errors / warnings / requiredness
            //     - for these props it does not make sense to propagate such meta-information from
            //     parent property -- the parent prop (if added in master) will show that errors concisely
            if (typeof entity["@" + propertyName + "_error"] !== 'undefined') {
                this._bindError(resultMessages(entity["@" + propertyName + "_error"]));
            } else if (typeof entity["@" + propertyName + "_warning"] !== 'undefined') {
                this._bindWarning(resultMessages(entity["@" + propertyName + "_warning"]));
            } else if (typeof entity["@" + propertyName + "_informative"] !== 'undefined') {
                this._bindInformative(resultMessages(entity["@" + propertyName + "_informative"]));
            } else if (typeof entity["@" + propertyName + "_required"] !== 'undefined') {
                this._bindRequired(entity["@" + propertyName + "_required"]);
            } else {
                this._resetMessages();
            }

            this._bindUppercase(entity, propertyName);

        } else {
            this._resetMessages();
            this._resetMetaPropDecorations();
        }
    }
    
    _editorValidationMsgChanged (newValue, oldValue) {
        if (this._validationComponentsDefined === true) {
            this._bindMessages(this.entity, this.propertyName, newValue);
            this._tryFireErrorMsg(newValue);
        }
    }
    
    _tryFireErrorMsg (error) {
        if (error) {
            this.dispatchEvent(new CustomEvent('editor-error-appeared', { bubbles: true, composed: true, detail: this }));
        }
    }

    _bindUppercase (entity, propertyName) {
        if (typeof entity["@" + propertyName + "_uppercase"] !== 'undefined') {
            this.decoratedInput().classList.add("upper-case");
        } else {
            this.decoratedInput().classList.remove("upper-case");
        }
        this.updateStyles();
    }

    _resetMetaPropDecorations () {
        this.decorator().classList.remove("required");
        this.updateStyles();
    }

    _resetMessages () {
        this._invalid = false;
        this._error = null;
        this._extendedError = null;
        this.decorator().classList.remove("warning");
        this.decorator().classList.remove("informative");
        this.updateStyles();
    }

    _bindError (messages) {
        this._resetMessages();
        this.decorator().classList.remove("required");
        this._invalid = true;
        this._error = "" + messages.short;
        this._extendedError = "" + messages.extended;
        this.updateStyles();
    }
    
    _bindWarning (messages) {
        this._resetMessages();
        this.decorator().classList.remove("required");
        this.decorator().classList.add("warning");
        this._invalid = true;
        this._error = "" + messages.short;
        this._extendedError = "" + messages.extended;
        this.updateStyles();
    }

    _bindInformative (messages) {
        this._resetMessages();
        this.decorator().classList.remove("required");
        this.decorator().classList.add("informative");
        this._invalid = true;
        this._error = "" + messages.short;
        this._extendedError = "" + messages.extended;
        this.updateStyles();
    }

    _bindRequired (required) {
        this._resetMessages();
        if (required) {
            this.setAttribute("required", "");
            this.decorator().classList.add("required");
        } else {
            this.removeAttribute("required");
            this.decorator().classList.remove("required");
        }
        this.updateStyles();
    }

    /**
     * Override this to provide custom formatting for entered text.
     */
    _formatText (value) {
        return value;
    }
    
    /**
     * Override this to provide custom formatting for tooltip text.
     */
    _formatTooltipText (value) {
        const formatedText = this._formatText(value);
        return formatedText && "<b>" + formatedText + "</b>";
    }

    /**
     * Create context holder with custom '@@searchString' property ('tg-entity-editor' and 'tg-entity-search-criteria' only).
     */
    createContextHolder (inputText) {
        var contextHolder = this.reflector().createContextHolder(
            this.requireSelectionCriteria, this.requireSelectedEntities, this.requireMasterEntity,
            this.createModifiedPropertiesHolder, this.getSelectedEntities, this.getMasterEntity
        );
        this.reflector().setCustomProperty(contextHolder, "@@searchString", inputText);
        return contextHolder;
    }

    /**
     * Opens confirmation dialog for extended error / warning / informative message on tap.
     */
    _inputErrorTapHandler (event) {
        if (this._extendedError) {
            this.$.confirmationDialog.showConfirmationDialog(this._extendedError, [{name:'Close', confirm:true, autofocus:true}]);
        }
    }

    /**
     * Checks built-in validation of the 'input' element.
     * 
     * This is applicable e.g. for <input type="number"> elements, where characters like 'e', '.' and '-' are applicable but invalid combinations of those are possible.
     * 
     * In all cases such invalid input will lead to _editingValue === ''.
     * This complicates things, as _onChange may not be invoked at all.
     * So we use _outFocus (on-blur) and _onKeyDown (on-keydown) on-Enter callbacks to cover majority of cases.
     */
    _checkBuiltInValidation () {
        if (this.builtInValidationMessage && this.$ && this.$.input && this.$.input.checkValidity) {
            this._editorValidationMsg = this._editingValue === '' && !this.$.input.checkValidity() ? this.builtInValidationMessage : null;
        }
    }

    _propertyActionIndexChanged (newIndex, oldIndex) {
        let shouldBeFocused = false;
        if (oldIndex >= 0) {
            const oldAction = this.propertyActions[oldIndex];
            if (oldAction) {
                shouldBeFocused = isInHierarchy(oldAction, deepestActiveElement());
                oldAction.setAttribute('hidden', '');
            }
        }
        if (newIndex >= 0) {
            const newAction = this.propertyActions[newIndex];
            if (newAction) {
                newAction.removeAttribute('hidden');
                if (shouldBeFocused) {
                    newAction.$.iActionButton.focus();
                }
            }
        }
    }

}
