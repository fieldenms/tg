package ua.com.fielden.platform.reflection.asm.impl;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;

import com.google.inject.asm.ClassAdapter;
import com.google.inject.asm.ClassVisitor;
import com.google.inject.asm.MethodVisitor;
import com.google.inject.asm.Opcodes;


/**
 * A class adapter designed for changing a class supertype.
 *
 * @author TG Team
 *
 */
public class AdvancedChangeSupertypeAdapter extends ClassAdapter implements Opcodes {

    private final String supertype;

    public AdvancedChangeSupertypeAdapter(final String supertype, final ClassVisitor visitor) {
	super(visitor);
	this.supertype = supertype;
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, sets supertype to the specified one.
     */
    @Override
    public synchronized void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
	super.visit(version,  ACC_PUBLIC + ACC_SUPER, name, null, supertype, null);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        if  ("<init>".equals(name)) {
           return null;
        } else {
	    return super.visitMethod(access, name, desc, signature, exceptions);
	}

    }

    @Override
    public void visitEnd() {
	 MethodVisitor mv;
         {
                 mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                 mv.visitCode();
                 mv.visitVarInsn(ALOAD, 0);
                 mv.visitMethodInsn(INVOKESPECIAL, supertype, "<init>", "()V");
                 mv.visitInsn(RETURN);
                 mv.visitMaxs(1, 1);
                 mv.visitEnd();
         }
        super.visitEnd();
    }
}
