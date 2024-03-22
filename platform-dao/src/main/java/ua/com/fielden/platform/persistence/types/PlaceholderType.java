package ua.com.fielden.platform.persistence.types;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * A general placeholder Hibernate type for those cases where an arbitrary instance of {@link Type} is required.
 */
public final class PlaceholderType implements Type {

    public static final Type PLACEHOLDER_TYPE = new PlaceholderType();

    private PlaceholderType() {}

    @Override
    public boolean isAssociationType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCollectionType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEntityType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAnyType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isComponentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnSpan(final Mapping mapping) throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] sqlTypes(final Mapping mapping) throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Size[] dictatedSizes(final Mapping mapping) throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Size[] defaultSizes(final Mapping mapping) throws MappingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class getReturnedClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSame(final Object x, final Object y) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEqual(final Object x, final Object y) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEqual(final Object x, final Object y, final SessionFactoryImplementor factory) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHashCode(final Object x) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHashCode(final Object x, final SessionFactoryImplementor factory) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compare(final Object x, final Object y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirty(final Object old, final Object current, final SharedSessionContractImplementor session) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirty(final Object oldState, final Object currentState, final boolean[] checkable, final SharedSessionContractImplementor session) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isModified(final Object dbState, final Object currentState, final boolean[] checkable, final SharedSessionContractImplementor session)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session, final Object owner)
            throws HibernateException, SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String name, final SharedSessionContractImplementor session, final Object owner) throws HibernateException, SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final boolean[] settable, final SharedSessionContractImplementor session)
            throws HibernateException, SQLException {}

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SharedSessionContractImplementor session)
            throws HibernateException, SQLException {}

    @Override
    public String toLoggableString(final Object value, final SessionFactoryImplementor factory) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object deepCopy(final Object value, final SessionFactoryImplementor factory) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable disassemble(final Object value, final SharedSessionContractImplementor session, final Object owner) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object assemble(final Serializable cached, final SharedSessionContractImplementor session, final Object owner) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeAssemble(final Serializable cached, final SharedSessionContractImplementor session) {}

    @Override
    public Object hydrate(final ResultSet rs, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws HibernateException, SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object resolve(final Object value, final SharedSessionContractImplementor session, final Object owner) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object semiResolve(final Object value, final SharedSessionContractImplementor session, final Object owner) throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getSemiResolvedType(final SessionFactoryImplementor factory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object replace(final Object original, final Object target, final SharedSessionContractImplementor session, final Object owner, final Map copyCache)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object replace(final Object original, final Object target, final SharedSessionContractImplementor session, final Object owner, final Map copyCache, final ForeignKeyDirection foreignKeyDirection)
            throws HibernateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[] toColumnNullness(final Object value, final Mapping mapping) {
        throw new UnsupportedOperationException();
    }

}
