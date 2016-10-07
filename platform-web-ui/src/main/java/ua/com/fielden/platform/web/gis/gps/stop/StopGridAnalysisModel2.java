/**
 *
 */
package ua.com.fielden.platform.web.gis.gps.stop;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;

import tg.domain.geo.Coordinate;
import tg.domain.geo.ICoordinate;
import tg.tablecode.Stop;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.report.query.generation.IQueryComposer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.gis.gps.GpsGridAnalysisModel2;

/**
 * A model for {@link Stop}'s {@link GridAnalysisView}.
 *
 * @author Developers
 */
public class StopGridAnalysisModel2 extends GpsGridAnalysisModel2<Stop> {
    private final ICoordinate coordinateCompanion;
    private final Map<Long, AbstractEntity<?>> currentStopsById = new LinkedHashMap<>();
    private int deltaCount = 0;

    public StopGridAnalysisModel2(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, Stop, IEntityDao<Stop>> criteria, final IAnalysisQueryCustomiser<Stop, GridAnalysisModel<Stop, ICentreDomainTreeManagerAndEnhancer>> queryCustomiser) {
        super(criteria, queryCustomiser, Stop.class);
        this.coordinateCompanion = criteria.getControllerProvider().find(Coordinate.class);
    }

    @Override
    protected void provideCustomPropertiesForQueries(final ICentreDomainTreeManagerAndEnhancer cdtmaeCopy) {
        super.provideCustomPropertiesForQueries(cdtmaeCopy);

        // TODO check custom stuff
        //        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "gpsTime");
        //        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "vectorSpeed");
        //        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "y");
        //        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "x");
        //        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), MessagePoint.MACHINE_PROP_ALIAS);
        //        checkFetchPropertyIfNotChecked(cdtmaeCopy, getEntityType(), "vectorAngle");
    }

    //    @Override
    //    protected fetch<Stop> customFetchModel(final Class<Stop> enhancedType) {
    //        return fetch(enhancedType).with("coordinates"); //null; //fetch(enhancedType).with("coordinates", fetchAll((Class<AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(enhancedType, "coordinates")));
    //    }

    @Override
    protected Pair<IQueryComposer<Stop>, IQueryComposer<Stop>> enhanceByTransactionDateBoundaries(final Date oldNow, final Date now) {
        if (oldNow == null) { // main query is performed -- reset a counter for delta queries
            deltaCount = 0;
        }
        setFitToBounds(deltaCount == 1);
        setFirstQuery(deltaCount == 0);

        return super.enhanceByTransactionDateBoundaries(null, null);
    }

    @Override
    protected Result getDelta(final Date old, final Date oldNow) {
        final DateTime start = new DateTime();
        System.err.println("GET DELTA...");

        deltaCount++;

        setFirstQuery(deltaCount == 0);
        setFitToBounds(deltaCount == 1); // first delta query is performed (after main query) -- GEO messages should fitted to bounds

        // final MachineMonitor machineMonitor = getCriteria().getEntityFactory().newByKey(MachineMonitor.class, "NOMATTER").setRequestUpdate(createRequestUpdate());
        // final MachineMonitor updated = machineMonitorCompanion.save(machineMonitor);

        //        final EntityResultQueryModel<Coordinate> query = select(Coordinate.class).model();

        //        final List<Coordinate> allCoordinates = coordinateCompanion.getAllEntities(//
        //        from(query)//
        //        .with(fetchAll(Coordinate.class))//
        //        .with(orderBy().prop("stop").asc().prop("order").asc().model())//
        //        .model());

        // int messageCount = 0;
        // String s = "";
        //        for (final Coordinate coordinate : allCoordinates) {
        //            final AbstractEntity<?> stopToBeUpdated = currentStopsById.get(coordinate.getStop().getId());
        //
        //            if (stopToBeUpdated.get("coordinates") == null || ((Set<Coordinate>) stopToBeUpdated.get("coordinates")).isEmpty()) {
        //                stopToBeUpdated.set("coordinates", new LinkedHashSet<Coordinate>());
        //            }
        //            ((Set<Coordinate>) stopToBeUpdated.get("coordinates")).add(coordinate);
        //        }
        //        // check order
        //        for (final AbstractEntity<?> stop : currentStopsById.values()) {
        //            if (notSorted((Set<Coordinate>) stop.get("coordinates"))) {
        //                throw new RuntimeException("Not sorted");
        //            }
        //        }

        final IPage<AbstractEntity<?>> page = createSinglePage(null, currentStopsById.values());

        final Period pd = new Period(start, new DateTime());
        System.out.println("GET DELTA: done in " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
        return Result.successful(page);
    }

    //    private boolean notSorted(final Set<Coordinate> set) {
    //        final Iterator<Coordinate> iter = set.iterator();
    //        if (!iter.next().getOrder().equals(new Integer(0))) {
    //            System.err.println("Not sorted: " + set);
    //            return false;
    //        }
    //        if (!iter.next().getOrder().equals(new Integer(1))) {
    //            System.err.println("Not sorted: " + set);
    //            return false;
    //        }
    //
    //        return true;
    //    }

    @Override
    public IPage<Stop> promotePage(final Result result) {
        final IPage<Stop> promotedPage = super.promotePage(result);
        currentStopsById.clear();
        for (final AbstractEntity<?> stop : promotedPage.data()) {
            currentStopsById.put(stop.getId(), stop);
        }
        return promotedPage;
    }

    @Override
    protected long transactionEntityDeltaDelay() {
        return 300000;
    }

    @Override
    protected long initialTransactionEntityDeltaDelay() {
        return 0;
    }
}