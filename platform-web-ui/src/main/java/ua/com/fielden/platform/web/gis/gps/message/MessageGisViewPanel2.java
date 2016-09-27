package ua.com.fielden.platform.web.gis.gps.message;

import java.util.List;
import java.util.function.Function;

import javax.swing.ListSelectionModel;

import tg.tablecode.Message;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.web.gis.GisViewPanel2;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisView2;

/**
 * A GIS view panel for {@link Message} entity.
 *
 * @author TG Team
 *
 */
public class MessageGisViewPanel2 extends GisViewPanel2<Message> {
    private static final long serialVersionUID = -93397025790949362L;

    public MessageGisViewPanel2(final GpsGridAnalysisView2<Message, ?> parentView, final EntityGridInspector egi, final ListSelectionModel listSelectionModel, final PageHolder pageHolder) {
        super(parentView, egi, listSelectionModel, pageHolder);
    }

    @Override
    protected String mapPage() {
        return "http://localhost:1691/gis/message.html";
    }

    @Override
    protected List<String> convertToGeoJsonFeatures(final List<AbstractEntity<?>> entities) {
        final Function<AbstractEntity<?>, String> createMessageCoords = createMessageCoordsFun();
        return convertToGeoJsonFeatures(entities, //
                (e) -> createFeature(e, //
                        createMessageGeometryFun(createMessageCoords), //
                        createMessagePropertiesFun(createMessageCoords, this.parentView().getModel().getCdtme(), this.parentView().getModel().getEntityType()) //
                ), //
                (es) -> createLineStringFeature(es, createMessageCoords) //
        );
    }
}
