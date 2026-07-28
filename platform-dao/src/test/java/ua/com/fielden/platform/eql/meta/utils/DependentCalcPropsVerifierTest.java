package ua.com.fielden.platform.eql.meta.utils;

import org.junit.Test;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DependentCalcPropsVerifierTest extends AbstractDaoTestCase {

    private final DependentCalcPropsVerifier verifier = getInstance(DependentCalcPropsVerifier.class);
    private final QuerySourceInfoProvider querySourceInfoProvider = getInstance(QuerySourceInfoProvider.class);

    @Test
    public void verification_of_calc_prop_dependencies_passes_for_all_modelled_entity_types() {
        // Mirrors the startup verification service: every modelled entity type in the domain must have an
        // acyclic calculated-property dependency graph.
        assertThatNoException().isThrownBy(() -> verifier.verify(querySourceInfoProvider.modelledQuerySourceInfos()));
    }

    @Test
    public void a_chain_of_dependent_calc_props_without_a_cycle_passes_verification() {
        assertThatNoException().isThrownBy(() -> verifier.verify(qsi(EntityWithAcyclicCalcProps.class)));
    }

    @Test
    public void a_direct_cycle_between_two_calc_props_is_detected() {
        assertCyclicDependencies(EntityWithCyclicCalcProps.class, "i1", "i2");
    }

    @Test
    public void a_self_referential_calc_prop_is_detected_as_a_cycle() {
        assertCyclicDependencies(EntityWithSelfReferentialCalcProp.class, "selfRef");
    }

    @Test
    public void a_transitive_cycle_across_three_calc_props_is_detected() {
        assertCyclicDependencies(EntityWithTransitiveCyclicCalcProps.class, "alpha", "beta", "gamma");
    }

    // ------------------------------------------------------------------------------------------------
    // Helpers

    /// Query source info for an (unregistered) test entity type -- its metadata is generated ad-hoc.
    ///
    private QuerySourceInfo<?> qsi(final Class<? extends AbstractEntity<?>> entityType) {
        return querySourceInfoProvider.getModelledQuerySourceInfo(entityType);
    }

    /// Asserts that verifying `entityType` throws [EqlException] whose message names the entity and every
    /// property that participates in the reported cycle.
    ///
    private void assertCyclicDependencies(final Class<? extends AbstractEntity<?>> entityType, final String... propsInCycle) {
        assertThatThrownBy(() -> verifier.verify(qsi(entityType)))
                .isInstanceOf(EqlException.class)
                .hasMessageContaining(entityType.getSimpleName())
                .hasMessageContainingAll(propsInCycle);
    }

}
