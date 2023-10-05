package ua.com.fielden.platform.eql.meta.query;

import java.util.SortedMap;

import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public interface IResolvable<T> {
    PropResolutionProgress resolve(final PropResolutionProgress context);
    Class<T> javaType();
    
    public static PropResolutionProgress resolve(final PropResolutionProgress context, final SortedMap<String, AbstractQuerySourceInfoItem<?>> props) {
        if (context.isSuccessful()) {
            return context;
        } else {
            final AbstractQuerySourceInfoItem<?> foundPart = props.get(context.getNextPending());
            return foundPart == null ? context : foundPart.resolve(context.registerResolutionAndClone(foundPart));
        }
    }
}