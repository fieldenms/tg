package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder2;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// In case if functional entity saves its master entity, it is necessary to bind saved instance to its respective entity master.
/// Use this [IPostAction] in [IEntityActionBuilder2#postActionSuccess(IPostAction)] call.
/// Or in [IEntityActionBuilder2#postActionError(IPostAction)] call.
///
/// @author TG Team
public class BindSavedPropertyPostAction implements IPostAction {
    private final IConvertableToPath property;
    private final boolean erroneous;

    BindSavedPropertyPostAction(final IConvertableToPath property, final boolean erroneous) {
        this.property = requireNonNull(property);
        this.erroneous = erroneous;
    }

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("bindSavedProperty", "master/actions/tg-bind-saved-property"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return jsCode("""
            bindSavedProperty(functionalEntity, '%s', self, %s);
        """.formatted(
            property.toPath(),
            erroneous
        ));
    }

}