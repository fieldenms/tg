package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.proxy.IIdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.tiny.IActionIdentifier;
import ua.com.fielden.platform.tiny.TinyHyperlink;
import ua.com.fielden.platform.tiny.TinyHyperlinkCo;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.ICentreConfigSharingModel;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action1;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action2;
import ua.com.fielden.platform.web.resources.webui.test_entities.Action3;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.validation.EntityExistsValidator.ERR_ENTITY_WAS_NOT_FOUND;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.resources.webui.TinyHyperlinkResource.WARN_CONFIGURED_VALUE_COULD_NOT_BE_USED;

public class TinyHyperlinkEntityRestorationTest extends AbstractWebResourceWithDaoTestCase {

    @Inject private ICompanionObjectFinder companionFinder;
    @Inject private ICriteriaGenerator critGenerator;
    @Inject private IWebUiConfig webUiConfig;
    @Inject private IUserProvider userProvider;
    @Inject private EntityFactory entityFactory;
    @Inject private ICentreConfigSharingModel sharingModel;
    @Inject private ISerialiser serialiser;

    @Test
    public void entity_restored_from_tiny_hyperlink_has_exactly_the_same_property_values_that_were_specified_as_modified_when_tiny_hyperlink_was_created() {
        final var personName = save(new_(TgPersonName.class, "HP"));

        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final Map<CharSequence, Object> modifiedProperties = CollectionUtil.mapOf(
                t2(Action1.Properties.str1, "Hello"),
                t2(Action1.Properties.str2, ""),
                t2(Action1.Properties.bool, true),
                t2(Action1.Properties.nInt, 5),
                t2(Action1.Properties.nLong, 30L),
                t2(Action1.Properties.date1, date("2025-02-03")),
                t2(Action1.Properties.date2, null),
                t2(Action1.Properties.colour, Colour.WHITE),
                t2(Action1.Properties.hyperlink, new Hyperlink("https://www.fielden.com.au/")),
                t2(Action1.Properties.money, Money.of("25.25")),
                t2(Action1.Properties.bigDecimal, new BigDecimal("44.44")),
                t2(Action1.Properties.richText, RichText.fromHtml("<p>Hello</p>")),
                t2(Action1.Properties.personName, personName));
        final var tinyHyperlink = coTinyHyperlink.save(
                Action1.class,
                modifiedProperties,
                new CentreContextHolder(),
                IActionIdentifier.of("test"));

        TinyHyperlinkResource.restoreSharedEntity(tinyHyperlink, entityFactory, critGenerator, companionFinder, serialiser, webUiConfig, userProvider, DeviceProfile.DESKTOP, sharingModel)
                .run2(restoredEntity -> {
                    assertEquals(Action1.class, restoredEntity.getType());
                    final var restoredAction = (Action1) restoredEntity;
                    modifiedProperties.forEach((prop, val) -> assertEquals(val, restoredAction.get(prop.toString())));
                });
    }

    @Test
    public void entity_restoration_skips_non_existing_properties_that_were_specified_as_modified_when_tiny_hyperlink_was_created() {
        final var personName = save(new_(TgPersonName.class, "HP"));

        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final Map<CharSequence, Object> modifiedProperties = CollectionUtil.mapOf(
                t2(Action1.Properties.str1, "Hello"),
                t2(Action1.Properties.str2, ""),
                t2(Action1.Properties.bool, true),
                t2(Action1.Properties.nInt, 5),
                t2(Action1.Properties.nLong, 30L),
                t2(Action1.Properties.date1, date("2025-02-03")),
                t2(Action1.Properties.date2, null),
                t2(Action1.Properties.colour, Colour.WHITE),
                t2(Action1.Properties.hyperlink, new Hyperlink("https://www.fielden.com.au/")),
                t2(Action1.Properties.money, Money.of("25.25")),
                t2(Action1.Properties.bigDecimal, new BigDecimal("44.44")),
                t2(Action1.Properties.richText, RichText.fromHtml("<p>Hello</p>")),
                t2(Action1.Properties.personName, personName));
        final var tinyHyperlink = coTinyHyperlink.save(
                Action1.class,
                modifiedProperties,
                new CentreContextHolder(),
                IActionIdentifier.of("test"));
        final var tinyHyperlinkToRestore = save(copyWithEntityType(tinyHyperlink, Action2.class));
        assertEquals(Action2.class, ClassesRetriever.findClass(tinyHyperlinkToRestore.getEntityTypeName()));

        TinyHyperlinkResource.restoreSharedEntity(tinyHyperlinkToRestore, entityFactory, critGenerator, companionFinder, serialiser, webUiConfig, userProvider, DeviceProfile.DESKTOP, sharingModel)
                .run2(restoredEntity -> {
                    assertEquals(Action2.class, restoredEntity.getType());
                    for (final var prop : Action2.Properties.values()) {
                        if (modifiedProperties.containsKey(prop.name())) {
                            assertEquals(modifiedProperties.get(prop.name()), restoredEntity.get(prop.name()));
                        }
                    }
                });
    }

