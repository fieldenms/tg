package ua.com.fielden.platform.example.entities.daos;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociationDao;
import ua.com.fielden.platform.dao.IUserRoleDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.example.entities.IPersonDao;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(User.class)
public class UserController extends CommonEntityDao<User> implements IUserController {

    private final IPersonDao personDao;
    private final IUserRoleDao userRoleDao;
    private final IUserAndRoleAssociationDao userAssociationDao;

    @Inject
    public UserController(final IPersonDao personDao, final IUserRoleDao userRoleDao, final IUserAndRoleAssociationDao userAssociationDao,  final IFilter filter) {
	super(filter);
	this.personDao = personDao;
	this.userRoleDao = userRoleDao;
	this.userAssociationDao = userAssociationDao;
    }

    @Override
    @SessionRequired
    public List<? extends UserRole> findAllUserRoles() {
	return userRoleDao.findAll();
    }

    @Override
    @SessionRequired
    public List<User> findAllUsers() {
	return new ArrayList<User>(personDao.retrieveAllPersonsWithRoles());
    }

    @Override
    public void updateUser(final User user, final List<UserRole> checkedRoles) {
	// remove list at the first stage of the algorithm contains the associations of the given user.
	// At the last stage of the algorithm that list contains only associations those must be removed from the data base
	final List<UserAndRoleAssociation> removeList = new ArrayList<UserAndRoleAssociation>(user.getRoles());
	// contains the list of associations those must be saved
	final List<UserAndRoleAssociation> saveList = new ArrayList<UserAndRoleAssociation>();

	for (int userRoleIndex = 0; userRoleIndex < checkedRoles.size(); userRoleIndex++) {
	    final UserAndRoleAssociation roleAssociation = user.getEntityFactory().newByKey(UserAndRoleAssociation.class, user, checkedRoles.get(userRoleIndex));
	    if (!removeList.contains(roleAssociation)) {
		saveList.add(roleAssociation);
	    } else {
		removeList.remove(roleAssociation);
	    }
	}

	// first remove user/role associations
	userAssociationDao.removeAssociation(removeList);
	// then insert new user/role associations
	saveAssociation(saveList);

    }

    private void saveAssociation(final List<UserAndRoleAssociation> associations) {
	for (final UserAndRoleAssociation association : associations) {
	    userAssociationDao.save(association);
	}
    }

    @Override
    public User findUserByIdWithRoles(final Long id) {
	return personDao.findPersonByIdWithRoles(id);
    }

    @Override
    public User findUserByKeyWithRoles(final String key) {
	return personDao.findPersonByKeyWithUserRoles(key);
    }

    @Override
    public List<User> findAllUsersWithRoles() {
	return new ArrayList<User>(personDao.retrieveAllPersonsWithRoles());
    }

    @Override
    public User findUser(final String username) {
	return findUserByKeyWithRoles(username);
    }
}
