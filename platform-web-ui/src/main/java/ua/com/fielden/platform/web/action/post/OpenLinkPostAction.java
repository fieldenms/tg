package ua.com.fielden.platform.web.action.post;

import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// A standard post-action that should be used for opening a link from a property.
///
/// Standard `'window.open'` opening can be susceptible to tab-napping.
/// So it is always recommended to use this post-action for user-entered links.
///
/// See more in `'tg-polymer-utils.openLink'`.
///
/// @author TG Team
public class OpenLinkPostAction implements IPostAction {
    protected final IConvertableToPath property;

    protected OpenLinkPostAction(final IConvertableToPath property) {
        this.property = requireNonNull(property);
    }

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("openLink", "reflection/tg-polymer-utils"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return jsCode("""
            openLink(functionalEntity.get('%s'));
        """.formatted(
            property.toPath()
        ));
    }

}