    @Test
    public void restoration_of_properties_whose_type_has_changed() {
        final var personName = save(new_(TgPersonName.class, "HP"));

        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final Map<CharSequence, Object> modifiedProperties = CollectionUtil.mapOf(
                t2(Action1.Properties.nInt, 10),
                t2(Action1.Properties.nLong, 42L),
                t2(Action1.Properties.tc1, personName),
                t2(Action1.Properties.tc2, "Hello"),
                t2(Action1.Properties.tc3, RichText.fromHtml("Hello")),
                t2(Action1.Properties.tc4, 50));
        final var tinyHyperlink = coTinyHyperlink.save(
                Action1.class,
                modifiedProperties,
                new CentreContextHolder(),
                IActionIdentifier.of("test"));
        final var tinyHyperlinkToRestore = save(copyWithEntityType(tinyHyperlink, Action2.class));
        assertEquals(Action2.class, ClassesRetriever.findClass(tinyHyperlinkToRestore.getEntityTypeName()));

        TinyHyperlinkResource.restoreSharedEntity(tinyHyperlinkToRestore, entityFactory, critGenerator, companionFinder, serialiser, webUiConfig, userProvider, DeviceProfile.DESKTOP, sharingModel)
                .run2(restoredEntity -> {
                    assertEquals(Action2.class, restoredEntity.getType());
                    final var action2 = (Action2) restoredEntity;
                    assertEquals(10, action2.getNInt().intValue());
                    assertEquals(42L, action2.getNLong().longValue());

                    final var assertor = new PropertyValueAfterTypeChangeAssert(Action1.class, restoredEntity);

                    assertor.assertNull(Action2.Properties.tc1, TgPersonName.class, TgFuelType.class);
                    assertEquals(ERR_ENTITY_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(TgFuelType.class).getKey(), personName.getKey()),
                                 restoredEntity.getProperty(Action2.Properties.tc1.name()).validationResult().getMessage());

                    assertor.assertNull(Action2.Properties.tc2, String.class, TgNote.class);
                    assertEquals(ERR_ENTITY_WAS_NOT_FOUND.formatted(getEntityTitleAndDesc(TgNote.class).getKey(), "Hello"),
                                 restoredEntity.getProperty(Action2.Properties.tc2.name()).validationResult().getMessage());

                    assertor.assertNull(Action2.Properties.tc3, RichText.class, String.class);
                    assertEquals(WARN_CONFIGURED_VALUE_COULD_NOT_BE_USED,
                                 restoredEntity.getProperty(Action2.Properties.tc3.name()).validationResult().getMessage());

                    assertor.assertEquals(Action2.Properties.tc4, Integer.class, BigDecimal.class, new BigDecimal("50.00"));
                    assertTrue(restoredEntity.getProperty(Action2.Properties.tc4.name()).isValid());
                });
    }

    @Test
    public void restoration_with_centre_context() {
        final var personName1 = save(new_(TgPersonName.class, "HP1"));
        final var personName2 = save(new_(TgPersonName.class, "HP2"));
        final var selectedEntities = List.of(personName1, personName2);

        final TinyHyperlinkCo coTinyHyperlink = co$(TinyHyperlink.class);
        final var chosenProperty = "test";
        final var tinyHyperlink = coTinyHyperlink.save(
                Action3.class,
                Map.of(),
                new CentreContextHolder()
                        .setChosenProperty(chosenProperty)
                        .setSelectedEntities(selectedEntities),
                Action3.ACTION_ID_ACTION3);

        TinyHyperlinkResource.restoreSharedEntity(tinyHyperlink, entityFactory, critGenerator, companionFinder, serialiser, webUiConfig, userProvider, DeviceProfile.DESKTOP, sharingModel)
                .run2(restoredEntity -> {
                    assertEquals(Action3.class, restoredEntity.getType());
                    final var action3 = (Action3) restoredEntity;
                    assertEquals(selectedEntities.stream().map(AbstractEntity::getId).collect(toSet()),
                                 action3.getSelectedIds());
                    assertEquals(Action3.COMPUTED_STRING_VALUE, action3.getComputedString());
                    assertEquals(chosenProperty, action3.getChosenProperty());
                });
    }

    @Test
    public void an_id_only_proxy_entity_can_be_specified_as_a_value_for_a_modified_property_and_can_then_be_restored() {
        final TinyHyperlinkCo coTinyHyperlink = co(TinyHyperlink.class);

        final var make = save(new_(TgVehicleMake.class, "BMW"));
        final var idOnlyMake = entityFactory.newEntity(getInstance(IIdOnlyProxiedEntityTypeCache.class).getIdOnlyProxiedTypeFor(TgVehicleMake.class), make.getId());
        assertTrue(idOnlyMake.isIdOnlyProxy());

        final var tinyHyperlink = coTinyHyperlink.save(
                TgVehicleModel.class,
                Map.of("make", idOnlyMake),
                new CentreContextHolder(),
                IActionIdentifier.of("test"));

        TinyHyperlinkResource.restoreSharedEntity(tinyHyperlink, entityFactory, critGenerator, companionFinder, serialiser, webUiConfig, userProvider, DeviceProfile.DESKTOP, sharingModel)
                .run2(restoredEntity -> {
                    assertEquals(TgVehicleModel.class, restoredEntity.getType());
                    final var model = (TgVehicleModel) restoredEntity;
                    assertEquals(idOnlyMake, model.getMake());
                });
    }


    record PropertyValueAfterTypeChangeAssert
            (Class<? extends AbstractEntity<?>> oldEntityType,
             AbstractEntity<?> restoredEntity)
    {
        void assertNull(CharSequence prop, Class<?> oldType, Class<?> newType) {
            assertEquals(prop, oldType, newType, null);
        }

        void assertEquals(CharSequence prop, Class<?> oldType, Class<?> newType, Object value) {
            assertTypes(prop, oldType, newType);
            Assert.assertEquals("Restored value of [%s] in entity [%s].".formatted(prop, restoredEntity),
                                value, restoredEntity.get(prop.toString()));
        }

        private void assertTypes(CharSequence prop, Class<?> oldType, Class<?> newType) {
            Assert.assertEquals("Type of property [%s] before change.".formatted(prop),
                                oldType, determinePropertyType(oldEntityType, prop));
            Assert.assertEquals("Type of property [%s] after change.".formatted(prop),
                                newType, determinePropertyType(restoredEntity.getType(), prop));
        }
    }

    private TinyHyperlink copyWithEntityType(final TinyHyperlink tinyHyperlink, final Class<? extends AbstractEntity<?>> entityType) {
        final var prevEntityTypeName = tinyHyperlink.getEntityTypeName();
        final var newEntityTypeName = entityType.getName();
        final var newSavingInfoHolder = new String(tinyHyperlink.getSavingInfoHolder()).replace(prevEntityTypeName, newEntityTypeName).getBytes();
        return new_(TinyHyperlink.class)
                .setEntityTypeName(newEntityTypeName)
                .setSavingInfoHolder(newSavingInfoHolder)
                // To ensure a different hash, as there might be nothing to replace in the saving info holder.
                .setActionIdentifier(tinyHyperlink.getActionIdentifier() + "_");
    }

}
