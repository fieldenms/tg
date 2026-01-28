package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.apache.commons.text.RandomStringGenerator;
import org.junit.Test;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.IDates;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.CHANGED_PROPS;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.LAST_UPDATED_BY;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;

public class AuditingTest extends AbstractDaoTestCase {

    private @Inject IDates dates;
    private IAuditTypeFinder auditTypeFinder;
    private Class<AbstractSynAuditEntity<TgVehicle>> tgVehicleSynAuditType;
    private ISynAuditEntityDao<TgVehicle> coTgVehicleAudit;
    private Class<AbstractSynAuditProp<TgVehicle>> tgVehicleSynAuditPropType;
    private final AuditAssertions a3t_assertions = getInstance(AuditAssertions.class);

    @Inject
    void setAuditTypeFinder(final IAuditTypeFinder auditTypeFinder) {
        this.auditTypeFinder = auditTypeFinder;
        tgVehicleSynAuditType = auditTypeFinder.navigate(TgVehicle.class).synAuditEntityType();
        coTgVehicleAudit = co(tgVehicleSynAuditType);
        tgVehicleSynAuditPropType = auditTypeFinder.navigate(TgVehicle.class).synAuditPropType();
    }

    @Test
    public void audit_record_is_created_when_new_TgVehicle_is_saved() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car2 = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var car2_a3t = coTgVehicleAudit.getAuditOrThrow(car2, fetchAll(tgVehicleSynAuditType));

        a3t_assertions.assertThat(car2_a3t).isAuditFor(car2);

