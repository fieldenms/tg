package ua.com.fielden.platform.web.security;

import static ua.com.fielden.platform.utils.CollectionUtil.linkedSetOf;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.SAVE;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.getPostAction;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.getPostActionError;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.SecurityMatrixInsertionPoint;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.DefaultEntityAction;

/**
 * An entity master that represents a chart for {@link VehiclePmCostSavingsChart}.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class SecurityMatrixInsertionPointMaster implements IMaster<SecurityMatrixInsertionPoint> {

    private final IRenderable renderable;
    private final DefaultEntityAction realodActionConfig;

    public SecurityMatrixInsertionPointMaster() {
        final DomElement tokenFilter = new DomElement("tg-singleline-text-editor")
                .attr("id", "tokenFilter")
                .attr("class", "filter-element")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("original-entity", "{{_originalBindingEntity}}")
                .attr("previous-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]")
                .attr("property-name", "tokenFilter")
                .attr("validation-callback", "[[doNotValidate]]")
                .attr("prop-title", "Type to filter security tokens")
                .attr("prop-desc", "Display tokens those matched entered text")
                .attr("current-state", "[[currentState]]");

        final DomElement roleFilter = new DomElement("tg-singleline-text-editor")
                .attr("id", "roleFilter")
                .attr("class", "filter-element")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("original-entity", "{{_originalBindingEntity}}")
                .attr("previous-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]")
                .attr("property-name", "roleFilter")
                .attr("validation-callback", "[[doNotValidate]]")
                .attr("prop-title", "Type to filter user roles")
                .attr("prop-desc", "Display user roles those matched entered text")
                .attr("current-state", "[[currentState]]");

        realodActionConfig = new DefaultEntityAction(SAVE.name(), getPostAction(SAVE), getPostActionError(SAVE));
        realodActionConfig.setShortDesc("Reload");
        realodActionConfig.setLongDesc("Cancels changes and reloads security matrix");
        final DomElement reloadAction = realodActionConfig.render().attr("id", "reloadAction");

        final DomElement securityMatrix = new DomElement("tg-security-matrix")
                .attr("id", "securityMatrix")
                .attr("entity", "{{_currBindingEntity}}")
                .attr("original-entity", "{{_originalBindingEntity}}")
                .attr("parent-modified-properties-holder", "[[_previousModifiedPropertiesHolder]]")
                .attr("centre-selection", "[[centreSelection]]")
                .attr("custom-event-target", "[[customEventTarget]]")
                .attr("retrieved-entities", "{{retrievedEntities}}")
                .attr("is-centre-running", "[[isCentreRunning]]")
                .attr("uuid", "[[centreUuid]]")
                .attr("lock", "[[lock]]")
                .add(tokenFilter, roleFilter, reloadAction);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace(IMPORTS, createImports(linkedSetOf("components/tg-security-matrix", "editors/tg-singleline-text-editor")))
                .replace("@entity_type", SecurityMatrixInsertionPoint.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", securityMatrix.toString())
                .replace("//generatedPrimaryActions", "")
                .replace("//@ready-callback", readyCallback())
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private String readyCallback() {
        return realodActionConfig.code().toString() + "\n"
                + "self.classList.add('layout');\n"
                + "self.classList.add('vertical');\n"
                + "self._masterDom().classList.add('layout');\n"
                + "self._masterDom().classList.add('vertical');\n"
                + "//Implemented can leave to check whether user has saved changes or not.\n"
                + "self.canLeave = function () {\n"
                + "    return this.$.securityMatrix.canLeave();\n"
                + "}.bind(self);\n"
                + "//Need for security marix editors binding.\n"
                + "self._isNecessaryForConversion = function (propertyName) { \n"
                + "    return ['tokenFilter', 'roleFilter'].indexOf(propertyName) >= 0; \n"
                + "}; \n"
                + "self.$.tokenFilter._onInput = function () {\n"
                + "    // clear token filter timer if it is in progress.\n"
                + "    this._cancelTokenFilterTimer();\n"
                + "    this._filterTokenTimer = this.async(this._filterToken, 500);\n"
                + "}.bind(self);\n"
                + "self._cancelTokenFilterTimer = function () {\n"
                + "    if (this._filterTokenTimer) {\n"
                + "        this.cancelAsync(this._filterTokenTimer);\n"
                + "        this._filterTokenTimer = null;\n"
                + "    }\n"
                + "}.bind(self);\n"
                + "self._filterToken = function () {\n"
                + "    this.$.securityMatrix.filterTokens(this.$.tokenFilter._editingValue);\n"
                + "}.bind(self);\n"
                + "self.$.roleFilter._onInput = function () {\n"
                + "    // clear role filter timer if it is in progress.\n"
                + "    this._cancelRoleFilterTimer();\n"
                + "    this._filterRoleTimer = this.async(this._filterRoles, 500);\n"
                + "}.bind(self);\n"
                + "self._cancelRoleFilterTimer = function () {\n"
                + "    if (this._filterRoleTimer) {\n"
                + "        this.cancelAsync(this._filterRoleTimer);\n"
                + "        this._filterRoleTimer = null;\n"
                + "    }\n"
                + "}.bind(self);\n"
                + "self._filterRoles = function () {\n"
                + "    this.$.securityMatrix.filterRoles(this.$.roleFilter._editingValue);\n"
                + "}.bind(self);\n"
                + "//Locks/Unlocks tg-security-matrix lock layer during insertion point activation.\n"
                + "self.disableViewForDescendants = function () {\n"
                + "    Polymer.TgBehaviors.TgEntityBinderBehavior.disableViewForDescendants.call(this);\n"
                + "    self.lock = true;\n"
                + "    self.showDataLoadingPromt();\n"
                + "};\n"
                + "self.enableViewForDescendants = function () {\n"
                + "    Polymer.TgBehaviors.TgEntityBinderBehavior.enableViewForDescendants.call(this);\n"
                + "    self.lock = false;"
                + "    self.showDataLoadedPromt();\n"
                + "};\n"
                + "self.showDataLoadingPromt = function () {\n"
                + "    this._toastGreeting().text = 'Loading security matrix...';\n"
                + "    this._toastGreeting().hasMore = false;\n"
                + "    this._toastGreeting().showProgress = true;\n"
                + "    this._toastGreeting().msgHeading = 'Info';\n"
                + "    this._toastGreeting().isCritical = false;\n"
                + "    this._toastGreeting().show();\n"
                + "};\n"
                + "self.showDataLoadedPromt = function () {\n"
                + "    this._toastGreeting().text = 'Loading completed successfully';\n"
                + "    this._toastGreeting().hasMore = false;\n"
                + "    this._toastGreeting().showProgress = false;\n"
                + "    this._toastGreeting().msgHeading = 'Info';\n"
                + "    this._toastGreeting().isCritical = false;\n"
                + "    this._toastGreeting().show();\n"
                + "};\n";
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<SecurityMatrixInsertionPoint, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

}
