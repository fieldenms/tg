import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout.js';

import '/resources/polymer/@polymer/paper-button/paper-button.js'

import '/resources/master/tg-entity-master.js';
import '/resources/components/tree-table/tg-tree-table.js';
import '/resources/master/actions/tg-action.js';

import {Polymer} from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import {html} from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';

import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import { EntityStub } from '/resources/reflection/tg-polymer-utils.js';

const template = html`
    <style>
        :host {
            min-height: 0;
            @apply --layout-vertical;

        }
        tg-entity-master {
            min-height: 0;
            @apply --layout-vertical;
        }
        #treeTableContainer {
            width: 100%;
            height: 100%;
            min-height: 0;
            @apply --layout-vertical;
            @apply --layout-relative;
        }
        #actionPanel {
            padding: 20px; 
            flex-shrink: 0;
            @apply --layout-horizontal;
            @apply --layout-center-justified;
        }
        #actionPanel ::slotted(tg-action),
        tg-action {
            margin: 10px;
        }
        tg-tree-table {
            min-height:0;
        }
        .filter-panel ::slotted(.filter-element) {
            margin: 0 10px;
            @apply --layout-flex;

        }
        .filter-panel {
            flex-shrink: 0;
            @apply --layout-horizontal;
        }
        .lock-layer {
            opacity: 0.5;
            display: none;
            background-color: white;
            @apply --layout-fit;
        }
        .lock-layer[lock] {
            display: initial;
        }
    </style>
    <tg-entity-master id="masterDom" entity-type="[[entityType]]" entity-id="new" _post-validated-default="[[_postValidatedDefault]]" _post-validated-default-error="[[_postValidatedDefaultError]]" _process-response="[[_processResponse]]" _process-error="[[_processError]]" _process-retriever-response="[[_processRetrieverResponse]]" _process-retriever-error="[[_processRetrieverError]]" _process-saver-response="[[_processSaverResponse]]" _process-saver-error="[[_processSaverError]]" _saver-loading="{{_saverLoading}}">
        <div id="treeTableContainer">
            <div class="filter-panel">
                <slot name="filter-element"></slot>
            </div>
            <tg-tree-table id="securityMatrix" entities="[[entities]]" hierarchy-column="[[hierarchyColumn]]" columns="[[columns]]"></tg-tree-table>
            <div id="actionPanel">
                <slot name="reload-action"></slot>
                <tg-action id="cancelButton" enabled-states='[[_actions.REFRESH.enabledStates]]' short-desc='CANCEL' long-desc='Cancels all changes after save' current-state='EDIT' shortcut='ctrl+r meta+r' role='refresh' action='[[_resetSecurityMatrix]]' post-action='{{_postResetSecurityMatrix}}' post-action-error='{{_postResetSecurityMatrixError}}' style="margin:10px;"></tg-action>
                <tg-action id="saveButton" enabled-states='[[_actions.SAVE.enabledStates]]' short-desc='SAVE' long-desc='Save changes' current-state='EDIT' shortcut='ctrl+s meta+s' action='[[_saveSecurityMatrix]]' post-action='{{_postSavedDefault}}' post-action-error='{{_postSavedDefaultError}}' style="margin:10px;"></tg-action>
                <!-- save and cancel buttons goes here -->
            </div>
            <div class="lock-layer" lock$="[[lock]]"></div>
        </div>
    </tg-entity-master>`;

class SecurityMatrixEntity extends EntityStub {

