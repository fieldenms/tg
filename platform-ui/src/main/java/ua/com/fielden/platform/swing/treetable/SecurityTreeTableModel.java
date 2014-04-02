package ua.com.fielden.platform.swing.treetable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.user.UserRole;

import com.jidesoft.grid.TreeTableModel;

/**
 * {@link TreeTableModel} that builds the tree for the given set of {@link SecurityTokenNode}
 * 
 * @author TG Team
 * 
 */
public class SecurityTreeTableModel extends DynamicTreeTableModel {

    // the list of all available user roles
    private final List<UserRole> availableUserRoles = new ArrayList<UserRole>();

    // the list of all security tokens
    private final List<Class<? extends ISecurityToken>> securityTokens = new ArrayList<Class<? extends ISecurityToken>>();

    /**
     * Creates new empty {@link SecurityTreeTableModel}
     */
    public SecurityTreeTableModel() {
        super();
    }

    /**
     * Creates new instance of {@link SecurityTreeTableModel} for the given set of {@link SecurityTokenNode}s and {@link ISecurityTokenController}
     * 
     * @param securityTokens
     * @param tokenControler
     */
    public SecurityTreeTableModel(final Set<SecurityTokenNode> securityTokens, final Map<Class<? extends ISecurityToken>, Set<UserRole>> mapOfAssociation, final List<UserRole> availableUserRoles) {
        loadData(securityTokens, mapOfAssociation, availableUserRoles);
    }

    /**
     * Loads data to the model
     * 
     * @param securityTokens
     * @param mapOfAssociation
     * @param availableUserRoles
     */
    public void loadData(final Set<SecurityTokenNode> securityTokens, final Map<Class<? extends ISecurityToken>, Set<UserRole>> mapOfAssociation, final List<UserRole> availableUserRoles) {

        this.availableUserRoles.clear();
        this.securityTokens.clear();
        this.availableUserRoles.addAll(availableUserRoles);

        final SecurityTreeTableNode root = new SecurityTreeTableNode(new ArrayList<Boolean>(), ISecurityToken.class, new HashSet<UserRole>());
        root.setLongDesc("root");
        root.setShortDesc("root");
        root.setUserObject("root");
        setRoot(root);

        buildTreeModel(getRoot(), securityTokens, mapOfAssociation);
    }

    // builds the tree of the security tokens
    private void buildTreeModel(final AbstractMutableTreeTableNode root, final Set<SecurityTokenNode> tokens, final Map<Class<? extends ISecurityToken>, Set<UserRole>> mapOfAssociation) {
        for (final SecurityTokenNode tokenNode : tokens) {
            Set<UserRole> userRoles = mapOfAssociation.get(tokenNode.getToken());
            if (userRoles == null) {
                userRoles = new HashSet<>();
            }

            final List<Boolean> userRolesIndicators = new ArrayList<Boolean>();
            for (final UserRole userRole : getAvailableUserRoles()) {
                userRolesIndicators.add(userRoles.contains(userRole));
            }

            final SecurityTreeTableNode treeTableNode = new SecurityTreeTableNode(userRolesIndicators, tokenNode.getToken(), userRoles);
            treeTableNode.setLongDesc(tokenNode.getLongDesc());
            treeTableNode.setShortDesc(tokenNode.getShortDesc());
            treeTableNode.setUserObject(tokenNode.getShortDesc());
            securityTokens.add(tokenNode.getToken());
            insertNodeInto(treeTableNode, root, root.getChildCount());
            buildTreeModel(treeTableNode, tokenNode.getSubTokenNodes(), mapOfAssociation);
        }
    }

    /**
     * Returns a map with data to be used for removal and insertion of user role/ security token associations. See {@link #saveTreeModel(SecurityTreeTableNode)} fro more details.
     * 
     * @return
     */
    public Map<Class<? extends ISecurityToken>, Set<UserRole>> getModelData() {
        return saveTreeModel(getRoot());
    }

    /**
     * Traces the tree model and fills the resultant map with data to be used for removal and insertion of user role/security token associations. The key of this map contains a
     * class representing security token. Its values are roles that should be associated with a corresponding token.
     * 
     * @param treeTableNode
     * @param securityTokens
     */
    private Map<Class<? extends ISecurityToken>, Set<UserRole>> saveTreeModel(final SecurityTreeTableNode treeTableNode) {
        final Map<Class<? extends ISecurityToken>, Set<UserRole>> securityTokens = new Hashtable<Class<? extends ISecurityToken>, Set<UserRole>>();

        final Class<? extends ISecurityToken> securityToken = treeTableNode.getSecurityToken();

        // find out which roles are selected for the current security token
        final Set<UserRole> selectedUserRoles = new HashSet<UserRole>();
        for (int userRoleIndex = 0; userRoleIndex < treeTableNode.getData().size(); userRoleIndex++) {
            if (treeTableNode.getData().get(userRoleIndex)) {
                selectedUserRoles.add(availableUserRoles.get(userRoleIndex));
            }
        }
        // update user roles associated with this security token to reflect the actual selected/unselected state
        treeTableNode.setUserRoles(selectedUserRoles);

        // put devised data into the resultant map
        securityTokens.put(securityToken, selectedUserRoles);
        // move to child nodes, which might also have suffered changes
        for (int childIndex = 0; childIndex < treeTableNode.getChildCount(); childIndex++) {
            securityTokens.putAll(saveTreeModel((SecurityTreeTableNode) treeTableNode.getChildAt(childIndex)));
        }
        // return the resultant map containing all possible changes to role/token associations
        return securityTokens;
    }

    @Override
    public int getColumnCount() {
        return getAvailableUserRoles().size() + 1;
    }

    /**
     * Returns the all available user roles for this {@link SecurityTreeTableModel}
     * 
     * @return
     */
    public List<? extends UserRole> getAvailableUserRoles() {
        return availableUserRoles;
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        if (column > 0 && !(column < 0)) {
            return Boolean.class;
        } else if (column == 0) {
            return String.class;
        } else {
            throw new IllegalArgumentException("the index of the column is out of the bounds");
        }

    }

    @Override
    public String getColumnName(final int column) {
        if (column > 0 && !(column < 0)) {
            return getAvailableUserRoles().get(column - 1).getKey();
        } else if (column == 0) {
            return "Security Token";
        } else {
            throw new IllegalArgumentException("the index of the column is out of the bounds");
        }

    }

    /**
     * returns the description of the column specified with column index. The description is used as the tool tip test for table header.
     * 
     * @param column
     * @return
     */
    public String getColumnDesc(final int column) {
        if (column > 0 && !(column < 0)) {
            return getAvailableUserRoles().get(column - 1).getDesc();
        } else if (column == 0) {
            return "Security Token";
        } else {
            throw new IllegalArgumentException("the index of the column is out of the bounds");
        }
    }

    @Override
    public SecurityTreeTableNode getRoot() {
        return (SecurityTreeTableNode) super.getRoot();
    }

}
