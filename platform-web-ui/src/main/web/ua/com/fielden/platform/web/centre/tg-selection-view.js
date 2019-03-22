import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import '/resources/polymer/@polymer/iron-icons/iron-icons.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';

import { TgReflector } from '/app/tg-reflector.js';
import '/resources/components/tg-scrollable-component.js';
import { TgRequiredPropertiesFocusTraversalBehavior } from '/resources/components/tg-required-properties-focus-traversal-behavior.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import '/resources/actions/tg-ui-action.js';
import '/resources/images/tg-document-related-icons.js';

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
        .toolbar {
            padding: 0 12px;
            height: auto;
            position: relative;
            overflow: hidden;
            flex-grow: 0;
            flex-shrink: 0;
        }
        .toolbar-content ::slotted(*) {
            margin-top: 8px;
        }
    </style>
    <custom-style>
        <style include="iron-flex iron-flex-reverse iron-flex-alignment iron-flex-factors iron-positioning"></style>
    </custom-style>
    <!--Selection view toolbar-->
    <div class="toolbar layout horizontal wrap">
        <div class="toolbar-content layout horizontal center">
            <slot id="custom_action_selector" name="custom-front-action"></slot>
        </div>
        <div class="toolbar-content layout horizontal center" style="margin-left:auto">
            <tg-ui-action ui-role='ICON' short-desc='New configuration' long-desc='Create new configuration' icon='tg-document-related-icons:file-outline' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigNewAction' element-name='tg-CentreConfigNewAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigNewAction-master_0_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.0.attrs]]' pre-action='[[topLevelActions.0.preAction]]' post-action-success='[[topLevelActions.0.postActionSuccess]]' post-action-error='[[topLevelActions.0.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_computeConfigButtonDisabled(saveAsName)]]' style='[[_computeConfigButtonStyle(saveAsName)]]'></tg-ui-action>
            <tg-ui-action ui-role='ICON' short-desc='Load configuration' long-desc='Load configuration...' icon='icons:folder-open' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigLoadAction' element-name='tg-CentreConfigLoadAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigLoadAction-master_2_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.2.attrs]]' pre-action='[[topLevelActions.2.preAction]]' post-action-success='[[topLevelActions.2.postActionSuccess]]' post-action-error='[[topLevelActions.2.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_computeConfigButtonDisabled(saveAsName)]]' style='[[_computeConfigButtonStyle(saveAsName)]]'></tg-ui-action>
            <tg-ui-action ui-role='ICON' short-desc='Duplicate configuration' long-desc='Duplicate current configuration' icon='icons:content-copy' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction' element-name='tg-CentreConfigDuplicateAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigDuplicateAction-master_1_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.1.attrs]]' pre-action='[[topLevelActions.1.preAction]]' post-action-success='[[topLevelActions.1.postActionSuccess]]' post-action-error='[[topLevelActions.1.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_computeConfigButtonDisabled(saveAsName)]]' style='[[_computeConfigButtonStyle(saveAsName)]]'></tg-ui-action>
            <tg-ui-action ui-role='ICON' short-desc='Edit' long-desc='Edit title and description...' icon='tg-document-related-icons:square-edit-outline' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigEditAction' element-name='tg-CentreConfigEditAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigEditAction-master_3_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.3.attrs]]' pre-action='[[topLevelActions.3.preAction]]' post-action-success='[[topLevelActions.3.postActionSuccess]]' post-action-error='[[topLevelActions.3.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_computeConfigButtonDisabled(saveAsName)]]' style='[[_computeConfigButtonStyle(saveAsName)]]'></tg-ui-action>
            <tg-ui-action ui-role='ICON' short-desc='Delete configuration' long-desc='Delete current configuration' icon='tg-document-related-icons:delete-outline' icon-style='' component-uri='/master_ui/ua.com.fielden.platform.web.centre.CentreConfigDeleteAction' element-name='tg-CentreConfigDeleteAction-master' action-kind='TOP_LEVEL' element-alias='tg-CentreConfigDeleteAction-master_4_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.4.attrs]]' pre-action='[[topLevelActions.4.preAction]]' post-action-success='[[topLevelActions.4.postActionSuccess]]' post-action-error='[[topLevelActions.4.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled='[[_computeConfigButtonDisabled(saveAsName)]]' style='[[_computeConfigButtonStyle(saveAsName)]]'></tg-ui-action>
            <tg-ui-action ui-role='ICON' short-desc='Settings' long-desc='Customize selection criteria and resultset of the current configuration' icon='icons:settings' icon-style='' component-uri='/master_ui/fielden.work.ui_actions.OpenWorkActivityMasterAction' element-name='tg-OpenWorkActivityMasterAction-master' action-kind='TOP_LEVEL' element-alias='tg-OpenWorkActivityMasterAction-master_5_TOP_LEVEL' show-dialog='[[_showDialog]]' create-context-holder='[[_createContextHolder]]' attrs='[[topLevelActions.5.attrs]]' pre-action='[[topLevelActions.5.preAction]]' post-action-success='[[topLevelActions.5.postActionSuccess]]' post-action-error='[[topLevelActions.5.postActionError]]' require-selection-criteria='true' require-selected-entities='NONE' require-master-entity='false' disabled style='cursor:initial'></tg-ui-action>
        </div>
    </div>
    <tg-scrollable-component class="relative">
        <slot name="custom-selection-criteria"></slot>
    </tg-scrollable-component>
    <slot name="selection-criteria-buttons"></slot>
`;

Polymer({
    _template: template,

    is: 'tg-selection-view',

    behaviors: [
        IronA11yKeysBehavior,
        TgRequiredPropertiesFocusTraversalBehavior,
        TgShortcutProcessingBehavior
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
        _confirm: Function
    },

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
        this.keyBindings = {};
        this.keyBindings[customShortcuts.join(" ") + (customShortcuts.length > 0 ? " " : "") + "ctrl+s ctrl+e ctrl+r f5"] = '_shortcutPressed';
    },

    attached: function () {
        const self = this;
        this.async(function () {
            self.keyEventTarget = self._getKeyEventTarget();

            self.topLevelActions = [
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigNewAction'),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction'),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigLoadAction'),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigEditAction'),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigDeleteAction', () => self._confirm('Should this configuration be deleted?', [{ name: 'NO' }, { name: 'YES', confirm: true, autofocus: true }])),
                self._createActionObject('ua.com.fielden.platform.web.centre.CentreConfigSettingsAction')
            ];
        }, 1);
    },

    _getKeyEventTarget: function () {
        let parent = this;
        while (parent && (parent.tagName !== 'TG-CUSTOM-ACTION-DIALOG' && parent.tagName !== 'TG-MENU-ITEM-VIEW')) {
            parent = parent.parentElement || parent.getRootNode().host;
        }
        return parent || this.parentElement.parentElement;
    },

    _shortcutPressed: function (e) {
        this.processShortcut(e, ['paper-button', 'tg-ui-action']);
    },

    _computeConfigButtonDisabled: function (saveAsName) {
        return saveAsName === this._reflector.LINK_CONFIG_TITLE;
    },

    _computeConfigButtonStyle: function (saveAsName) {
        return saveAsName === this._reflector.LINK_CONFIG_TITLE ? 'cursor:initial' : '';
    }
});