package ua.com.fielden.platform.web.action.post;

import static ua.com.fielden.platform.web.action.post.BindSavedPropertyPostActionSuccess.createPostAction;

import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * In case if functional entity saves its master entity, it is necessary to bind saved instance to its respective entity master.
 * Use this {@link IPostAction} in {@link IEntityActionBuilder2#postActionError(IPostAction)} call.
 * 
 * @author TG Team
 *
 */
public class BindSavedPropertyPostActionError implements IPostAction {
    private final String propertyName;

    /**
     * Creates {@link BindSavedPropertyPostActionError} with {@code propertyName} indicating where master entity resides.
     * 
     * @param propertyName
     */
    public BindSavedPropertyPostActionError(final String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public JsCode build() {
        return createPostAction(true, propertyName);
    }

}