package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.reflection.asm.impl.TypeMaker.GET_ORIG_TYPE_METHOD_NAME;

import org.kohsuke.asm5.ClassVisitor;
import org.kohsuke.asm5.Label;
import org.kohsuke.asm5.MethodVisitor;
import org.kohsuke.asm5.Opcodes;
import org.kohsuke.asm5.Type;

/**
 * A class adapter designed for adding a public static method {@code GET_ORIG_TYPE_METHOD_NAME} that returns the original type, which was used to derive the generated one from.
 *
 * @author TG Team
 *
 */
public class AdvancedRecordOriginalTypeAdapter extends ClassVisitor implements Opcodes {
    private final Class<?> origType;
    
    public AdvancedRecordOriginalTypeAdapter(final ClassVisitor cv, final Class<?> origType) {
        super(Opcodes.ASM5, cv);
        this.origType = origType;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    /**
     * Generates a new public static method to get the original type.
     */
    @Override
    public void visitEnd() {
        final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, GET_ORIG_TYPE_METHOD_NAME, "()Ljava/lang/Class;", "()Ljava/lang/Class<*>;", null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(27, l0);
        mv.visitLdcInsn(Type.getType(Type.getDescriptor(origType)));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
        super.visitEnd();
    }

}
