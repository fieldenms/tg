package ua.com.fielden.platform.web.audit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.AbstractSynAuditEntity;
import ua.com.fielden.platform.audit.IAuditTypeFinder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.meta.PropertyMetadata;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.test.server.config.StandardActions;

import java.math.BigDecimal;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.audit.AbstractSynAuditEntity.*;
import static ua.com.fielden.platform.entity.meta.PropertyDescriptor.pdTypeFor;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.AUDIT_PROPERTY;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isDate;
import static ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder.centreFor;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.*;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.EXPORT_EMBEDDED_CENTRE_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardScrollingConfigs.standardEmbeddedScrollingConfig;
import static ua.com.fielden.platform.web.test.server.config.StandardScrollingConfigs.standardStandaloneScrollingConfig;

final class AuditWebUiConfigFactory implements IAuditWebUiConfigFactory {

    private static final Logger LOGGER = getLogger();

    private final IAuditTypeFinder auditTypeFinder;
    private final MiTypeGenerator miTypeGenerator;
    private final Injector injector;
    private final IDomainMetadata domainMetadata;

    @Inject
    AuditWebUiConfigFactory(final IAuditTypeFinder auditTypeFinder, final MiTypeGenerator miTypeGenerator,
                            final Injector injector, final IDomainMetadata domainMetadata) {
        this.auditTypeFinder = auditTypeFinder;
        this.miTypeGenerator = miTypeGenerator;
        this.injector = injector;
        this.domainMetadata = domainMetadata;
    }

