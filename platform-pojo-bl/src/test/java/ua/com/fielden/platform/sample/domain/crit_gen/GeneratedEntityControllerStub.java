package ua.com.fielden.platform.sample.domain.crit_gen;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.pagination.IPage;

@Singleton
public class GeneratedEntityControllerStub<T extends AbstractEntity<?>> implements IGeneratedEntityController<T> {

    @Override
    public Class<T> getEntityType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEntityType(final Class<T> type) {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<? extends Comparable> getKeyType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> firstPage(final QueryExecutionModel<T, ?> qem, final QueryExecutionModel<T, ?> summaryModel, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> qem, final int pageNo, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPage<T> getPage(final QueryExecutionModel<T, ?> model, final int pageNo, final int pageCount, final int pageCapacity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<T> getAllEntities(final QueryExecutionModel<T, ?> qem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
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
    public List<T> getFirstEntities(final QueryExecutionModel<T, ?> qem, final int numberOfEntities) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public Optional<Integer> progress() {
        return Optional.empty();
    }

    @Override
    public Stream<T> stream(QueryExecutionModel<T, ?> queryModel, final int fetchSize) {
        // TODO Auto-generated method stub
        return null;
    }

}
