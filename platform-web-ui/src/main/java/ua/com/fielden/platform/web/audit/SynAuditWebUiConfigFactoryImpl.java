package ua.com.fielden.platform.web.audit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.*;
import static ua.com.fielden.platform.audit.AuditUtils.isAuditProperty;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.pdTypeFor;
import static ua.com.fielden.platform.reflection.Finder.streamRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.CELL_LAYOUT;

final class SynAuditWebUiConfigFactoryImpl implements SynAuditWebUiConfigFactory {

    private final IAuditTypeFinder auditTypeFinder;
    private final MiTypeGenerator miTypeGenerator;
    private final Injector injector;

    @Inject
    SynAuditWebUiConfigFactoryImpl(final IAuditTypeFinder auditTypeFinder, final MiTypeGenerator miTypeGenerator,
                                   final Injector injector) {
        this.auditTypeFinder = auditTypeFinder;
        this.miTypeGenerator = miTypeGenerator;
        this.injector = injector;
    }

    @Override
    public <E extends AbstractEntity<?>> SynAuditWebUiConfig<E> create(
            final Class<E> auditedType,
            final IWebUiBuilder builder)
    {
        // Must exist
        final var synAuditType = auditTypeFinder.getSynAuditEntityType(auditedType);
        final var miType = miTypeGenerator.generate(synAuditType);

        final var centre = createCentre(auditedType, synAuditType, miType);
        builder.register(centre);

        return new SynAuditWebUiConfig<>(centre);
    }

    private <S extends AbstractSynAuditEntity<E>, E extends AbstractEntity<?>> EntityCentre<S> createCentre(
            final Class<E> auditedType,
            final Class<S> synAuditType,
            final Class<? extends MiWithConfigurationSupport<?>> miType)
    {
        // Cannot use IDomainMetadata as it resides in the dao module
        final List<Field> auditProperties = streamRealProperties(synAuditType)
                .filter(prop -> isAuditProperty(prop.getName()))
                .collect(toImmutableList());

        final var desktopLayout = cell(
                cell(CELL_LAYOUT).repeat(4)
                .cell(CELL_LAYOUT).repeat(auditProperties.size())
                )
                .toString();

        IAlsoCrit centreBuilder1 = EntityCentreBuilder.centreFor(synAuditType)
                .addCrit(AUDITED_ENTITY).asMulti().autocompleter(auditedType).also()
                .addCrit(AUDIT_DATE).asRange().dateTime().also()
                .addCrit(CHANGED_PROPS_CRIT).asMulti().autocompleter(pdTypeFor(synAuditType)).also()
                .addCrit(USER).asMulti().autocompleter(User.class);

        for (final Field prop : auditProperties) {
            final var result = addAuditPropertyAsCrit(centreBuilder1.also(), prop);
            centreBuilder1 = result == null ? centreBuilder1 : result;
        }

        var centreBuilder2 = centreBuilder1
                .setLayoutFor(DESKTOP, Optional.empty(), desktopLayout)

                .addProp(AUDITED_ENTITY)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching audit records.").also()
                .addProp(AUDIT_DATE).order(1).desc().also()
                .addProp(CHANGED_PROPS).minWidth(200).also()
                .addProp(USER);

        for (final Field prop : auditProperties) {
            centreBuilder2 = centreBuilder2.also().addProp(prop.getName());
        }

        final var ecc = centreBuilder2.build();

        return new EntityCentre<>(miType, ecc, injector);
    }

    private static @Nullable IAlsoCrit addAuditPropertyAsCrit(
            final ISelectionCriteriaBuilder builder,
            final Field property)
    {
        final var propType = property.getType();

        if (isEntityType(propType)) {
            return builder.addCrit(property.getName()).asMulti().autocompleter((Class<? extends AbstractEntity<?>>) propType);
        }
        else if (isDate(propType)) {
            return builder.addCrit(property.getName()).asRange().dateTime();
        }
        else if (propType == Integer.class || propType == Long.class) {
            return builder.addCrit(property.getName()).asRange().integer();
        }
        else if (propType == BigDecimal.class) {
            return builder.addCrit(property.getName()).asRange().decimal();
        }
        else if (propType == String.class) {
            return builder.addCrit(property.getName()).asMulti().text();
        }
        else if (propType == boolean.class) {
            return builder.addCrit(property.getName()).asMulti().bool();
        }
        else if (propType == Money.class) {
            return builder.addCrit(property.getName()).asRange().decimal();
        }
        else if (propType == RichText.class) {
            return builder.addCrit(property.getName()).asMulti().text();
        }
        else {
            return null;
        }
    }

}