    constructor(securityTokenEntity, userRoles, tokenRoleMap, columnList, afterCheckCallback, parent) {
        super(securityTokenEntity.get("key"));
        this.$visible = true;
        this.$columnListMap = {};
        columnList.forEach(column => {
            this.$columnListMap[column.property] = column;
        });
        this.$afterCheckCallback = afterCheckCallback;
        this.$state = {};
        this.title = securityTokenEntity.get("title");
        this.desc = securityTokenEntity.get("desc");
        if (parent) {
            this.parent = parent;
        }
        this.children = [];
        if (securityTokenEntity.children && securityTokenEntity.children.length > 0) {
            this.children = securityTokenEntity.children.map(securityToken => new SecurityMatrixEntity(securityToken, userRoles, tokenRoleMap, columnList, afterCheckCallback, this));
        }
        this.roleIdMap = {};
        this.idRoleMap = {};

        Object.values(userRoles).forEach(role => {
            const roleKey = replaceWhitespacesWithUnderscore(role.get("key"));
            this.roleIdMap[roleKey] = role.get("id");
            this.idRoleMap[role.get("id")] = roleKey;
            this[roleKey] = false;
            if (!this.hasChildren()) {
                this.$state[roleKey] = "UNCHECKED";
            } else {
                this.$state[roleKey] = calculateState(this.children.map(child => child.$state[roleKey]));
            }
        });
        if (!this.hasChildren()) {
            const availTokenList = tokenRoleMap[securityTokenEntity.get("key")];
            if (availTokenList) {
                availTokenList.forEach(roleId => {
                    this[this.idRoleMap[roleId]] = true;
                    this.$state[this.idRoleMap[roleId]] = "CHECKED";
                });
            }
            this.$state._token = calculateState(Object.keys(this.roleIdMap).map(roleKey => this.$state[roleKey]));
        } else {
            this.$state._token = calculateState(this.children.map(child => child.$state._token));
        }
        //Initiate
        this._tokenRoleAssociationHandler = {};
        this._newAssociations = [];
        this._removedAssociations = [];
    }
    
    set(property, value) {
        if (!this.hasChildren()) {
            const oldValue = this.get(property);
            super.set(property, value);
            this.$state[property] = value ? "CHECKED" : "UNCHECKED"
            if (oldValue !== value) {
                if (value) {
                    updateAssociations(this.roleIdMap[property], this._newAssociations, this._removedAssociations);
                } else {
                    updateAssociations(this.roleIdMap[property], this._removedAssociations, this._newAssociations);
                }
            }
            if (typeof this._tokenRoleAssociationHandler[property] === "function") {
                this._tokenRoleAssociationHandler[property](value, this.getState(property));
            }
        }
    }
    
    getState(property, visible) {
        if (!visible) {
            return this.$state[property];
        } else {
            if (this.hasChildren()) {
                return calculateState(this.children.filter(child => child.$visible).map(child => child.getState(property, visible)));
            } else {
                if (property === "_token") {
                    return calculateState(Object.keys(this.roleIdMap).filter(roleKey => visible ? this._isPropertyVisible(roleKey) : true).map(roleKey => this.getState(roleKey)));
                } else {
                    return this.$state[property] ? "CHECKED" : "UNCHECKED";
                }
            }
        }
    }

    getAssociationsToSave(objToSave) {
        if (this.hasChildren()) {
            this.children.forEach(child => child.getAssociationsToSave(objToSave));
        } else {
            objToSave[this.get("id")] = this._newAssociations.slice();
        }
    }

    getAssociationsToRemove(objToSave) {
        if (this.hasChildren()) {
            this.children.forEach(child => child.getAssociationsToRemove(objToSave));
        } else {
            objToSave[this.get("id")] = this._removedAssociations.slice();
        }
    }

    isChanged() {
        return (this.hasChildren() && this.children.some(child => child.isChanged())) || this._isChanged();
    }

    reset() {
        const roleTouched = {};
        if (this.hasChildren()) {
            this.children.forEach(child => {
                Object.assign(roleTouched, child.reset());
            });
            Object.keys(roleTouched).forEach(roleKey => {
                this._updatePropertyState(roleKey, true);
            });
        } else {
            this._newAssociations.slice().forEach(associationId => {
                this.set(this.idRoleMap[associationId], false);
                roleTouched[this.idRoleMap[associationId]] = true;
            });
            this._removedAssociations.slice().forEach(associationId => {
                this.set(this.idRoleMap[associationId], true);
                roleTouched[this.idRoleMap[associationId]] = true;
            });
        }
        if (Object.keys(roleTouched).length > 0) {
            this._updatePropertyState("_token", true);
        }
        return roleTouched;
    }

    clearCurrentState() {
        if (this.hasChildren()) {
            this.children.forEach(child => child.clearCurrentState());
        } else {
            this._newAssociations = [];
            this._removedAssociations = [];
        }
    }
    
    hasChildren() {
        return this.children && this.children.length > 0;
    }

