package ua.com.fielden.platform.web.view.master.api.impl;

import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.setRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.UI_ROLE;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IExecutable;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig7;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.DefaultEntityAction;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.ILayoutConfigWithDone;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;
import ua.com.fielden.platform.web.view.master.api.helpers.IWidgetSelector;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHtmlTextConfig;

public class SimpleMasterBuilder<T extends AbstractEntity<?>> implements ISimpleMasterBuilder<T>, IPropertySelector<T>, ILayoutConfig<T>, ILayoutConfigWithDone<T>, IEntityActionConfig7<T> {

    private final List<WidgetSelector<T>> widgets = new ArrayList<>();
    private final List<Object> entityActions = new ArrayList<>();
    
    private final FlexLayout layout = new FlexLayout();

    private final Map<String, Class<? extends IValueMatcherWithContext<T, ?>>> valueMatcherForProps = new HashMap<>();

    private Class<T> entityType;
    private boolean saveOnActivation = false;
    


    @Override
    public IPropertySelector<T> forEntity(final Class<T> type) {
        this.entityType = type;
        return this;
    }

    @Override
    public IPropertySelector<T> forEntityWithSaveOnActivate(final Class<T> type) {
        this.entityType = type;
        this.saveOnActivation = true;
        return this;
    }

    @Override
    public IEntityActionConfig7<T> addAction(final ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig action) {
        entityActions.add(setRole(action, UI_ROLE.BUTTON));
        return this;
    }
    
    @Override
    public IEntityActionConfig0<T> addAction(final MasterActions masterAction) {
        final EntityActionConfig<T> entityAction = new EntityActionConfig<>(new DefaultEntityAction(masterAction.name(), getPostAction(masterAction), getPostActionError(masterAction)), this);
        entityActions.add(entityAction);
        return entityAction;
    }

    private String getPostActionError(final MasterActions masterAction) {
        if (MasterActions.REFRESH == masterAction) {
            return "_postRetrievedDefaultError";
        } else if (MasterActions.VALIDATE == masterAction) {
            return "_postValidatedDefaultError";
        } else if (MasterActions.SAVE == masterAction) {
            return "_postSavedDefaultError";
        } else if (MasterActions.EDIT == masterAction) {
            return "_actions.EDIT.postActionError"; // TODO maybe, should be deleted (no ajax request sends)?
        } else if (MasterActions.VIEW == masterAction) {
            return "_actions.VIEW.postActionError"; // TODO maybe, should be deleted (no ajax request sends)?
        } else {
            throw new UnsupportedOperationException(masterAction.toString());
        }
    }

    private String getPostAction(final MasterActions masterAction) {
        if (MasterActions.REFRESH == masterAction) {
            return "_postRetrievedDefault";
        } else if (MasterActions.VALIDATE == masterAction) {
            return "_postValidatedDefault";
        } else if (MasterActions.SAVE == masterAction) {
            return "_postSavedDefault";
        } else if (MasterActions.EDIT == masterAction) {
            return "_actions.EDIT.postAction";
        } else if (MasterActions.VIEW == masterAction) {
            return "_actions.VIEW.postAction";
        } else {
            throw new UnsupportedOperationException(masterAction.toString());
        }
    }

    @Override
    public IWidgetSelector<T> addProp(final String propName) {
        final WidgetSelector<T> widget = new WidgetSelector<>(this, propName, new WithMatcherCallback());
        widgets.add(widget);
        return widget;
    }

    /**
     * A callback for recording custom matcher that are specified for property autocompletion.
     *
     */
    public class WithMatcherCallback {
        public void assign(final String propName, final Class<? extends IValueMatcherWithContext<T, ?>> matcher) {
            valueMatcherForProps.put(propName, matcher);
        }
    }

    @Override
    public IDividerConfig<T> addDivider() {
        throw new UnsupportedOperationException("Divider is not yet supported.");
    }

    @Override
    public IHtmlTextConfig<T> addHtmlLabel(final String htmlText) {
        throw new UnsupportedOperationException("HTML label is not yet supported.");
    }

    @Override
    public ILayoutConfigWithDone<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Device and orientation (optional) are required for specifying the layout.");
        }
        layout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    @Override
    public IMaster<T> done() {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("polymer/polymer/polymer");

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

        // entity actions should be type matched for rendering due to inclusion of both "standard" actions such as SAVE or CANCLE as well as the functional actions 
        final StringBuilder entityActionsStr = new StringBuilder();
        final String prefix = ",\n";
        final int prefixLength = prefix.length();
        final StringBuilder primaryActionObjects = new StringBuilder();
        int funcActionSeq = 0;
        for (final Object action: entityActions) {
            if (action instanceof ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig) {
                final ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig<?> config = (ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig<?>) action;
                importPaths.add(config.action().importPath());
                if (config.action() instanceof IRenderable) {
                    editorContainer.add(((IRenderable) config.action()).render());
                }
                if (config.action() instanceof IExecutable) {
                    entityActionsStr.append(((IExecutable) config.action()).code().toString());
                }
            } else {
                final ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig config = (ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig) action;
                if (!config.isNoAction()) {
                    final FunctionalActionElement el = new FunctionalActionElement(config, funcActionSeq++, FunctionalActionKind.PRIMARY_RESULT_SET);
                    importPaths.add(el.importPath());
                    editorContainer.add(el.render().clazz("primary-action"));
                    primaryActionObjects.append(prefix + el.createActionObject());
                }
            }

        }
        
        
        final String primaryActionObjectsString = primaryActionObjects.toString();
        
        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", createImports(importPaths))
                .replace("@entity_type", entityType.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", editorContainer.toString()) // TODO should contain prop actions
                .replace("//@ready-callback", layout.code().toString() + "\n" + entityActionsStr.toString() + "\n" + propertyActionsStr.toString())
                .replace("//generatedPrimaryActions", primaryActionObjectsString.length() > prefixLength ? primaryActionObjectsString.substring(prefixLength)
                        : primaryActionObjectsString)
                
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", saveOnActivation + "");

        final IRenderable representation = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };

        return new SimpleMaster(representation, valueMatcherForProps);
    }

    /**
     * Creates import statements from a list of paths.
     *
     * @param importPaths
     * @return
     */
    public static String createImports(final LinkedHashSet<String> importPaths) {
        final StringBuilder sb = new StringBuilder();
        importPaths.forEach(path -> {
            sb.append("<link rel='import' href='/resources/" + path + ".html'>\n");
        });
        return sb.toString();
    }

    public Class<T> getEntityType() {
        return entityType;
    }


    private class SimpleMaster implements IMaster<T> {

        private final IRenderable renderableRepresentation;
        private final Map<String, Class<? extends IValueMatcherWithContext<T, ?>>> valueMatcherForProps;

        public SimpleMaster(
                final IRenderable renderableRepresentation,
                final Map<String, Class<? extends IValueMatcherWithContext<T, ?>>> valueMatcherForProps) {
            this.renderableRepresentation = renderableRepresentation;
            this.valueMatcherForProps = valueMatcherForProps;
        }

        @Override
        public IRenderable render() {
            return renderableRepresentation;
        }

        @Override
        public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
            return Optional.ofNullable(valueMatcherForProps.get(propName));
        }

    }

    public static void main(final String[] args) {
        final SimpleMasterBuilder<TgPersistentEntityWithProperties> sm = new SimpleMasterBuilder<>();
        final IMaster<TgPersistentEntityWithProperties> smConfig = sm.forEntity(TgPersistentEntityWithProperties.class)
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
                .addAction(MasterActions.REFRESH)
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
        System.out.println(smConfig.render().toString());
    }

}
