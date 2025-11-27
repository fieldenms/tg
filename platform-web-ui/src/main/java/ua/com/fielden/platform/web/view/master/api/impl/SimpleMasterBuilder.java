package ua.com.fielden.platform.web.view.master.api.impl;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.UI_ROLE;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
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
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig5;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfigWithoutNew;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.DefaultEntityAction;
import ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.*;
import ua.com.fielden.platform.web.view.master.api.helpers.impl.WidgetSelector;
import ua.com.fielden.platform.web.view.master.api.widgets.IDividerConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.IHtmlTextConfig;
import ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.AbstractEntityAutocompletionWidget;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;
import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;
import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.setRole;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;

public class SimpleMasterBuilder<T extends AbstractEntity<?>> implements ISimpleMasterBuilder<T>, IPropertySelector<T>, ILayoutConfig<T>, ILayoutConfigWithDimensionsAndDone<T>, IEntityActionConfig5<T>, IActionBarLayoutConfig1<T> {

    private static final String ERR_WIDGET_IS_ALREADY_PRESENT = "A widget with property [%s] is already present in the entity master for [%s].";

    private final List<WidgetSelector<T>> widgets = new ArrayList<>();
    private final List<Object> entityActions = new ArrayList<>();

    private final FlexLayout layout = new FlexLayout("editors");
    private final FlexLayout actionBarLayout = new FlexLayout("actions");

    private final Map<String, Class<? extends IValueMatcherWithContext<T, ?>>> valueMatcherForProps = new HashMap<>();

    private Class<T> entityType;
    private Optional<PrefDim> prefDim = empty();
    private boolean saveOnActivation = false;

    private Optional<JsCode> customCode = empty();
    private Optional<JsCode> customCodeOnAttach = empty();
    private Optional<JsCode> customImports = empty();

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
    public IEntityActionConfig5<T> addAction(final ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig action) {
        entityActions.add(setRole(action, UI_ROLE.BUTTON));
        return this;
    }

    @Override
    public IEntityActionConfig0<T> addAction(final MasterActions masterAction) {
        final DefaultEntityAction<T> defaultEntityAction = new DefaultEntityAction<T>(masterAction.name(), this.entityType, getPostAction(masterAction), getPostActionError(masterAction));
        final Optional<String> shortcut = getShortcut(masterAction);
        if (shortcut.isPresent()) {
            defaultEntityAction.setShortcut(shortcut.get()); // default value of shortcut if present
        }
        final Optional<String> focusingCallback = getFocusingCallback(masterAction);
        if (focusingCallback.isPresent()) {
            defaultEntityAction.setFocusingCallback(focusingCallback.get()); // default value of focusingCallback if present
        }
        final EntityActionConfig<T> entityAction = new EntityActionConfig<T>(defaultEntityAction, this);
        entityActions.add(entityAction);
        return entityAction;
    }

    @Override
    public IEntityActionConfigWithoutNew<T> addSaveAction() {
        return (IEntityActionConfigWithoutNew<T>) addAction(MasterActions.SAVE);
    }

    @Override
    public IEntityActionConfigWithoutNew<T> addCancelAction() {
        return (IEntityActionConfigWithoutNew<T>) addAction(MasterActions.REFRESH);
    }

    @Override
    public IComplete<T> withDimensions(final PrefDim prefDim) {
        this.prefDim = Optional.ofNullable(prefDim);
        return this;
    }

