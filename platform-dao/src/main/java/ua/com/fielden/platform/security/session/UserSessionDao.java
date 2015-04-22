package ua.com.fielden.platform.security.session;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IUserSession}.
 *
 * @author Developers
 *
 */
@EntityType(UserSession.class)
public class UserSessionDao extends CommonEntityDao<UserSession> implements IUserSession {

    @Inject
    public UserSessionDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public void clearAll(final User user) {
        super.delete(select(UserSession.class).where().prop("user").eq().val(user).model());
    }

    @Override
    public void clearUntrusted(final User user) {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                .where()
                    .prop("user").eq().val(user)
                    .and().prop("trusted").eq().val(false)
                .model();
        super.delete(query);
    }

    @Override
    public void clearAll() {
        super.delete(select(UserSession.class).model());

    }

    @Override
    public void clearUntrusted() {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                .where()
                    .prop("trusted").eq().val(false)
                .model();
        super.delete(query);
    }

    @Override
    public void clearExpired(final User user) {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                .where()
                    .prop("user").eq().val(user)
                    .and().prop("expiryTime").lt().now()
                .model();
        super.delete(query);
   }

    @Override
    public void clearExpired() {
        final EntityResultQueryModel<UserSession> query =
                select(UserSession.class)
                .where()
                    .prop("expiryTime").lt().now()
                .model();
        super.delete(query);
    }
}