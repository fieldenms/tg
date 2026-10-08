import '/resources/polymer/@polymer/polymer/polymer-legacy.js';

import { deepestActiveElement, isInHierarchy } from '/resources/reflection/tg-polymer-utils.js';

/**
 * The message of the blocking pane while the master entity is new, as for sections of a compound master.
 */
export const MSG_BLOCKED_FOR_NEW_ENTITY = 'A new entity is being created. Please save or cancel your changes.';

/**
 * The message of the blocking pane while the master entity has changes that are not yet saved, as for leaving a master with such changes.
 */
export const MSG_BLOCKED_FOR_UNSAVED_CHANGES = 'Please save or cancel changes.';

/**
 * The styles of the blocking pane.
 * They are the first styles of the component, so that its own styles take precedence; the host becomes a containing block for the pane only if the component does not position it.
 */
const blockingPaneStyles = `
    :host {
        position: relative;
    }
    .tg-component-blocking-pane {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        padding: 20px;
        text-align: center;
        font-size: 18px;
        color: var(--tg-component-blocking-pane-color, var(--paper-grey-400));
        background-color: var(--tg-component-blocking-pane-background-color, rgba(255, 255, 255, 0.85));
    }
    .tg-component-blocking-pane[hidden] {
        display: none;
    }
`;

/**
 * The contract of an application-provided web component in an entity master, common to a component in place of an editor (`asComponent` in the Entity Master DSL)
 * and to a component bound to the entity (`addComponent`).
 *
 * The component is read-only and is bound to the fully-fledged entity of the master through `entity`.
 * It receives the title and description of its property, or those declared for it, if any.
 *
 * The property actions of the component are the children of its element in slot `property-action`, rendered by the master.
 * The component places them with `<slot name="property-action"></slot>` in its template, and this behaviour shows only the action at `propertyActionIndex`,
 * which the master calculates for the entity, as it does for the property actions of editors.
 *
 * While `blockWhenUnsaved` is set and the entity is an instance of a persistent type that is new or has changes that are not yet saved,
 * the component is `blocked`: a pane covers it with a message to save or cancel the changes, and it is made `inert`, so that neither the component nor its actions can be interacted with.
 * Changes that are not yet saved are those committed to the master, indicated by `entityModified`, and edits in progress, indicated by `entityEdited`.
 * `blockWhenUnsaved` may change at run time.
 *
 * The component receives the context of the master through `createContextHolder`, and `masterEntityContext()` provides the master entity, including its unsaved changes,
 * as the context of what the component runs, such as `getMasterEntity` of an embedded centre.
 *
 * The behaviour is used with `behaviors: [...]` for an element declared with `Polymer({...})`, or with `mixinBehaviors([...], PolymerElement)` for a class-based element.
 */
export const TgMasterComponentBehavior = {

    properties: {

        /**
         * The fully-fledged entity of the master, or `null` while the master is being reset.
         */
        entity: {
            type: Object,
            value: null
        },

        /**
         * The title of the property that the component represents, or the title declared for a component bound to the entity.
         */
        propTitle: {
            type: String
        },

        /**
         * The description of the property that the component represents, or the description declared for a component bound to the entity.
         */
        propDesc: {
            type: String
        },

        /**
         * The index of the property action to show; no action is shown for an index out of range.
         */
        propertyActionIndex: {
            type: Number,
            value: -1,
            observer: '_propertyActionIndexChanged'
        },

        /**
         * Indicates whether the component is blocked while the entity is unsaved.
         */
        blockWhenUnsaved: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether the master has changes that are committed but not yet saved.
         */
        entityModified: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether the master has edits in progress.
         */
        entityEdited: {
            type: Boolean,
            value: false
        },

        /**
         * The function of the master that creates the contexts of its property and entity actions,
         * with arguments `(requireSelectionCriteria, requireSelectedEntities, requireMasterEntity, actionKind, actionNumber)`.
         */
        createContextHolder: {
            type: Function
        },

        /**
         * Indicates whether the component is blocked, which is reflected to attribute `blocked` for the component to style itself.
         */
        blocked: {
            type: Boolean,
            value: false,
            readOnly: true,
            notify: true,
            reflectToAttribute: true
        }
    },

    observers: [
        '_updateBlocking(blockWhenUnsaved, entity, entityModified, entityEdited)'
    ],

    ready: function () {
        this._propertyActions = [...this.querySelectorAll(':scope > [slot="property-action"]')];
        this._propertyActions.forEach((action, index) => this._setActionVisible(action, index === this.propertyActionIndex));

        if (this.shadowRoot) {
            const styles = document.createElement('style');
            styles.textContent = blockingPaneStyles;
            this.shadowRoot.prepend(styles);
            this._blockingPane = document.createElement('div');
            this._blockingPane.className = 'tg-component-blocking-pane';
            this.shadowRoot.append(this._blockingPane);
        }
        this._renderBlocking();
    },

    /**
     * Returns the master entity, including its unsaved changes, in the form in which the contexts of the master carry it, as for property and entity actions.
     */
    masterEntityContext: function () {
        return this.createContextHolder(null, null, 'true', null, null)['masterEntity'];
    },

    /**
     * Blocks the component while `blockWhenUnsaved` is set and `entity` is an instance of a persistent type that is new or has changes that are not yet saved.
     */
    _updateBlocking: function (blockWhenUnsaved, entity, entityModified, entityEdited) {
        const persistent = !!entity && entity.type().isPersistent();
        const newEntity = persistent && !entity.isPersisted();
        this._setBlocked(!!blockWhenUnsaved && persistent && (newEntity || !!entityModified || !!entityEdited));
        this._blockingMessage = newEntity ? MSG_BLOCKED_FOR_NEW_ENTITY : MSG_BLOCKED_FOR_UNSAVED_CHANGES;
        this._renderBlocking();
    },

    /**
     * Shows the blocking pane and makes the component inert while it is blocked; the pane exists once the component is ready.
     */
    _renderBlocking: function () {
        this.inert = this.blocked;
        if (this._blockingPane) {
            this._blockingPane.hidden = !this.blocked;
            this._blockingPane.textContent = this._blockingMessage || '';
        }
    },

    /**
     * Shows the property action at `newIndex` in place of the one at `oldIndex`, and moves focus to it if the previous action was focused.
     * The property actions exist once the component is ready.
     */
    _propertyActionIndexChanged: function (newIndex, oldIndex) {
        if (!this._propertyActions) {
            return;
        }
        const oldAction = this._propertyActions[oldIndex];
        const shouldBeFocused = !!oldAction && isInHierarchy(oldAction, deepestActiveElement());
        if (oldAction) {
            this._setActionVisible(oldAction, false);
        }
        const newAction = this._propertyActions[newIndex];
        if (newAction) {
            this._setActionVisible(newAction, true);
            if (shouldBeFocused && newAction.$ && newAction.$.iActionButton) {
                newAction.$.iActionButton.focus();
            }
        }
    },

    _setActionVisible: function (action, visible) {
        if (visible) {
            action.removeAttribute('hidden');
        } else {
            action.setAttribute('hidden', '');
        }
    }
};
