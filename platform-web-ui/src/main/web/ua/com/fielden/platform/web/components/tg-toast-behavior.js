import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

const paperToastStyle = html`
    <custom-style>
        <style>
            paper-toast {
                @apply --layout-horizontal;
                @apply --layout-center;
                max-width: 256px;
                left:0;
                bottom:0;
            }
            .more {
                padding-left: 8px;
                color: #03A9F4;
                font-weight: 800;
                cursor: pointer;
            }
            #toast paper-spinner {
                width: 1.5em;
                height: 1.5em;
                min-width: 1em;
                min-height: 1em;
                max-width: 2em;
                max-height: 2em;
                padding: 2px;
                margin-left: 1em;
                --paper-spinner-layer-1-color: var(--paper-blue-500);
                --paper-spinner-layer-2-color: var(--paper-blue-500);
                --paper-spinner-layer-3-color: var(--paper-blue-500);
                --paper-spinner-layer-4-color: var(--paper-blue-500);
            }
            .toast-dialog paper-button {
                color: var(--paper-light-blue-500);
                --paper-button-flat-focus-color: var(--paper-light-blue-50);
            }
            .toast-dialog paper-button:hover {
                background: var(--paper-light-blue-50);
            }
        </style>
    </custom-style>`;
paperToastStyle.setAttribute('style', 'display: none;');
document.head.appendChild(paperToastStyle.content);

export const TgToastBehavior = {

    ready: function () {
         //Indicates whether toast overlay can be closed via history (back button or not)
         this._toast().skipHistoryAction = true;

         // Styles to truncate the toast text.
         const label = this._toast().$$('#label');
         label.style.flex = '1';
         label.style.whiteSpace = 'nowrap';
         label.style.overflow = 'hidden';
         label.style.textOverflow = 'ellipsis';
    },

    getDocumentToast: function (toastId) {
        const toasts = document.querySelectorAll('#' + toastId);
        let toast = null;
        let existingToastCount = 0;
        for (let index = 0; index < toasts.length; index++) {
            const currToast = toasts.item(index);
            if (currToast.parentNode === document.body) {
                existingToastCount++;
                if (existingToastCount > 1) {
                    throw 'More than one toast exist in body direct children.';
                }
                toast = currToast;
            }
        }
        return toast;
    },

    /**
     * Overrode this to provide reference on paper-toast element 
     */
    _toast: function () {}

};