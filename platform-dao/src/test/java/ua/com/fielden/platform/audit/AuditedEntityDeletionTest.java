package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.AuditedEntity;
import ua.com.fielden.platform.sample.domain.AuditedEntityDao;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/// Tests for deletion of an audited entity together with its own audit records.
///
/// Deleting an audited entity deletes all of its own audit records first — audit-prop records, then audit-entity records, across all audit versions.
/// This happens within the same transaction as the deletion of the entity itself (see [ua.com.fielden.platform.companion.DeleteOperations]).
///
/// This applies uniformly to every deletion entry point: the single-entity delete, the query-model-based deletes, and all batch-delete variants.
/// These tests exercise each of the public `defaultDelete` / `defaultBatchDelete` methods declared on [AuditedEntityDao].
///
/// The test database is created without foreign-key constraints.
/// These tests therefore assert on the observable outcome — that audit records are actually removed, and that none are left orphaned — rather than on foreign-key enforcement, which could be exercised only against a real RDBMS with FKs.
///
public class AuditedEntityDeletionTest extends AbstractDaoTestCase {

    private IAuditTypeFinder auditTypeFinder;
    private AuditedEntityDao coAuditedEntity;
    private ISynAuditEntityDao<AuditedEntity> coAuditedEntityAudit;

    @Inject
    void init(final IAuditTypeFinder auditTypeFinder) {
        this.auditTypeFinder = auditTypeFinder;
        this.coAuditedEntity = co(AuditedEntity.class);
        this.coAuditedEntityAudit = co(auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType());
    }

    @Test
    public void delete_of_entity_removes_its_audit_records() {
        final var entity = saveAudited("A");
        final var id = entity.getId();
        assertHasAuditRecords(id);

        coAuditedEntity.delete(entity);

        assertGoneWithoutAuditRecords(id);
    }

