package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/**
 * In case if functional entity saves its master entity, it is necessary to bind saved instance to its respective entity master.
 * Use this {@link IPostAction} in {@link IEntityActionBuilder2#postActionSuccess(IPostAction)} call.
 * 
 * @author TG Team
 *
 */
public class BindSavedPropertyPostActionSuccess implements IPostAction {
    private final IConvertableToPath property;

    /**
     * Creates {@link BindSavedPropertyPostActionSuccess} with {@code property} indicating where master entity resides.
     * 
     * @param property
     */
    public BindSavedPropertyPostActionSuccess(final IConvertableToPath property) {
        this.property = requireNonNull(property);
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return createPostAction(false, property);
    }

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("getParentAnd", "reflection/tg-polymer-utils"));
    }

    /**
     * Creates {@link IPostAction}. For {@code erroneous} one we also attaches 'exceptionOccurred' to the master entity.
     * 
     * @param erroneous
     * @return
     */
    static JsCode createPostAction(final boolean erroneous, final IConvertableToPath property) {
        return new JsCode(format(""
            + "const parentMasterName = `tg-${functionalEntity.type().prop('%s').type()._simpleClassName()}-master`;\n"
            + "const parentMaster = getParentAnd(self, parent => parent.matches(parentMasterName));\n"
            + "const masterEntity = functionalEntity.get('%s');\n"
            + (erroneous ? "parentMaster._provideExceptionOccurred(masterEntity, functionalEntity.exceptionOccurred());\n" : "")
            // in successful case leave propertyActionIndices as previously;
            //  we are not able to calculate them in companion 'save' methods because multi-action selectors are UI concept;
            //  still, this temporal unsyncing is not a problem;
            //  this is because propertyActionIndices will be updated on parentMaster 'validate' process following immediately after postActionSuccess (see tg-ui-action._onExecuted.postSaved postal publish)
            // in unsuccessful case it is even more important to leave propertyActionIndices as previously (not to clear them or something);
            //  this is because parentMaster update will not be performed and, in case of clearing, all actions on parentMaster will disappear (at this stage even non-multi action should have zero index)
            + "parentMaster._postSavedDefault([masterEntity, { propertyActionIndices: parentMaster._propertyActionIndices }]);\n",
            property.toPath(), property.toPath()
        ));
    }

}