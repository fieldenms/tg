import '/resources/actions/tg-ui-action.js';

/// Opens share action master with tiny URL and QR code.
///
/// @param toaster - object containing toast related methods (can be <tg-toast> itself)
/// @param toast - <tg-toast> for displaying informational toasts
/// @param parentUuid - uuid of the parent element
/// @param showDialog - showDialog function of the parent element
/// @param createContextHolder - createContextHolder function of the parent element or some other custom logic for parent context creation
/// @param calculateSharedUri - function to calculate TinyHyperlink.target link; provide null if not needed
/// @param enhanceAction - mutator function of resultant action to be performed before _run()
///
export function openShareAction (toaster, toast, parentUuid, showDialog, createContextHolder, calculateSharedUri, enhanceAction) {
    // Create dynamic share action.
    const shareAction = document.createElement('tg-ui-action');

    // Provide only the necessary attributes.
    // Avoid shouldRefreshParentCentreAfterSave, because there is no need to refresh parent master.
    shareAction.shortDesc = 'Share';
    shareAction.componentUri = '/master_ui/ua.com.fielden.platform.share.ShareAction';
    shareAction.elementName = 'tg-ShareAction-master';
    shareAction.showDialog = showDialog;
    shareAction.createContextHolder = createContextHolder;
    shareAction.toaster = toaster;
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

    // Copy link to a clipboard on successful action completion (which is performed in retrieval request).
    shareAction.modifyFunctionalEntity = (_currBindingEntity, master, action) => {
        if (_currBindingEntity && _currBindingEntity.get('hyperlink')) {
            _copyLinkToClipboard(_currBindingEntity.get('hyperlink').value, toast);
        }
    };

    shareAction._sharedUri = calculateSharedUri();

    // Run dynamic share action.
    shareAction._run();
}

/// Copies non-empty link to clipboard and shows informational toast.
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