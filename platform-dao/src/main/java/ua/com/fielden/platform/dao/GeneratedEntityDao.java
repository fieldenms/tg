package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;

import com.google.inject.Inject;

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
    public Integer progress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<T> getEntityType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEntityType(final Class<T> type) {
        this.companion = coFinder.find(type);
    }

    @Override
    public Class<? extends Comparable> getKeyType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T findById(final Long id, final fetch<T> fetchModel, final List<byte[]> binaryTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T findById(final Long id, final List<byte[]> binaryTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T getEntity(final QueryExecutionModel<T, ?> model, final List<byte[]> binaryTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final int pageCapacity, final List<byte[]> binaryTypes) {
        return companion.firstPage(qem, pageCapacity);
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity, final List<byte[]> binaryTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> qem, final int pageNo, final int pageCapacity, final List<byte[]> binaryTypes) {
        return null;
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity, final List<byte[]> binaryTypes) {
        return companion.getPage(model, pageNo, pageCount, pageCapacity);
    }

    @Override
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> qem, final List<byte[]> binaryTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> qem, final int numberOfEntities, final List<byte[]> binaryTypes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles, final List<byte[]> binaryTypes) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }
}
