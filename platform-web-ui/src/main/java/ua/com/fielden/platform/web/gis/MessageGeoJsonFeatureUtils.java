package ua.com.fielden.platform.web.gis;

import java.util.List;
import java.util.function.Function;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgMessage;

public class MessageGeoJsonFeatureUtils extends GeoJsonFeatureUtils {
    @Override
    public List<String> convertToGeoJsonFeatures(final List<AbstractEntity<?>> entities, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final Function<AbstractEntity<?>, String> createMessageCoords = createMessageCoordsFun();
        return convertToGeoJsonFeatures(entities, //
                (e) -> createFeature(e, //
                        createMessageGeometryFun(createMessageCoords), //
                        createMessagePropertiesFun(createMessageCoords, cdtmae, TgMessage.class) //
                ), //
                (es) -> createLineStringFeature(es, createMessageCoords) //
        );
    }
}
