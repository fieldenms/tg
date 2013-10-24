package ua.com.fielden.platform.swing.usertable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Table model that represents associations between users and user roles.
 *
 * @author TG Team
 *
 */
public class UserTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -6136607693042124665L;

    // the list of user roles (row data)
    private final List<User> users = new ArrayList<User>();

    // list of user roles (column data)
    private final List<UserRole> userRoles = new ArrayList<UserRole>();

    // data it self (if the value is True then user role belongs to user otherwise user role doesn't belong.
    private final List<List<Boolean>> data = new ArrayList<List<Boolean>>();

    /** Creates new UserTableModel with no data. */
    public UserTableModel() {
    }

    /**
     * Creates new instance of UserTableModel and adds the specified user and user roles to the model
     *
     * @param users
     * @param userRoles
     */
    public UserTableModel(final List<User> users, final List<UserRole> userRoles) {
	loadData(users, userRoles);
    }

    @Override
    public int getColumnCount() {
	return userRoles.size() + 1;
    }

    @Override
    public int getRowCount() {
	return users.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
	if (columnIndex > 0) {
	    return data.get(rowIndex).get(columnIndex - 1);
	}
	final User user = users.get(rowIndex);
	return user.getKey() + " (" + user.getDesc() + ")";
    }

    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
	if (columnIndex > 0) {
	    data.get(rowIndex).set(columnIndex - 1, (Boolean) value);
	}
    }

    @Override
    public String getColumnName(final int column) {
	if (column > 0) {
	    return userRoles.get(column - 1).getKey();
	}
	return "User";
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
	return columnIndex > 0 ? Boolean.class : String.class;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
	return columnIndex > 0;
    }

    /**
     * Returns the tool tip text for column headers.
     *
     * @param columnIndex
     * @return
     */
    public String getColumnHeaderToolTip(final int columnIndex) {
	return columnIndex > 0 ? userRoles.get(columnIndex - 1).getDesc() : "User";
    }

    /**
     * Returns the tool tip for the table cells.
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    public String getCellToolTip(final int rowIndex, final int columnIndex) {
	return columnIndex > 0 ? data.get(rowIndex).get(columnIndex - 1).toString() : users.get(rowIndex).getDesc();
    }

    /**
     * Adds the user to the model.
     *
     * @param newUser
     */
    public void addUser(final User newUser) {
	users.add(newUser);
	final List<Boolean> userRoleIndicator = new ArrayList<Boolean>(userRoles.size());
	for (int userRoleIndex = 0; userRoleIndex < userRoles.size(); userRoleIndex++) {
	    final boolean flag = newUser.getRoles().contains(new UserAndRoleAssociation(newUser, userRoles.get(userRoleIndex)));
	    userRoleIndicator.set(userRoleIndex, flag);
	}
	data.add(userRoleIndicator);
	fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    /**
     * Adds the user role to the model
     *
     * @param newUserRole
     */
    public void addUserRole(final UserRole newUserRole) {
	userRoles.add(newUserRole);
	for (int userIndex = 0; userIndex < users.size(); userIndex++) {
	    final User currentUser = users.get(userIndex);
	    final boolean flag = currentUser.roles().contains(newUserRole);
	    data.get(userIndex).add(flag);
	}
	fireTableStructureChanged();
    }

    /**
     * Clears the previous data from model and loads the specified list of users and list of user roles in to the table model
     *
     * @param users
     * @param userRoles
     */
    public void loadData(final List<? extends User> users, final List<? extends UserRole> userRoles) {
	this.users.clear();
	this.userRoles.clear();
	this.data.clear();
	for (int userIndex = 0; userIndex < users.size(); userIndex++) {
	    data.add(new ArrayList<Boolean>());
	}
	for (final User user : users) {
	    addUser(user);
	}
	for (final UserRole userRole : userRoles) {
	    addUserRole(userRole);
	}
    }

    /**
     * Returns the users of the table model
     *
     * @return
     */
    public List<User> getUsers() {
	return users;
    }

    /**
     * Returns the user roles of the table model
     *
     * @return
     */
    public List<UserRole> getUserRoles() {
	return userRoles;
    }

    /**
     * Returns true if the specified column is the column that holds the users otherwise it returns the false
     *
     * @param column
     * @return
     */
    public boolean isUserColumn(final int column) {
	if (column > 0) {
	    return false;
	}
	return true;
    }

    /**
     * Returns the list of {@link UserRole}s those are checked or unchecked for given {@link User}
     *
     * @param user
     * @param checked
     * @return
     */
    public Set<UserRole> getUserRolesFor(final User user, final boolean checked) {
	final Set<UserRole> checkedUserRoles = new HashSet<UserRole>();
	final int userIndex = users.indexOf(user);
	if (userIndex < 0) {
	    return null;
	}
	final List<Boolean> indicators = data.get(userIndex);
	for (int indicatorIndex = 0; indicatorIndex < indicators.size(); indicatorIndex++) {
	    if (indicators.get(indicatorIndex) == checked) {
		checkedUserRoles.add(userRoles.get(indicatorIndex));
	    }
	}
	return checkedUserRoles;
    }

}
