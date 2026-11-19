package ua.com.fielden.platform.audit;

import org.junit.Test;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.audit.annotations.Audited;
import ua.com.fielden.platform.audit.annotations.InactiveAuditProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

/// Tests that [AuditTypeFinder] requires audit types to agree with their audited types on annotation `@Invisible`, when auditing is enabled.
///
/// Audit types are generated sources.
/// The ones declared here are reduced to what the finder needs, and are written by hand to represent audit types that were generated before their audited types changed.
///
public class AuditTypeFinderInvisiblePropertiesTest {

    private static final List<Class<?>>
            TYPES_FOR_CHANGED = List.of(Changed.class, Changed_a3t_1.class, Changed_a3t_1_Prop.class, ReChanged_a3t.class, ReChanged_a3t_Prop.class),
            TYPES_FOR_UNCHANGED = List.of(Unchanged.class, Unchanged_a3t_1.class, Unchanged_a3t_1_Prop.class, ReUnchanged_a3t.class, ReUnchanged_a3t_Prop.class);

    @Test
    public void audit_types_that_disagree_with_their_audited_type_on_Invisible_are_rejected_if_auditing_is_enabled() {
        assertThatThrownBy(() -> new AuditTypeFinder(TYPES_FOR_CHANGED, AuditingMode.ENABLED))
                .isInstanceOf(EntityDefinitionException.class)
                .hasMessageContaining(Changed.class.getTypeName())
                // An invisible property with an audit-property that is not.
                .hasMessageContaining("Changed_a3t_1.a3t_secret")
                .hasMessageContaining("ReChanged_a3t.a3t_secret")
                // A property that is not invisible with an audit-property that is.
                .hasMessageContaining("Changed_a3t_1.a3t_open")
                .hasMessageContaining("ReChanged_a3t.a3t_open")
                // A difference in `centreOnly`.
                .hasMessageContaining("Changed_a3t_1.a3t_internal")
                .hasMessageContaining("ReChanged_a3t.a3t_internal")
                // An inactive audit-property whose audited property is still declared.
                .hasMessageContaining("Changed_a3t_1.a3t_unaudited")
                .hasMessageContaining("ReChanged_a3t.a3t_unaudited")
                // Audit-properties that agree with their audited properties, and one whose audited property is no longer declared.
                .hasMessageNotContaining("a3t_key")
                .hasMessageNotContaining("a3t_removed");
    }

    /// The generator of audit types runs in this mode, and is what brings audit types in line.
    ///
    @Test
    public void audit_types_that_disagree_with_their_audited_type_on_Invisible_are_accepted_in_the_generation_mode() {
        final var finder = new AuditTypeFinder(TYPES_FOR_CHANGED, AuditingMode.GENERATION);
        assertEquals(Changed_a3t_1.class, finder.navigate(Changed.class).auditEntityType());
    }

