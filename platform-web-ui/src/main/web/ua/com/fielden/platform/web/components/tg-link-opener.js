import { TgAppConfig } from '/app/tg-app-config.js';
import { TgConfirmationDialog } from '/resources/components/tg-confirmation-dialog.js';
import { localStorageKey } from '/resources/reflection/tg-polymer-utils.js';

import moment from '/resources/polymer/lib/moment-lib.js';

import '/resources/polymer/@polymer/paper-styles/color.js';

const appConfig = new TgAppConfig();
const confirmationDialog = new TgConfirmationDialog();

/**
 * Loads a specified resource into new or existing browsing context (see https://developer.mozilla.org/en-US/docs/Web/API/Window/open).
 * Logs an error in case if resource opening was blocked by popup blocker or some other problem prevented it.
 *
 * Unspecified 'target' means '_blank' i.e. most likely to be opened in a new tab (or window with special user options).
 */
const openLink = function (url, target, windowFeatures) {
    const newWindow = window.open(url, target, windowFeatures);
    if (newWindow) {
        // Always prevent tabnapping.
        // I.e. prevent ability by new tab / window to rewrite 'location' of original tab / window through 'opener' property.
        newWindow.opener = null;

        if (newWindow.focus) {
            // Create an asynchronous request to bring to view a newly opened window / tab.
            // In most cases this will work without 'focus()' call.
            // However, tapping on original tab / window may prevent this behaviour.
            newWindow.focus();
        }
    } else {
        // The window wasn't allowed to open.
        // This is likely caused by built-in or external popup blockers.
        // Log this to both server and user.
        throw new Error(`Link [${url}] blocked. Target: [${target}], windowFeatures: [${windowFeatures}].`);
    }
};

/**
 * Determines whether a given URL points to an external site relative to the current application.
 * 
 * @param {string} url - The URL to check.
 * @returns {boolean} `true` if the URL is external, `false` otherwise.
 */
export function isExternalURL(url) {
    try {
        return new URL(url).hostname !== window.location.hostname
    } catch (e) {
        return false;
    }
}

/**
 * Displays a confirmation dialog before opening a potentially external link.
 * If the user accepts, their choice can be remembered to skip the dialog in the future for the same URL or host.
 * 
 * @param {String} url - the URL to open.
 * @param {String} target - the `target` attribute used when opening the link (e.g., "_blank").
 * @param {Object} windowFeatures - optional features passed to `window.open()` when opening the link.
 */
export const checkLinkAndOpen = function (url, target, windowFeatures) {
    const dateFormat = 'YYYY MM DD';
    const urlInstance = URL.parse(url);
    if (urlInstance) {
        const hostName = urlInstance.hostname;

        const isAllowedSite = () => appConfig.siteAllowlist.includes(hostName);

        const wasAcceptedByUser = () => {
            const now = moment();
            const isRecent = (key) =>
                localStorage.getItem(key) &&
                now.diff(moment(localStorage.getItem(key), dateFormat), 'days') < appConfig.daysUntilSitePermissionExpires;

            return isRecent(localStorageKey(url)) || isRecent(localStorageKey(hostName));
        };

        if (isExternalURL(url) && !isAllowedSite() && !wasAcceptedByUser()) {
            const text = `The link is taking you to another site.<br>Are you sure you would like to continue?<br><pre style="line-break:anywhere;max-width:500px;white-space:normal;color:var(--paper-light-blue-500);">${url}</pre>`;
            const options = ["Don't show this again for this link", "Don't show this again for this site"];
            const buttons = [{ name: 'Cancel' }, { name: 'Continue', confirm: true, autofocus: true, classes: "red" }];

            confirmationDialog.showConfirmationDialog(text, buttons, { single: true, options }, "Double-check this link").then(opt => {
                if (opt[options[0]]) {
                    localStorage.setItem(localStorageKey(url), moment().format(dateFormat));
                }
                if (opt[options[1]]) {
                    localStorage.setItem(localStorageKey(hostName), moment().format(dateFormat));
                }
                openLink(url, "_blank", windowFeatures);
            });
        } else {
            openLink(url, target, windowFeatures);
        }
    }
}