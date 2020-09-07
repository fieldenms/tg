package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;
import java.util.function.BiConsumer;

import ua.com.fielden.platform.entity.AbstractEntity;
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
    <M extends AbstractEntity<?> >IResultSetBuilderAlsoDynamicProps<T> addProps(final String propName, final Class<? extends IDynamicColumnBuilder<T>> dynColBuilderType, final BiConsumer<M, Optional<CentreContext<T, ?>>> entityPreProcessor, final CentreContextConfig contextConfig);
}
