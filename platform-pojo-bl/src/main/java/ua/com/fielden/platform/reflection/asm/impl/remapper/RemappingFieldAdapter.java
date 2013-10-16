package ua.com.fielden.platform.reflection.asm.impl.remapper;

import org.kohsuke.asm3.AnnotationVisitor;
import org.kohsuke.asm3.Attribute;
import org.kohsuke.asm3.FieldVisitor;

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

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final AnnotationVisitor av = fv.visitAnnotation(remapper.mapDesc(desc), visible);
        return av == null ? null : new RemappingAnnotationAdapter(av, remapper);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        fv.visitAttribute(attr);
    }

    @Override
    public void visitEnd() {
        fv.visitEnd();
    }
}