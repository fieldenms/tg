import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import { IronResizableBehavior } from '/resources/polymer/@polymer/iron-resizable-behavior/iron-resizable-behavior.js';
import { tearDownEvent } from '/resources/reflection/tg-polymer-utils.js';

/**
 * Resizable & movable component with title bar (id = 'titleBar') and resizer.
 */
export const TgResizableMovableBehavior = {

    properties: {
        /**
         * Fixed title bar height.
         */
        titleBarHeight: {
            type: Number,
            value: 44
        },

        /**
         * Fixed resizer height.
         */
        resizerHeight: {
            type: Number,
            value: 14
        },

        /**
         * Minimum width of resizable component.
         */
        minimumWidth: {
            type: Number
        },

        /**
         * Persists size, if resizing have actually performed.
         */
        persistSize: Function,

        /**
         * Persists size, if resizing have actually performed.
         */
        persistPosition: Function,

        /**
         * Calculates indication whether the component can be moved.
         */
        allowMove: Function
    },

    behaviors: [
        IronResizableBehavior
    ],

    resizeComponent: function (event) {
        const resizedHeight = this.offsetHeight + event.detail.ddy;
        const heightNeedsResize = resizedHeight >= this.titleBarHeight + this.resizerHeight;
        if (heightNeedsResize) {
            this.style.height = resizedHeight + 'px';
        }
        const resizedWidth = this.offsetWidth + event.detail.ddx;
        const widthNeedsResize = resizedWidth >= this.minimumWidth;
        if (widthNeedsResize) {
            this.style.width = resizedWidth + 'px';
        }
        if (heightNeedsResize || widthNeedsResize) {
            this.persistSize();
            this.notifyResize();
        }
    },

    moveComponent: function (e) {
        if (e.target === this.$.titleBar && this.allowMove()) {
            switch (e.detail.state) {
                case 'start':
                    this.$.titleBar.style.cursor = 'move';
                    this._windowHeight = window.innerHeight;
                    this._windowWidth = window.innerWidth;
                    break;
                case 'track':
                    const _titleBarDimensions = this.$.titleBar.getBoundingClientRect();
                    const leftNeedsChange = _titleBarDimensions.right + e.detail.ddx >= this.titleBarHeight && _titleBarDimensions.left + e.detail.ddx <= this._windowWidth - this.titleBarHeight;
                    if (leftNeedsChange) {
                        this.style.left = _titleBarDimensions.left + e.detail.ddx + 'px';
                    }
                    const topNeedsChange = _titleBarDimensions.top + e.detail.ddy >= 0 && _titleBarDimensions.bottom + e.detail.ddy <= this._windowHeight;
                    if (topNeedsChange) {
                        this.style.top = _titleBarDimensions.top + e.detail.ddy + 'px';
                    }
                    if (leftNeedsChange || topNeedsChange) {
                        this.persistPosition();
                    }
                    break;
                case 'end':
                    this.$.titleBar.style.removeProperty('cursor');
                    break;
            }
        }
        tearDownEvent(e);
    }

};