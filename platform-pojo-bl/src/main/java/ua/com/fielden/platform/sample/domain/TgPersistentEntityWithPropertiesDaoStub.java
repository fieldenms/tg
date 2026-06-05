package ua.com.fielden.platform.sample.domain;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.attachment.Attachment;
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

/**
 * Just a stub for companion to be able to run deserialisation of instrumented instance in pojo-bl's EntityDeserialisationWithJacksonTest.
 * 
 * @author TG Team
 *
 */
@Singleton
public class TgPersistentEntityWithPropertiesDaoStub implements ITgPersistentEntityWithProperties {

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public byte[] export(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException {
        return null;
    }

    @Override
    public Class<TgPersistentEntityWithProperties> getEntityType() {
        return null;
    }

    @Override
    public Class<? extends Comparable<?>> getKeyType() {
        return null;
    }

    @Override
    public IFetchProvider<TgPersistentEntityWithProperties> getFetchProvider() {
        return null;
    }

    @Override
    public boolean isStale(final Long entityId, final Long version) {
        return false;
    }

    @Override
    public TgPersistentEntityWithProperties findById(final boolean filtered, final Long id, final fetch<TgPersistentEntityWithProperties> fetchModel, final IFillModel<TgPersistentEntityWithProperties> fillModel) {
        return null;
    }

    @Override
    public TgPersistentEntityWithProperties findById(final Long id) {
        return null;
    }

    @Override
    public TgPersistentEntityWithProperties findByKey(final Object... keyValues) {
        return null;
    }

    @Override
    public TgPersistentEntityWithProperties findByKeyAndFetch(final boolean filtered, final fetch<TgPersistentEntityWithProperties> fetchModel, final Object... keyValues) {
        return null;
    }

    @Override
    public TgPersistentEntityWithProperties findByEntityAndFetch(final fetch<TgPersistentEntityWithProperties> fetchModel, final TgPersistentEntityWithProperties entity) {
        return null;
    }

    @Override
    public List<TgPersistentEntityWithProperties> getFirstEntities(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> query, final int numberOfEntities) {
        return null;
    }

    @Override
    public IPage<TgPersistentEntityWithProperties> firstPage(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> query, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TgPersistentEntityWithProperties> getPage(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> query, final int pageNo, final int pageCapacity) {
        return null;
    }

    @Override
    public IPage<TgPersistentEntityWithProperties> getPage(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> query, final int pageNo, final int pageCount, final int pageCapacity) {
        return null;
    }

    @Override
    public List<TgPersistentEntityWithProperties> getAllEntities(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> query) {
        return null;
    }

    @Override
    public TgPersistentEntityWithProperties getEntity(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> model) {
        return null;
    }

    @Override
    public Optional<TgPersistentEntityWithProperties> getEntityOptional(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> model) {
        return Optional.empty();
    }

    @Override
    public boolean entityExists(final TgPersistentEntityWithProperties entity) {
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
    public int count(final EntityResultQueryModel<TgPersistentEntityWithProperties> model, final Map<String, Object> paramValues) {
        return 0;
    }

    @Override
    public int count(final EntityResultQueryModel<TgPersistentEntityWithProperties> model) {
        return 0;
    }

    @Override
    public boolean exists(final EntityResultQueryModel<TgPersistentEntityWithProperties> model, final Map<String, Object> paramValues) {
        return false;
    }

    @Override
    public TgPersistentEntityWithProperties new_() {
        return null;
    }

    @Override
    public Stream<TgPersistentEntityWithProperties> stream(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> qem, final int fetchSize) {
        return null;
    }

    @Override
    public Stream<TgPersistentEntityWithProperties> stream(final QueryExecutionModel<TgPersistentEntityWithProperties, ?> qem) {
        return null;
    }

    @Override
    public TgPersistentEntityWithProperties save(final TgPersistentEntityWithProperties entity) {
        return null;
    }

    @Override
    public boolean stop() {
        return false;
    }

    @Override
    public Optional<Integer> progress() {
        return null;
    }

    @Override
    public TgPersistentEntityWithPropertiesAttachment attach(final Attachment attachment, final TgPersistentEntityWithProperties entity) {
        return null;
    }

}