    public static Optional<String> getFocusingCallback(final MasterActions masterAction) {
        if (MasterActions.SAVE == masterAction) {
            return Optional.of("focusViewBound"); // focuses enabled input or other subcomponent on SAVE completion either from actual tap or shortcut activation; see 'focusViewBound' in 'tg-entity-master-behavior'
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getShortcut(final MasterActions masterAction) {
        if (MasterActions.REFRESH == masterAction) {
            return Optional.of("ctrl+x meta+x");
        } else if (MasterActions.SAVE == masterAction) {
            return Optional.of("ctrl+s meta+s");
        } else if (MasterActions.VALIDATE == masterAction || MasterActions.EDIT == masterAction || MasterActions.VIEW == masterAction) {
            return Optional.empty();
        } else {
            throw new UnsupportedOperationException(masterAction.toString());
        }
    }

    public static String getPostActionError(final MasterActions masterAction) {
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

    public static String getPostAction(final MasterActions masterAction) {
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
    public IWidgetSelector<T> addProp(final CharSequence propName) {
        widgets.stream().filter(widget -> widget.propertyName.contentEquals(propName)).findFirst().ifPresent(widget -> {
            throw new EntityMasterConfigurationException(format(ERR_WIDGET_IS_ALREADY_PRESENT, propName, this.entityType.getSimpleName()));
        });
        final WidgetSelector<T> widget = new WidgetSelector<>(this, propName.toString(), new WithMatcherCallback());
        widgets.add(widget);
        return widget;
    }

    /// A callback for recording custom matcher that are specified for property autocompletion.
    ///
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
    public ILayoutConfigWithDimensionsAndDone<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Device and orientation (optional) are required for specifying the layout.");
        }
        layout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    @Override
    public IMaster<T> done() {
        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        // importPaths.add("polymer/polymer/polymer"); // FIXME check and delete if all good -- this is not really needed due to tg-entity-master-template-behavior dependencies

        final AtomicInteger funcActionSeq = new AtomicInteger(0); // used for both entity and property level functional actions
        final String prefix = ",\n";
        final int prefixLength = prefix.length();
        final StringBuilder primaryActionObjects = new StringBuilder();

        final StringBuilder propertyActionsStr = new StringBuilder();
        final DomElement editorContainer = layout.render().attr("slot", "property-editors").attr("context", "[[_currEntity]]");
        importPaths.add(layout.importPath());
        final StringBuilder shortcuts = new StringBuilder();
        for (final WidgetSelector<T> widget : widgets) {
            importPaths.add(widget.widget().importPath());

            if (!widget.widget().action().isPresent()) {
                editorContainer.add(widget.widget().render());
            } else {
                final DomElement widgetElement = widget.widget().render();
                final EntityMultiActionConfig config = widget.widget().action().get();
                config.actions().forEach(actionConfig -> {
                    final FunctionalActionElement el = FunctionalActionElement.newPropertyActionForMaster(actionConfig, funcActionSeq.getAndIncrement(), widget.propertyName);
                    if (actionConfig.shortcut.isPresent()) {
                        shortcuts.append(actionConfig.shortcut.get() + " ");
                    }
                    importPaths.add(el.importPath());
                    widgetElement.add(el.render().attr("slot", "property-action").clazz("property-action-icon"));
                    primaryActionObjects.append(prefix + el.createActionObject());
                });
                editorContainer.add(widgetElement);
            }
        }

        // entity actions should be type matched for rendering due to inclusion of both "standard" actions such as SAVE or CANCLE as well as the functional actions
        final DomElement actionContainer = actionBarLayout.render().attr("slot", "action-bar");
        final StringBuilder entityActionsStr = new StringBuilder();
        for (final Object action: entityActions) {
            if (action instanceof ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig) {
                final ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig<?> config = (ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig<?>) action;
                importPaths.add(config.action().importPath());
                if (config.action().shortcut() != null) {
                    shortcuts.append(config.action().shortcut() + " ");
                }
                if (config.action() instanceof IRenderable) {
                    actionContainer.add(((IRenderable) config.action()).render());
                }
                if (config.action() instanceof IExecutable) {
                    entityActionsStr.append(((IExecutable) config.action()).code().toString());
                }
            } else {
                final ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig config = (ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig) action;
                final FunctionalActionElement el = FunctionalActionElement.newEntityActionForMaster(config, funcActionSeq.getAndIncrement());
                if (config.shortcut.isPresent()) {
                    shortcuts.append(config.shortcut.get() + " ");
                }
                importPaths.add(el.importPath());
                actionContainer.add(el.render().clazz("primary-action"));
                primaryActionObjects.append(prefix + el.createActionObject());
            }
        }

        final StringBuilder prefDimBuilder = new StringBuilder();

        if (prefDim.isPresent()) {
            final PrefDim dims = prefDim.get();
            prefDimBuilder.append(format("{'width': function() {return %s}, 'height': function() {return %s}, 'widthUnit': '%s', 'heightUnit': '%s'}", dims.width, dims.height, dims.widthUnit.value, dims.heightUnit.value));
        } else {
            prefDimBuilder.append("null");
        }

        final DomElement elementContainer = new DomContainer().add(editorContainer, actionContainer);
        final String primaryActionObjectsString = primaryActionObjects.toString();
        final String dimensionsString = prefDimBuilder.toString();

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, createImports(importPaths) + customImports.map(ci -> ci.toString()).orElse(""))
                .replace(ENTITY_TYPE, flattenedNameOf(entityType))
                .replace("<!--@tg-entity-master-content-->", elementContainer.toString()) // TODO should contain prop actions
                .replace("//@ready-callback",
                        layout.code().toString() + "\n"
                      + actionBarLayout.code().toString() + "\n"
                      + entityActionsStr.toString() + "\n"
                      + propertyActionsStr.toString() + "\n"
                      + genReadyCallback())
                .replace("//@attached-callback", genAttachedCallback())
                .replace("//generatedPrimaryActions", primaryActionObjectsString.length() > prefixLength ? primaryActionObjectsString.substring(prefixLength)
                        : primaryActionObjectsString)
                .replace("//@master-is-ready-custom-code", customCode.map(code -> code.toString()).orElse(""))
                .replace("//@master-has-been-attached-custom-code", customCodeOnAttach.map(code -> code.toString()).orElse(""))
                .replace("@SHORTCUTS", shortcuts)
                .replace("@prefDim", dimensionsString)
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", saveOnActivation + "");

        final IRenderable representation = () -> new InnerTextElement(entityMasterStr);
        return new SimpleMaster(representation, valueMatcherForProps);
    }

    private String genReadyCallback() {
        return "self.wasLoaded = function () {\n"
                + "    return !!this._viewLoaded;\n"
                + "}.bind(self);\n"
                + "//Init event listener that indicates whether content was loaded\n"
                + "if (!self._hasEmbededView()) {\n"
                + "    const _entityMasterContentLoaded = function (e) {\n"
                + "        this._viewLoaded = true;\n"
                + "        this.fire('tg-view-loaded', this);\n"
                + "    }.bind(self);\n"
                + "    self.addEventListener('tg-entity-master-content-loaded', _entityMasterContentLoaded);\n"
                + "}\n";
    }

    private String genAttachedCallback() {
        return "self.registerCentreRefreshRedirector();\n";
    }

    /// Creates import statements from a list of paths.
    ///
    public static String createImports(final LinkedHashSet<String> importPaths) {
        final StringBuilder sb = new StringBuilder();
        importPaths.forEach(path -> {
            sb.append("import '/resources/" + path + ".js';\n");
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
        public Stream<ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig> streamActionConfigs() {
            return Stream.concat(widgets.stream()
                                         .flatMap(w -> w.widget().action().stream())
                                         .map(EntityMultiActionConfig::actions)
                                         .flatMap(List::stream),
                                 entityActions.stream()
                                         .mapMulti(typeFilter(ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig.class)));
        }

        @Override
        public Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherTypeFor(final String propName) {
            return Optional.ofNullable(valueMatcherForProps.get(propName));
        }

        @Override
        public Set<String> additionalAutocompleterPropertiesFor(final String propertyName) {
            for (final WidgetSelector<T> widgetSelector: widgets) {
                if (widgetSelector.propertyName != null && widgetSelector.propertyName.equals(propertyName)) {
                    if (widgetSelector.widget() instanceof AbstractEntityAutocompletionWidget) {
                        final AbstractEntityAutocompletionWidget widget = (AbstractEntityAutocompletionWidget) widgetSelector.widget();
                        return widget.additionalProps().keySet();
                    } else {
                        return setOf();
                    }
                }
            }
            return setOf();
        }

        @Override
        public <V extends AbstractEntity<?>> Optional<Class<V>> getAutocompleterAssociatedType(final Class<T> entityType, final String propertyName) {
            return IMaster.super.<V>getAutocompleterAssociatedType(entityType, propertyName)
                    .or(() -> widgets.stream()
                            .filter(w -> w.propertyName != null && w.propertyName.equals(propertyName) && w.widget() instanceof AbstractEntityAutocompletionWidget)
                            .findFirst()
                            .map(w -> (AbstractEntityAutocompletionWidget) w.widget())
                            .map(ww -> (Class<V>) ww.propType));
        }

        /// Returns action configuration for concrete action kind and its number in that kind's space.
        ///
        /// This method implementation is tightly coupled with SimpleMasterBuilder.done() method, where the numbering of actions during their generation appears.
        ///
        @Override
        public  ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
            if (FunctionalActionKind.PRIMARY_RESULT_SET == actionKind) {
                int funcActionSeq = actionNumber; // used for both entity and property level functional actions
                for (final WidgetSelector<T> widget : widgets) {
                    if (widget.widget().action().isPresent()) {
                        final EntityMultiActionConfig config = widget.widget().action().get();
                        final List<ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig> actions = config.actions();
                        if (funcActionSeq < actions.size()) {
                            return actions.get(funcActionSeq);
                        } else {
                            funcActionSeq -= actions.size();
                        }
                    }
                }
                // entity actions should be type matched for rendering due to inclusion of both "standard" actions such as SAVE or CANCLE as well as the functional actions
                for (final Object action: entityActions) {
                    if (!(action instanceof ua.com.fielden.platform.web.view.master.api.actions.entity.impl.EntityActionConfig)) {
                        final ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig config = (ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig) action;
                        if (funcActionSeq == 0) {
                            return config;
                        }
                        funcActionSeq--;
                    }
                }
                throw new IllegalStateException("No master action has been found.");
            } // TODO implement other types
            throw new UnsupportedOperationException(actionKind + " is not supported yet.");
        }

        @Override
        public Map<String, Class<? extends IEntityMultiActionSelector>> propertyActionSelectors() {
            return widgets.stream().filter(widget -> widget.widget().action().isPresent()).map(widget -> {
                return t2(widget.widget().propertyName(), widget.widget().action().get().actionSelectorClass());
            }).collect(toMap(tt -> tt._1, tt -> tt._2));
        }
    }

    @Override
    public IActionBarLayoutConfig1<T> setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        if (device == null || orientation == null) {
            throw new IllegalArgumentException("Device and orientation (optional) are required for specifying the layout.");
        }
        actionBarLayout.whenMedia(device, orientation.isPresent() ? orientation.get() : null).set(flexString);
        return this;
    }

    public List<Object> getEntityActions() {
        return entityActions;
    }

    /// Injects custom JavaScript code into respective master implementation. This code will be executed after
    /// master component creation.
    ///
    public SimpleMasterBuilder<T> injectCustomCode(final JsCode customCode) {
        this.customCode = Optional.of(customCode);
        return this;
    }

    /// Injects custom JavaScript code into respective master implementation. This code will be executed every time
    /// master component is attached to client application's DOM.
    ///
    public SimpleMasterBuilder<T> injectCustomCodeOnAttach(final JsCode customCode) {
        this.customCodeOnAttach = Optional.of(customCode);
        return this;
    }

    /// Injects custom JavaScript imports into centre implementation.
    ///
    public SimpleMasterBuilder<T> injectCustomImports(final JsCode customImports) {
        this.customImports = of(customImports);
        return this;
    }

}
