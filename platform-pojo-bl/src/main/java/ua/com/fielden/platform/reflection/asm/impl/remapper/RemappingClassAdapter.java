package ua.com.fielden.platform.reflection.asm.impl.remapper;


import org.kohsuke.asm3.AnnotationVisitor;
import org.kohsuke.asm3.ClassAdapter;
import org.kohsuke.asm3.ClassVisitor;
import org.kohsuke.asm3.FieldVisitor;
import org.kohsuke.asm3.MethodVisitor;

/**
 * A <code>ClassAdapter</code> for type remapping.
 *
 * @author Eugene Kuleshov
 */
public class RemappingClassAdapter extends ClassAdapter {

    protected final Remapper remapper;

    protected String className;

    public RemappingClassAdapter(final ClassVisitor cv, final Remapper remapper) {
        super(cv);
        this.remapper = remapper;
    }

    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces)
    {
        this.className = name;
        super.visit(version,
                access,
                remapper.mapType(name),
                remapper.mapSignature(signature, false),
                remapper.mapType(superName),
                interfaces == null ? null
                        : remapper.mapTypes(interfaces));
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationVisitor av;
        av = super.visitAnnotation(remapper.mapDesc(desc), visible);
        return av == null ? null : createRemappingAnnotationAdapter(av);
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value)
    {
        final FieldVisitor fv = super.visitField(access,
                remapper.mapFieldName(className, name, desc),
                remapper.mapDesc(desc),
                remapper.mapSignature(signature, true),
                remapper.mapValue(value));
        return fv == null ? null : createRemappingFieldAdapter(fv);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
        final String newDesc = remapper.mapMethodDesc(desc);
        final MethodVisitor mv = super.visitMethod(access,
                remapper.mapMethodName(className, name, desc),
                newDesc,
                remapper.mapSignature(signature, false),
                exceptions == null ? null : remapper.mapTypes(exceptions));
        return mv == null ? null : createRemappingMethodAdapter(access, newDesc, mv);
    }

    @Override
    public void visitInnerClass(
        final String name,
        final String outerName,
        final String innerName,
        final int access)
    {
        super.visitInnerClass(remapper.mapType(name),
                outerName == null ? null : remapper.mapType(outerName),
                innerName, // TODO should it be changed?
                access);
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        super.visitOuterClass(remapper.mapType(owner),
                name == null ? null : remapper.mapMethodName(owner, name, desc),
                desc == null ? null : remapper.mapMethodDesc(desc));
    }

    protected FieldVisitor createRemappingFieldAdapter(final FieldVisitor fv) {
        return new RemappingFieldAdapter(fv, remapper);
    }

    protected MethodVisitor createRemappingMethodAdapter(
        final int access,
        final String newDesc,
        final MethodVisitor mv)
    {
        return new RemappingMethodAdapter(access, newDesc, mv, remapper);
    }

    protected AnnotationVisitor createRemappingAnnotationAdapter(
        final AnnotationVisitor av)
    {
        return new RemappingAnnotationAdapter(av, remapper);
    }
}