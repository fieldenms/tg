package ua.com.fielden.platform.eql.stage2;

public class TransformationResultFromStage2To3<T> {
    public final T item;
    public final TransformationContextFromStage2To3 updatedContext;

    public TransformationResultFromStage2To3(final T item, final TransformationContextFromStage2To3 updatedContext) {
        this.item = item;
        this.updatedContext = updatedContext;
    }

    public static <T> TransformationResultFromStage2To3<T> skipTransformation(final TransformationContextFromStage2To3 updatedContext) {
        return new TransformationResultFromStage2To3<T>(null, updatedContext);
    }
}