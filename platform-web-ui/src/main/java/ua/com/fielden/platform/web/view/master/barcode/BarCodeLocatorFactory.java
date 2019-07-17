package ua.com.fielden.platform.web.view.master.barcode;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.KeyLocator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;

public class BarCodeLocatorFactory {

    public static <K extends AbstractEntity<?>> EntityActionConfig mkLocator(
            final IWebUiBuilder builder,
            final Class<K> barCodeLocatorType,
            final String icon,
            final String iconStyle) {

        final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(barCodeLocatorType);
        final String desc = format("Scan %s", entityTitleAndDesc.getKey());

        return createBarLocatorAction(builder, barCodeLocatorType)
                .withContext(context().withSelectionCrit().build(), barCodeLocatorType)
                .icon(icon)
                .withStyle(iconStyle)
                .shortDesc(desc)
                .longDesc(desc)
                .build();
    }

    public static EntityMaster<KeyLocator> createBarCodeLocatorMaster (final IWebUiBuilder builder, final Injector injector) {
        if (!builder.getMaster(KeyLocator.class).isPresent()) {
            return builder.register(EntityMaster.noUiFunctionalMaster(KeyLocator.class, false, createReadyCode(), injector));
        }
        return builder.getMaster(KeyLocator.class).get();
    }

    private static <K extends AbstractEntity<?>> BarCodeLocator<K> createBarLocatorAction (
            final IWebUiBuilder builder,
            final Class<K> barCodeLocatorType) {

        final BarCodeLocator<K> openLocatorAction = new BarCodeLocator<>(builder.getOpenMasterAction(barCodeLocatorType));
        return openLocatorAction;
    }

    private static JsCode createReadyCode() {
        return new JsCode("self._isNecessaryForConversion = function (propertyName) { \n"
                        + "return ['entityKey', 'entity', 'entityType'].indexOf(propertyName) >= 0; \n"
                        + "}; \n");
    }
}
