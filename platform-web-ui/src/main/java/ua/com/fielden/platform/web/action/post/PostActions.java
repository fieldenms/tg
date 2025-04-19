package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/// Factory for [ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction]s.
///
/// @author TG Team
public interface PostActions {

    /// Creates [BindSavedPropertyPostActionSuccess] with `property` indicating where master entity resides.
    static IPostAction bindSavedProperty(final IConvertableToPath property) {
        return new BindSavedPropertyPostActionSuccess(property);
    }

    /// Creates [BindSavedPropertyPostActionError] with `property` indicating where master entity resides.
    static IPostAction bindSavedPropertyError(final IConvertableToPath property) {
        return new BindSavedPropertyPostActionError(property);
    }

    /// Creates {@link FileSaverPostAction}.
    static IPostAction saveFile() {
        return new FileSaverPostAction();
    }

    /// Creates [OpenLinkPostAction] for `property` containing a link to open.
    static IPostAction openLink(final IConvertableToPath property) {
        return new OpenLinkPostAction(property);
    }

    /// Creates {@link CloseForciblyPostAction}.
    static IPostAction closeForcibly() {
        return new CloseForciblyPostAction();
    }

}