    @Override
    public AuditWebUiConfig create(final Class<? extends AbstractEntity<?>> auditedType, final IWebUiBuilder builder) {
        // Must exist
        final var synAuditType = auditTypeFinder.navigate(auditedType).synAuditEntityType();
        final var miType = miTypeGenerator.generate(synAuditType);

        final var centre = createCentre(auditedType, synAuditType, miType);
        builder.register(centre);

        return new AuditWebUiConfig(centre, synAuditType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public EntityCentre<?> createEmbeddedCentre(final Class<? extends AbstractEntity<?>> auditedType) {
        final var baseAuditedType = baseEntityType(auditedType);
        return new EntityCentre<>(miTypeForEmbeddedCentre(auditedType),
                                  createEmbeddedCentreConfig(auditTypeFinder.navigate(baseAuditedType).synAuditEntityType()),
                                  injector);
    }

    <T extends AbstractSynAuditEntity<?>> EntityCentreConfig<T> createEmbeddedCentreConfig(final Class<T> synAuditType) {
        final var auditProperties = domainMetadata.forEntity(synAuditType)
                .properties()
                .stream()
                .filter(p -> p.has(AUDIT_PROPERTY))
                .collect(toImmutableList());
        final var critAuditProperties = auditProperties.stream().filter(AuditWebUiConfigFactory::canBeCrit).toList();

        final var layout = LayoutComposer.mkGridForCentre(4 + critAuditProperties.size(), 1);

        final EntityActionConfig standardExportAction = EXPORT_EMBEDDED_CENTRE_ACTION.mkAction(synAuditType);
        final EntityActionConfig standardSortAction = CUSTOMISE_COLUMNS_ACTION.mkAction();

        IAlsoCrit<T> centreBuilder1 = centreFor(synAuditType)
                .runAutomatically()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)

                .addCrit(AUDIT_DATE).asRange().dateTime().also()
                .addCrit(AUDIT_USER).asMulti().autocompleter(User.class).also()
                .addCrit(AUDITED_VERSION).asRange().integer().also()
                .addCrit(CHANGED_PROPS_CRIT).asMulti().autocompleter(pdTypeFor(synAuditType));

        for (final var prop : critAuditProperties) {
            final var result = addAuditPropertyAsCrit(centreBuilder1.also(), prop);
            centreBuilder1 = result == null ? centreBuilder1 : result;
        }

        var centreBuilder2 = centreBuilder1
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .withScrollingConfig(standardEmbeddedScrollingConfig(1))

                // Order by version, which resembles entity history more accurately than audit date.
                .addProp(AUDITED_VERSION).order(1).desc().minWidth(50)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching audit records.").also()
                .addProp(AUDIT_DATE).minWidth(150).also()
                .addProp(AUDIT_USER).minWidth(80).also()
                .addProp(CHANGED_PROPS).minWidth(120);

        for (final var prop : auditProperties) {
            centreBuilder2 = centreBuilder2.also().addProp(prop.name());
        }

        return centreBuilder2
                .setRenderingCustomiser(AuditEntityRenderingCustomiser.class)
                .setQueryEnhancer(AuditEntityQueryEnhancer.klass(), context().withMasterEntity().build())
                .build();
    }

    private EntityCentre<?> createCentre(
            final Class<? extends AbstractEntity<?>> auditedType,
            final Class<? extends AbstractSynAuditEntity<?>> synAuditType,
            final Class<? extends MiWithConfigurationSupport<?>> miType)
    {
        return new EntityCentre<>(miType, createCentreConfig(auditedType, synAuditType), injector);
    }

    <T extends AbstractSynAuditEntity<?>> EntityCentreConfig<T> createCentreConfig(
            final Class<? extends AbstractEntity<?>> auditedType,
            final Class<T> synAuditType)
    {
        final var standardExportAction = StandardActions.EXPORT_ACTION.mkAction(synAuditType);
        final var standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final var auditProperties = domainMetadata.forEntity(synAuditType)
                .properties()
                .stream()
                .filter(p -> p.has(AUDIT_PROPERTY))
                .collect(toImmutableList());
        final var critAuditProperties = auditProperties.stream().filter(AuditWebUiConfigFactory::canBeCrit).toList();

        final var layout = LayoutComposer.mkGridForCentre(5 + critAuditProperties.size(), 1);

        IAlsoCrit<T> centreBuilder1 = EntityCentreBuilder.centreFor(synAuditType)
                .addTopAction(standardExportAction).also()
                .addTopAction(standardSortAction)

                .addCrit(AUDITED_ENTITY).asMulti().autocompleter(auditedType).also()
                .addCrit(AUDIT_DATE).asRange().dateTime().also()
                .addCrit(AUDIT_USER).asMulti().autocompleter(User.class).also()
                .addCrit(AUDITED_VERSION).asRange().integer().also()
                .addCrit(CHANGED_PROPS_CRIT).asMulti().autocompleter(pdTypeFor(synAuditType));

        for (final var prop : critAuditProperties) {
            final var result = addAuditPropertyAsCrit(centreBuilder1.also(), prop);
            centreBuilder1 = result == null ? centreBuilder1 : result;
        }

        var centreBuilder2 = centreBuilder1
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .withScrollingConfig(standardStandaloneScrollingConfig(3))

                .addProp(AUDIT_DATE).order(1).desc().minWidth(150)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching audit records.").also()
                // Order by audit date, since this is a top-level centre that includes all audit records.
                // Moreover, there is no standalone index for auditedVersion to make such an ordering performant.
                .addProp(AUDITED_ENTITY).order(2).desc().minWidth(140).also()
                .addProp(AUDIT_USER).minWidth(80).also()
                .addProp(AUDITED_VERSION).minWidth(50).also()
                .addProp(CHANGED_PROPS).minWidth(120);

        for (final var prop : auditProperties) {
            centreBuilder2 = centreBuilder2.also().addProp(prop.name());
        }

        return centreBuilder2.setRenderingCustomiser(AuditEntityRenderingCustomiser.class).build();
    }

    /// Keep types in sync with [#addAuditPropertyAsCrit].
    ///
    private static boolean canBeCrit(final PropertyMetadata property) {
        class $ {
            static final Set<Class<?>> CRIT_TYPES = Set.of(Integer.class, Long.class, BigDecimal.class, String.class, boolean.class, Money.class, RichText.class);
        }
        final var type = property.type();
        return type.isEntity()
               || isDate(type.javaType())
               || $.CRIT_TYPES.contains(type.javaType());
    }

    /// Keep types in sync with [#canBeCrit].
    ///
    private static <T extends AbstractEntity<?>> @Nullable IAlsoCrit<T> addAuditPropertyAsCrit(
            final ISelectionCriteriaBuilder<T> builder,
            final PropertyMetadata property)
    {
        final var propType = property.type();

        if (propType.isEntity()) {
            return builder.addCrit(property.name()).asMulti().autocompleter(propType.asEntity().orElseThrow().javaType());
        }
        else if (isDate(propType.javaType())) {
            return builder.addCrit(property.name()).asRange().dateTime();
        }
        else if (propType.javaType() == Integer.class || propType.javaType() == Long.class) {
            return builder.addCrit(property.name()).asRange().integer();
        }
        else if (propType.javaType() == BigDecimal.class) {
            return builder.addCrit(property.name()).asRange().decimal();
        }
        else if (propType.javaType() == String.class) {
            return builder.addCrit(property.name()).asMulti().text();
        }
        else if (propType.javaType() == boolean.class) {
            return builder.addCrit(property.name()).asMulti().bool();
        }
        else if (propType.javaType() == Money.class) {
            return builder.addCrit(property.name()).asRange().decimal();
        }
        else if (propType.javaType() == RichText.class) {
            return builder.addCrit(property.name()).asMulti().text();
        }
        else {
            LOGGER.warn(() -> format("Audit-property [%s] will not be added to centre criteria as its type [%s] is not among supported criteria types.",
                                     property.name(), property.type().genericJavaType().getTypeName()));
            return null;
        }
    }

    @Override
    public Class<MiWithConfigurationSupport<?>> miTypeForEmbeddedCentre(final Class<? extends AbstractEntity<?>> auditedType) {
        final var baseAuditedType = baseEntityType(auditedType);
        final var synAuditType = auditTypeFinder.navigate(baseAuditedType).synAuditEntityType();
        // TODO: The Mi naming convention should be captured in a standalone utility class.
        final var miType = miTypeGenerator.generate("Mi%sMaster_%s".formatted(baseAuditedType.getSimpleName(), synAuditType.getSimpleName()),
                                                    synAuditType);
        return (Class) miType;
    }

}
