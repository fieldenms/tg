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
    public AbstractQueryDashboardItem(final IDashboardParamsGetter paramsGetter, final IQueryBody<?> ... queryBodies) {
	super(paramsGetter, createComputationMonitor(queryBodies));
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
	    pages.add(queryBody.run());
	}
        return formResult(pages);
    }

    public interface IQueryBody<M extends AbstractEntity<?>> {
	List<M> run();
	IComputationMonitor computationMonitor();
    }

    public static class AggregatedQueryBody implements IQueryBody<EntityAggregates> {
	private final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query;
	private final IEntityAggregatesDao controller;

	public AggregatedQueryBody(final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> query, final IEntityAggregatesDao controller) {
	    this.query = query;
	    this.controller = controller;
	}

	@Override
	public List<EntityAggregates> run() {
	    return controller.getAllEntities(query);
	}

	@Override
	public IComputationMonitor computationMonitor() {
	    return controller;
	}
    }

    public static class QueryBody<M extends AbstractEntity<?>> implements IQueryBody<M> {
	private final QueryExecutionModel<M, ?> query;
	private final IEntityDao<M> controller;

	public QueryBody(final QueryExecutionModel<M, EntityResultQueryModel<M>> query, final IEntityDao<M> controller) {
	    this.query = query;
	    this.controller = controller;
	}

	@Override
	public List<M> run() {
	    return controller.getAllEntities(query);
	}

	@Override
	public IComputationMonitor computationMonitor() {
	    return controller;
	}
    }

    public static class GeneratedQueryBody<M extends AbstractEntity<?>> implements IQueryBody<M> {
	private final Class<M> managedType;
	private final List<byte[]> managedTypeArrays;
	private final QueryExecutionModel<M, EntityResultQueryModel<M>> query;
	private final IGeneratedEntityController<M> controller;

	public GeneratedQueryBody(final Class<M> managedType, final List<byte[]> managedTypeArrays, final QueryExecutionModel<M, EntityResultQueryModel<M>> query, final IGeneratedEntityController<M> controller) {
	    this.managedType = managedType;
	    this.managedTypeArrays = managedTypeArrays;
	    this.query = query;
	    this.controller = controller;
	}

	@Override
	public List<M> run() {
	    controller.setEntityType(managedType);
	    return controller.getAllEntities(query, managedTypeArrays);
	}

	@Override
	public IComputationMonitor computationMonitor() {
	    return controller;
	}
    }
}
