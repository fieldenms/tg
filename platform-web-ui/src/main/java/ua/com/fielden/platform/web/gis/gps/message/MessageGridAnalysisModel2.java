/**
 *
 */
package ua.com.fielden.platform.web.gis.gps.message;

import java.util.Date;

import org.joda.time.DateTime;

import tg.tablecode.Message;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.javafx.gis.gps.MessagePoint;
import ua.com.fielden.platform.report.query.generation.IQueryComposer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisModel2;

/**
 * A model for {@link Message}'s {@link GridAnalysisView}.
 *
 * @author Developers
 */
public class MessageGridAnalysisModel2 extends GpsGridAnalysisModel2<Message> {

    public MessageGridAnalysisModel2(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, Message, IEntityDao<Message>> criteria, final IAnalysisQueryCustomiser<Message, GridAnalysisModel<Message, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser) {
        super(criteria, queryCustomiser, Message.class);
    }

    @Override
    protected long initialTransactionEntityDeltaDelay() {
        return new DateTime(2100, 1, 1, 0, 0).getMillis() - new Date().getTime();
    }

    @Override
    protected void provideCustomPropertiesForQueries(final ICentreDomainTreeManagerAndEnhancer cdtmaeCopy) {
        super.provideCustomPropertiesForQueries(cdtmaeCopy);

        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "gpsTime");
        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "vectorSpeed");
        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "y");
        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "x");
        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), MessagePoint.MACHINE_PROP_ALIAS);
        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "vectorAngle");
    }

    @Override
    protected Pair<IQueryComposer<Message>, IQueryComposer<Message>> enhanceByTransactionDateBoundaries(final Date oldNow, final Date now) {
        setFitToBounds(oldNow == null); // RUN is performed (not Delta)
        setFirstQuery(oldNow == null);
        return super.enhanceByTransactionDateBoundaries(oldNow, now);
    }
}