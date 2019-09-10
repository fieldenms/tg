package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;
import java.util.function.BiConsumer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

public interface IResultSetBuilderDynamicProps<T extends AbstractEntity<?>> {

    /**
     * Provides definer for dynamic properties those depends on centre context
     *
     * @param propDefiner
     * @return
     */
    <M extends AbstractEntity<?> >IResultSetBuilderAlsoDynamicProps<T> addProps( final String propName, final Class<? extends IDynamicPropDefiner<T>> propDefiner, final CentreContextConfig contextConfig, final BiConsumer<M, Optional<CentreContext<T, ?>>> consumer);
}
