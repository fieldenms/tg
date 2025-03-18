package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.CHANGED_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchNone;

public class AuditingTest extends AbstractDaoTestCase {

    private Class<AbstractSynAuditEntity<TgVehicle>> tgVehicleSynAuditType;
    private ISynAuditEntityDao<TgVehicle> coTgVehicleAudit;
    private Class<AbstractSynAuditProp<TgVehicle>> tgVehicleSynAuditPropType;

    @Inject
    void setAuditTypeFinder(final IAuditTypeFinder auditTypeFinder) {
        tgVehicleSynAuditType = auditTypeFinder.getSynAuditEntityType(TgVehicle.class);
        coTgVehicleAudit = co(tgVehicleSynAuditType);
        tgVehicleSynAuditPropType = auditTypeFinder.getSynAuditPropTypeForSynAuditEntity(tgVehicleSynAuditType);
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

        assertEquals(car2, car2_a3t.getAuditedEntity());
        assertEquals(car2.getVersion(), car2_a3t.getAuditedVersion());
        assertEquals(car2.getKey(), car2_a3t.getA3t("key"));
        assertEquals(car2.getInitDate(), car2_a3t.getA3t("initDate"));
        assertEquals(car2.getReplacedBy(), car2_a3t.getA3t("replacedBy"));
        assertEquals(car2.getStation(), car2_a3t.getA3t("station"));
        assertEquals(car2.getModel(), car2_a3t.getA3t("model"));
        assertEquals(car2.getPrice(), car2_a3t.getA3t("price"));
        assertEquals(car2.getPurchasePrice(), car2_a3t.getA3t("purchasePrice"));
        assertEquals(car2.getActive(), car2_a3t.getA3t("active"));
        assertEquals(car2.getLeased(), car2_a3t.getA3t("leased"));
        assertEquals(car2.getLastMeterReading(), car2_a3t.getA3t("lastMeterReading"));

        assertNotNull(car2_a3t.getAuditDate());
        assertNotNull(car2_a3t.getUser());
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

        assertEquals(car1, car1_a3t.getAuditedEntity());
        assertEquals(car1.getVersion(), car1_a3t.getAuditedVersion());
        assertEquals(car1.getKey(), car1_a3t.getA3t("key"));
        assertEquals(car1.getInitDate(), car1_a3t.getA3t("initDate"));
        assertEquals(car1.getReplacedBy(), car1_a3t.getA3t("replacedBy"));
        assertEquals(car1.getStation(), car1_a3t.getA3t("station"));
        assertEquals(car1.getModel(), car1_a3t.getA3t("model"));
        assertEquals(car1.getPrice(), car1_a3t.getA3t("price"));
        assertEquals(car1.getPurchasePrice(), car1_a3t.getA3t("purchasePrice"));
        assertEquals(car1.getActive(), car1_a3t.getA3t("active"));
        assertEquals(car1.getLeased(), car1_a3t.getA3t("leased"));
        assertEquals(car1.getLastMeterReading(), car1_a3t.getA3t("lastMeterReading"));

        assertNotNull(car1_a3t.getAuditDate());
        assertNotNull(car1_a3t.getUser());
        assertNotNull(car1_a3t.getAuditedTransactionGuid());
    }

    @Test
    public void only_changed_properties_with_nonnull_values_are_audited_when_new_TgVehicle_is_saved() {
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
        final var expectedChangedPropNames = Stream.of("key", "initDate", "model", "price", "purchasePrice", "active", "leased")
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

}
