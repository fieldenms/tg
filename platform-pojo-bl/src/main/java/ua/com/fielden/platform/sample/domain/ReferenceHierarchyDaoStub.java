package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;
import ua.com.fielden.platform.companion.ICanReadUninstrumented;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.IFillModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.ref_hierarchy.IReferenceHierarchy;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.security.user.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A stub DAO implementation to support usage of {@link ReferenceHierarchy}.
 *
 * @author TG Team
 */
@EntityType(ReferenceHierarchy.class)
public class ReferenceHierarchyDaoStub implements IReferenceHierarchy, ICanReadUninstrumented {

    @Inject
    private EntityFactory entityFactory;

    @Override
    public ReferenceHierarchy save(final ReferenceHierarchy entity) {
        entity.isValid().ifFailure(Result::throwRuntime);
        return entity;
    }

    @Override
    public ReferenceHierarchy new_() {
        return entityFactory.newEntity(ReferenceHierarchy.class);
    }

    @Override
    public Class<ReferenceHierarchy> getEntityType() {
        return null;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return null;
    }

    @Override
    public IFetchProvider<ReferenceHierarchy> getFetchProvider() {
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        return false;
    }

    @Override
    public ReferenceHierarchy findById(final boolean filtered, final Long id, final fetch<ReferenceHierarchy> fetchModel, final IFillModel<ReferenceHierarchy> fillModel) {
        return null;
    }

    @Override
    public ReferenceHierarchy findById(final Long id) {
        return null;
    }

    @Override
    public ReferenceHierarchy findByKey(final Object... keyValues) {
        return null;
    }

    @Override
    public ReferenceHierarchy findByKeyAndFetch(final boolean filtered, final fetch<ReferenceHierarchy> fetchModel, final Object... keyValues) {
        return null;
    }

    @Override
    public ReferenceHierarchy findByEntityAndFetch(final fetch<ReferenceHierarchy> fetchModel, final ReferenceHierarchy entity) {
        return null;
    }

    @Override
    public List<ReferenceHierarchy> getFirstEntities(final QueryExecutionModel<ReferenceHierarchy, ?> query, final int numberOfEntities) {
        return null;
    }

    @Override
    public IPage<ReferenceHierarchy> firstPage(final QueryExecutionModel<ReferenceHierarchy, ?> query, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<ReferenceHierarchy> getPage(final QueryExecutionModel<ReferenceHierarchy, ?> query, final int pageNo, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<ReferenceHierarchy> getPage(final QueryExecutionModel<ReferenceHierarchy, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        return null;
    }

    @Override
    public List<ReferenceHierarchy> getAllEntities(final QueryExecutionModel<ReferenceHierarchy, ?> query) {
        return null;
    }

    @Override
    public ReferenceHierarchy getEntity(final QueryExecutionModel<ReferenceHierarchy, ?> model) {
        return null;
    }

    @Override
    public Optional<ReferenceHierarchy> getEntityOptional(final QueryExecutionModel<ReferenceHierarchy, ?> model) {
        return Optional.empty();
    }

    @Override
    public boolean entityExists(final ReferenceHierarchy entity) {
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
    public int count(final EntityResultQueryModel<ReferenceHierarchy> model, final Map<String, Object> paramValues) {
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<ReferenceHierarchy> model) {
        return 0;
    }

    @Override
    public boolean exists(final EntityResultQueryModel<ReferenceHierarchy> model, final Map<String, Object> paramValues) {
        return false;
    }

    @Override
    public Stream<ReferenceHierarchy> stream(final QueryExecutionModel<ReferenceHierarchy, ?> qem, final int fetchSize) {
        return null;
    }

    @Override
    public Stream<ReferenceHierarchy> stream(final QueryExecutionModel<ReferenceHierarchy, ?> qem) {
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
    public String getUsername() {
        return null;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<ReferenceHierarchy, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        return new byte[0];
    }

    @Override
    public void readUninstrumented() {
    }

}
