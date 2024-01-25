package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

public interface IResultSetBuilderDynamicProps<T extends AbstractEntity<?>> {

    /**
     * An API to add collectional property to an entity centre with columns built dynamically based on {@code dynColBuilderType}.
     *
     * @param dynColBuilderType -- a type of {@link IDynamicColumnBuilder} that builds the information required to add dynamic columns to EGI for displaying collectional properties in-line.
     * @param entityPreProcessor -- an optional consumer that mutates each entity in a collectional association before it is returned to the client to be displayed;
     *                              the main purpose of this is to perform additional computations that are context depended and are used for display-only purposes.
     * @param contextConfig -- configuration that defines the context for executing both column builder {@code dynColBuilderType} and pre-processor {@code entityPreProcessor}.
     * @return
     */
    <M extends AbstractEntity<?>> IResultSetBuilderDynamicPropsAction<T> addProps(final String propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<M, Optional<CentreContext<T, ?>>> entityPreProcessor, final CentreContextConfig contextConfig);

    /**
     * An API to add collectional property to an entity centre with columns built dynamically based on {@code dynColBuilderType}. Also provides function to set up rendering hints for dynamic properties.
     *
     * @param propName
     * @param dynColBuilderType -- a type of {@link IDynamicColumnBuilder} that builds the information required to add dynamic columns to EGI for displaying collectional properties in-line.
     * @param entityPreProcessor -- an optional consumer that mutates each entity in a collectional association before it is returned to the client to be displayed;
     *                              the main purpose of this is to perform additional computations that are context dependent and are used for display-only purposes.
     * @param renderingHintsProvider -- an optional context dependent function that defines rendering hints for specified entity that is used to provide data for dynamic column.
     * @param contextConfig -- configuration that defines the context for executing column builder {@code dynColBuilderType}, pre-processor {@code entityPreProcessor} and rendering hints provider {@code renderingHintsProvider}.
     * @return
     */
    <M extends AbstractEntity<?>> IResultSetBuilderDynamicPropsAction<T> addProps(final String propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<M, Optional<CentreContext<T, ?>>> entityPreProcessor, final BiFunction<Collection<M>, Optional<CentreContext<T, ?>>, Map> renderingHintsProvider, final CentreContextConfig contextConfig);

    /**
     * An API to add collectional property to an entity centre with columns built dynamically based on {@code dynColBuilderType}.
     *
     * @param dynColBuilderType -- a type of {@link IDynamicColumnBuilder} that builds the information required to add dynamic columns to EGI for displaying collectional properties in-line.
     * @param entityPreProcessor -- an optional consumer that mutates each entity in a collectional association before it is returned to the client to be displayed;
     *                              the main purpose of this is to perform additional computations that are context depended and are used for display-only purposes.
     * @param contextConfig -- configuration that defines the context for executing both column builder {@code dynColBuilderType} and pre-processor {@code entityPreProcessor}.
     * @return
     */
    default <M extends AbstractEntity<?>> IResultSetBuilderDynamicPropsAction<T> addProps(final IConvertableToPath propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<M, Optional<CentreContext<T, ?>>> entityPreProcessor, final CentreContextConfig contextConfig) {
        return addProps(propName.toPath(), dynColBuilderType, entityPreProcessor, contextConfig);
    }

    /**
     * An API to add collectional property to an entity centre with columns built dynamically based on {@code dynColBuilderType}. Also provides function to set up rendering hints for dynamic properties.
     *
     * @param propName
     * @param dynColBuilderType -- a type of {@link IDynamicColumnBuilder} that builds the information required to add dynamic columns to EGI for displaying collectional properties in-line.
     * @param entityPreProcessor -- an optional consumer that mutates each entity in a collectional association before it is returned to the client to be displayed;
     *                              the main purpose of this is to perform additional computations that are context dependent and are used for display-only purposes.
     * @param renderingHintsProvider -- an optional context dependent function that defines rendering hints for specified entity that is used to provide data for dynamic column.
     * @param contextConfig -- configuration that defines the context for executing column builder {@code dynColBuilderType}, pre-processor {@code entityPreProcessor} and rendering hints provider {@code renderingHintsProvider}.
     * @return
     */
    default <M extends AbstractEntity<?>> IResultSetBuilderDynamicPropsAction<T> addProps(final IConvertableToPath propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<M, Optional<CentreContext<T, ?>>> entityPreProcessor, final BiFunction<Collection<M>, Optional<CentreContext<T, ?>>, Map> renderingHintsProvider, final CentreContextConfig contextConfig) {
        return addProps(propName.toPath(), dynColBuilderType, entityPreProcessor, renderingHintsProvider, contextConfig);
    }


}