import { TgAppConfig } from '/app/tg-app-config.js';
import { TgConfirmationDialog } from '/resources/components/tg-confirmation-dialog.js';
import { localStorageKey } from '/resources/reflection/tg-polymer-utils.js';

import moment from '/resources/polymer/lib/moment-lib.js';

import '/resources/polymer/@polymer/paper-styles/color.js';

const appConfig = new TgAppConfig();
const confirmationDialog = new TgConfirmationDialog();

// 'mailto:' protocol, which is supported in TG links.
export const MAILTO_PROTOCOL = 'mailto:';
// Protocols, which are supported in TG links (besides mailto: above).
export const SUPPORTED_PROTOCOLS = ['https:', 'http:', 'ftp:', 'ftps:'];
// An error indicating unsupported protocol usage in links.
export const ERR_UNSUPPORTED_PROTOCOL = 'One of http, https, ftp, ftps or mailto hyperlink protocols is expected.';

/**
 * Loads a specified resource into new or existing browsing context (see https://developer.mozilla.org/en-US/docs/Web/API/Window/open).
 * Logs an error in case if resource opening was blocked by popup blocker or some other problem prevented it.
 *
 * Unspecified 'target' means '_blank' i.e. most likely to be opened in a new tab (or window with special user options).
 */
function openLink(url, target, windowFeatures) {
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
 * Processes the given `urlString` and returns an object containing URL and hostname property.
 * Returns null if the URL string is invalid.
 *
 * This function validates URLs with 'mailto:' protocol.
 * Invalid email addresses are treated as internal and we let browser to handle them.
 *
 * The same is for unsupported protocols (e.g. file:).
 * Browser is rather strict with all such protocols in context of existing app.
 *
 * @param {string} url - The URL string to process.
 * @returns {{ url: URL, hostname: string } | null} An object with the { url; hostname} properties, or null if the URL is invalid.
 */
export function processURL(urlString) {
    try {
        const urlInstance = createUrl(urlString);
        if (!urlInstance) {
            return null;
        }
        // Determine hostname from email address for 'mailto:' URL.
        else if (urlInstance.protocol === MAILTO_PROTOCOL) {
            const mail = urlInstance.pathname;
            const mailParts = mail.split('@');
            if (mailParts.length === 2) {
                return { url: urlInstance, hostname: mailParts[1] };
            } else {
                console.error(`URL: [${urlString}].`, new Error(`Invalid e-mail address: [${mail}].`));
                return { url: urlInstance, hostname: window.location.hostname };
            }
        }
        // Supported protocol URLs are hadled as is; and they can contain broken links with our origin prepended.
        else if (SUPPORTED_PROTOCOLS.includes(urlInstance.protocol)) {
            return { url: urlInstance, hostname: urlInstance.hostname };
        }
        // Unsupported protocol URLs are treated as internal and are handled consistently as browser would do.
        else {
            console.error(`URL: [${urlString}].`, new Error(ERR_UNSUPPORTED_PROTOCOL));
            return { url: urlInstance, hostname: window.location.hostname };
        }
    } catch (e) {
        console.error(`URL: [${urlString}].`, e);
        return null;
    }
}

/**
 * Determines whether a given URL points to an external site relative to the current application.
 * 
 * @param {Object} urlAndHostname - an object with hostname to compare; or empty object if representing broken link (https://[xyz]/).
 * @returns {boolean} `true` if the URL is external, `false` otherwise.
 */
export function isExternalURL(urlAndHostname) {
    return urlAndHostname && urlAndHostname.hostname !== window.location.hostname;
}

/**
 * Displays a confirmation dialog before opening a potentially external link.
 * If the user accepts, their choice can be remembered to skip the dialog in the future for the same URL or host.
 * 
 * @param {String} urlString - the URL string to be processed before opening.
 * @param {String} target - the `target` attribute used when opening the link (e.g., "_blank").
 * @param {Object} windowFeatures - optional features passed to `window.open()` when opening the link.
 */
export function checkLinkAndOpen(urlString, target, windowFeatures) {
    const dateFormat = 'YYYY MM DD';
    const urlAndHostname = processURL(urlString);
    if (urlAndHostname) {
        const url = urlAndHostname.url.href;
        if (isExternalURL(urlAndHostname)) {
            const hostName = urlAndHostname.hostname;
            const isAllowedSite = () => appConfig.getSiteAllowlist() && appConfig.getSiteAllowlist().find(pattern => pattern.test(hostName));
            const wasAcceptedByUser = () => {
                const now = moment();
                const isRecent = (key) =>
                    appConfig.getDaysUntilSitePermissionExpires() && 
                    localStorage.getItem(key) &&
                    now.diff(moment(localStorage.getItem(key), dateFormat), 'days') < appConfig.getDaysUntilSitePermissionExpires();

                return isRecent(localStorageKey(url)) || isRecent(localStorageKey(hostName));
            };
            if (!isAllowedSite() && !wasAcceptedByUser()) {
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
                openLink(url, "_blank", windowFeatures);
            }
        } else {
            openLink(url, target, windowFeatures);
        }
    }
}

/**
 * Creates URL instance from `urlString` in context of our origin (e.g. https://tgdev.com).
 *
 * Broken links will be appended to our origin exactly as how browser does it during opening.
 * I.e. it opens them in a context of current site (origin) with all necessary encodings etc.
 *
 * Links with proper protocol (e.g. https:, mailto: or file:) are left "as is".
 * Links like `#/Work%20Activities/Work%20Activity` will be appended to our origin (as broken ones) and become actionable.
 */
function createUrl(urlString) {
    try {
        return new URL(urlString, window.location.origin);
    }
    // Still catch an error - it should rarely appear though (e.g. `https://[xyz]/`).
    catch (error) {
        console.error(`URL: [${urlString}].`, error);
        return null;
    }
}