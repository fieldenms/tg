package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.function.Consumer;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

public interface IResultSetBuilderDynamicProps<T extends AbstractEntity<?>> {

    /**
     * Provides definer for dynamic properties those depends on centre context
     *
     * @param propDefiner
     * @return
     */
    <M extends AbstractEntity<?> >IResultSetBuilderAlsoDynamicProps<T> addProps( final String propName, final Class<? extends IDynamicPropDefiner<T>> propDefiner, final CentreContextConfig contextConfig, final Consumer<M> consumer);
}
