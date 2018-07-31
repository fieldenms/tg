package ua.com.fielden.platform.sample.domain.crit_gen;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.sample.domain.ITgSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.security.user.User;

@EntityType(TgSystem.class)
public class TgSystemDaoStub implements ITgSystem {

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public Class<TgSystem> getEntityType() {
        return TgSystem.class;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        return false;
    }

    @Override
    public TgSystem findById(final Long id, final fetch<TgSystem> fetchModel) {
        return null;
    }

    @Override
    public TgSystem findById(final Long id) {
        return null;
    }

    @Override
    public TgSystem findByKey(final Object... keyValues) {
        return null;
    }

    @Override
    public TgSystem findByKeyAndFetch(final fetch<TgSystem> fetchModel, final Object... keyValues) {
        return null;
    }

    @Override
    public IPage<TgSystem> firstPage(final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TgSystem> getPage(final int pageNo, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TgSystem> firstPage(final QueryExecutionModel<TgSystem, ?> query, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TgSystem> getPage(final QueryExecutionModel<TgSystem, ?> query, final int pageNo, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TgSystem> getPage(final QueryExecutionModel<TgSystem, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        return null;
    }

    @Override
    public TgSystem save(final TgSystem entity) {
        return null;
    }

    @Override
    public boolean entityExists(final TgSystem entity) {
        return false;
    }

    @Override
    public boolean entityExists(final Long id) {
        return false;
    }

    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        return false;
    }

    @Override
    public TgSystem getEntity(final QueryExecutionModel<TgSystem, ?> model) {
        return null;
    }

    @Override
    public int count(final EntityResultQueryModel<TgSystem> model, final Map<String, Object> paramValues) {
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<TgSystem> model) {
        return 0;
    }

    @Override
    public List<TgSystem> getAllEntities(final QueryExecutionModel<TgSystem, ?> query) {
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<TgSystem, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        return null;
    }

    @Override
    public List<TgSystem> getFirstEntities(final QueryExecutionModel<TgSystem, ?> query, final int numberOfEntities) {
        return null;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Optional<Integer> progress() {
        return Optional.empty();
    }

    @Override
    public TgSystem findByEntityAndFetch(final fetch<TgSystem> fetchModel, final TgSystem entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IFetchProvider<TgSystem> getFetchProvider() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TgSystem new_() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TgSystem> stream(final QueryExecutionModel<TgSystem, ?> qem, final int fetchSize) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TgSystem> stream(final QueryExecutionModel<TgSystem, ?> qem) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public boolean hasEmptyResult(EntityResultQueryModel<TgSystem> model, Map<String, Object> paramValues) {
		// TODO Auto-generated method stub
		return false;
	}

}