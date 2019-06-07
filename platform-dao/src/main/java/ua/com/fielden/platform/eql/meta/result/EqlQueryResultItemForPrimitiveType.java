package ua.com.fielden.platform.eql.meta.result;

public class EqlQueryResultItemForPrimitiveType<T> extends AbstractEqlQueryResultItem<T> implements IEqlQueryResultItem<T> {

    public EqlQueryResultItemForPrimitiveType(final String name, final Class<T> javaType) {
        super(name, javaType);
    }

    @Override
    public EqlPropResolutionProgress resolve(final EqlPropResolutionProgress resolutionProgress) {
        return resolutionProgress;
    }
}