    @Test
    public void delete_with_query_model_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());
        assertHasAuditRecords(b.getId());

        coAuditedEntity.delete(select(AuditedEntity.class).where().prop(KEY).in().values("A", "B").model());

        assertGoneWithoutAuditRecords(a.getId());
        assertGoneWithoutAuditRecords(b.getId());
    }

    @Test
    public void delete_with_parameterised_query_model_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());

        coAuditedEntity.delete(select(AuditedEntity.class).where().prop(KEY).eq().param("key").model(), Map.of("key", "A"));

        assertGoneWithoutAuditRecords(a.getId());
        assertExistsWithAuditRecords(b.getId());
    }

    @Test
    public void batchDelete_with_query_model_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());
        assertHasAuditRecords(b.getId());

        final int count = coAuditedEntity.batchDelete(select(AuditedEntity.class).where().prop(KEY).in().values("A", "B").model());

        assertEquals(2, count);
        assertGoneWithoutAuditRecords(a.getId());
        assertGoneWithoutAuditRecords(b.getId());
    }

    @Test
    public void batchDelete_with_parameterised_query_model_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());

        final int count = coAuditedEntity.batchDelete(select(AuditedEntity.class).where().prop(KEY).eq().param("key").model(), Map.of("key", "A"));

        assertEquals(1, count);
        assertGoneWithoutAuditRecords(a.getId());
        assertExistsWithAuditRecords(b.getId());
    }

    @Test
    public void batchDelete_by_id_values_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());
        assertHasAuditRecords(b.getId());

        final int count = coAuditedEntity.batchDelete(List.of(a.getId(), b.getId()));

        assertEquals(2, count);
        assertGoneWithoutAuditRecords(a.getId());
        assertGoneWithoutAuditRecords(b.getId());
    }

    @Test
    public void batchDelete_by_entity_instances_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());
        assertHasAuditRecords(b.getId());

        final int count = coAuditedEntity.batchDelete(List.of(a, b));

        assertEquals(2, count);
        assertGoneWithoutAuditRecords(a.getId());
        assertGoneWithoutAuditRecords(b.getId());
    }

    @Test
    public void batchDeleteByPropertyValues_by_id_values_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());
        assertHasAuditRecords(b.getId());

        // `AuditedEntity` has no persistent entity-typed property that is convenient to match on, so ID is used as the property.
        final int count = coAuditedEntity.batchDeleteByPropertyValues(ID, List.of(a.getId(), b.getId()));

        assertEquals(2, count);
        assertGoneWithoutAuditRecords(a.getId());
        assertGoneWithoutAuditRecords(b.getId());
    }

    @Test
    public void batchDeleteByPropertyValues_by_entity_instances_removes_audit_records() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(a.getId());
        assertHasAuditRecords(b.getId());

        final int count = coAuditedEntity.batchDeleteByPropertyValues(ID, List.of(a, b));

        assertEquals(2, count);
        assertGoneWithoutAuditRecords(a.getId());
        assertGoneWithoutAuditRecords(b.getId());
    }

    @Test
    public void deleting_an_audited_entity_does_not_affect_audit_records_of_other_entities() {
        final var a = saveAudited("A");
        final var b = saveAudited("B");
        assertHasAuditRecords(b.getId());

        coAuditedEntity.delete(a);

        // "A" and its audit records are gone, but "B" and its audit records remain untouched.
        assertGoneWithoutAuditRecords(a.getId());
        assertExistsWithAuditRecords(b.getId());
    }

    // ---------- helpers ----------

    /// Saves a new audited entity with an audited change, so that both an audit-entity record and audit-prop records are created.
    ///
    private AuditedEntity saveAudited(final String key) {
        return save(new_(AuditedEntity.class, key).setStr2(key));
    }

    private void assertHasAuditRecords(final Long id) {
        assertThat(coAuditedEntityAudit.getAudits(id)).isNotEmpty();
        assertThat(countAuditProps(id)).isPositive();
    }

    private void assertGoneWithoutAuditRecords(final Long id) {
        assertFalse(coAuditedEntity.entityExists(id));
        assertThat(coAuditedEntityAudit.getAudits(id)).isEmpty();
        assertEquals(0, countAuditProps(id));
        assertNoOrphanedAuditProps();
    }

    private void assertExistsWithAuditRecords(final Long id) {
        assertTrue(coAuditedEntity.findByIdOptional(id).isPresent());
        assertHasAuditRecords(id);
    }

    /// Counts persisted audit-prop records for the audited entity with the specified ID, across all audit versions.
    ///
    /// This count is *reachability-based*: [AbstractAuditProp#PATH_TO_AUDITED_ENTITY] starts with a required key-member, so EQL renders it as an inner join.
    /// Audit-prop records whose audit-entity record no longer exists are therefore invisible to this count.
    /// Detecting those requires [#assertNoOrphanedAuditProps()].
    ///
    private int countAuditProps(final Long auditedEntityId) {
        return auditTypeFinder.navigate(AuditedEntity.class)
                .allAuditPropTypes()
                .stream()
                .mapToInt(auditPropType -> co(auditPropType).count(
                        select(auditPropType).where().prop(AbstractAuditProp.PATH_TO_AUDITED_ENTITY).eq().val(auditedEntityId).model()))
                .sum();
    }

    /// Asserts that no audit-prop record is orphaned, that is, that every audit-prop record still references an existing audit-entity record.
    ///
    /// Deleting audit-entity records without first deleting their audit-prop records would orphan the latter, and [#countAuditProps(Long)] cannot detect this, because its inner join silently drops orphans instead of revealing them.
    /// This assertion matches audit-prop records on their own [AbstractAuditProp#AUDIT_ENTITY] property, so no join is involved and orphans are visible.
    ///
    private void assertNoOrphanedAuditProps() {
        final var auditTypes = auditTypeFinder.navigate(AuditedEntity.class);
        for (final var auditPropType : auditTypes.allAuditPropTypes()) {
            final var auditEntityType = auditTypes.auditEntityType(AuditUtils.getAuditTypeVersion(auditPropType));
            final int orphanCount = co(auditPropType).count(
                    select(auditPropType).where()
                            .prop(AbstractAuditProp.AUDIT_ENTITY).notIn().model(select(auditEntityType).model())
                            .model());
            assertEquals("Orphaned audit-prop records in [%s].".formatted(auditPropType.getSimpleName()), 0, orphanCount);
        }
    }

}
