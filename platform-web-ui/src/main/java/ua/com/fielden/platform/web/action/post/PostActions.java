package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/// Factory for [IPostAction]s.
///
/// @author TG Team
public interface PostActions {

    /// Creates [IPostAction] that binds saved `property` value to parent Entity Master.
    static IPostAction bindSavedProperty(final IConvertableToPath property) {
        return new BindSavedPropertyPostActionSuccess(property);
    }

    /// Creates [IPostAction]`Error` that binds saved `property` value to parent Entity Master.
    static IPostAction bindSavedPropertyError(final IConvertableToPath property) {
        return new BindSavedPropertyPostActionError(property);
    }

    /// Creates [IPostAction] that should be used for saving data to a local file.
    /// Its implementation depends on the contract that the underlying functional entity has properties:
    ///   - mime -- a MIME type for the data being exported
    ///   - fileName -- a file name including file extension where the data should be saved
    ///   - data -- base64 string representing a binary array
    ///
    /// Clears EGI selection by default (if performed in Entity Centre context).
    static IPostAction saveFile() {
        return new FileSaverPostAction();
    }

    /// Creates [IPostAction] that opens a link from [String] `property`.
    static IPostAction openLink(final IConvertableToPath property) {
        return new OpenLinkPostAction(property);
    }

    /// Creates [IPostAction] that closes parent Entity Master forcibly.
    static IPostAction closeForcibly() {
        return new CloseForciblyPostAction();
    }

}
