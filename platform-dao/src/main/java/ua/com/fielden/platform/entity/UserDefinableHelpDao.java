package ua.com.fielden.platform.entity;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

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
    public UserDefinableHelpDao(final @Named("help.defaultUri") String defaultHelpUri, final IFilter filter) {
        super(filter);
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
    @Authorise(UserDefinableHelp_CanSave_Token.class)
    public UserDefinableHelp save(final UserDefinableHelp entity) {
        if (!entity.isSkipUi()) {
            return super.save(entity);
        }
        return entity;
    }

    @Override
    protected IFetchProvider<UserDefinableHelp> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}