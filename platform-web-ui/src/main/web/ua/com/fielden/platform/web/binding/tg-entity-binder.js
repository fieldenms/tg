import { Polymer } from '/resources/polymer/@polymer/polymer/lib/legacy/polymer-fn.js';
import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/serialisation/tg-serialiser.js';
// FIXME <link rel="import" href="/resources/components/tg-toast.html">

const template = html`
    <tg-serialiser id="serialiser"></tg-serialiser>
    <!--tg-toast id="toastGreeting"></tg-toast-->
`;
Polymer({
    is: 'tg-entity-binder',

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        return this.$.serialiser;
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        return this._serialiser().reflector();
    },

    // /**
    //  * The toast component.
    //  */
    // _toastGreeting: function () {
    //     return this.$.toastGreeting;
    // }
});