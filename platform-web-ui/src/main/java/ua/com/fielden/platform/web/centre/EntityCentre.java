package ua.com.fielden.platform.web.centre;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.ICentre;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.BooleanCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.BooleanSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DateCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DateSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DecimalCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DecimalSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.EntityCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.EntitySingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.IntegerCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.IntegerSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.StringSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import com.google.inject.Injector;

/**
 * Represents the entity centre.
 *
 * @author TG Team
 *
 */
public class EntityCentre<T extends AbstractEntity<?>> implements ICentre<T> {
    private final Logger logger = Logger.getLogger(getClass());
    private final Class<? extends MiWithConfigurationSupport<?>> menuItemType;
    private final String name;
    private final EntityCentreConfig<T> dslDefaultConfig;
    private final Injector injector;
    private final Class<T> entityType;
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final ICompanionObjectFinder coFinder;
    private final ICentreDomainTreeManagerAndEnhancer defaultCentre;

    /**
     * Creates new {@link EntityCentre} instance for the menu item type and with specified name.
     *
     * @param miType
     *            - the menu item type for which this entity centre is to be created.
     * @param name
     *            - the name for this entity centre.
     * @param dslDefaultConfig
     *            -- default configuration taken from Centre DSL
     */
    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final EntityCentreConfig<T> dslDefaultConfig, final Injector injector, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        this.menuItemType = miType;
        this.name = name;
        this.dslDefaultConfig = dslDefaultConfig;

