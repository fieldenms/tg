package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import static ua.com.fielden.platform.web.action.post.BindSavedPropertyPostActionSuccess.createPostAction;

/// In case if functional entity saves its master entity, it is necessary to bind saved instance to its respective entity master.
/// Use this [IPostAction] in [IEntityActionBuilder2#postActionError(IPostAction)] call.
///
/// @author TG Team
public class BindSavedPropertyPostActionError implements IPostAction {
    private final IConvertableToPath property;

    /// Creates [BindSavedPropertyPostActionError] with `property` indicating where master entity resides.
    public BindSavedPropertyPostActionError(final IConvertableToPath property) {
        this.property = property;
    }

    @Override
    public JsCode build() {
        return createPostAction(true, property);
    }

}