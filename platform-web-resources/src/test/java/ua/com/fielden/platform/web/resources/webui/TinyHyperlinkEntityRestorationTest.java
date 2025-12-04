package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Inject;
import org.junit.Test;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.sample.domain.TgPersonName;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.types.tuples.T2.t2;

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

}
