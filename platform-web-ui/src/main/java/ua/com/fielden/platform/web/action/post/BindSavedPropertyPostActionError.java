package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Set.of;
import static ua.com.fielden.platform.web.action.post.BindSavedPropertyPostActionSuccess.createPostAction;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// In case if functional entity saves its master entity, it is necessary to bind saved instance to its respective entity master.
/// Use this [IPostAction] in [IEntityActionBuilder2#postActionError(IPostAction)] call.
///
/// @author TG Team
public class BindSavedPropertyPostActionError implements IPostAction {
    private final IConvertableToPath property;

    BindSavedPropertyPostActionError(final IConvertableToPath property) {
        this.property = requireNonNull(property);
    }

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("getParentAnd", "reflection/tg-polymer-utils"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return createPostAction(true, property);
    }

}