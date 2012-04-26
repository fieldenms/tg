package ua.com.fielden.platform.reflection.asm.impl.remapper;

import com.google.inject.asm.AnnotationVisitor;
import com.google.inject.asm.Attribute;
import com.google.inject.asm.FieldVisitor;

/**
 * A <code>FieldVisitor</code> adapter for type remapping.
 *
 * @author Eugene Kuleshov
 */
public class RemappingFieldAdapter implements FieldVisitor {

    private final FieldVisitor fv;

    private final Remapper remapper;

    public RemappingFieldAdapter(final FieldVisitor fv, final Remapper remapper) {
        this.fv = fv;
        this.remapper = remapper;
    }

    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final AnnotationVisitor av = fv.visitAnnotation(remapper.mapDesc(desc), visible);
        return av == null ? null : new RemappingAnnotationAdapter(av, remapper);
    }

    public void visitAttribute(final Attribute attr) {
        fv.visitAttribute(attr);
    }

    public void visitEnd() {
        fv.visitEnd();
    }
}