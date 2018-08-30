package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.security.SignatureException;
import java.util.Collection;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.cypher.SessionIdentifierGenerator;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.exceptions.SecurityException;

/**
 * DAO implementation of {@link IUserSecret}.
 *
 * @author TG Team
 *
 */
@EntityType(UserSecret.class)
public class UserSecretDao extends CommonEntityDao<UserSecret> implements IUserSecret {

    private final Logger logger = Logger.getLogger(getClass());
    private final SessionIdentifierGenerator crypto;

    @Inject
    public UserSecretDao(
            final SessionIdentifierGenerator crypto,
            final IFilter filter) {
        super(filter);
        this.crypto = crypto;
    }

    @Override
    public final String hashPasswd(final String passwd, final String salt) {
        try {
            return crypto.calculatePBKDF2WithHmacSHA1(passwd, salt);
        } catch (final SignatureException ex) {
            logger.error(ex);
            throw new SecurityException("Could not hash user password.", ex);
        }
    }
    
    @Override
    protected IFetchProvider<UserSecret> createFetchProvider() {
        return super.createFetchProvider().with("key.email", "key.base", "salt", "password", "resetUuid");
    }

    @SessionRequired
    @Override
    public Optional<UserSecret> findByUsername(String username) {
        final EntityResultQueryModel<UserSecret> query = select(UserSecret.class).where().prop("key.key").eq().val(username).model();
        final QueryExecutionModel<UserSecret, EntityResultQueryModel<UserSecret>> qem = from(query).with(createFetchProvider().fetchModel()).model();
        return getEntityOptional(qem);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    public String newSalt() {
        return crypto.genSalt();
    }

}