package ua.com.fielden.platform.web.view.master.dashboard;

import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.menu.module.impl.WebView;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class DashboardMaster<T extends AbstractEntity<?>> implements IMaster<T> {

    private final IRenderable renderable;

    public DashboardMaster(final Class<T> entityType, final List<EntityCentre<?>> centres) {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("components/tg-dashboard");

        final DomElement dashboardDom = new DomElement("tg-dashboard")
                .attr("id", "dashboard")
                .attr("views", "[[centres]]");

        final StringBuilder prefDimBuilder = new StringBuilder();
        prefDimBuilder.append("{'width': function() {return '100%'}, 'height': function() {return '100%'}, 'widthUnit': '', 'heightUnit': ''}");

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(importPaths)
                        + "\nimport { TgEntityBinderBehavior } from '/resources/binding/tg-entity-binder-behavior.js';\n")
                .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                .replace("<!--@tg-entity-master-content-->", dashboardDom.toString())
                .replace("//@ready-callback", readyCallback(centres))
                .replace("@prefDim", prefDimBuilder.toString())
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private String readyCallback(final List<EntityCentre<?>> centres) {
        final String centresAttrs = StringUtils.join(centres.stream().map(centre -> new WebView(centre).code().toString()).collect(Collectors.toList()), ",\n");
        return "self.classList.add('layout');\n"
                + "self.classList.add('vertical');\n"
                + "self.canLeave = function () {\n"
                + "    return null;\n"
                + "}.bind(self);\n"
                + "self.centres = [" + centresAttrs + "];\n";
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