        this.injector = injector;
        this.miType = miType;
        this.entityType = (Class<T>) CentreUtils.getEntityType(miType);
        this.coFinder = this.injector.getInstance(ICompanionObjectFinder.class);
        defaultCentre = createDefaultCentre(dslDefaultConfig, postCentreCreated);
    }

    /**
     * Generates default centre from DSL config and postCentreCreated callback.
     *
     * @param dslDefaultConfig
     * @param postCentreCreated
     * @return
     */
    private static <T extends AbstractEntity<?>> ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final EntityCentreConfig<T> dslDefaultConfig, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        final ICentreDomainTreeManagerAndEnhancer createdCentre = null;
        // TODO implement generation from dslDefaultConfig
        // TODO implement generation from dslDefaultConfig
        // TODO implement generation from dslDefaultConfig
        // TODO implement generation from dslDefaultConfig
        // TODO implement generation from dslDefaultConfig
        return postCentreCreated == null ? createdCentre : postCentreCreated.apply(createdCentre);
    }

    /**
     * Returns the menu item type for this {@link EntityCentre} instance.
     *
     * @return
     */
    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
        return this.menuItemType;
    }

    /**
     * Returns the entity centre name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public IRenderable build() {
        return createRenderableRepresentation();
    }

    /**
     * Returns default centre manager that was formed using DSL configuration and postCentreCreated hook.
     *
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer getDefaultCentre() {
        return defaultCentre;
    }

    private IRenderable createRenderableRepresentation() {
        final ICentreDomainTreeManagerAndEnhancer centre = CentreUtils.getFreshCentre(getUserSpecificGdtm(), this.menuItemType);
        logger.debug("Building renderable for cdtmae:" + centre);

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("polymer/polymer/polymer");
        importPaths.add("master/tg-entity-master");

        final FlexLayout layout = this.dslDefaultConfig.getSelectionCriteriaLayout();

        final DomElement editorContainer = layout.render();

        importPaths.add(layout.importPath());

        final Class<?> root = this.entityType;

        final List<AbstractCriterionWidget> criteriaWidgets = new ArrayList<>();
        final Class<?> managedType = centre.getEnhancer().getManagedType(root);
        for (final String critProp : centre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(critProp)) {
                final boolean isEntityItself = "".equals(critProp); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, critProp);

                final AbstractCriterionWidget criterionWidget;
                if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType, critProp)) { // two editors are required
                    if (EntityUtils.isBoolean(propertyType)) {
                        criterionWidget = new BooleanCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isDate(propertyType)) {
                        criterionWidget = new DateCriterionWidget(root, managedType, critProp);
                    } else if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new IntegerCriterionWidget(root, managedType, critProp);
                    } else if (BigDecimal.class.isAssignableFrom(propertyType)) { // TODO do not forget about Money later (after Money widget will be available)
                        criterionWidget = new DecimalCriterionWidget(root, managedType, critProp);
                    } else {
                        throw new UnsupportedOperationException(String.format("The double-editor type [%s] is currently unsupported.", propertyType));
                    }
                } else {
                    if (EntityUtils.isBoolean(propertyType)) {
                        criterionWidget = new BooleanSingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isDate(propertyType)) {
                        criterionWidget = new DateSingleCriterionWidget(root, managedType, critProp);
                    } else if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new IntegerSingleCriterionWidget(root, managedType, critProp);
                    } else if (BigDecimal.class.isAssignableFrom(propertyType)) { // TODO do not forget about Money later (after Money widget will be available)
                        criterionWidget = new DecimalSingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isString(propertyType)) {
                        criterionWidget = new StringSingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isEntityType(propertyType)) {
                        if (AbstractDomainTree.isCritOnlySingle(managedType, critProp)) {
                            criterionWidget = new EntitySingleCriterionWidget(root, managedType, critProp, getCentreContextConfigFor(critProp));
                        } else {
                            criterionWidget = new EntityCriterionWidget(root, managedType, critProp, getCentreContextConfigFor(critProp));
                        }
                    } else {
                        throw new UnsupportedOperationException(String.format("The single-editor type [%s] is currently unsupported.", propertyType));
                    }
                }
                criteriaWidgets.add(criterionWidget);
            }
        }
        criteriaWidgets.forEach(widget -> {
            importPaths.add(widget.importPath());
            importPaths.addAll(widget.editorsImportPaths());
            editorContainer.add(widget.render());
        });

        final List<PropertyColumnElement> propertyColumns = new ArrayList<>();
        for (final String resultProp : centre.getSecondTick().checkedProperties(root)) {
            final boolean isEntityItself = "".equals(resultProp); // empty property means "entity itself"
            final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultProp);

            final PropertyColumnElement el = new PropertyColumnElement(resultProp, centre.getSecondTick().getWidth(root, resultProp), propertyType, CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultProp));
            propertyColumns.add(el);
        }
        final DomContainer egiColumns = new DomContainer();
        propertyColumns.forEach(column -> {
            importPaths.add(column.importPath());
            egiColumns.add(column.render());
        });

        final String entityCentreStr = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.html").
                replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths)).
                replace("@entity_type", entityType.getSimpleName()).
                replace("@full_entity_type", entityType.getName()).
                replace("@mi_type", miType.getName()).
                replace("<!--@criteria_editors-->", editorContainer.toString()).
                replace("<!--@egi_columns-->", egiColumns.toString());

        final IRenderable representation = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityCentreStr);
            }
        };
        return representation;
    }

    /**
     * Return DSL representation for property name.
     *
     * @param name
     * @return
     */
    private static String dslName(final String name) {
        return name.equals("") ? "this" : name;
    }

    private CentreContextConfig getCentreContextConfigFor(final String critProp) {
        final String dslProp = dslName(critProp);
        return dslDefaultConfig.getValueMatchersForSelectionCriteria().isPresent()
                && dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp) != null
                && dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp).getValue() != null
                && dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp).getValue().isPresent()
                ? dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp).getValue().get()
                : new CentreContextConfig(false, false, false, false);
    }

    /**
     * Returns the global manager for the user on this concrete thread, on which {@link #build()} was invoked.
     *
     * @return
     */
    private IGlobalDomainTreeManager getUserSpecificGdtm() {
        return injector.getInstance(IGlobalDomainTreeManager.class);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public <V extends AbstractEntity<?>> Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> createValueMatcherAndContextConfig(final Class<? extends AbstractEntity<?>> criteriaType, final String criterionPropertyName) {
        final Optional<Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>>> matchers = dslDefaultConfig.getValueMatchersForSelectionCriteria();

        final String originalPropertyName = CentreUtils.getOriginalPropertyName(criteriaType, criterionPropertyName);
        final String dslProp = dslName(originalPropertyName);
        logger.error("createValueMatcherAndContextConfig: propertyName = " + criterionPropertyName + " originalPropertyName = " + dslProp);
        final Class<? extends IValueMatcherWithCentreContext<V>> matcherType = matchers.isPresent() && matchers.get().containsKey(dslProp) ?
                (Class<? extends IValueMatcherWithCentreContext<V>>) matchers.get().get(dslProp).getKey() : null;
        final Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> matcherAndContextConfig;
        if (matcherType != null) {
            matcherAndContextConfig = new Pair<>(injector.getInstance(matcherType), matchers.get().get(dslProp).getValue());
        } else {
            matcherAndContextConfig = createDefaultValueMatcherAndContextConfig(CentreUtils.getOriginalType(criteriaType), originalPropertyName, coFinder);
        }
        return matcherAndContextConfig;
    }

    /**
     * Creates default value matcher and context config for the specified entity property.
     *
     * @param propertyName
     * @param criteriaType
     * @param coFinder
     * @return
     */
    private <V extends AbstractEntity<?>> Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> createDefaultValueMatcherAndContextConfig(final Class<? extends AbstractEntity<?>> originalType, final String originalPropertyName, final ICompanionObjectFinder coFinder) {
        final boolean isEntityItself = "".equals(originalPropertyName); // empty property means "entity itself"
        final Class<V> propertyType = (Class<V>) (isEntityItself ? originalType : PropertyTypeDeterminator.determinePropertyType(originalType, originalPropertyName));
        final IEntityDao<V> co = coFinder.find(propertyType);
        return new Pair<>(new FallbackValueMatcherWithCentreContext<V>(co), Optional.empty());
    }
}
