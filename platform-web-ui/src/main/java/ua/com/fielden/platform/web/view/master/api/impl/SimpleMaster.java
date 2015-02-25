package ua.com.fielden.platform.web.view.master.api.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityAction;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfigWithDone;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHtmlText;

public class SimpleMaster<T extends AbstractEntity<?>> implements IPropertySelector<T>, ILayoutConfig, ILayoutConfigWithDone {

    private final List<WidgetSelector<T>> widgets = new ArrayList<>();
    private final List<EntityActionConfig<T>> entityActions = new ArrayList<>();
    private final FlexLayout layout = new FlexLayout();

    public final Class<T> entityType;

    public SimpleMaster(final Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public IEntityActionConfig0<T> addAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity) {
        final EntityActionConfig<T> entityAction = new EntityActionConfig<>(new EntityAction(name, functionalEntity), this);
        entityActions.add(entityAction);
        return entityAction;
    }

    @Override
    public IWidgetSelector<T> addProp(final String propName) {
        final WidgetSelector<T> widget = new WidgetSelector<>(this, propName);
        widgets.add(widget);
        return widget;
    }

    @Override
    public IDividerConfig<T> addDivider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IHtmlText<T> addHtmlLabel(final String htmlText) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILayoutConfigWithDone setLayoutFor(final Device device, final Orientation orientation, final String flexString) {
        layout.whenMedia(device, orientation).set(flexString);
        return this;
    }

    @Override
    public IRenderable done() {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("polymer/polymer/polymer");
        importPaths.add("master/tg-entity-master");

        final StringBuilder propertyActionsStr = new StringBuilder();
        final DomElement editorContainer = layout.render();

        importPaths.add(layout.importPath());
        widgets.forEach(widget -> {
            importPaths.add(widget.widget().importPath());
            editorContainer.add(widget.widget().render());
            if (widget.widget().action() != null) {
                propertyActionsStr.append(widget.widget().action().code().toString());
            }
        });

        final StringBuilder entityActionsStr = new StringBuilder();
        final DomContainer actionContainer = new DomContainer();
        entityActions.forEach(action -> {
            importPaths.add(action.action().importPath());
            actionContainer.add(action.action().render());
            entityActionsStr.append(action.action().code().toString());
        });

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html").
                replace("<!--@imports-->", createImports(importPaths)).
                replace("@entity_type", entityType.getSimpleName()).
                replace("<!--@editors-->", editorContainer.toString()).
                replace("<!--@actions-->", actionContainer.toString()).
                replace("//@entityActions", entityActionsStr.toString()).
                replace("//@propertyActions", propertyActionsStr.toString());
        return new IRenderable() {

            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    /**
     * Creates import statements from a list of paths.
     *
     * @param importPaths
     * @return
     */
    private String createImports(final LinkedHashSet<String> importPaths) {
        final StringBuilder sb = new StringBuilder();
        importPaths.forEach(path -> {
            sb.append("<link rel=\"import\" href=\"/resources/" + path + ".html\">\n");
        });
        return sb.toString();
    }

    public static void main(final String[] args) {
        final SimpleMasterConfig sm = new SimpleMasterConfig();
        final IRenderable masterRenderable = sm.forEntity(TgPersistentEntityWithProperties.class)
                // PROPERTY EDITORS
                .addProp("stringProp").asSinglelineText()
                .withAction("#validateDesc", TgPersistentEntityWithProperties.class)
                .preAction(new IPreAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionSuccess(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionError(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).enabledWhen(EnabledState.ANY).icon("trending-up")
                .also()

                .addProp("stringProp").asMultilineText()
                .also()
                .addProp("dateProp").asDateTimePicker().skipValidation()
                .also()
                .addProp("booleanProp").asCheckbox().skipValidation()
                .also()
                .addProp("bigDecimalProp").asDecimal().skipValidation()
                .also()
                .addProp("integerProp").asSpinner().skipValidation()
                .also()

                // ENTITY CUSTOM ACTIONS
                .addAction("#export", TgPersistentEntityWithProperties.class)
                .preAction(new IPreAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionSuccess(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).postActionError(new IPostAction() {
                    @Override
                    public JsCode build() {
                        return new JsCode("");
                    }
                }).enabledWhen(EnabledState.VIEW).shortDesc("Export")
                .setLayoutFor(Device.DESKTOP, null, "[[]]")
                .setLayoutFor(Device.TABLET, null, "[[]]")
                .setLayoutFor(Device.TABLET, null, "[[]]")
                .done();
        System.out.println(masterRenderable.render().toString());
    }
}