    @Test
    public void audit_types_that_agree_with_their_audited_type_on_Invisible_are_accepted_if_auditing_is_enabled() {
        final var finder = new AuditTypeFinder(TYPES_FOR_UNCHANGED, AuditingMode.ENABLED);
        assertEquals(Unchanged_a3t_1.class, finder.navigate(Unchanged.class).auditEntityType());
        assertEquals(ReUnchanged_a3t.class, finder.navigate(Unchanged.class).synAuditEntityType());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : An audited type whose audit types disagree with it
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class Changed extends AbstractEntity<String> {
        @IsProperty
        @MapTo
        @Invisible
        private String secret;

        @IsProperty
        @MapTo
        private String open;

        @IsProperty
        @MapTo
        @Invisible(centreOnly = true)
        private String internal;

        @IsProperty
        @MapTo
        @Invisible
        private String unaudited;
    }

    @AuditFor(value = Changed.class, version = 1)
    @KeyType(DynamicEntityKey.class)
    public static class Changed_a3t_1 extends AuditEntity<Changed> {
        @IsProperty
        private String a3t_key;

        @IsProperty
        private String a3t_secret;

        @IsProperty
        @Invisible
        private String a3t_open;

        @IsProperty
        @Invisible
        private String a3t_internal;

        @IsProperty
        @InactiveAuditProperty
        private String a3t_unaudited;

        @IsProperty
        @InactiveAuditProperty
        @Invisible
        private String a3t_removed;
    }

    @AuditFor(value = Changed.class, version = 1)
    @KeyType(DynamicEntityKey.class)
    public static class Changed_a3t_1_Prop extends AuditProp<Changed> {}

    @AuditFor(Changed.class)
    @KeyType(DynamicEntityKey.class)
    public static class ReChanged_a3t extends SynAuditEntity<Changed> {
        @IsProperty
        private String a3t_key;

        @IsProperty
        private String a3t_secret;

        @IsProperty
        @Invisible
        private String a3t_open;

        @IsProperty
        @Invisible
        private String a3t_internal;

        @IsProperty
        @InactiveAuditProperty
        private String a3t_unaudited;

        @IsProperty
        @InactiveAuditProperty
        @Invisible
        private String a3t_removed;
    }

    @AuditFor(Changed.class)
    @KeyType(DynamicEntityKey.class)
    public static class ReChanged_a3t_Prop extends SynAuditProp<Changed> {}

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : An audited type whose audit types agree with it
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @KeyType(String.class)
    @MapEntityTo
    @Audited
    public static class Unchanged extends AbstractEntity<String> {
        @IsProperty
        @MapTo
        @Invisible
        private String secret;

        @IsProperty
        @MapTo
        private String open;

        @IsProperty
        @MapTo
        @Invisible(centreOnly = true)
        private String internal;

        @IsProperty
        @MapTo
        @Invisible
        private String unaudited;
    }

    @AuditFor(value = Unchanged.class, version = 1)
    @KeyType(DynamicEntityKey.class)
    public static class Unchanged_a3t_1 extends AuditEntity<Unchanged> {
        @IsProperty
        private String a3t_key;

        @IsProperty
        @Invisible
        private String a3t_secret;

        @IsProperty
        private String a3t_open;

        @IsProperty
        @Invisible(centreOnly = true)
        private String a3t_internal;

        @IsProperty
        @InactiveAuditProperty
        @Invisible
        private String a3t_unaudited;

        @IsProperty
        @InactiveAuditProperty
        private String a3t_removed;
    }

    @AuditFor(value = Unchanged.class, version = 1)
    @KeyType(DynamicEntityKey.class)
    public static class Unchanged_a3t_1_Prop extends AuditProp<Unchanged> {}

    @AuditFor(Unchanged.class)
    @KeyType(DynamicEntityKey.class)
    public static class ReUnchanged_a3t extends SynAuditEntity<Unchanged> {
        @IsProperty
        private String a3t_key;

        @IsProperty
        @Invisible
        private String a3t_secret;

        @IsProperty
        private String a3t_open;

        @IsProperty
        @Invisible(centreOnly = true)
        private String a3t_internal;

        @IsProperty
        @InactiveAuditProperty
        @Invisible
        private String a3t_unaudited;

        @IsProperty
        @InactiveAuditProperty
        private String a3t_removed;
    }

    @AuditFor(Unchanged.class)
    @KeyType(DynamicEntityKey.class)
    public static class ReUnchanged_a3t_Prop extends SynAuditProp<Unchanged> {}

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Base types that implement what audit types are required to
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    public static class AuditEntity<E extends AbstractEntity<?>> extends AbstractAuditEntity<E> {
        @Override
        public E getAuditedEntity() {
            return null;
        }

        @Override
        public AbstractAuditEntity<E> setAuditedEntity(final E auditedEntity) {
            return this;
        }
    }

    public static class AuditProp<E extends AbstractEntity<?>> extends AbstractAuditProp<E> {
        @Override
        public AbstractAuditEntity<E> getAuditEntity() {
            return null;
        }

        @Override
        public AbstractAuditProp<E> setAuditEntity(final AbstractAuditEntity<E> entity) {
            return this;
        }

        @Override
        public PropertyDescriptor<AbstractSynAuditEntity<E>> getProperty() {
            return null;
        }

        @Override
        public AbstractAuditProp<E> setProperty(final PropertyDescriptor<AbstractSynAuditEntity<E>> property) {
            return this;
        }
    }

    public static class SynAuditEntity<E extends AbstractEntity<?>> extends AbstractSynAuditEntity<E> {
        @Override
        public E getAuditedEntity() {
            return null;
        }

        @Override
        public Set<? extends AbstractSynAuditProp<E>> getChangedProps() {
            return Set.of();
        }
    }

    public static class SynAuditProp<E extends AbstractEntity<?>> extends AbstractSynAuditProp<E> {
        @Override
        public AbstractSynAuditEntity<E> getAuditEntity() {
            return null;
        }

        @Override
        public AbstractSynAuditProp<E> setAuditEntity(final AbstractSynAuditEntity<E> entity) {
            return this;
        }

        @Override
        public PropertyDescriptor<AbstractSynAuditEntity<E>> getProperty() {
            return null;
        }

        @Override
        public AbstractSynAuditProp<E> setProperty(final PropertyDescriptor<AbstractSynAuditEntity<E>> propertyDescriptor) {
            return this;
        }
    }

}
