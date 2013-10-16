package ua.com.fielden.platform.reflection.asm.impl.remapper;

import org.kohsuke.asm3.signature.SignatureVisitor;

/**
 * A <code>SignatureVisitor</code> adapter for type mapping.
 *
 * @author Eugene Kuleshov
 */
public class RemappingSignatureAdapter implements SignatureVisitor {
    private final SignatureVisitor v;
    private final Remapper remapper;
    private String className;

    public RemappingSignatureAdapter(final SignatureVisitor v, final Remapper remapper) {
        this.v = v;
        this.remapper = remapper;
    }

    @Override
    public void visitClassType(final String name) {
        className = name;
        v.visitClassType(remapper.mapType(name));
    }

    @Override
    public void visitInnerClassType(final String name) {
        className = className + '$' + name;
        final String remappedName = remapper.mapType(className);
        v.visitInnerClassType(remappedName.substring(remappedName.lastIndexOf('$') + 1));
    }

    @Override
    public void visitFormalTypeParameter(final String name) {
        v.visitFormalTypeParameter(name);
    }

    @Override
    public void visitTypeVariable(final String name) {
        v.visitTypeVariable(name);
    }

    @Override
    public SignatureVisitor visitArrayType() {
        v.visitArrayType();
        return this;
    }

    @Override
    public void visitBaseType(final char descriptor) {
        v.visitBaseType(descriptor);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        v.visitClassBound();
        return this;
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        v.visitExceptionType();
        return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
        v.visitInterface();
        return this;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        v.visitInterfaceBound();
        return this;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        v.visitParameterType();
        return this;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        v.visitReturnType();
        return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        v.visitSuperclass();
        return this;
    }

    @Override
    public void visitTypeArgument() {
        v.visitTypeArgument();
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        v.visitTypeArgument(wildcard);
        return this;
    }

    @Override
    public void visitEnd() {
        v.visitEnd();
    }

}
