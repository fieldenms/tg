import '/resources/polymer/@polymer/app-layout/app-drawer/app-drawer.js';

/**
 * An `app-drawer` that ignores `track` events which do not belong to a gesture it started.
 *
 * Polymer's `track` recogniser keeps a single, global gesture state, and resets it only on `mousedown` / `touchstart`.
 * As a result, `track` and `end` can be delivered to a drawer that never received `start` -- a gesture begun on another element, or a listener attached mid-gesture.
 * An `end` can also be delivered twice for one gesture, when a `touchmove` arrives after `touchend` for the same touch identifier.
 * In all of those cases `app-drawer._trackDetails` is `null`, and `app-drawer` dereferences it in `_trackMove` and in `_calculateVelocity`.
 */
class TgAppDrawer extends customElements.get('app-drawer') {

    _track (event) {
        if (this.persistent || this.disableSwipe) {
            return;
        }
        // Only a gesture that this drawer actually started has `_trackDetails` to work with.
        if (event.detail.state !== 'start' && this._drawerState !== this._DRAWER_STATE.TRACKING) {
            return;
        }
        super._track(event);
    }

}

customElements.define('tg-app-drawer', TgAppDrawer);