    _isPropertyVisible(property) {
        const column = this.$columnListMap[property];
        return column ? column.visible : true;
    }

    _isChanged() {
        return this._newAssociations.length > 0 || this._removedAssociations.length > 0;
    }

    _check(property, value) {
        if (this.$visible && this._isPropertyVisible(property)) {
            this._checkWithoutParent(property, value);
            this._checkColumnParent(property, value);
            this.$afterCheckCallback();
        }
    }
    
     _checkColumnParent(property, value) {
        if (this.parent) {
            if (Object.keys(this.roleIdMap).includes(property)) {
                this.parent._updatePropertyState(property, value);
            } else {
                Object.keys(this.roleIdMap).forEach(roleKey => this.parent._updatePropertyState(roleKey, value));
            }
            this.parent._updatePropertyState("_token", value);
            this.parent._checkColumnParent(property, value);
        }
    }

    _checkWithoutParent(property, value) {
        if (this.hasChildren()) {
            this.children.forEach(child => {
                if (child.$visible && this._isPropertyVisible(property)) {
                    child._checkWithoutParent(property, value);
                }
            });
            if (property === "_token") {
                Object.keys(this.roleIdMap).forEach(roleKey => {
                    if (this._isPropertyVisible(roleKey)) {
                        this._updatePropertyState(roleKey, value);
                    }
                });
                this._updatePropertyState("_token", value);
            } else {
                if (this._isPropertyVisible(property)) {
                    this._updatePropertyState(property, value);
                    this._updatePropertyState("_token", value);
                }
            }
        } else {
            if (property === "_token") {
                Object.keys(this.roleIdMap).forEach(roleKey => {
                    if (this._isPropertyVisible(roleKey)) {
                        this.set(roleKey, value);
                    }
                });
                this._updatePropertyState("_token", value);
            } else {
                if (this._isPropertyVisible(property)) {
                    this.set(property, value);
                    this._updatePropertyState("_token", value);
                }
            }
        }
    }
    
    _updatePropertyState(property, value) {
        if ("_token" === property) {
            this.$state._token = calculateState(Object.keys(this.roleIdMap).map(roleKey => this.getState(roleKey)));
        } else {
            if (this.hasChildren()) {
                this.$state[property] = calculateState(this.children.map(child => child.getState(property)));
            } else {
                this.$state[property] = value ? "CHECKED" : "UNCHECKED";
            }
        }
        if (typeof this._tokenRoleAssociationHandler[property] === "function") {
            const newAllState = this.getState(property);
            const newVisibleState = this.getState(property, true);
            const newValue  = newAllState === "SEMICHECKED" ? (newVisibleState === "SEMICHECKED" ? !value : newVisibleState === "CHECKED") : newAllState === "CHECKED";
            this._tokenRoleAssociationHandler[property](newValue, newAllState);
        }
    }
};
const updateAssociations = function (value, newAssociations, removedAssociations) {
    const roleIndex = removedAssociations.indexOf(value);
    if (roleIndex >= 0) {
        removedAssociations.splice(roleIndex, 1);
    } else {
        newAssociations.push(value);
    }
}
const removeValueFrom = function (value, list) {
    const indexToRemove = list.indexOf(value);
    if (indexToRemove >= 0) {
        list.splice(indexToRemove, 1);
    }
};
const calculateState = function (states) {
    if (states.length > 0 && states.every(state => state === "CHECKED")) {
        return "CHECKED";
    } else if (states.length > 0 && states.every(state => state === "UNCHECKED")) {
        return "UNCHECKED";
    }
    return "SEMICHECKED";
};
const calculateNextState = function (state, prevState) {
    if (!prevState) {
        return state;
    }
    return calculateState([prevState, state]);
}
const replaceWhitespacesWithUnderscore = function (str) {
    return str.replace(/\s+/gi, "_");
};

