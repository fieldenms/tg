import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import '/resources/polymer/@polymer/paper-icon-button/paper-icon-button.js';

import { TgReflector } from '/app/tg-reflector.js';
import '/resources/components/tg-scrollable-component.js';
import { TgRequiredPropertiesFocusTraversalBehavior } from '/resources/components/tg-required-properties-focus-traversal-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import { TgLongTouchHandlerBehaviour } from '/resources/components/tg-long-touch-handler-behaviour.js';
import '/resources/actions/tg-ui-action.js';
import '/resources/images/tg-document-related-icons.js';
import { TgElementSelectorBehavior } from '/resources/components/tg-element-selector-behavior.js';
import '/resources/egi/tg-responsive-toolbar.js';
import { getKeyEventTarget } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            min-height: 0;
            background-color: white;
            @apply --layout-vertical;
        }
        tg-scrollable-component {
            --tg-scrollable-layout: {
                flex: 1;
            };
        }
        tg-responsive-toolbar {
            margin-top: 8px;
            padding: 0 12px;
            height: auto;
            position: relative;
            overflow: hidden;
            flex-grow: 0;
            flex-shrink: 0;
        }
        .button-group {
            margin-bottom: 20px;
        }
        .right-button-group {
            margin-left: auto;
        }
        .selection-criteria-buttons {
            padding: 20px 20px 0 20px;
            min-height: -webkit-fit-content;
            min-height: -moz-fit-content;
            min-height: fit-content;
        }
    </style>
    <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    <!--Selection view toolbar-->
    <tg-responsive-toolbar>
        <slot id="custom_action_selector" slot="entity-specific-action" name="custom-front-action"></slot>
        <slot slot="standart-action" name="custom-share-action"></slot>
        <tg-ui-action slot="standart-action" ui-role='ICON' short-desc='New configuration' long-desc='Create new configuration' icon='tg-document-related-icons:file-outline' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigNewAction' element-name='tg-CentreConfigNewAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigNewAction-master_0_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.0.attrs]]' pre-action='[[topLevelActions.0.preAction]]' post-action-success='[[topLevelActions.0.postActionSuccess]]' post-action-error='[[topLevelActions.0.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_buttonDisabled]]' style='[[_computeButtonStyle(_buttonDisabled)]]'></tg-ui-action>
        <tg-ui-action id="loadAction" slot="standart-action" ui-role='ICON' short-desc='Load configuration' long-desc='Load configuration...' icon='icons:folder-open' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigLoadAction' element-name='tg-CentreConfigLoadAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigLoadAction-master_2_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.2.attrs]]' pre-action='[[topLevelActions.2.preAction]]' post-action-success='[[topLevelActions.2.postActionSuccess]]' post-action-error='[[topLevelActions.2.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_buttonDisabled]]' style='[[_computeButtonStyle(_buttonDisabled)]]'></tg-ui-action>
        <tg-ui-action slot="standart-action" ui-role='ICON' short-desc='Duplicate configuration' long-desc='Duplicate current configuration' icon='icons:content-copy' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction' element-name='tg-CentreConfigDuplicateAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigDuplicateAction-master_1_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.1.attrs]]' pre-action='[[topLevelActions.1.preAction]]' post-action-success='[[topLevelActions.1.postActionSuccess]]' post-action-error='[[topLevelActions.1.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_buttonDisabled]]' style='[[_computeButtonStyle(_buttonDisabled)]]'></tg-ui-action>
        <tg-ui-action slot="standart-action" ui-role='ICON' short-desc='Edit' long-desc='Edit title, description and dashboard settings...' icon='tg-document-related-icons:square-edit-outline' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigEditAction' element-name='tg-CentreConfigEditAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigEditAction-master_3_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.3.attrs]]' pre-action='[[topLevelActions.3.preAction]]' post-action-success='[[topLevelActions.3.postActionSuccess]]' post-action-error='[[topLevelActions.3.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_buttonDisabled]]' style='[[_computeButtonStyle(_buttonDisabled)]]'></tg-ui-action>
        <tg-ui-action slot="standart-action" ui-role='ICON' short-desc='Delete configuration' long-desc='Delete current configuration' icon='tg-document-related-icons:delete-outline' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigDeleteAction' element-name='tg-CentreConfigDeleteAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigDeleteAction-master_4_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.4.attrs]]' pre-action='[[topLevelActions.4.preAction]]' post-action-success='[[topLevelActions.4.postActionSuccess]]' post-action-error='[[topLevelActions.4.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_buttonDisabled]]' style='[[_computeButtonStyle(_buttonDisabled)]]'></tg-ui-action>
        <tg-ui-action slot="standart-action" ui-role='ICON' short-desc='Configure' long-desc='Configure running automatically...' icon='icons:settings' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigConfigureAction' element-name='tg-CentreConfigConfigureAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigConfigureAction-master_5_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.5.attrs]]' pre-action='[[topLevelActions.5.preAction]]' post-action-success='[[topLevelActions.5.postActionSuccess]]' post-action-error='[[topLevelActions.5.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' hidden="[[embedded]]" disabled='[[_configureButtonDisabled]]' style='[[_computeButtonStyle(_configureButtonDisabled)]]'></tg-ui-action>
        <paper-icon-button id="helpAction" slot="standart-action" style="color:#727272" icon="icons:help-outline" tooltip-text="Tap to open help in a window or tap with Ctrl/Cmd to open help in a tab.<br>Alt&nbsp+&nbspTap or long touch to edit the help link."></paper-icon-button>
    </tg-responsive-toolbar>
    <tg-scrollable-component class="relative">
        <slot name="custom-selection-criteria"></slot>
    </tg-scrollable-component>
    <div class="selection-criteria-buttons layout horizontal justified wrap">
        <div class="layout horizontal button-group">
            <slot name="left-selection-criteria-button"></slot>
        </div>
        <div class="layout horizontal button-group right-button-group">
            <slot name="right-selection-criteria-button"></slot>
        </div>
    </div>`;

Polymer({
    _template: template,

    is: 'tg-selection-view',

    behaviors: [
        TgLongTouchHandlerBehaviour,
        IronA11yKeysBehavior,
        TgRequiredPropertiesFocusTraversalBehavior,
        TgShortcutProcessingBehavior,
        TgElementSelectorBehavior
    ],

    properties: {
        _showDialog: Function,
        /**
         * Creates standard action object for centre config action like EDIT, LOAD etc.
         */
        _createActionObject: {
            type: Function
        },
        /**
         * Current saveAsName for centre configuration. This will flow upward to main centre elements.
         */
        saveAsName: {
            type: String,
            notify: true
        },
        _createContextHolder: Function,
        uuid: String,
        _confirm: Function,
        _buttonDisabled: Boolean,
        _configureButtonDisabled: {
            type: Boolean,
            computed: '_computeConfigureButtonDisabled(_buttonDisabled, embedded)'
        },
        embedded: Boolean,
        initiateAutoRun: Function,
        _resetAutocompleterState: Function,
        _longHelpTouchHandler: Function,
        _shortHelpTouchHandler: Function
    },

    observers: ["_initHelpAction(_longHelpTouchHandler, _shortHelpTouchHandler)"],

    created: function () {
        this._reflector = new TgReflector();
    },

    ready: function () {
        const customShortcuts = [];
        Array.prototype.forEach.call(this.$.custom_action_selector.assignedNodes({ flatten: true }), function (item) {
            if (item.getAttribute("shortcut")) {
                customShortcuts.push(item.getAttribute("shortcut"));
            }
        }.bind(this));
        this._ownKeyBindings = {};
        this._ownKeyBindings[customShortcuts.join(" ") + (customShortcuts.length > 0 ? " " : "") + "ctrl+s ctrl+e ctrl+r f5"] = '_shortcutPressed';
    },

    attached: function () {
        const self = this;
        this.async(function () {
            self.keyEventTarget = getKeyEventTarget(this, this.parentElement.parentElement);

            self.topLevelActions = [
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigNewAction',
                    null,
                    self._resetAutocompleterState // need to reset autocompleter states when clearing default configuration or moving from save-as to default
                ),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction'),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigLoadAction',
                    null,
                    () => { self._resetAutocompleterState(); self.initiateAutoRun(); } // need to reset autocompleter states when explicitly loading other (or the same) configuration
                ),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigEditAction'),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigDeleteAction',
                    () => self._confirm('Should this configuration be deleted?', [{ name: 'NO' }, { name: 'YES', confirm: true, autofocus: true }]),
                    self._resetAutocompleterState // need to reset autocompleter states when deleting save-as configuration and moving to clean default one
                ),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigConfigureAction')
            ];
        }, 1);
    },

    _initHelpAction: function (_longHelpTouchHandler, _shortHelpTouchHandler) {
        if (_longHelpTouchHandler && _shortHelpTouchHandler) {
            this.enhanceWithLongTouchEventHandlers(this.$.helpAction, _longHelpTouchHandler, _shortHelpTouchHandler);
        }
    },

    _shortcutPressed: function (e) {
        this.processShortcut(e, ['paper-button', 'tg-ui-action', 'paper-icon-button']);
    },

    _computeConfigureButtonDisabled: function (_buttonDisabled, embedded) {
        return _buttonDisabled || embedded;
    },

    /**
     * Overrides standard hand cursor for disabled button to simple pointer.
     */
    _computeButtonStyle: function (_buttonDisabled) {
        return _buttonDisabled ? 'cursor:initial' : '';
    }
});