package ua.com.fielden.platform.sample.domain.crit_gen;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.IFillModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.security.user.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LastLevelEntityDaoStub implements ILastLevelEntity {

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public Class<LastLevelEntity> getEntityType() {
        return null;
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
    public LastLevelEntity findById(final boolean filtered, final Long id, final fetch<LastLevelEntity> fetchModel, final IFillModel<LastLevelEntity> fillModel) {
        return null;
    }

    @Override
    public LastLevelEntity findById(final Long id) {
        return null;
    }

    @Override
    public LastLevelEntity findByKey(final Object... keyValues) {
        return null;
    }

    @Override
    public LastLevelEntity findByKeyAndFetch(final boolean filtered, final fetch<LastLevelEntity> fetchModel, final Object... keyValues) {
        return null;
    }

    @Override
    public IPage<LastLevelEntity> firstPage(final QueryExecutionModel<LastLevelEntity, ?> query, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<LastLevelEntity> getPage(final QueryExecutionModel<LastLevelEntity, ?> query, final int pageNo, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<LastLevelEntity> getPage(final QueryExecutionModel<LastLevelEntity, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        return null;
    }

    @Override
    public LastLevelEntity save(final LastLevelEntity entity) {
        return null;
    }

    @Override
    public boolean entityExists(final LastLevelEntity entity) {
        return "EntityKey".equals(entity.getKey());
    }

    @Override
    public boolean entityExists(final Long id) {
        return false;
    }

    @Override
    public boolean entityWithKeyExists(final Object... keyValues) {
        for (final Object keyValue : keyValues) {
            if (!"EntityKey".equals(keyValue)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public LastLevelEntity getEntity(final QueryExecutionModel<LastLevelEntity, ?> model) {
        return null;
    }

    @Override
    public Optional<LastLevelEntity> getEntityOptional(final QueryExecutionModel<LastLevelEntity, ?> model) {
        return Optional.empty();
    }

    @Override
    public int count(final EntityResultQueryModel<LastLevelEntity> model, final Map<String, Object> paramValues) {
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<LastLevelEntity> model) {
        return 0;
    }

    @Override
    public List<LastLevelEntity> getAllEntities(final QueryExecutionModel<LastLevelEntity, ?> query) {
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<LastLevelEntity, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        return null;
    }

    @Override
    public List<LastLevelEntity> getFirstEntities(final QueryExecutionModel<LastLevelEntity, ?> query, final int numberOfEntities) {
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
    public LastLevelEntity findByEntityAndFetch(final fetch<LastLevelEntity> fetchModel, final LastLevelEntity entity) {
        return null;
    }

    @Override
    public IFetchProvider<LastLevelEntity> getFetchProvider() {
        return null;
    }

    @Override
    public LastLevelEntity new_() {
        return null;
    }

    @Override
    public Stream<LastLevelEntity> stream(final QueryExecutionModel<LastLevelEntity, ?> qem, final int fetchSize) {
        return null;
    }

    @Override
    public Stream<LastLevelEntity> stream(final QueryExecutionModel<LastLevelEntity, ?> qem) {
        return null;
    }

	@Override
	public boolean exists(final EntityResultQueryModel<LastLevelEntity> model, final Map<String, Object> paramValues) {
		return false;
	}

}
