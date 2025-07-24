import { checkLinkAndOpen } from '/resources/components/tg-link-opener.js';

export function postActionLinkOpen(urlString, target, windowFeatures) {
    checkLinkAndOpen(urlString, target, windowFeatures);
}