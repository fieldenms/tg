package ua.com.fielden.platfrom.web.centre.api.context.impl;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.linkedMapOf;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Map;
import java.util.function.BiFunction;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

/**
 * Ensures correct construction of the entity centre context configuration as the result of using Entity Centre DSL.
 *
 * @author TG Team
 *
 */
public class EntityCentreContextSelectorTest {

    @Test
    public void context_with_current_entity() {
       final CentreContextConfig config = context().withCurrentEntity().build();
       assertEquals(new CentreContextConfig(true, false, false, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_all_selected_entities() {
       final CentreContextConfig config = context().withSelectedEntities().build();
       assertEquals(new CentreContextConfig(false, true, false, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selection_crit() {
       final CentreContextConfig config = context().withSelectionCrit().build();
       assertEquals(new CentreContextConfig(false, false, true, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_master_entity() {
       final CentreContextConfig config = context().withMasterEntity().build();
       assertEquals(new CentreContextConfig(false, false, false, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_current_entity_and_selection_crit() {
       final CentreContextConfig config = context().withCurrentEntity().withSelectionCrit().build();
       assertEquals(new CentreContextConfig(true, false, true, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selection_crit_and_current_entity() {
       final CentreContextConfig config = context().withSelectionCrit().withCurrentEntity().build();
       assertEquals(new CentreContextConfig(true, false, true, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_current_entity_and_master_entity() {
       final CentreContextConfig config = context().withCurrentEntity().withMasterEntity().build();
       assertEquals(new CentreContextConfig(true, false, false, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_master_entity_and_current_entity() {
       final CentreContextConfig config = context().withMasterEntity().withCurrentEntity().build();
       assertEquals(new CentreContextConfig(true, false, false, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selected_entities_and_selection_crit() {
       final CentreContextConfig config = context().withSelectedEntities().withSelectionCrit().build();
       assertEquals(new CentreContextConfig(false, true, true, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selection_crit_and_selected_entities() {
       final CentreContextConfig config = context().withSelectionCrit().withSelectedEntities().build();
       assertEquals(new CentreContextConfig(false, true, true, false, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selected_entities_and_master_entity() {
       final CentreContextConfig config = context().withSelectedEntities().withMasterEntity().build();
       assertEquals(new CentreContextConfig(false, true, false, true, null,  empty(), empty()), config);
    }

    @Test
    public void context_with_master_entity_and_selected_entities() {
       final CentreContextConfig config = context().withMasterEntity().withSelectedEntities().build();
       assertEquals(new CentreContextConfig(false, true, false, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_master_entity_and_selection_crit() {
       final CentreContextConfig config = context().withMasterEntity().withSelectionCrit().build();
       assertEquals(new CentreContextConfig(false, false, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selection_crit_and_master_entity() {
       final CentreContextConfig config = context().withSelectionCrit().withMasterEntity().build();
       assertEquals(new CentreContextConfig(false, false, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_master_entity_and_selection_crit_and_curr_entity() {
       final CentreContextConfig config = context().withMasterEntity().withSelectionCrit().withCurrentEntity().build();
       assertEquals(new CentreContextConfig(true, false, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_master_entity_and_selection_crit_and_selected_entities() {
       final CentreContextConfig config = context().withMasterEntity().withSelectionCrit().withSelectedEntities().build();
       assertEquals(new CentreContextConfig(false, true, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selection_crit_and_master_entity_and_curr_entity() {
       final CentreContextConfig config = context().withSelectionCrit().withMasterEntity().withCurrentEntity().build();
       assertEquals(new CentreContextConfig(true, false, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selection_crit_and_master_entity_and_selected_entities() {
       final CentreContextConfig config = context().withSelectionCrit().withMasterEntity().withSelectedEntities().build();
       assertEquals(new CentreContextConfig(false, true, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_current_entity_and_selection_crit_and_master_entity() {
       final CentreContextConfig config = context().withCurrentEntity().withSelectionCrit().withMasterEntity().build();
       assertEquals(new CentreContextConfig(true, false, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_current_entity_and_master_entity_and_selection_crit() {
       final CentreContextConfig config = context().withCurrentEntity().withMasterEntity().withSelectionCrit().build();
       assertEquals(new CentreContextConfig(true, false, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selected_entities_and_selection_crit_and_master_entity() {
       final CentreContextConfig config = context().withSelectedEntities().withSelectionCrit().withMasterEntity().build();
       assertEquals(new CentreContextConfig(false, true, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selected_entities_and_master_entity_and_selection_crit() {
       final CentreContextConfig config = context().withSelectedEntities().withMasterEntity().withSelectionCrit().build();
       assertEquals(new CentreContextConfig(false, true, true, true, null, empty(), empty()), config);
    }

    @Test
    public void context_with_selected_entities_and_selection_crit_and_related_contexts() {
       final CentreContextConfig config = context().withSelectionCrit().withSelectedEntities()
               .extendWithInsertionPointContext(TgPersistentEntityWithProperties.class, context().withSelectedEntities().withSelectionCrit().build()).build();
       final Map<Class<? extends AbstractFunctionalEntityWithCentreContext<?>>, CentreContextConfig> relatedContexts =
               linkedMapOf(t2(TgPersistentEntityWithProperties.class, context().withSelectionCrit().withSelectedEntities().build()));
       assertEquals(new CentreContextConfig(false, true, true, false, null, of(relatedContexts), empty()), config);
    }

    @Test
    public void context_with_selected_entities_and_selection_crit_and_parent_centre_contexts() {
       final CentreContextConfig config = context().withSelectionCrit().withSelectedEntities()
               .extendWithParentCentreContext(context().withSelectedEntities().withSelectionCrit().build()).build();
       assertEquals(new CentreContextConfig(false, true, true, false, null, empty(), of (context().withSelectionCrit().withSelectedEntities().build())), config);
    }

    @Test
    public void context_referentially_identical_computation_components_are_equal() {
       final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation = (entity, context) -> entity.getType();
       final Either<Exception, CentreContextConfig> either = Try(() -> context().withSelectedEntities().withMasterEntity().withSelectionCrit().withComputation(computation).build());
       assertTrue(either instanceof Right);
       final CentreContextConfig config = ((Right<Exception, CentreContextConfig>) either).value;
       assertEquals(new CentreContextConfig(false, true, true, true, computation, empty(), empty()), config);
       assertTrue(config.computation.isPresent());
       assertEquals(computation, config.computation.get());
    }

    @Test
    public void configuring_context_with_null_computation_is_restriected() {
        assertTrue(Try(() -> context().withSelectionCrit().withComputation(null).build()) instanceof Left);
        assertTrue(Try(() -> context().withMasterEntity().withComputation(null).build()) instanceof Left);
        assertTrue(Try(() -> context().withSelectedEntities().withComputation(null).build()) instanceof Left);
        assertTrue(Try(() -> context().withSelectedEntities().withMasterEntity().withComputation(null).build()) instanceof Left);
        assertTrue(Try(() -> context().withSelectedEntities().withMasterEntity().withSelectionCrit().withComputation(null).build()) instanceof Left);

    }
}
