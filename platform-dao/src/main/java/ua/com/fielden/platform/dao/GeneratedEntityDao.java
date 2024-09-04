package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;

/**
 * TODO document this class and provide additional implementations!
 * <p>
 * At this stage the only firstPage(qem, pageCapacity) has been implemented to be used in server-side EQC run() method.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class GeneratedEntityDao<T extends AbstractEntity<?>> implements IGeneratedEntityController<T> {
    private IEntityDao companion; // not typed deliberately
    private final ICompanionObjectFinder coFinder;

    @Inject
    public GeneratedEntityDao(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Optional<Integer> progress() {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Class<T> getEntityType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEntityType(final Class<T> type) {
        // It is important to use a companion that produces instrumented instances.
        // This is to support customisation where necessary to retrieve either instrumented or unstrumented entity instances,
        // which is controlled by using lightweight query models to avoid instrumentation.
        // The use of lightweight query models is the default for Entity Centres.
        this.companion = coFinder.find(type);
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T findById(final Long id, final fetch<T> fetchModel) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T findById(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T getEntity(final QueryExecutionModel<T, ?> model) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final int pageCapacity) {
        return companion.firstPage(qem, pageCapacity);
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        return companion.firstPage(qem, summaryModel, pageCapacity);
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> qem, final int pageNo, final int pageCapacity) {
        return companion.getPage(qem, pageNo, pageCapacity);
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        return companion.getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> qem) {
        return companion.getAllEntities(qem);
    }
    
    @Override
    public Stream<T> stream(final QueryExecutionModel<T, ?> queryModel, final int fetchSize) {
        return companion.stream(queryModel, fetchSize);
    }

    @Override
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> qem, final int numberOfEntities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
