package ua.com.fielden.platform.swing.treetable;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenController;
import ua.com.fielden.platform.security.provider.SecurityTokenNode;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Model for {@link SecurityTokenViewer}. That model has load and save actions, {@link ISecurityTokenController} - logic for retrieving data and also tree table model for data
 * presentation.
 * 
 * @author TG Team
 * 
 */
public class SecurityTokenViewerModel {

    private final ISecurityTokenController controller;

    private final SecurityTreeTableModel treeTableModel;

    private Action loadAction;
    private Action saveAction;

    private final SecurityTokenProvider tokenProvider;

    /**
     * Holds the data that is currently loaded.
     */
    private SecurityData currentData = null;

    /**
     * Creates new instance of {@link SecurityTokenViewerModel} for the given {@link ISecurityTokenController}, initiates the tree table model.
     * 
     * @param controller
     * @throws Exception
     */
    public SecurityTokenViewerModel(final ISecurityTokenController controller, final SecurityTokenProvider tokenProvider) throws Exception {
        this.controller = controller;
        this.treeTableModel = new SecurityTreeTableModel();
        this.tokenProvider = tokenProvider;
    }

    /**
     * Returns the action that save the security token - user role associations
     * 
     * @param pane
     * @return
     */
    private Action createSaveAction(final BlockingIndefiniteProgressLayer pane) {
        final Action action = new BlockingLayerCommand<Void>("Save", pane) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                final boolean result = super.preAction();
                setMessage("Saving...");
                return result;
            }

            @Override
            protected Void action(final ActionEvent e) throws Exception {
                final Map<Class<? extends ISecurityToken>, Set<UserRole>> curModel = treeTableModel.getModelData();
                curModel.remove(ISecurityToken.class);
                final Map<Class<? extends ISecurityToken>, Set<UserRole>> oldModel = currentData.getSecurityTokens();
                final Map<Class<? extends ISecurityToken>, Set<UserRole>> changedModel = new HashMap<>();
                for (final Class<? extends ISecurityToken> clazz : curModel.keySet()) {
                    final Set<UserRole> curUserRoles = curModel.get(clazz);
                    final Set<UserRole> oldUserRoles = oldModel.get(clazz);
                    if (!EntityUtils.safeEquals(curUserRoles, oldUserRoles)) {
                        changedModel.put(clazz, curUserRoles);
                    }
                }
                controller.saveSecurityToken(changedModel);
                return null;
            }

        };
        action.putValue(Action.SHORT_DESCRIPTION, "Saves changes.");
        action.setEnabled(true);
        return action;
    }

    /**
     * Returns the actions that loads data with controller and then pass it to the tree table model
     * 
     * @param pane
     * @return
     */
    private Action createLoadAction(final BlockingIndefiniteProgressLayer pane) {
        final Action action = new BlockingLayerCommand<SecurityData>("Load", pane) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                final boolean result = super.preAction();
                setMessage("Loading...");
                return result;
            }

            @Override
            protected SecurityData action(final ActionEvent e) throws Exception {
                final List<SecurityTokenNode> tokenList = new ArrayList<SecurityTokenNode>(tokenProvider.getTopLevelSecurityTokenNodes());
                final List<Class<? extends ISecurityToken>> securityTokenClasses = new ArrayList<>();
                for (int tokenInd = 0; tokenInd < tokenList.size(); tokenInd++) {
                    final SecurityTokenNode tokenNode = tokenList.get(tokenInd);
                    securityTokenClasses.add(tokenNode.getToken());
                    tokenList.addAll(tokenNode.getSubTokenNodes());
                }
                System.out.println(securityTokenClasses.size());
                final Map<Class<? extends ISecurityToken>, Set<UserRole>> securityTokens = controller.findAllAssociations();
                final List<UserRole> roles = controller.findUserRoles();
                return new SecurityData(roles, securityTokens, tokenProvider.getTopLevelSecurityTokenNodes());
            }

            @Override
            protected void postAction(final SecurityData data) {
                currentData = data;
                treeTableModel.loadData(data.getTokenNodes(), data.getSecurityTokens(), data.getRoles());
                super.postAction(data);
            }

        };
        action.putValue(Action.SHORT_DESCRIPTION, "Loads data.");
        action.setEnabled(true);
        return action;
    }

    /**
     * see {@link #createLoadAction(BlockingIndefiniteProgressLayer)}
     * 
     * @param pane
     * @return
     */
    public Action getLoadAction(final BlockingIndefiniteProgressLayer pane) {
        if (loadAction == null) {
            loadAction = createLoadAction(pane);
        }
        return loadAction;
    }

    /**
     * see {@link #createSaveAction(BlockingIndefiniteProgressLayer)}
     * 
     * @param pane
     * @return
     */
    public Action getSaveAction(final BlockingIndefiniteProgressLayer pane) {
        if (saveAction == null) {
            saveAction = createSaveAction(pane);
        }
        return saveAction;
    }

    /**
     * Returns the {@link ISecurityTokenController}
     * 
     * @return
     */
    public ISecurityTokenController getController() {
        return controller;
    }

    /**
     * Returns the tree table model
     * 
     * @return
     */
    public SecurityTreeTableModel getTreeTableModel() {
        return treeTableModel;
    }

    /**
     * Inner class that holds data loaded with {@link ISecurityTokenController}.
     * 
     * @author TG Team
     * 
     */
    private static class SecurityData {

        private final List<UserRole> roles;
        private final Map<Class<? extends ISecurityToken>, Set<UserRole>> securityTokens;
        private final Set<SecurityTokenNode> tokenNodes;

        /**
         * Instantiates the {@link SecurityData} class with list of available user roles, map that holds association between security token and it's user roles, and the hierarchy
         * of security token nodes.
         * 
         * @param roles
         * @param securityTokens
         * @param tokenNodes
         */
        public SecurityData(final List<UserRole> roles, final Map<Class<? extends ISecurityToken>, Set<UserRole>> securityTokens, final Set<SecurityTokenNode> tokenNodes) {

            this.roles = roles;
            this.securityTokens = securityTokens;
            this.tokenNodes = tokenNodes;
        }

        /**
         * Returns the list of available user roles
         * 
         * @return
         */
        public List<UserRole> getRoles() {
            return roles;
        }

        /**
         * Returns the map of associations between the security token and the list of user roles
         * 
         * @return
         */
        public Map<Class<? extends ISecurityToken>, Set<UserRole>> getSecurityTokens() {
            return securityTokens;
        }

        /**
         * Returns the hierarchy of the security token nodes
         * 
         * @return
         */
        public Set<SecurityTokenNode> getTokenNodes() {
            return tokenNodes;
        }
    }
}
