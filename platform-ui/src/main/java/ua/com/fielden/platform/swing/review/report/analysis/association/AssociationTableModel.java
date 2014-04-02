package ua.com.fielden.platform.swing.review.report.analysis.association;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Table model that represents associations between users and user roles.
 * 
 * @author TG Team
 * 
 */
public class AssociationTableModel<T extends AbstractEntity<?>> /*extends AbstractTableModel*/{

    private static final long serialVersionUID = -6136607693042124665L;

    //    //The type of associations
    //    private final Class<T> associationType;
    //    //The prope
    //    private final String columnsNameProprety;
    //
    //    private final String rowsNameProperty;
    //
    //    private final String valuesProperty;
    //    //The list of entities those will be displayed in the rows of the first column.
    //    private final List<? extends AbstractEntity<?>> rows = new ArrayList<>();
    //
    //    //The list of entities those will be displayed as table header.
    //    private final List<? extends AbstractEntity<?>> columns = new ArrayList<>();
    //
    //    //The map between row/column header and the value.
    //    private final Map<Pair<? extends AbstractEntity<?>, ? extends AbstractEntity<?>>, Object> data = new HashMap<>();
    //
    //    /** Creates new {@link AssociationTable} with no data. */
    //    public AssociationTableModel() {
    //    }
    //
    //    /**
    //     * Creates new instance of {@link AssociationTable} and adds the specified column and row headers to the model
    //     *
    //     * @param rows
    //     * @param columns
    //     */
    //    public AssociationTableModel(final List<? extends AbstractEntity<?>> rows, final List<? extends AbstractEntity<?>> columns) {
    //	setRows(rows);
    //	setColumns(columns);
    //    }
    //
    //    @Override
    //    public int getColumnCount() {
    //	return columns.size() + 1;
    //    }
    //
    //    @Override
    //    public int getRowCount() {
    //	return rows.size();
    //    }
    //
    //    @Override
    //    public Object getValueAt(final int rowIndex, final int columnIndex) {
    //	if (columnIndex > 0) {
    //	    return data.get(new Pair<AbstractEntity<?>, AbstractEntity<?>>(rows.get(rowIndex), columns.get(columnIndex - 1)));
    //	}
    //	final AbstractEntity<?> rowEntity = rows.get(rowIndex);
    //	return rowEntity.getKey() + " (" + rowEntity.getDesc() + ")";
    //    }
    //
    //    @Override
    //    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    //	if (columnIndex > 0) {
    //	    data.put(new Pair<AbstractEntity<?>, AbstractEntity<?>>(rows.get(rowIndex), columns.get(columnIndex - 1)), value);
    //	}
    //    }
    //
    //    @Override
    //    public String getColumnName(final int column) {
    //	if (column > 0) {
    //	    return columns.get(column - 1).toString();
    //	}
    //	return "User";
    //    }
    //
    //    @Override
    //    public Class<?> getColumnClass(final int columnIndex) {
    //	return columnIndex > 0 ? Boolean.class : String.class;
    //    }
    //
    //    @Override
    //    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    //	return columnIndex > 0;
    //    }
    //
    //    /**
    //     * Returns the tool tip text for column headers.
    //     *
    //     * @param columnIndex
    //     * @return
    //     */
    //    public String getColumnHeaderToolTip(final int columnIndex) {
    //	return columnIndex > 0 ? userRoles.get(columnIndex - 1).getDesc() : "User";
    //    }
    //
    //    /**
    //     * Returns the tool tip for the table cells.
    //     *
    //     * @param rowIndex
    //     * @param columnIndex
    //     * @return
    //     */
    //    public String getCellToolTip(final int rowIndex, final int columnIndex) {
    //	return columnIndex > 0 ? data.get(rowIndex).get(columnIndex - 1).toString() : users.get(rowIndex).getDesc();
    //    }
    //
    //    /**
    //     * Adds the user to the model.
    //     *
    //     * @param newUser
    //     */
    //    public void addUser(final User newUser) {
    //	users.add(newUser);
    //	final List<Boolean> userRoleIndicator = new ArrayList<Boolean>();
    //	for (final UserRole role : userRoles) {
    //	    final boolean flag = newUser.getRoles().contains(new UserAndRoleAssociation(newUser, role));
    //	    userRoleIndicator.add(flag);
    //	}
    //	System.out.println("================whether contains itself===================");
    //	for (final UserAndRoleAssociation assoc : newUser.getRoles()) {
    //	    System.out.println("contains association " + assoc + ": " + newUser.getRoles().contains(assoc));
    //	}
    //	System.out.println("==========================================================");
    //	System.out.println("============new user roles association hash===================");
    //	for(final UserAndRoleAssociation assoc : newUser.getRoles()) {
    //	    System.out.println(assoc.toString() + " hash: " + assoc.hashCode());
    //	}
    //	System.out.println("============end of new user roles association hash===================");
    //	System.out.println("============this user roles association hash===================");
    //	for(final UserRole role : userRoles) {
    //	    final UserAndRoleAssociation assoc = new UserAndRoleAssociation(newUser, role);
    //	    System.out.println(assoc.toString() + " hash: " + assoc.hashCode());
    //	}
    //	System.out.println("============end of this user roles association hash===================");
    //
    //	data.add(userRoleIndicator);
    //	fireTableRowsInserted(data.size() - 1, data.size() - 1);
    //    }
    //
    //    /**
    //     * Adds the user role to the model
    //     *
    //     * @param newUserRole
    //     */
    //    public void addUserRole(final UserRole newUserRole) {
    //	userRoles.add(newUserRole);
    //	for (int userIndex = 0; userIndex < users.size(); userIndex++) {
    //	    final User currentUser = users.get(userIndex);
    //	    final boolean flag = currentUser.roles().contains(newUserRole);
    //	    data.get(userIndex).add(flag);
    //	}
    //	fireTableStructureChanged();
    //    }
    //
    //    /**
    //     * Set the list of {@link UserRole}s that should be displayed in the table header.
    //     * Throws {@link NullPointerException} if the given userRoles parameter is null.
    //     * @param userRoles
    //     */
    //    public void setRoles(final List<? extends UserRole> userRoles) {
    //	this.userRoles.clear();
    //	for(final List<Boolean> association : data){
    //	    association.clear();
    //	}
    //	for (final UserRole userRole : userRoles) {
    //	    addUserRole(userRole);
    //	}
    //
    //	fireTableDataChanged();
    //    }
    //
    //    /**
    //     * Clears the previous data from model and loads the specified list of users and list of user roles in to the table model
    //     *
    //     * @param users
    //     * @param userRoles
    //     */
    //    public void setUsers(final List<? extends User> users) {
    //	this.users.clear();
    //	this.data.clear();
    //	for (final User user : users) {
    //	    addUser(user);
    //	}
    //
    //	fireTableDataChanged();
    //    }
    //
    //    /**
    //     * Returns the users of the table model
    //     *
    //     * @return
    //     */
    //    public List<User> getUsers() {
    //	return users;
    //    }
    //
    //    /**
    //     * Returns the user roles of the table model
    //     *
    //     * @return
    //     */
    //    public List<UserRole> getUserRoles() {
    //	return userRoles;
    //    }
    //
    //    /**
    //     * Returns true if the specified column is the column that holds the users otherwise it returns the false
    //     *
    //     * @param column
    //     * @return
    //     */
    //    public boolean isUserColumn(final int column) {
    //	if (column > 0) {
    //	    return false;
    //	}
    //	return true;
    //    }
    //
    //    /**
    //     * Returns the list of {@link UserRole}s those are checked or unchecked for given {@link User}
    //     *
    //     * @param user
    //     * @param checked
    //     * @return
    //     */
    //    public Set<UserRole> getUserRolesFor(final User user, final boolean checked) {
    //	final Set<UserRole> checkedUserRoles = new HashSet<UserRole>();
    //	final int userIndex = users.indexOf(user);
    //	if (userIndex < 0) {
    //	    return null;
    //	}
    //	final List<Boolean> indicators = data.get(userIndex);
    //	for (int indicatorIndex = 0; indicatorIndex < indicators.size(); indicatorIndex++) {
    //	    if (indicators.get(indicatorIndex) == checked) {
    //		checkedUserRoles.add(userRoles.get(indicatorIndex));
    //	    }
    //	}
    //	return checkedUserRoles;
    //    }

}
