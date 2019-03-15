import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';
import '/resources/polymer/@polymer/paper-styles/paper-styles-classes.js';
import { IronA11yKeysBehavior } from '/resources/polymer/@polymer/iron-a11y-keys-behavior/iron-a11y-keys-behavior.js';
import '/resources/master/tg-entity-master.js';
import { TgEntityMasterBehavior } from '/resources/master/tg-entity-master-behavior.js';
import '/resources/master/tg-entity-master-styles.js';
import { TgShortcutProcessingBehavior } from '/resources/actions/tg-shortcut-processing-behavior.js';
import { generateUUID } from '/resources/reflection/tg-polymer-utils.js';
import '/resources/polymer/@polymer/iron-flex-layout/iron-flex-layout-classes.js';

const TgEntityMasterTemplateBehaviorImpl = {

    ready: function () {
        const self = this;
        self.isMasterTemplate = true;
        self._registrationListener = self._registrationListener.bind(self);
        self.classList.add("canLeave");

        // the value for property uuid needs to be assign only if this has not been done yet
        if (self.uuid === undefined) {
            self.uuid = self.is + '/' + generateUUID();
        }
    },

    attached: function () {
        const self = this;
        if (self.prefDim) {
            self._masterDom().setAttribute('with-dimensions', 'true');
        } else {
            self._masterDom().removeAttribute('with-dimensions');
        }
    },

    /**
     * Adds 'with-dimensions' attribute to this tg-entity-master to make it suitable for resizing: action bar is placed on top of scrollable editor container.
     * Returns current dimensions with widthUnit = heightUnit = 'px'.
     */
    makeResizable: function () {
        const self = this;
        const prefDim = {
            width: self._masterDom().$.masterContainer.offsetWidth,
            height: self._masterDom().$.masterContainer.offsetHeight,
            widthUnit: 'px',
            heightUnit: 'px'
        };
        this._masterDom().setAttribute('with-dimensions', 'true');
        return prefDim;
    },

    addOwnKeyBindings: function () {
        const keyBindings = this.keyBindings;
        if (this.$.loader) {
            if (this.$.loader.wasLoaded) {
                if (typeof this.$.loader.loadedElement.addOwnKeyBindings === 'function') {
                    this.$.loader.loadedElement.addOwnKeyBindings();
                    return;
                }
            } else {
                this.$.loader.addEventListener('after-load', this._registrationListener);
                return;
            }
        }
        for (let shortcuts in keyBindings) {
            this.addOwnKeyBinding(shortcuts, keyBindings[shortcuts]);
        }
    },

    removeOwnKeyBindings: function () {
        if (this.$.loader) {
            if (this.$.loader.wasLoaded) {
                if (typeof this.$.loader.loadedElement.removeOwnKeyBindings === 'function') {
                    this.$.loader.loadedElement.removeOwnKeyBindings();
                    return;
                }
            } else {
                return;
            }
        }
        IronA11yKeysBehavior.removeOwnKeyBindings.call(this);
    },

    confirm: function (message, buttons) {
        return this._masterDom().confirm(message, buttons);
    },

    _shouldOverridePrefDim: function () {
        let parent = this.parentElement || this.getRootNode().host;
        while (parent && (parent.tagName !== 'TG-CUSTOM-ACTION-DIALOG' && parent.tagName !== 'TG-MENU-ITEM-VIEW')) {
            if (parent.isMasterTemplate && parent.prefDim) {
                return false;
            }
            parent = parent.parentElement || parent.getRootNode().host;
        }
        return true;
    },

    _registrationListener: function (e) {
        const target = e.target || e.srcElement;

        if (target === this.$.loader) {
            if (e.detail && typeof e.detail.addOwnKeyBindings === 'function') {
                e.detail.addOwnKeyBindings();
            }

            this.$.loader.removeEventListener('after-load', this._registrationListener);
        }
    },

    /**
     * Returns the key event target it might be a dialog or this element if the master is not in dialog.
     * Also it configures key bindings if the master is not a part of compound master.
     */
    _getKeyEventTarget: function () {
        let parent = this;
        let automaticAddKeyBindings = true;
        while (parent && (parent.tagName !== 'TG-CUSTOM-ACTION-DIALOG' && parent.tagName !== 'TG-MENU-ITEM-VIEW')) {
            if (parent.tagName === 'TG-MASTER-MENU-ITEM-SECTION') {
                automaticAddKeyBindings = false;
            }
            parent = parent.parentElement || parent.getRootNode().host;
        }
        if (automaticAddKeyBindings) {
            this.addOwnKeyBindings();
        }
        return parent || this;
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
    },

    _shortcutPressed: function (e) {
        this.processShortcut(e, ['tg-action', 'tg-ui-action']);
    }
};

export const TgEntityMasterTemplateBehavior = [
    IronA11yKeysBehavior,
    TgShortcutProcessingBehavior,
    TgEntityMasterBehavior,
    TgEntityMasterTemplateBehaviorImpl
];
export { Polymer, html };