import '/resources/actions/tg-ui-action.js';

/// Opens share action master with tiny URL and QR code.
///
/// @param {TgToast} toast - <tg-toast> for displaying informational toasts
/// @param {String} parentUuid - uuid of the parent element
/// @param {Function} showDialog - showDialog function of the parent element
/// @param {Function} createContextHolder - createContextHolder function of the parent element or some other custom logic for parent context creation
/// @param {Function} [calculateSharedUri] - optional function that produces a URL to be recorded as TinyHyperlink.target
/// @param {Function} [enhanceAction] - mutator function of resultant action to be performed before _run()
///
export function openShareAction (toast, parentUuid, showDialog, createContextHolder, calculateSharedUri, enhanceAction) {
    // Create a dynamic share action.
    const shareAction = document.createElement('tg-ui-action');

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

/// Copies `link` to the clipboard, if non-empty, and shows an informational toast.
///
function _copyLinkToClipboard (link, toast) {
    if (link) {
        // Writing into clipboard is always permitted for currently open tab
        //   (https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText)
        //   -- that's why promise error should never occur.
        // If for some reason the promise will be rejected then 'Unexpected error occurred.' will be shown to the user.
        // Also, global handler will report that to the server.
        navigator.clipboard && navigator.clipboard.writeText(link).then(() => {
            toast.text = 'Copied to clipboard.';
            toast.hasMore = true;
            toast.msgText = link;
            toast.showProgress = false;
            toast.isCritical = false;
            toast.show();
        });
    }
}
