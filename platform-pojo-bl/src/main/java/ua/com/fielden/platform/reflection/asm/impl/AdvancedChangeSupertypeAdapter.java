package ua.com.fielden.platform.reflection.asm.impl;

import static net.bytebuddy.jar.asm.Opcodes.ACC_PUBLIC;
import static net.bytebuddy.jar.asm.Opcodes.ACC_SUPER;
import static net.bytebuddy.jar.asm.Opcodes.ALOAD;
import static net.bytebuddy.jar.asm.Opcodes.ASM9;
import static net.bytebuddy.jar.asm.Opcodes.INVOKESPECIAL;
import static net.bytebuddy.jar.asm.Opcodes.RETURN;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.pool.TypePool;

/**
 * A {@link ClassVisitor} that changes the supertype name of a class.
 *
 * @author TG Team
 *
 */
public class AdvancedChangeSupertypeAdapter extends ClassVisitor {

    private final String supertype;

    /**
     * Creates an instance of this adapter and wraps it in order to be used with 
     * {@link net.bytebuddy.dynamic.DynamicType.Builder#visit(AsmVisitorWrapper)}.
     * 
     * @param supertype
     * @return
     */
    public static AsmVisitorWrapper asAsmVisitorWrapper(final String supertype) {
        return new AsmVisitorWrapper.AbstractBase() {
           @Override
           public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, 
                   Context implementationContext, TypePool typePool, 
                   FieldList<InDefinedShape> fields, MethodList<?> methods, 
                   int writerFlags, int readerFlags) 
           {
               // wrap this adapter
               return new AdvancedChangeSupertypeAdapter(supertype.replace('.', '/'), classVisitor);
           }
       };
    }
    
    public AdvancedChangeSupertypeAdapter(final String supertype, final ClassVisitor visitor) {
        super(ASM9, visitor);
        this.supertype = supertype;
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, sets the supertype name to the specified one.
     */
    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        super.visit(version, ACC_PUBLIC + ACC_SUPER, name, null, supertype, null);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        // prevent the class visitor to which we delegate to fiddle with the constructors
        if ("<init>".equals(name)) {
            return null;
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    @Override
    public void visitEnd() {
        // modify the empty constructor to call the correct super() method
        MethodVisitor mv;
        {
            mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, supertype, "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        super.visitEnd();
    }
}
