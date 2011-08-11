package ua.com.fielden.web.security;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.cypher.Cypher;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

public class UserControllerForTestPurposes implements IUserController {
    public static final String USER_NAME = "user";
    public static final String PASSWORD = "password";
    private User user;

    void initUser(final EntityFactory factory) throws Exception {
	user =  factory.newByKey(DummyUser.class, USER_NAME);
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
    public void updateUser(final User users, final List<UserRole> newUserRoles) {
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
    public User findById(final Long id, final fetch<User> fetchModel) {
	return user;
    }

    @Override
    public User findByKey(final Object... keyValues) {
	return USER_NAME.equals(keyValues[0]) ? user : null;
    }

    @Override
    public IPage<User> firstPage(final int pageCapacity) {
	return null;
    }

    @Override
    public IPage<User> firstPage(final IQueryOrderedModel<User> query, final int pageCapacity) {
	return null;
    }

    @Override
    public User getEntity(final IQueryOrderedModel<User> model) {
	return null;
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
    public IPage<User> getPage(final int pageNo, final int pageCapacity) {
	return null;
    }

    @Override
    public IPage<User> getPage(final IQueryOrderedModel<User> query, final int pageNo, final int pageCapacity) {
	return null;
    }

    public IPage<User> getPage(final IQueryOrderedModel<User> query, final int pageNo, final int pageCount, final int pageCapacity) {
	return getPage(query, pageNo, 0, pageCapacity);
    }

    @Override
    public int count(final IQueryOrderedModel<User> model) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public byte[] export(final IQueryOrderedModel<User> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return null;
    }

    @Override
    public List<User> getEntities(final IQueryOrderedModel<User> query) {
	return null;
    }

    @Override
    public void delete(final User user) {
    }


    @Override
    public void delete(final IQueryOrderedModel<User> model) {
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
	return false;
    }

    @Override
    public IPage<User> firstPage(final IQueryOrderedModel<User> model, final IQueryOrderedModel<EntityAggregates> summaryModel, final int pageCapacity) {
	return null;
    }

    @Override
    public boolean entityExists(final Long id) {
	return false;
    }

    @Override
    public byte[] export(final IQueryOrderedModel<User> query, final fetch<User> fetchModel, final String[] propertyNames, final String[] propertyTitles) throws IOException {
	return null;
    }

    @Override
    public User findById(final Long id) {
	return null;
    }

    @Override
    public User findByKeyAndFetch(final fetch<User> fetchModel, final Object... keyValues) {
	return findByKey(keyValues);
    }

    @Override
    public IPage<User> firstPage(final IQueryOrderedModel<User> query, final fetch<User> fetchModel, final int pageCapacity) {
	return null;
    }

    @Override
    public IPage<User> firstPage(final IQueryOrderedModel<User> model, final fetch<User> fetchModel, final IQueryOrderedModel<EntityAggregates> summaryModel, final int pageCapacity) {
	return null;
    }

    @Override
    public List<User> getEntities(final IQueryOrderedModel<User> query, final fetch<User> fetchModel) {
	return null;
    }

    @Override
    public User getEntity(final IQueryOrderedModel<User> model, final fetch<User> fetchModel) {
	return null;
    }

    @Override
    public IPage<User> getPage(final IQueryOrderedModel<User> query, final fetch<User> fetchModel, final int pageNo, final int pageCapacity) {
	return null;
    }

    @Override
    public IPage<User> getPage(final IQueryOrderedModel<User> query, final fetch<User> fetchModel, final int pageNo, final int pageCount, final int pageCapacity) {
	return null;
    }

    @Override
    public User findUser(final String username) {
	return findByKey(username);
    }

    @Override
    public void setUsername(final String username) {
	throw new UnsupportedOperationException("Setting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }

    @Override
    public String getUsername() {
	throw new UnsupportedOperationException("Getting username is not required at the client side, and this fact most likely points to a programming mistake.");
    }
}
