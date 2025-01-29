package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static ua.com.fielden.platform.audit.AbstractAuditEntity.CHANGED_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

public class AuditingTest extends AbstractDaoTestCase {

    private Class<AbstractAuditEntity<TgVehicle>> tgVehicleAuditType;
    private Class<AbstractSynAuditEntity<TgVehicle>> tgVehicleSynAuditType;
    private IAuditEntityDao<TgVehicle, AbstractAuditEntity<TgVehicle>> coTgVehicleAudit;
    private Class<AbstractAuditProp<AbstractAuditEntity<TgVehicle>>> tgVehicleAuditPropType;

    @Inject
    void setAuditTypeFinder(final IAuditTypeFinder auditTypeFinder) {
        tgVehicleAuditType = auditTypeFinder.getAuditEntityType(TgVehicle.class);
        tgVehicleSynAuditType = auditTypeFinder.getSynAuditEntityType(TgVehicle.class);
        coTgVehicleAudit = co(tgVehicleAuditType);
        tgVehicleAuditPropType = auditTypeFinder.getAuditPropTypeForAuditEntity(tgVehicleAuditType);
    }

    @Test
    public void audit_record_is_created_when_new_TgVehicle_is_saved() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var vehicle = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var vehicleAudit = coTgVehicleAudit.getAuditOrThrow(vehicle, fetchAll(tgVehicleAuditType));

        assertEquals(vehicle, vehicleAudit.getAuditedEntity());
        assertEquals(vehicle.getVersion(), vehicleAudit.getAuditedVersion());
        assertEquals(vehicle.getKey(), vehicleAudit.getA3t("key"));
        assertEquals(vehicle.getInitDate(), vehicleAudit.getA3t("initDate"));
        assertEquals(vehicle.getReplacedBy(), vehicleAudit.getA3t("replacedBy"));
        assertEquals(vehicle.getStation(), vehicleAudit.getA3t("station"));
        assertEquals(vehicle.getModel(), vehicleAudit.getA3t("model"));
        assertEquals(vehicle.getPrice(), vehicleAudit.getA3t("price"));
        assertEquals(vehicle.getPurchasePrice(), vehicleAudit.getA3t("purchasePrice"));
        assertEquals(vehicle.getActive(), vehicleAudit.getA3t("active"));
        assertEquals(vehicle.getLeased(), vehicleAudit.getA3t("leased"));
        assertEquals(vehicle.getLastMeterReading(), vehicleAudit.getA3t("lastMeterReading"));

        assertNotNull(vehicleAudit.getAuditDate());
        assertNotNull(vehicleAudit.getUser());
        assertNotNull(vehicleAudit.getAuditedTransactionGuid());
    }

    @Test
    public void only_changed_properties_with_nonnull_values_are_audited_when_new_TgVehicle_is_saved() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var vehicle = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var vehicleAudit = coTgVehicleAudit.getAuditOrThrow(vehicle, fetchAll(tgVehicleAuditType).with(CHANGED_PROPS, fetchAll(tgVehicleAuditPropType)));

        final var changedProps = vehicleAudit.getChangedProps();
        final var expectedChangedPropNames = Stream.of("key", "initDate", "model", "price", "purchasePrice", "active", "leased")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());
        assertEquals(expectedChangedPropNames,
                     changedProps.stream().map(AbstractAuditProp::getProperty).map(PropertyDescriptor::getPropertyName).collect(toSet()));

        for (final var changedProp : changedProps) {
            assertEquals("Incorrect audit-entity referenced by changed property [%s].".formatted(changedProp),
                         vehicleAudit, changedProp.getAuditEntity());
            assertEquals("Incorrect entity type referenced by property descriptor of changed property [%s]".formatted(changedProp),
                         tgVehicleSynAuditType, changedProp.getProperty().getEntityType());
        }
    }

    @Test
    public void all_changed_properties_are_audited_when_persisted_TgVehicle_is_saved() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var vehicle = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var m2 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m2.key);
        final var modVehicle = save(vehicle.setPrice(Money.of("100")).setLeased(true).setModel(m2).setInitDate(null));

        final var vehicleAudit = coTgVehicleAudit.getAuditOrThrow(modVehicle, fetchAll(tgVehicleAuditType).with(CHANGED_PROPS, fetchAll(tgVehicleAuditPropType)));
        final var changedProps = vehicleAudit.getChangedProps();
        final var expectedChangedPropNames = Stream.of("price", "leased", "model", "initDate")
                .map(AuditUtils::auditPropertyName)
                .collect(toSet());
        assertEquals(expectedChangedPropNames,
                     changedProps.stream().map(AbstractAuditProp::getProperty).map(PropertyDescriptor::getPropertyName).collect(toSet()));
    }

    @Test
    public void audit_prop_entities_reference_the_audit_entity_that_recorded_the_change() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var vehicle = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var vehicleAudit = coTgVehicleAudit.getAuditOrThrow(vehicle, fetchAll(tgVehicleAuditType).with(CHANGED_PROPS, fetchAll(tgVehicleAuditPropType)));

        for (final var changedProp : vehicleAudit.getChangedProps()) {
            assertEquals("Incorrect audit-entity referenced by changed property [%s].".formatted(changedProp),
                         vehicleAudit, changedProp.getAuditEntity());
        }
    }

    @Test
    public void property_descriptors_of_audit_prop_entities_reference_the_synthetic_audit_entity_type() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var vehicle = save(new_(TgVehicle.class, "CAR2").
                                         setInitDate(date("2001-01-01 00:00:00")).
                                         setModel(m1).
                                         setPrice(Money.of("20")).
                                         setPurchasePrice(Money.of("10")).
                                         setActive(true).
                                         setLeased(false));

        final var vehicleAudit = coTgVehicleAudit.getAuditOrThrow(vehicle, fetchAll(tgVehicleAuditType).with(CHANGED_PROPS, fetchAll(tgVehicleAuditPropType)));

        for (final var changedProp : vehicleAudit.getChangedProps()) {
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
