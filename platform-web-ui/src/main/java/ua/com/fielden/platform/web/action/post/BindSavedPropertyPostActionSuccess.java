package ua.com.fielden.platform.web.action.post;

import static java.lang.String.format;

import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

/**
 * In case if functional entity saves its master entity, it is necessary to bind saved instance to its respective entity master.
 * Use this {@link IPostAction} in {@link IEntityActionBuilder2#postActionSuccess(IPostAction)} call.
 * 
 * @author TG Team
 *
 */
public class BindSavedPropertyPostActionSuccess implements IPostAction {
    private final String propertyName;

    /**
     * Creates {@link BindSavedPropertyPostActionSuccess} with {@code propertyName} indicating where master entity resides.
     * 
     * @param propertyName
     */
    public BindSavedPropertyPostActionSuccess(final String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public JsCode build() {
        return createPostAction(false, propertyName);
    }

    /**
     * Creates {@link IPostAction}. For {@code erroneous} one we also attaches 'exceptionOccured' to the master entity.
     * 
     * @param erroneous
     * @return
     */
    static JsCode createPostAction(final boolean erroneous, final String propertyName) {
        return new JsCode(format(""
            + "const parentMasterName = `tg-${functionalEntity.type().prop('%s').type()._simpleClassName()}-master`;\n"
            + "const parentMaster = getParentAnd(self, parent => parent.matches(parentMasterName));\n"
            + "const masterEntity = functionalEntity.get('%s');\n"
            + (erroneous ? "parentMaster._provideExceptionOccured(masterEntity, functionalEntity.exceptionOccured());\n" : "")
            + "parentMaster._postSavedDefault(masterEntity);\n",
            propertyName, propertyName
        ));
    }

}