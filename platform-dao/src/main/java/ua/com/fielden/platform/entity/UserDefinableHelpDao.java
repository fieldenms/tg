package ua.com.fielden.platform.entity;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.UserDefinableHelp_CanSave_Token;
import ua.com.fielden.platform.types.Hyperlink;

/**
 * DAO implementation for companion object {@link UserDefinableHelpCo}.
 *
 * @author TG Team
 *
 */
@EntityType(UserDefinableHelp.class)
public class UserDefinableHelpDao extends CommonEntityDao<UserDefinableHelp> implements UserDefinableHelpCo {

    private final String defaultHelpUri;

    @Inject
    public UserDefinableHelpDao(final @Named("help.defaultUri") String defaultHelpUri) {
        this.defaultHelpUri = defaultHelpUri;
    }

    @Override
    public UserDefinableHelp findOrNewWithDefaultHelp(final String refElement) {
        return findByKeyAndFetchOptional(FETCH_PROVIDER.fetchModel(), refElement).orElseGet(() -> {
            final UserDefinableHelp help = new_().setReferenceElement(refElement);
            if (!isEmpty(defaultHelpUri)) {
                help.setHelp(new Hyperlink(defaultHelpUri));
            }
            return help;
        });
    }

    @Override
    @SessionRequired
    public UserDefinableHelp save(final UserDefinableHelp entity) {
        if (!entity.isSkipUi()) {
            return saveToChange(entity);
        }
        return entity;
    }

    @Authorise(UserDefinableHelp_CanSave_Token.class)
    protected UserDefinableHelp saveToChange(final UserDefinableHelp entity) {
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<UserDefinableHelp> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
