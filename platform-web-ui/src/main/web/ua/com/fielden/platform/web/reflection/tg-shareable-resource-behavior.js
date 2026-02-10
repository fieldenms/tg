import { html } from '/resources/polymer/@polymer/polymer/lib/utils/html-tag.js';

import '/resources/actions/tg-ui-action.js';
import { UnreportableError } from '/resources/components/tg-global-error-handler.js';

/// Copies `link` to the clipboard, if non-empty, and shows an informational toast.
///
function _copyLinkToClipboard (link, toast) {
    if (link) {
        // Writing into clipboard is mostly permitted for currently open tab
        //   (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText).
        // Unless it is a Safari-based browser and the action is performed asynchronously to the user input
        //   (`NotAllowedError: The request is not allowed by the user agent or the platform in the current context, possibly because the user denied permission.`).
        // Writing errors should not be shown to the user, but let's log them to the console and server logger.
        navigator.clipboard && navigator.clipboard.writeText(link).then(
            () => {
                toast.text = 'Copied to clipboard.';
                toast.hasMore = true;
                toast.msgText = link;
                toast.showProgress = false;
                toast.isCritical = false;
                toast.show();
            },
            error => {
                console.error(error);
                throw new UnreportableError(error);
            }
        );
    }
}

export function getShareActionTemplate() {
    return html`<tg-ui-action
        id="shareAction"
        hidden>
    </tg-ui-action>`;
}

export const TgShareableResourceBehavior = {

    openShareAction: function (toast, parentUuid, showDialog, createContextHolder, calculateSharedUri, enhanceAction) {
        // Create a dynamic share action.
        const shareAction = this.$.shareAction || document.createElement('tg-ui-action');

        // Provide only the necessary attributes.
        // Avoid shouldRefreshParentCentreAfterSave, because there is no need to refresh parent master.
        // Avoid also `toaster` object in `tg-ui-action`.
        // It is only used for dynamic actions. That's why this object setting can be skipped here.
        // See `tg-entity-binder-behavior.toaster = ...` initialisation for more details on how toaster object looks
        //   (different from <tg-toast> elements).
        shareAction.shortDesc = 'Share';
        shareAction.componentUri = '/master_ui/ua.com.fielden.platform.share.ShareAction';
        shareAction.elementName = 'tg-ShareAction-master';
        shareAction.showDialog = showDialog;
        shareAction.createContextHolder = createContextHolder;
        shareAction.attrs = {
            entityType: 'ua.com.fielden.platform.share.ShareAction',
            currentState: 'EDIT',
            // `centreUuid` is important to be able to close master through CANCEL (aka CLOSE) `tg-action`.
            centreUuid: parentUuid
        };
        shareAction.requireSelectionCriteria = 'false';
        shareAction.requireSelectedEntities = 'NONE';
        shareAction.requireMasterEntity = 'true';

        enhanceAction && enhanceAction(shareAction);

        // Copy `ShareAction.hyperlink` to the clipboard on successful action completion (which is performed in the retrieval request).
        shareAction.modifyFunctionalEntity = (_currBindingEntity, master, action) => {
            if (_currBindingEntity && _currBindingEntity.get('hyperlink')) {
                _copyLinkToClipboard(_currBindingEntity.get('hyperlink').value, toast);
            }
        };

        if (calculateSharedUri) {
            const uri = calculateSharedUri();
            if (uri === null) {
                throw new Error("The result of [calculateSharedUri] must not be null.");
            }
            shareAction._sharedUri = uri;
        }

        shareAction._run();
    }
}