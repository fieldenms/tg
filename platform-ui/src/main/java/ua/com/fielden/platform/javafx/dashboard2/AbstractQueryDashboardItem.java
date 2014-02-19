package ua.com.fielden.platform.javafx.dashboard2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.embed.swing.JFXPanel;
import ua.com.fielden.platform.dao.IComputationMonitor;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dashboard.IDashboardItemResult;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;

import com.google.inject.Inject;

/**
 * An abstract dashboard item with query-specific mechanism of data refresh action.
 *
 * @author TG Team
 *
 * @param <RESULT>
 * @param <UI>
 */
public abstract class AbstractQueryDashboardItem <RESULT extends IDashboardItemResult, UI extends JFXPanel & IDashboardItemUi<RESULT>> extends AbstractDashboardItem<RESULT, UI> {
    private final List<IQueryBody<?>> queryBodies;

    @Inject
    public AbstractQueryDashboardItem(final IDashboardParamsGetter paramsGetter, final Class<? extends AbstractEntity<?>> mainType, final IQueryBody<?> ... queryBodies) {
	super(paramsGetter, createComputationMonitor(queryBodies), mainType);
	this.queryBodies = Arrays.asList(queryBodies);
    }

    /** Creates computation monitor, which controls query-based computations. */
    private static IComputationMonitor createComputationMonitor(final IQueryBody<?> ... queryBodies) {
	return new IComputationMonitor() {
	    @Override
	    public boolean stop() {
		boolean anyStopped = false;
		for (final IQueryBody<?> queryBody : queryBodies) {
		    final boolean stopped = queryBody.computationMonitor().stop();
		    anyStopped = stopped ? true : anyStopped;
		}
		return anyStopped;
	    }

	    @Override
	    public Integer progress() {
		return null;
	    }
	};
    }

    /** Wraps the result of the queries into custom result type. */
    protected abstract RESULT formResult(final List<List<?>> pages);

    /**
     * Refreshes item information using custom parameters.
     */
    @Override
    protected final RESULT refresh(final List<QueryProperty> customParameters) {
	final List<List<?>> pages = new ArrayList<>();
	for (final IQueryBody<?> queryBody : queryBodies) {
	    pages.add(queryBody.run(customParameters));
	}
        return formResult(pages);
    }

    public interface IQueryBody<M extends AbstractEntity<?>> {
	List<M> run(final List<QueryProperty> customParameters);
	IComputationMonitor computationMonitor();
    }

    public static class AggregatedQueryBody implements IQueryBody<EntityAggregates> {
	private final IAggregatedQueryComposer aggregatedQueryComposer;
	private final IEntityAggregatesDao controller;

	public AggregatedQueryBody(final IAggregatedQueryComposer aggregatedQueryComposer, final IEntityAggregatesDao controller) {
	    this.aggregatedQueryComposer = aggregatedQueryComposer;
	    this.controller = controller;
	}

	@Override
	public List<EntityAggregates> run(final List<QueryProperty> customParams) {
	    return controller.getAllEntities(aggregatedQueryComposer.composeQuery(customParams));
	}

	@Override
	public IComputationMonitor computationMonitor() {
	    return controller;
	}
    }

    public static interface IAggregatedQueryComposer {
	QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> composeQuery(final List<QueryProperty> customParams);
    }

    public static class QueryBody<M extends AbstractEntity<?>> implements IQueryBody<M> {
	private final IQueryComposer<M> queryComposer;
	private final IEntityDao<M> controller;

	public QueryBody(final IQueryComposer<M> queryComposer, final IEntityDao<M> controller) {
	    this.queryComposer = queryComposer;
	    this.controller = controller;
	}

	@Override
	public List<M> run(final List<QueryProperty> customParams) {
	    return controller.getAllEntities(queryComposer.composeQuery(customParams));
	}

	@Override
	public IComputationMonitor computationMonitor() {
	    return controller;
	}
    }

    public static interface IQueryComposer<M extends AbstractEntity<?>> {
	QueryExecutionModel<M, EntityResultQueryModel<M>> composeQuery(final List<QueryProperty> customParams);
    }

    public static class GeneratedQueryBody<M extends AbstractEntity<?>> implements IQueryBody<M> {
	private final Class<M> managedType;
	private final List<byte[]> managedTypeArrays;
	private final IQueryComposer<M> queryComposer;
	private final IGeneratedEntityController<M> controller;

	public GeneratedQueryBody(final Class<M> managedType, final List<byte[]> managedTypeArrays, final IQueryComposer<M> queryComposer, final IGeneratedEntityController<M> controller) {
	    this.managedType = managedType;
	    this.managedTypeArrays = managedTypeArrays;
	    this.queryComposer = queryComposer;
	    this.controller = controller;
	}

	@Override
	public List<M> run(final List<QueryProperty> customParams) {
	    controller.setEntityType(managedType);
	    return controller.getAllEntities(queryComposer.composeQuery(customParams), managedTypeArrays);
	}

	@Override
	public IComputationMonitor computationMonitor() {
	    return controller;
	}
    }
}
