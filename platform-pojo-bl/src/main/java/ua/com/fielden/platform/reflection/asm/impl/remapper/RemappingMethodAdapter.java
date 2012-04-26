package ua.com.fielden.platform.reflection.asm.impl.remapper;

import com.google.inject.asm.AnnotationVisitor;
import com.google.inject.asm.Label;
import com.google.inject.asm.MethodVisitor;


/**
 * A <code>MethodAdapter</code> for type mapping.
 *
 * @author Eugene Kuleshov
 */
public class RemappingMethodAdapter extends LocalVariablesSorter {

    protected final Remapper remapper;

    public RemappingMethodAdapter(
        final int access,
        final String desc,
        final MethodVisitor mv,
        final Remapper renamer)
    {
        super(access, desc, mv);
        this.remapper = renamer;
    }

    public void visitFieldInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        super.visitFieldInsn(opcode,
                remapper.mapType(owner),
                remapper.mapFieldName(owner, name, desc),
                remapper.mapDesc(desc));
    }

    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        super.visitMethodInsn(opcode,
                remapper.mapType(owner),
                remapper.mapMethodName(owner, name, desc),
                remapper.mapMethodDesc(desc));
    }

    public void visitTypeInsn(final int opcode, final String type) {
        super.visitTypeInsn(opcode, remapper.mapType(type));
    }

    public void visitLdcInsn(final Object cst) {
        super.visitLdcInsn(remapper.mapValue(cst));
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        super.visitMultiANewArrayInsn(remapper.mapDesc(desc), dims);
    }

    public void visitTryCatchBlock(
        final Label start,
        final Label end,
        final Label handler,
        final String type)
    {
        super.visitTryCatchBlock(start, end, handler, //
                type == null ? null : remapper.mapType(type));
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
        super.visitLocalVariable(name,
                remapper.mapDesc(desc),
                remapper.mapSignature(signature, true),
                start,
                end,
                index);
    }

    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final AnnotationVisitor av = mv.visitAnnotation(remapper.mapDesc(desc), visible);
        return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
    }

    public AnnotationVisitor visitAnnotationDefault() {
        final AnnotationVisitor av = mv.visitAnnotationDefault();
        return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
    }

    public AnnotationVisitor visitParameterAnnotation(
        final int parameter,
        final String desc,
        final boolean visible)
    {
        final AnnotationVisitor av = mv.visitParameterAnnotation(parameter,
                remapper.mapDesc(desc),
                visible);
        return av == null ? av : new RemappingAnnotationAdapter(av, remapper);
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
        super.visitFrame(type, nLocal, remapEntries(nLocal, local), nStack, remapEntries(nStack, stack));
    }

    private Object[] remapEntries(final int n, final Object[] entries) {
        for (int i = 0; i < n; i++) {
            if (entries[i] instanceof String) {
                final Object[] newEntries = new Object[n];
                if (i > 0) {
                    System.arraycopy(entries, 0, newEntries, 0, i);
                }
                do {
                    final Object t = entries[i];
                    newEntries[i++] = t instanceof String
                            ? remapper.mapType((String) t)
                            : t;
                } while (i < n);
                return newEntries;
            }
        }
        return entries;
    }

}