        assertNotNull(car2_a3t.getAuditDate());
        assertNotNull(car2_a3t.getAuditUser());
        assertNotNull(car2_a3t.getAuditedTransactionGuid());
    }

    @Test
    public void values_for_proxied_audited_properties_are_refetched_to_create_an_audit_record() {
        // Fetch essential properties for saving and `price` to be modified.
        final var car1WithProxies = co$(TgVehicle.class)
                .findByKeyAndFetch(fetchNone(TgVehicle.class).with("id", "version", "key", "price"),
                                   TgVehicles.CAR1.key);
        final var car1WithProxiesSaved = save(car1WithProxies.setPrice(car1WithProxies.getPrice().plus(Money.ONE)));

        final var car1_a3t = coTgVehicleAudit.getAuditOrThrow(car1WithProxiesSaved, fetchAll(tgVehicleSynAuditType));
        final var car1 = co(TgVehicle.class).findByKeyAndFetch(fetchAll(TgVehicle.class), TgVehicles.CAR1.key);

        assertEquals(car1WithProxiesSaved.getPrice(), car1.getPrice());

        a3t_assertions.assertThat(car1_a3t).isAuditFor(car1);

        assertNotNull(car1_a3t.getAuditDate());
        assertNotNull(car1_a3t.getAuditUser());
        assertNotNull(car1_a3t.getAuditedTransactionGuid());
    }

    @Test
    public void all_properties_are_audited_when_new_TgVehicle_is_saved() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car2 = save(new_(TgVehicle.class, "CAR2").
                                      setInitDate(date("2001-01-01 00:00:00")).
                                      setReplacedBy(null).
                                      setStation(null).
                                      setModel(m1).
                                      setPrice(Money.of("20")).
                                      setPurchasePrice(Money.of("10")).
                                      setActive(true).
                                      setLeased(false));

        final var car2_a3t = coTgVehicleAudit.getAuditOrThrow(car2, fetchAll(coTgVehicleAudit.getEntityType())
                .with(CHANGED_PROPS, fetchAll(tgVehicleSynAuditPropType)));

        final var changedProps = car2_a3t.getChangedProps();
        final var expectedChangedPropNames = Stream.of("key", "desc", "initDate", "model", "price", "purchasePrice", "active", "leased", "station", "replacedBy", "lastMeterReading")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());
        assertEquals(expectedChangedPropNames,
                     changedProps.stream().map(p -> p.getProperty().getPropertyName()).collect(toSet()));
    }

    @Test
    public void all_changed_properties_are_audited_when_persisted_TgVehicle_is_saved() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car2 = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var m2 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m2.key);
        final var car2Saved = save(car2.setPrice(Money.of("100")).setLeased(true).setModel(m2).setInitDate(null));

        final var car2Saved_a3t = coTgVehicleAudit.getAuditOrThrow(car2Saved, fetchAll(coTgVehicleAudit.getEntityType())
                .with(CHANGED_PROPS, fetchAll(tgVehicleSynAuditPropType)));
        final var expectedChangedPropNames = Stream.of("price", "leased", "model", "initDate")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());
        assertEquals(expectedChangedPropNames,
                     car2Saved_a3t.getChangedProps().stream().map(p -> p.getProperty().getPropertyName()).collect(toSet()));
    }

    @Test
    public void audit_prop_entities_reference_the_audit_entity_that_recorded_the_change() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car2 = save(new_(TgVehicle.class, "CAR2").
                                      setInitDate(date("2001-01-01 00:00:00")).
                                      setModel(m1).
                                      setPrice(Money.of("20")).
                                      setPurchasePrice(Money.of("10")).
                                      setActive(true).
                                      setLeased(false));

        final var car2_a3t = coTgVehicleAudit.getAuditOrThrow(car2, fetchAll(coTgVehicleAudit.getEntityType())
                .with(CHANGED_PROPS, fetchAll(tgVehicleSynAuditPropType)));

        for (final var changedProp : car2_a3t.getChangedProps()) {
            assertEquals("Incorrect audit-entity referenced by changed property [%s].".formatted(changedProp),
                         car2_a3t, changedProp.getAuditEntity());
        }
    }

    @Test
    public void property_descriptors_of_audit_prop_entities_reference_the_synthetic_audit_entity_type() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car2 = save(new_(TgVehicle.class, "CAR2").
                                      setInitDate(date("2001-01-01 00:00:00")).
                                      setModel(m1).
                                      setPrice(Money.of("20")).
                                      setPurchasePrice(Money.of("10")).
                                      setActive(true).
                                      setLeased(false));

        final var car2_a3t = coTgVehicleAudit.getAuditOrThrow(car2, fetchAll(coTgVehicleAudit.getEntityType())
                .with(CHANGED_PROPS, fetchAll(tgVehicleSynAuditPropType)));

        for (final var changedProp : car2_a3t.getChangedProps()) {
            assertEquals("Incorrect entity type referenced by property descriptor of changed property [%s]".formatted(changedProp),
                         tgVehicleSynAuditType, changedProp.getProperty().getEntityType());
        }
    }

    @Test
    public void audit_record_is_not_created_if_none_of_audited_properties_were_changed() {
        final var entity = save(new_(AuditedEntity.class, "A"));
        final ISynAuditEntityDao<AuditedEntity> coAudit = co(auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType());

        assertThat(coAudit.getAudits(entity))
                .extracting(AbstractSynAuditEntity::getAuditedVersion)
                .containsExactlyInAnyOrder(0L);

        final IUser userCo = co$(User.class);
        final var anotherUser = userCo.findUser(User.system_users.UNIT_TEST_USER.name());
        assertNotNull(anotherUser);
        assertNotEquals(entity.get(LAST_UPDATED_BY), anotherUser);
        entity.set(LAST_UPDATED_BY, anotherUser);
        save(entity);

        assertThat(coAudit.getAudits(entity))
                .extracting(AbstractSynAuditEntity::getAuditedVersion)
                .containsExactlyInAnyOrder(0L);
    }

    @Test
    public void union_typed_properties_are_audited() {
        final ISynAuditEntityDao<AuditedEntity> coAudit = co(auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType());

        final AuditedEntity entity;

        {
            final var union = save(new_(UnionEntity.class).setPropertyOne(save(new_(EntityOne.class, "One"))));
            entity = save(new_(AuditedEntity.class, "A").setUnion(union));

            final var audit = coAudit.getAudit(entity);
            a3t_assertions.assertThat(audit)
                    .auditPropertySatisfies(AuditedEntity.Property.union, union_a3t -> assertEquals(union, union_a3t));
        }

        {
            final var union = save(new_(UnionEntity.class).setPropertyTwo(save(new_(EntityTwo.class, "2"))));
            final var entity_v2 = save(entity.setUnion(union));

            final var audit = coAudit.getAudit(entity_v2);
            a3t_assertions.assertThat(audit)
                    .auditPropertySatisfies(AuditedEntity.Property.union, union_a3t -> assertEquals(union, union_a3t));
        }
    }

    @Test
    public void component_typed_properties_are_audited() {
        final ISynAuditEntityDao<AuditedEntity> coAudit = co(auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType());

        final AuditedEntity entity;

        {
            final var richText = RichText.fromPlainText("First version");
            entity = save(new_(AuditedEntity.class, "A").setRichText(richText));

            final var audit = coAudit.getAudit(entity);
            a3t_assertions.assertThat(audit)
                    .auditPropertySatisfies(AuditedEntity.Property.richText, richText_a3t -> assertEquals(richText, richText_a3t));
        }

        {
            final var richText = RichText.fromPlainText("Second version");
            final var entity_v2 = save(entity.setRichText(richText));

            final var audit = coAudit.getAudit(entity_v2);
            a3t_assertions.assertThat(audit)
                    .auditPropertySatisfies(AuditedEntity.Property.richText, richText_a3t -> assertEquals(richText, richText_a3t));
        }
    }

    @Test
    public void invalid_entities_can_be_audited() {
        final ISynAuditEntityDao<AuditedEntity> coAudit = co(auditTypeFinder.navigate(AuditedEntity.class).synAuditEntityType());

        final var newEntity = new_(AuditedEntity.class, "A").setInvalidate(true);
        newEntity.getProperty(AuditedEntity.Property.invalidate).setDomainValidationResult(Result.successful());
        final var savedEntity = save(newEntity);
        assertFalse(savedEntity.isValid().isSuccessful());

        assertThat(coAudit.getAudits(savedEntity))
                .hasSize(1)
                .element(0).satisfies(audit0 -> a3t_assertions.assertThat(audit0).isAuditFor(savedEntity));
    }

    /// Performs the auditing operation in a session, as described in [IEntityAuditor#audit(AbstractEntity, String, Collection)].
    @SessionRequired
    protected <E extends AbstractEntity<?>> void audit(
            final ISynAuditEntityDao<E> coAudit,
            final Long auditedEntityId,
            final Long auditedEntityVersion,
            final String transactionGuid,
            final Collection<String> dirtyProperties)
    {
        coAudit.audit(auditedEntityId, auditedEntityVersion, transactionGuid, dirtyProperties);
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final var merc = save(new_(TgVehicleMake.class, TgVehicleMakes.MERC.key));
        final var audi = save(new_(TgVehicleMake.class, TgVehicleMakes.AUDI.key));

        final var m1 = save(new_(TgVehicleModel.class, TgVehicleModels.m1.key).setMake(merc));
        final var m2 = save(new_(TgVehicleModel.class, TgVehicleModels.m2.key).setMake(merc));

        save(new_(TgVehicle.class, TgVehicles.CAR1.key).
                     setInitDate(date("2001-01-01 00:00:00")).
                     setModel(m1).
                     setPrice(Money.of("100")).
                     setPurchasePrice(Money.of("200")).
                     setActive(true).
                     setLeased(false));
    }

    private enum TgVehicleMakes {
        MERC ("MERC"),
        AUDI ("AUDI");

        private final String key;

        TgVehicleMakes(final String key) {
            this.key = key;
        }
    }

    private enum TgVehicleModels {
        m1 ("m1"),
        m2 ("m2");

        private final String key;

        TgVehicleModels(final String key) {
            this.key = key;
        }
    }

    private enum TgVehicles {
        CAR1 ("CAR1");

        private final String key;

        TgVehicles(final String key) {
            this.key = key;
        }
    }

    private static String generateTransactionGuid() {
        class $ {
            static final RandomStringGenerator G = RandomStringGenerator.builder().withinRange('a', 'z').build();
        }

        return $.G.generate(32);
    }

}
