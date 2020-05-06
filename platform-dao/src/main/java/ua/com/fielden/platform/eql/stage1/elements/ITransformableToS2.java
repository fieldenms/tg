package ua.com.fielden.platform.eql.stage1.elements;

public interface ITransformableToS2<S2> {
    S2 transform(final PropsResolutionContext context);
}