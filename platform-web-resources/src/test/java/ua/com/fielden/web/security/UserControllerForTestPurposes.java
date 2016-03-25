package ua.com.fielden.web.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

public class UserControllerForTestPurposes implements IUserEx {
    public static final String USER_NAME = "user";
    public static final String PASSWORD = "password";
    private User user;

    void initUser(final EntityFactory factory) throws Exception {
        user = factory.newByKey(DummyUser.class, USER_NAME);
        final String password = new Cypher().encrypt(PASSWORD, UserAuthenticationTestCase.appWidePrivateKey);
        user.setPassword(password);
    }

    @Override
    public User findUserByKeyWithRoles(final String key) {
        return USER_NAME.equals(key) ? user : null;
    }

    @Override
    public User save(final User entity) {
        user = entity;
        return user;
    }

    @Override
    public List<? extends UserRole> findAllUserRoles() {
        return null;
    }

    @Override
    public void updateUsers(final Map<User, Set<UserRole>> userRoleMap) {
    }

    @Override
    public User findUserByIdWithRoles(final Long id) {
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> findAllUsersWithRoles() {
        return null;
    }

    @Override
    public boolean entityExists(final User entity) {
        return false;
    }

    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        return false;
    }

    @Override
    public User findByKey(final Object... keyValues) {
        return USER_NAME.equals(keyValues[0]) ? user : null;
    }

    @Override
    public Class<User> getEntityType() {
        return null;
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        return false;
    }

    @Override
    public boolean entityExists(final Long id) {
        return false;
    }

    @Override
    public User findById(final Long id) {
        return user;
    }

    @Override
    public User findByKeyAndFetch(final fetch<User> fetchModel, final Object... keyValues) {
        return findByKey(keyValues);
    }

    @Override
    public User findUser(final String username) {
        return findByKey(username);
    }

    @Override
    public String getUsername() {
        throw new UnsupportedOperationException("Getting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public User findById(final Long id, final fetch<User> fetchModel) {
        return user;
    }

    @Override
    public IPage<User> firstPage(final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<User> getPage(final int pageNo, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<User> firstPage(final QueryExecutionModel<User, ?> query, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<User> getPage(final QueryExecutionModel<User, ?> query, final int pageNo, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<User> getPage(final QueryExecutionModel<User, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User getEntity(final QueryExecutionModel<User, ?> model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int count(final EntityResultQueryModel<User> model, final Map<String, Object> paramValues) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<User> model) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<User> getAllEntities(final QueryExecutionModel<User, ?> query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<User, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<User> getFirstEntities(final QueryExecutionModel<User, ?> query, final int numberOfEntities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Integer progress() {
        return null;
    }

    @Override
    public IPage<? extends User> firstPageOfUsersWithRoles(final int capacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User findByEntityAndFetch(final fetch<User> fetchModel, final User entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IFetchProvider<User> getFetchProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User resetPasswd(User user) {
        throw new UnsupportedOperationException();
    }
}