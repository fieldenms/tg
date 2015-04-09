package ua.com.fielden.platform.web.centre.api.impl;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;

import org.junit.Test;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.helpers.QueryEnhancer;

/**
 * A test case for Entity Centre DSL produced result sets.
 *
 * @author TG Team
 *
 */
public class EntityCentreBuilderQueryEnhancerAndFetchProviderTest {

    @Test
    public void both_query_enhancer_and_its_context_config_should_be_present() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .setQueryEnhancer(QueryEnhancer.class, context().withMasterEntity().build())
                .build();

        assertTrue(config.getQueryEnhancerConfig().isPresent());
        assertEquals(QueryEnhancer.class, config.getQueryEnhancerConfig().get().getKey());
        assertTrue(config.getQueryEnhancerConfig().get().getValue().isPresent());
    }

    @Test
    public void query_enhancer_could_have_context_config_provided_as_null() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .setQueryEnhancer(QueryEnhancer.class, null)
                .build();

        assertTrue(config.getQueryEnhancerConfig().isPresent());
        assertEquals(QueryEnhancer.class, config.getQueryEnhancerConfig().get().getKey());
        assertFalse(config.getQueryEnhancerConfig().get().getValue().isPresent());
    }


    @Test
    public void query_enhancer_could_have_its_context_config_omitted() {
        final EntityCentreConfig<TgWorkOrder> config = centreFor(TgWorkOrder.class)
                .addProp("key")
                .setQueryEnhancer(QueryEnhancer.class)
                .build();

        assertTrue(config.getQueryEnhancerConfig().isPresent());
        assertEquals(QueryEnhancer.class, config.getQueryEnhancerConfig().get().getKey());
        assertFalse(config.getQueryEnhancerConfig().get().getValue().isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_null_query_enhancer_type_with_context_config_should_not_be_permitted() {
        centreFor(TgWorkOrder.class)
                .addProp("key")
                .setQueryEnhancer(null, context().withMasterEntity().build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_null_query_enhancer_type_even_without_context_config_should_not_be_permitted() {
        centreFor(TgWorkOrder.class)
                .addProp("key")
                .setQueryEnhancer(null)
                .build();
    }

}
