import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import '/resources/polymer/@polymer/iron-icon/iron-icon.js';
import '/resources/images/tg-icons.js';

import { addListener } from '/resources/polymer/@polymer/polymer/lib/utils/gestures.js';
import { TgDoubleTapHandlerBehavior } from '/resources/components/tg-double-tap-handler-behavior.js';
import { deepestActiveElement, isInHierarchy, tearDownEvent, localStorageKey, getRelativePos } from '/resources/reflection/tg-polymer-utils.js';

/**
 * The message of the blocking pane while the master entity is new, as for sections of a compound master.
 */
export const MSG_BLOCKED_FOR_NEW_ENTITY = 'A new entity is being created. Please save or cancel your changes.';

/**
 * The message of the blocking pane while the master entity has changes that are not yet saved, as for leaving a master with such changes.
 */
export const MSG_BLOCKED_FOR_UNSAVED_CHANGES = 'Please save or cancel changes.';

/**
 * The styles of the resizer and of the blocking pane.
 * They are the first styles of the component, so that its own styles take precedence; the host becomes a containing block for them only if the component does not position it.
 * The resizer precedes the blocking pane, which covers it while the component is blocked.
 */
const componentStyles = `
    :host {
        position: relative;
    }
    .tg-component-resizer {
        position: absolute;
        right: 0;
        bottom: 0;
        z-index: 1;
        cursor: ns-resize;
        --iron-icon-fill-color: var(--paper-grey-600);
    }
    .tg-component-resizer[hidden] {
        display: none;
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
 * Without a declared `height`, the component has no height of its own, so that it can fill the height of its layout cell.
 * With `height` declared, as a CSS length, the component is resizable, as a rich text editor with a declared height: the user drags the resizer at its bottom right corner to change the height,
 * and double taps the resizer to restore the declared height.
 * The height set by the user is remembered in the browser under the user, `entityType` and the element id, and takes the place of the declared height.
 * The height is never less than `minHeight`, which a component may give a default value by declaring the property with that value.
 * The component cannot be resized while it is blocked.
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
         * The entity type of the master, which is rendered together with `height`.
         */
        entityType: {
            type: String
        },

        /**
         * The declared height of the component, as a CSS length, which makes the component resizable; the component fills its layout cell without it.
         */
        height: {
            type: String
        },

        /**
         * The least height of the component, as a CSS length.
         * It has no observer of its own, so that a component can declare the property with its default value, which replaces this declaration.
         */
        minHeight: {
            type: String
        },

        /**
         * Indicates whether the component is resizable, which it is if `height` is declared; the resizer is shown only for a resizable component.
         * This is the counterpart of `autoResize` of a rich text editor, which is not resizable while its height is the default `100%`.
         * It is reflected to attribute `resizable` for the component to style itself, such as to keep its content clear of the resizer.
         */
        resizable: {
            type: Boolean,
            computed: '_isResizable(height)',
            observer: '_renderResizer',
            reflectToAttribute: true
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
        '_updateBlocking(blockWhenUnsaved, entity, entityModified, entityEdited)',
        '_updateHeight(height, minHeight, entityType)'
    ],

    ready: function () {
        this._propertyActions = [...this.querySelectorAll(':scope > [slot="property-action"]')];
        this._propertyActions.forEach((action, index) => this._setActionVisible(action, index === this.propertyActionIndex));

        if (this.shadowRoot) {
            const styles = document.createElement('style');
            styles.textContent = componentStyles;
            this.shadowRoot.prepend(styles);
            this._resizer = this._createResizer();
            this.shadowRoot.append(this._resizer);
            this._blockingPane = document.createElement('div');
            this._blockingPane.className = 'tg-component-blocking-pane';
            this.shadowRoot.append(this._blockingPane);
        }
        this._renderBlocking();
        this._renderResizer();
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
     * Applies the least height, and the height of a resizable component: the height set by the user, if any, or otherwise the declared height.
     */
    _updateHeight: function (height, minHeight, entityType) {
        this.style.minHeight = minHeight || '';
        this.style.height = this._isResizable(height) ? (localStorage.getItem(this._heightKey()) || height) : '';
    },

    _isResizable: function (height) {
        return !!height;
    },

    /**
     * Shows the resizer while the component is resizable; the resizer exists once the component is ready.
     */
    _renderResizer: function () {
        if (this._resizer) {
            this._resizer.hidden = !this.resizable;
        }
    },

    /**
     * Creates the resizer, which changes the height of the component as it is dragged, and restores the declared height on a double tap.
     */
    _createResizer: function () {
        const resizer = document.createElement('iron-icon');
        resizer.className = 'tg-component-resizer';
        resizer.icon = 'tg-icons:resize-bottom-right';
        resizer.hidden = true;
        resizer.setAttribute('tooltip-text', 'Drag to resize<br>Double tap to reset height');
        addListener(resizer, 'down', event => this._resizeStarted(event));
        addListener(resizer, 'up', event => this._resizeEnded(event));
        addListener(resizer, 'track', event => this._resize(event));
        addListener(resizer, 'tap', TgDoubleTapHandlerBehavior._createDoubleTapHandler.call(this, '_lastResizerTap', () => this._resetHeight()));
        return resizer;
    },

    /**
     * Changes the height of the component by the vertical movement of the resizer, and remembers the height once the resizer is released.
     * The component grows or shrinks further while the resizer is beyond the bottom or the top of the scrolling container, which is scrolled to follow the resizer.
     */
    _resize: function (event) {
        tearDownEvent(event);
        if (event.detail.state === 'track') {
            const style = getComputedStyle(this);
            const prevHeight = parseFloat(style.height);
            let newHeight = prevHeight + event.detail.ddy;
            const scrollContainer = this._scrollingParent();
            const mousePos = scrollContainer && getRelativePos(event.detail.x, event.detail.y, scrollContainer);
            const beyondScrollContainer = !!mousePos && (mousePos.y > scrollContainer.offsetHeight || mousePos.y < 0);
            if (beyondScrollContainer) {
                newHeight += mousePos.y > 0 ? mousePos.y - scrollContainer.offsetHeight : mousePos.y;
            }
            // the resizer stays within the component, so that the component can be resized again
            newHeight = Math.max(newHeight, parseFloat(style.minHeight) || 0, this._resizer.offsetHeight);
            this.style.height = `${newHeight}px`;
            if (beyondScrollContainer) {
                scrollContainer.scrollTop += newHeight - prevHeight;
            }
        } else if (event.detail.state === 'end') {
            localStorage.setItem(this._heightKey(), this.style.height);
        }
    },

    /**
     * Restores the declared height, forgetting the height set by the user.
     */
    _resetHeight: function () {
        localStorage.removeItem(this._heightKey());
        this.style.height = this.height;
    },

    /**
     * Shows the resizing cursor everywhere while the resizer is dragged, overriding the cursors of the application, as for a rich text editor.
     */
    _resizeStarted: function (event) {
        tearDownEvent(event);
        document.styleSheets[0].insertRule('* { cursor: ns-resize !important; }', 0);
    },

    _resizeEnded: function (event) {
        tearDownEvent(event);
        if (document.styleSheets.length > 0 && document.styleSheets[0].cssRules.length > 0) {
            document.styleSheets[0].deleteRule(0);
        }
    },

    /**
     * Returns the nearest ancestor of the component that scrolls, through the slots and shadow roots on the way.
     */
    _scrollingParent: function () {
        let parent = this;
        while (parent && parent.offsetHeight === parent.scrollHeight) {
            parent = parent.assignedSlot || parent.parentElement || parent.getRootNode().host;
        }
        return parent;
    },

    _heightKey: function () {
        return localStorageKey(`${this.entityType}_${this.id}_height`);
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
