package ua.com.fielden.platform.security;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.representation.Representation;

import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * RAO implementation of the {@link IUserEx}.
 * 
 * @author TG Team
 * 
 */

@EntityType(User.class)
public class UserControllerRao extends CommonEntityRao<User> implements IUserEx {
    private final fetch<User> fetchModel = fetch(User.class).with("basedOnUser").with("roles", fetchAll(UserAndRoleAssociation.class));//.with("userRole").with("user")

    private final IUserRoleDao userRoleDao;

    @Inject
    public UserControllerRao(final IUserRoleDao userRoleDao, final RestClientUtil restUtil) {
        super(restUtil);
        this.userRoleDao = userRoleDao;
    }

    @Override
    public List<? extends UserRole> findAllUserRoles() {
        return userRoleDao.findAll();
    }

    @Override
    public List<User> findAllUsers() {
        return findAllUsersWithRoles();
    }

    @Override
    public IPage<? extends User> firstPageOfUsersWithRoles(final int capacity) {
        final EntityResultQueryModel<User> model = select(User.class).where().prop(AbstractEntity.KEY).isNotNull().model();
        final OrderingModel orderBy = orderBy().prop(AbstractEntity.KEY).asc().model();
        return firstPage(from(model).with(fetchModel).with(orderBy).model(), capacity);
    }

    @Override
    public void updateUsers(final Map<User, Set<UserRole>> userRoleMap) {
        // prepare an envelope
        final Map<Long, List<Long>> envelopeContent = convert(userRoleMap);
        final Representation envelope = restUtil.represent(envelopeContent);
        // prepare a request
        final Request request = restUtil.newRequest(Method.POST, restUtil.getBaseUri(getDefaultWebResourceType()) + "/useroles");
        request.setEntity(envelope);
        // process request
        final Pair<Response, Result> result = restUtil.process(request);
        if (!result.getValue().isSuccessful()) {
            throw result.getValue();
        }
    }

    private Map<Long, List<Long>> convert(final Map<User, Set<UserRole>> userRoleMap) {
        final Map<Long, List<Long>> result = new HashMap<>();
        for (final User user : userRoleMap.keySet()) {
            final List<Long> roleIds = new ArrayList<Long>();
            for (final UserRole role : userRoleMap.get(user)) {
                roleIds.add(role.getId());
            }
            result.put(user.getId(), roleIds);
        }
        return result;
    }

    @Override
    public User findUserByIdWithRoles(final Long id) {
        return findById(id, fetchModel);
    }

    @Override
    public User findUserByKeyWithRoles(final String key) {
        final EntityResultQueryModel<User> model = select(User.class).where().prop("key").eq().val(key).model();
        return getEntity(from(model).with(fetchModel).model());
    }

    @Override
    public List<User> findAllUsersWithRoles() {
        final EntityResultQueryModel<User> model = select(User.class).where().prop("key").isNotNull().model();
        final OrderingModel orderBy = orderBy().prop("key").asc().model();
        return getAllEntities(from(model).with(fetchModel).with(orderBy).model());
    }

    @Override
    public User findUser(final String username) {
        return findByKeyAndFetch(fetchAll(User.class), username);
    }

    @Override
    public User resetPasswd(User user) {
        throw new UnsupportedOperationException();
    }
}