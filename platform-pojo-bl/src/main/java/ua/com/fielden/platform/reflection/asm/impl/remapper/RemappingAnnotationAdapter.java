package ua.com.fielden.platform.reflection.asm.impl.remapper;

import com.google.inject.asm.AnnotationVisitor;

/**
 * An <code>AnnotationVisitor</code> adapter for type remapping.
 *
 * @author Eugene Kuleshov
 */
public class RemappingAnnotationAdapter implements AnnotationVisitor {

    private final AnnotationVisitor av;

    private final Remapper renamer;

    public RemappingAnnotationAdapter(final AnnotationVisitor av, final Remapper renamer) {
        this.av = av;
        this.renamer = renamer;
    }

    public void visit(final String name, final Object value) {
        av.visit(name, renamer.mapValue(value));
    }

    public void visitEnum(final String name, final String desc, final String value) {
        av.visitEnum(name, renamer.mapDesc(desc), value);
    }

    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        final AnnotationVisitor v = av.visitAnnotation(name, renamer.mapDesc(desc));
        return v == null ? null : (v == av
                ? this
                : new RemappingAnnotationAdapter(v, renamer));
    }

    public AnnotationVisitor visitArray(final String name) {
        final AnnotationVisitor v = av.visitArray(name);
        return v == null ? null : (v == av
                ? this
                : new RemappingAnnotationAdapter(v, renamer));
    }

    public void visitEnd() {
        av.visitEnd();
    }
}