Polymer({
    _template: template,

    is: 'tg-security-matrix',

    behaviors: [IronResizableBehavior, TgEntityMasterBehavior],

    properties: {
        entityType: String,

        /**
         * The entity that comes from insertion point and holds the information for schduling.
         */
        entity: {
            type: Object,
            observer: "_entityChanged"
        },
        hierarchyColumn: Object,
        columns: Array,
        entities: Array,
        /**
         * Need for locking schduling component during insertion point activation or refreshing.
         */
        lock: {
            type: Boolean,
            value: false
        },
        check: Function,
        _resetSecurityMatrix: Function,
        _postResetSecurityMatrix: Function,
        _postResetSecurityMatrixError: Function,
        _saveSecurityMatrix: Function,
        _postSecurityMatrixSaved: Function
    },

    ready: function () {
        //Configuring the security matrix master
        this.entityType = "ua.com.fielden.platform.entity.SecurityMatrixSaveAction"
        this.check = function (entity, property, value) {
            entity._check(property, value);
        };
        this._resetSecurityMatrix = function () {
            this.entities.forEach(entity => entity.reset());
            this.$.cancelButton.postAction();
            this._toggleButtonStates();
        }.bind(this);
        this._postResetSecurityMatrix = function () {};
        this._postResetSecurityMatrixError = function () {};
        this._saveSecurityMatrix = function () {
            const associationsToSave = {};
            const associationsToRemove = {};
            this.entities.forEach(entity => entity.getAssociationsToSave(associationsToSave));
            this.entities.forEach(entity => entity.getAssociationsToRemove(associationsToRemove));
            this._currBindingEntity['associationsToSave'] = associationsToSave;
            this._currBindingEntity['associationsToRemove'] = associationsToRemove;
            this.save();
        }.bind(this);
        this.postSaved = function () {
            this.entities.forEach(entity => entity.clearCurrentState());
            this._toggleButtonStates();
        }.bind(this);
        this._toggleButtonStates = function () {
            const changed = this.entities.some(entity => entity.isChanged());
            this.$.saveButton.outerEnabled = changed;
            this.$.cancelButton.outerEnabled = changed;
        }.bind(this);
    },

    attached: function () {
        this.async(function () {
            if (!this._currBindingEntity) {
                this.retrieve();
            }
        });
    },
    
    canLeave: function () {
        if (this.entities.some(entity => entity.isChanged())) {
            return {
                msg: "Please save or cancel changes."
            };
        }
    },

    filterTokens: function (text) {
        this.$.securityMatrix.filterTokens(text.replace(/\s*,\s*/, "|"));
    },

    filterRoles: function (text) {
        this.$.securityMatrix.filterRoles(text.replace(/\s*,\s*/, "|"));
    },

    _isNecessaryForConversion: function (propertyName) {
        return ['associationsToSave', 'associationsToRemove'].includes(propertyName);
    },

    _entityChanged: function (newBindingEntity) {
        const newEntity = newBindingEntity ? newBindingEntity['@@origin'] : null;
        if (newEntity && newEntity.calculated) {
            this.hierarchyColumn = {
                property: "title",
                visible: true,
                childrenProperty: "children",
                parentProperty: "parent",
                type: "String",
                width: 200,
                minWidth: 200,
                growFactor: 1,
                columnTitle: "Security Tokens",
                columnDesc: "Security Tokens Hierarchy",
                check: this.check,
            };
            const columnList = [];
            newEntity.get("userRoles").forEach(role => {
                columnList.push({
                    property: replaceWhitespacesWithUnderscore(role.get("key")),
                    visible: true,
                    type: "Boolean",
                    width: 30,
                    minWidth: 30,
                    growFactor: 0,
                    columnTitle: role.get("key"),
                    columnDesc: role.get("desc"),
                    check: this.check,
                });
            });
            this.columns = columnList;
            this.entities = newEntity.get("tokens").map(token => new SecurityMatrixEntity(token, newEntity.get("userRoles"), newEntity.get("tokenRoleMap"), columnList, this._toggleButtonStates));
            this._toggleButtonStates();
        }
    },

    _masterDom: function () {
        return this.$.masterDom;
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        return this._masterDom()._ajaxRetriever();
    },

    /**
     * The core-ajax component for entity saving.
     */
    _ajaxSaver: function () {
        return this._masterDom()._ajaxSaver();
    },

    /**
     * The validator component.
     */
    _validator: function () {
        return this._masterDom()._validator();
    },

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this._masterDom()._serialiser();
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._masterDom()._reflector();
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        return this._masterDom()._toastGreeting();
    }

});