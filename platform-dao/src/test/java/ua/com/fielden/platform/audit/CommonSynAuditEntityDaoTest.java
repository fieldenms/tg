package ua.com.fielden.platform.audit;

import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.util.List;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;

public class CommonSynAuditEntityDaoTest extends AbstractDaoTestCase {

    private ISynAuditEntityDao<TgVehicle> coTgVehicleAudit;
    private final AuditAssertions auditAssertions = getInstance(AuditAssertions.class);

    @Inject
    protected void init(final IAuditTypeFinder auditTypeFinder) {
        coTgVehicleAudit = co(auditTypeFinder.navigate(TgVehicle.class).synAuditEntityType());
    }

    @Test
    public void streamAudits_streams_all_audit_records_for_an_entity() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final List<AbstractSynAuditEntity<TgVehicle>> car_a3ts_0;
        try (final var stream = coTgVehicleAudit.streamAudits(car1_v0)) {
            car_a3ts_0 = stream
                    .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                    .toList();
        }

        assertThat(car_a3ts_0)
                .zipSatisfy(List.of(car1_v0),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));

        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));

        final List<AbstractSynAuditEntity<TgVehicle>> car_a3ts_1;
        try (final var stream = coTgVehicleAudit.streamAudits(car1_v1)) {
            car_a3ts_1 = stream
                    .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                    .toList();
        }

        assertThat(car_a3ts_1)
                .zipSatisfy(List.of(car1_v0, car1_v1),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));
    }

    @Test
    public void streamAudits_by_id_streams_all_audit_records_for_an_entity() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final List<AbstractSynAuditEntity<TgVehicle>> car_a3ts_0;
        try (final var stream = coTgVehicleAudit.streamAudits(car1_v0.getId())) {
            car_a3ts_0 = stream
                    .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                    .toList();
        }

        assertThat(car_a3ts_0)
                .zipSatisfy(List.of(car1_v0),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));

        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));

        final List<AbstractSynAuditEntity<TgVehicle>> car_a3ts_1;
        try (final var stream = coTgVehicleAudit.streamAudits(car1_v1.getId())) {
            car_a3ts_1 = stream
                    .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                    .toList();
        }

        assertThat(car_a3ts_1)
                .zipSatisfy(List.of(car1_v0, car1_v1),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));
    }

    @Test
    public void streamAudits_by_id_returns_no_results_if_there_is_no_entity_with_specified_id() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final List<AbstractSynAuditEntity<TgVehicle>> car_a3ts_0;
        try (final var stream = coTgVehicleAudit.streamAudits(0L)) {
            car_a3ts_0 = stream
                    .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                    .toList();
        }

        assertThat(car_a3ts_0).isEmpty();
    }

    @Test
    public void getAudits_returns_all_audit_records_for_an_entity() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final var car_a3ts_0 = coTgVehicleAudit.getAudits(car1_v0)
                .stream()
                .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                .toList();

        assertThat(car_a3ts_0)
                .zipSatisfy(List.of(car1_v0),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));

        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));

        final var car_a3ts_1 = coTgVehicleAudit.getAudits(car1_v1)
                .stream()
                .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                .toList();

        assertThat(car_a3ts_1)
                .zipSatisfy(List.of(car1_v0, car1_v1),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));
    }

    @Test
    public void getAudits_by_id_returns_all_audit_records_for_an_entity() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final var car_a3ts_0 = coTgVehicleAudit.getAudits(car1_v0.getId())
                .stream()
                .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                .toList();

        assertThat(car_a3ts_0)
                .zipSatisfy(List.of(car1_v0),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));

        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));

        final var car_a3ts_1 = coTgVehicleAudit.getAudits(car1_v1.getId())
                .stream()
                .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                .toList();

        assertThat(car_a3ts_1)
                .zipSatisfy(List.of(car1_v0, car1_v1),
                            (a3t, car) -> auditAssertions.assertThat(a3t).isAuditFor(car));
    }

    @Test
    public void getAudits_by_id_returns_no_results_if_there_is_no_entity_with_specified_id() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final var car_a3ts_0 = coTgVehicleAudit.getAudits(0L)
                .stream()
                .sorted(comparing(AbstractSynAuditEntity::getAuditedVersion))
                .toList();

        assertThat(car_a3ts_0).isEmpty();
    }

    @Test
    public void getAudit_returns_the_last_audit_record_for_an_entity() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final var car1_v0_a3t = coTgVehicleAudit.getAudit(car1_v0);
        auditAssertions.assertThat(car1_v0_a3t).isAuditFor(car1_v0);

        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));
        final var car1_v1_a3t = coTgVehicleAudit.getAudit(car1_v1);
        auditAssertions.assertThat(car1_v1_a3t).isAuditFor(car1_v1).isNotAuditFor(car1_v0);
    }

    @Test
    public void getAudit_returns_null_if_there_are_not_audit_records_for_an_entity() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0_new = new_(TgVehicle.class, "CAR1").setModel(m1);

        assertThat(coTgVehicleAudit.getAudit(car1_v0_new)).isNull();
    }

    @Test
    public void getAudit_returns_null_if_specified_entity_has_version_greater_than_last_audited_version() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        final var car1_v1_new = car1_v0.copy(TgVehicle.class);
        car1_v1_new.set(VERSION, car1_v0.getVersion() + 1);

        assertThat(coTgVehicleAudit.getAudit(car1_v1_new)).isNull();
    }

    @Test
    public void getAudit_returns_an_audit_record_for_specified_entity_and_version_if_audit_record_exists() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));
        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));

        final var car1_v0_0_a3t = coTgVehicleAudit.getAudit(car1_v0, 0L);
        auditAssertions.assertThat(car1_v0_0_a3t).isAuditFor(car1_v0).isNotAuditFor(car1_v1);

        final var car1_v1_0_a3t = coTgVehicleAudit.getAudit(car1_v1, 0L);
        auditAssertions.assertThat(car1_v1_0_a3t).isAuditFor(car1_v0).isNotAuditFor(car1_v1);

        final var car1_v0_1_a3t = coTgVehicleAudit.getAudit(car1_v0, 1L);
        auditAssertions.assertThat(car1_v0_1_a3t).isAuditFor(car1_v1).isNotAuditFor(car1_v0);

        final var car1_v1_1_a3t = coTgVehicleAudit.getAudit(car1_v1, 1L);
        auditAssertions.assertThat(car1_v1_1_a3t).isAuditFor(car1_v1).isNotAuditFor(car1_v0);
    }

    @Test
    public void getAudit_returns_an_audit_record_for_specified_entity_id_and_version_if_audit_record_exists() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));
        final var car1_v1 = save(car1_v0.copy(TgVehicle.class).setPrice(Money.of("25")));

        assertThat(car1_v0.getId()).isEqualTo(car1_v1.getId());
        final var car1Id = car1_v0.getId();

        final var car1_v0_a3t = coTgVehicleAudit.getAudit(car1Id, 0L);
        auditAssertions.assertThat(car1_v0_a3t).isAuditFor(car1_v0).isNotAuditFor(car1_v1);

        final var car1_v1_a3t = coTgVehicleAudit.getAudit(car1Id, 1L);
        auditAssertions.assertThat(car1_v1_a3t).isAuditFor(car1_v1).isNotAuditFor(car1_v0);
    }

    @Test
    public void getAudit_returns_null_for_specified_entity_and_version_if_audit_record_does_not_exist() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));

        assertThat(coTgVehicleAudit.getAudit(car1_v0, 1L)).isNull();
        assertThat(coTgVehicleAudit.getAudit(car1_v0, 2L)).isNull();
    }

    @Test
    public void getAudit_returns_null_for_specified_entity_id_and_version_if_audit_record_does_not_exist() {
        final var m1 = co(TgVehicleModel.class).findByKey(TgVehicleModels.m1.key);
        final var car1_v0 = save(new_(TgVehicle.class, "CAR1").setModel(m1));
        final var car1Id = car1_v0.getId();

        assertThat(coTgVehicleAudit.getAudit(car1Id, 1L)).isNull();
        assertThat(coTgVehicleAudit.getAudit(car1Id, 2L)).isNull();
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final var merc = save(new_(TgVehicleMake.class, TgVehicleMakes.MERC.key));
        final var m1 = save(new_(TgVehicleModel.class, TgVehicleModels.m1.key).setMake(merc));
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

}
