package ua.com.fielden.platform.swing.treetable;

import java.util.Collections;
import java.util.List;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * TreeTableNode that holds the information about the {@link ISecurityToken} and also provides the API that allows one to represent the relationship between security tokens
 * 
 * @author TG Team
 * 
 */
public class SecurityTreeTableNode extends AbstractMutableTreeTableNode {

    // list of boolean values that represent the user roles associated with security token
    private final List<Boolean> data;

    // security token and it's user roles
    private final Class<? extends ISecurityToken> securityToken;
    private final List<UserRole> userRoles;

    // description needed for visual representation of the security token
    private String shortDesc;
    private String longDesc;

    /**
     * Creates new {@link SecurityTreeTableNode} for the specified list of user roles and security token
     * 
     * @param data
     * @param securityToken
     */
    public SecurityTreeTableNode(final List<Boolean> data, final Class<? extends ISecurityToken> securityToken, final List<UserRole> userRoles) {
	this(null, true, data, securityToken, userRoles);
    }

    /**
     * Constructs the {@link SecurityTreeTableNode} with specified user object, security token and user roles.
     * 
     * @param userObject
     * @param allowsChildren
     *            - indicates whether this tree table node may have children or not
     * @param userRoles1
     * @param securityToken
     */
    public SecurityTreeTableNode(final Object userObject, final boolean allowsChildren, final List<Boolean> data, final Class<? extends ISecurityToken> securityToken, final List<UserRole> userRoles) {
	super(userObject, allowsChildren);
	this.data = data;
	this.securityToken = securityToken;
	this.userRoles = userRoles;
    }

    @Override
    public int getColumnCount() {
	return data.size() + 1;
    }

    @Override
    public Object getValueAt(final int column) {
	if (column > 0 && !(column < 0)) {
	    return data.get(column - 1);
	} else if (column == 0) {
	    return getShortDesc().toString();
	} else {
	    throw new IllegalArgumentException("The index of the column is out of the bounds");
	}

    }

    @Override
    public void setValueAt(final Object value, final int column) {
	if (column > 0 && !(column < 0)) {
	    data.set(column - 1, (Boolean) value);
	} else if (column == 0) {
	    setShortDesc((String) value);
	} else {
	    throw new IllegalArgumentException("The index of the column is out of the bounds");
	}
    }

    @Override
    public boolean isEditable(final int column) {
	if (column > 0 && !(column < 0)) {
	    return true;
	} else if (column == 0) {
	    return false;
	} else {
	    throw new IllegalArgumentException("The index of the column is out of the bounds");
	}
    }

    /**
     * Returns the user roles for this security token as unmodifiable list.
     * 
     * @return
     */
    public List<Boolean> getData() {
	return Collections.unmodifiableList(data);
    }

    /**
     * Returns the represented security token.
     * 
     * @return
     */
    public Class<? extends ISecurityToken> getSecurityToken() {
	return securityToken;
    }

    /**
     * Returns the long description of the represented security token. Needed for tool tip text).
     * 
     * @return
     */
    public String getLongDesc() {
	return longDesc;
    }

    /**
     * Returns the short description of the security token. (Needed for visual representation of the security token instance)
     * 
     * @return
     */
    public String getShortDesc() {
	return shortDesc;
    }

    /**
     * Specifies a short description of the security token. See {@link #getShortDesc()} for more information.
     * 
     * @param shortDesc
     */
    public void setShortDesc(final String shortDesc) {
	this.shortDesc = shortDesc;
    }

    /**
     * Specifies a long description of the security token. See {@link #getLongDesc()} for more information.
     * 
     * @param longDesc
     */
    public void setLongDesc(final String longDesc) {
	this.longDesc = longDesc;
    }

    /**
     * Returns unmodifiable list of user roles associated with this {@link SecurityTreeTableNode}.
     * <p>
     * IMPORTANT: This list is updated only upon saving of user role/security token association.
     * 
     * @return
     */
    public List<UserRole> getUserRoles() {
	return Collections.unmodifiableList(userRoles);
    }

    /**
     * Updates a list of user role/security token association based on the provided data.
     * 
     * @param updatedList
     */
    public void setUserRoles(final List<UserRole> updatedList) {
	userRoles.clear();
	userRoles.addAll(updatedList);
    }
}
