package ua.com.fielden.platform.eql.stage2;

public class TransformationResult2<T> {
    public final T item;
    public final TransformationContext2 updatedContext;

    public TransformationResult2(final T item, final TransformationContext2 updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }
}