package ua.com.fielden.platform.web.view.master.api.with_centre.impl;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ICustomisableCanLeave;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;

import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;

/**
 * An entity master that represents a single Entity Centre.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class MasterWithCentre<T extends AbstractEntity<?>> implements IMaster<T> {
    public final EntityCentre<?> embeddedCentre;
    private final IRenderable renderable;

    MasterWithCentre(final Class<T> entityType, final boolean saveOnActivate, final EntityCentre<?> entityCentre, final Optional<JsCode> customCode, final Optional<JsCode> customCodeOnAttach, final Optional<JsCode> customImports) {
        embeddedCentre = entityCentre;
        final StringBuilder attrs = new StringBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        //// attributes should always be added with comma and space at the end, i.e. ", " ////
        //// this suffix is used to remove the last comma, which prevents JSON conversion ////
        //////////////////////////////////////////////////////////////////////////////////////
        attrs.append("{");
        attrs.append("\"embedded\": true, ");
        if (entityCentre.shouldEnforcePostSaveRefresh()) {
            attrs.append("\"enforcePostSaveRefresh\": true, ");
        }
        attrs.append(format("eventSourceClass: \"%s\",", entityCentre.eventSourceClass().map(clazz -> clazz.getName()).orElse("")));

        // let's make sure that uuid is defined from the embedded centre, which is required
        // for proper communication of the centre with related actions
        attrs.append("\"uuid\": this.uuid, ");
        attrs.append("}");

        final String attributes = attrs.toString().replace(", }", " }");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, "import '/resources/element_loader/tg-element-loader.js';\n" + customImports.map(ci -> ci.toString()).orElse(""))
                .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                .replace("<!--@tg-entity-master-content-->",
                        format(""
                        + "<tg-element-loader id='loader' context='[[_createContextHolderForEmbeddedViews]]' context-property='getMasterEntity' "
                        + "    import='/centre_ui/%s' "
                        + "    element-name='tg-%s-centre'>"
                        + "</tg-element-loader>",
                        entityCentre.getMenuItemType().getName(), entityCentre.getMenuItemType().getSimpleName()))
                .replace("//@ready-callback",
                        "self.masterWithCentre = true;\n" +
                        (ICustomisableCanLeave.class.isAssignableFrom(entityType) ? "" : "self.classList.remove('canLeave');\n") +
                        "self._focusEmbededView = function () {\n" +
                        "    if (this.wasLoaded() && this.$.loader.loadedElement.focusView) {\n" +
                        "        this.$.loader.loadedElement.focusView();\n" +
                        "    }\n" +
                        "}.bind(self);\n" +
                        "self._hasEmbededView = function () {\n" +
                        "    return true;\n" +
                        "}.bind(self);\n"+
                        "self.wasLoaded = function () {\n" +
                        "    if (this.$.loader.loadedElement) {\n" +
                        "        return this.$.loader.loadedElement.wasLoaded();\n" +
                        "    }\n" +
                        "    return false;\n" +
                        "}.bind(self);\n" +
                        "self._focusNextEmbededView = function (e) {\n" +
                        "    if (this.wasLoaded() && this.$.loader.loadedElement.focusNextView) {\n" +
                        "        this.$.loader.loadedElement.focusNextView(e);\n" +
                        "    }\n" +
                        "}.bind(self);\n" +
                        "self._focusPreviousEmbededView = function (e) {\n" +
                        "    if (this.wasLoaded() && this.$.loader.loadedElement.focusPreviousView) {\n" +
                        "        this.$.loader.loadedElement.focusPreviousView(e);\n" +
                        "    }\n" +
                        "}.bind(self);\n")
                .replace("//@attached-callback",
                        format(""
                        +"this.canLeave = function () {\n"
                        + "    return this.customCanLeave();\n"
                        + "}.bind(this);\n"
                        + "self.$.loader.attrs = %s;\n"
                        + "self.registerCentreRefreshRedirector();\n",
                        attributes))
                .replace("//@master-is-ready-custom-code", customCode.map(code -> code.toString()).orElse(""))
                .replace("//@master-has-been-attached-custom-code", customCodeOnAttach.map(code -> code.toString()).orElse(""))
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", saveOnActivate + "");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };

    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting of action configuration is not supported.");
    }
}
