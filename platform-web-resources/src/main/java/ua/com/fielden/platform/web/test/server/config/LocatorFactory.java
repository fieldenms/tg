package ua.com.fielden.platform.web.test.server.config;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.test.server.config.StandardActionsStyles.STANDARD_ACTION_COLOUR;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

/**
 * A set of factories for convenient construction of {@link Locator} actions. 
 *
 * @author TG Air Team
 *
 */
public class LocatorFactory {

    private static final PrefDim LOCATOR_DIM = mkDim(440, Unit.PX, 184, Unit.PX);

    private LocatorFactory() {}

    /**
     * Constructs a standard locator.
     *
     * @param builder
     * @param injector
     * @param locatorEntityType
     * @param propName
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityActionConfig mkLocator(
            final IWebUiBuilder builder, 
            final Injector injector, 
            final Class<T> locatorEntityType, 
            final String propName) {
        return mkStandardLocator(builder, injector, locatorEntityType, propName, Optional.empty(), Optional.empty());
    }
    
    /**
     * Constructs a standard locator with an icon style.
     *
     * @param builder
     * @param injector
     * @param locatorEntityType
     * @param propName
     * @param iconStyle
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityActionConfig mkLocator(
            final IWebUiBuilder builder, 
            final Injector injector, 
            final Class<T> locatorEntityType, 
            final String propName, 
            final String iconStyle) {
        return mkStandardLocator(builder, injector, locatorEntityType, propName, Optional.empty(), Optional.of(iconStyle));
    }

    /**
     * Constructs a standard locator with a custom value matcher.
     *
     * @param builder
     * @param injector
     * @param locatorEntityType
     * @param propName
     * @param matcherType
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityActionConfig mkLocator(
            final IWebUiBuilder builder, 
            final Injector injector, 
            final Class<T> locatorEntityType, 
            final String propName,
            final Class<? extends IValueMatcherWithContext<T, ?>> matcherType) {
        return mkStandardLocator(builder, injector, locatorEntityType, propName, Optional.of(matcherType),  Optional.empty());
    }
    
    /**
     * Constructs a standard locator with a custom value matcher and an icon style.
     *
     * @param builder
     * @param injector
     * @param locatorEntityType
     * @param propName
     * @param matcherType
     * @param iconStyle
     * @return
     */
    public static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityActionConfig mkLocator(
            final IWebUiBuilder builder, 
            final Injector injector, 
            final Class<T> locatorEntityType, 
            final String propName,
            final Class<? extends IValueMatcherWithContext<T, ?>> matcherType,
            final String iconStyle) {
        return mkStandardLocator(builder, injector, locatorEntityType, propName, Optional.of(matcherType),  Optional.of(iconStyle));
    }

    /**
     * Equips a locator that is created using {@link #createLocatorAction(IWebUiBuilder, Injector, Class, String, Class, Optional)} with a standard configuration.
     * 
     * @param builder
     * @param injector
     * @param locatorEntityType
     * @param propName
     * @param matcherType
     * @return
     */
    private static <T extends AbstractFunctionalEntityWithCentreContext<?>> EntityActionConfig mkStandardLocator(
            final IWebUiBuilder builder, 
            final Injector injector, 
            final Class<T> locatorEntityType, 
            final String propName,
            final Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherType, 
            final Optional<String> iconStyle) {
        
        final Class<AbstractEntity<?>> propType = Optional.of(PropertyTypeDeterminator.determinePropertyType(locatorEntityType, propName))
                .filter(AbstractEntity.class::isAssignableFrom)
                .map(type -> (Class<AbstractEntity<?>>) type)
                .orElseThrow(() -> new EntityMasterConfigurationException(format("Locator configuration is invalid. Property [%s] in [%s] is not of entity type.", propName, locatorEntityType.getSimpleName())));

        final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(propType);
        final String desc = format("Locate %s", entityTitleAndDesc.getKey());

        return createLocatorAction(builder, injector, locatorEntityType, propName, propType, matcherType)
                .withContext(context().withSelectionCrit().build())
                .icon("icons:search")
                .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                .shortDesc(desc)
                .longDesc(desc)
                .prefDimForView(LOCATOR_DIM)
                .build();
    }

    /**
     * Creates and registers an entity master for a locator entity and returns an action instance that can be further configured and added to UI.
     *
     * @param builder
     * @param injector
     * @param locatorEntityType -- an entity type that represents a locator.
     * @param propName -- a property of the locator entity, which must be of entity type itself; an Entity Master for the value of this property gets invoked.
     * @param propType -- a type of the specified property
     * @param matcherType -- optional custom matcher.
     * @return
     */
    private static <T extends AbstractFunctionalEntityWithCentreContext<?>> Locator<T> createLocatorAction(
            final IWebUiBuilder builder,
            final Injector injector, 
            final Class<T> locatorEntityType,
            final String propName,
            final Class<AbstractEntity<?>> propType,
            final Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherType) {
        
        final Locator<T> openLocatorAction = new Locator<>(propType, locatorEntityType, propName, matcherType, builder.getOpenMasterAction(propType));
        if (!builder.getMaster(locatorEntityType).isPresent()) {
            builder.register(new EntityMaster<>(locatorEntityType, openLocatorAction.masterConfig, injector));
        }
        return openLocatorAction;
    }
  
}
