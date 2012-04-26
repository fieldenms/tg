package ua.com.fielden.platform.reflection.asm.impl.remapper;

import com.google.inject.asm.signature.SignatureVisitor;

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

    public void visitClassType(final String name) {
        className = name;
        v.visitClassType(remapper.mapType(name));
    }

    public void visitInnerClassType(final String name) {
        className = className + '$' + name;
        final String remappedName = remapper.mapType(className);
        v.visitInnerClassType(remappedName.substring(remappedName.lastIndexOf('$') + 1));
    }

    public void visitFormalTypeParameter(final String name) {
        v.visitFormalTypeParameter(name);
    }

    public void visitTypeVariable(final String name) {
        v.visitTypeVariable(name);
    }

    public SignatureVisitor visitArrayType() {
        v.visitArrayType();
        return this;
    }

    public void visitBaseType(final char descriptor) {
        v.visitBaseType(descriptor);
    }

    public SignatureVisitor visitClassBound() {
        v.visitClassBound();
        return this;
    }

    public SignatureVisitor visitExceptionType() {
        v.visitExceptionType();
        return this;
    }

    public SignatureVisitor visitInterface() {
        v.visitInterface();
        return this;
    }

    public SignatureVisitor visitInterfaceBound() {
        v.visitInterfaceBound();
        return this;
    }

    public SignatureVisitor visitParameterType() {
        v.visitParameterType();
        return this;
    }

    public SignatureVisitor visitReturnType() {
        v.visitReturnType();
        return this;
    }

    public SignatureVisitor visitSuperclass() {
        v.visitSuperclass();
        return this;
    }

    public void visitTypeArgument() {
        v.visitTypeArgument();
    }

    public SignatureVisitor visitTypeArgument(final char wildcard) {
        v.visitTypeArgument(wildcard);
        return this;
    }

    public void visitEnd() {
        v.visitEnd();
    }

}
