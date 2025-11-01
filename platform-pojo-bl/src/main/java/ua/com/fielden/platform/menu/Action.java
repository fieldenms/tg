package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/**
 * Entity that represents the action configuration that will be sent with main menu configuration.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Element Name", desc = "The action's element name")
@DescTitle(value = "Short Description", desc = "Action's short description")
public class Action extends AbstractEntity<String> {

    @IsProperty
    @Title("Ui Role")
    private String uiRole;

    @IsProperty
    @Title("Long Description")
    private String longDesc;

    @IsProperty
    @Title("Shortcut")
    private String shortcut;

    @IsProperty
    @Title("Icon")
    private String icon;

    @IsProperty
    @Title("Icon Style")
    private String iconStyle;

    @IsProperty
    @Title("Refresh Parent Centre After Save?")
    private boolean refreshParentCentreAfterSave;

    @IsProperty
    @Title("Component URI")
    private String componentUri;

    @IsProperty
    @Title("Is Dynamic Action?")
    private boolean dynamicAction;

    @IsProperty
    @Title("Action #")
    private Integer numberOfAction;

    @IsProperty
    @Title("Action Kind")
    private String actionKind;

    @IsProperty
    @Title("Element Alias")
    private String elementAlias;

    @IsProperty
    @Title("Chosen Property")
    private String chosenProperty;

    @IsProperty
    @Title("Require Selection Criteria?")
    private String requireSelectionCriteria;

    @IsProperty
    @Title("Require Selected Entities")
    private String requireSelectedEntities;

    @IsProperty
    @Title("Require Master Entity?")
    private String requireMasterEntity;

    @IsProperty
    @Title("Pre-action")
    private String preAction;

    @IsProperty
    @Title("Post-action Success")
    private String postActionSuccess;

    @IsProperty
    @Title("Post-action Error")
    private String postActionError;

    @IsProperty
    @Title("Attributes")
    private String attrs;

    @IsProperty
    @Title("Module Name")
    private String moduleName;

    @Observable
    public Action setModuleName(final String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Observable
    public Action setAttrs(final String attrs) {
        this.attrs = attrs;
        return this;
    }

    public String getAttrs() {
        return attrs;
    }

    @Observable
    public Action setPostActionError(final String postActionError) {
        this.postActionError = postActionError;
        return this;
    }

    public String getPostActionError() {
        return postActionError;
    }

    @Observable
    public Action setPostActionSuccess(final String postActionSuccess) {
        this.postActionSuccess = postActionSuccess;
        return this;
    }

    public String getPostActionSuccess() {
        return postActionSuccess;
    }

    @Observable
    public Action setPreAction(final String preAction) {
        this.preAction = preAction;
        return this;
    }

    public String getPreAction() {
        return preAction;
    }

    @Observable
    public Action setRequireMasterEntity(final String requireMasterEntity) {
        this.requireMasterEntity = requireMasterEntity;
        return this;
    }

    public String getRequireMasterEntity() {
        return requireMasterEntity;
    }

    @Observable
    public Action setRequireSelectedEntities(final String requireSelectedEntities) {
        this.requireSelectedEntities = requireSelectedEntities;
        return this;
    }

    public String getRequireSelectedEntities() {
        return requireSelectedEntities;
    }

    @Observable
    public Action setRequireSelectionCriteria(final String requireSelectionCriteria) {
        this.requireSelectionCriteria = requireSelectionCriteria;
        return this;
    }

    public String getRequireSelectionCriteria() {
        return requireSelectionCriteria;
    }

    @Observable
    public Action setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
        return this;
    }

    public String getChosenProperty() {
        return chosenProperty;
    }

    @Observable
    public Action setElementAlias(final String elementAlias) {
        this.elementAlias = elementAlias;
        return this;
    }

    public String getElementAlias() {
        return elementAlias;
    }

    @Observable
    public Action setActionKind(final String actionKind) {
        this.actionKind = actionKind;
        return this;
    }

    public String getActionKind() {
        return actionKind;
    }

    @Observable
    public Action setNumberOfAction(final Integer numberOfAction) {
        this.numberOfAction = numberOfAction;
        return this;
    }

    public Integer getNumberOfAction() {
        return numberOfAction;
    }

    @Observable
    public Action setDynamicAction(final boolean dynamicAction) {
        this.dynamicAction = dynamicAction;
        return this;
    }

    public boolean isDynamicAction() {
        return dynamicAction;
    }

    @Observable
    public Action setComponentUri(final String componentUri) {
        this.componentUri = componentUri;
        return this;
    }

    public String getComponentUri() {
        return componentUri;
    }

    @Observable
    public Action setRefreshParentCentreAfterSave(final boolean refreshParentCentreAfterSave) {
        this.refreshParentCentreAfterSave = refreshParentCentreAfterSave;
        return this;
    }

    public boolean isRefreshParentCentreAfterSave() {
        return refreshParentCentreAfterSave;
    }

    @Observable
    public Action setIconStyle(final String iconStyle) {
        this.iconStyle = iconStyle;
        return this;
    }

    public String getIconStyle() {
        return iconStyle;
    }

    @Observable
    public Action setIcon(final String icon) {
        this.icon = icon;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    @Observable
    public Action setShortcut(final String shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    public String getShortcut() {
        return shortcut;
    }

    @Observable
    public Action setLongDesc(final String longDesc) {
        this.longDesc = longDesc;
        return this;
    }

    public String getLongDesc() {
        return longDesc;
    }

    @Observable
    public Action setUiRole(final String uiRole) {
        this.uiRole = uiRole;
        return this;
    }

    public String getUiRole() {
        return uiRole;
    }

    @Override
    @Observable
    public Action setDesc(String desc) {
        return super.setDesc(desc);
    }

}
