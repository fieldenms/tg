package ua.com.fielden.platform.reflection.asm.impl;

import static net.bytebuddy.jar.asm.Opcodes.ACC_PUBLIC;
import static net.bytebuddy.jar.asm.Opcodes.ALOAD;
import static net.bytebuddy.jar.asm.Opcodes.ARETURN;
import static net.bytebuddy.jar.asm.Opcodes.ASM9;
import static net.bytebuddy.jar.asm.Opcodes.DRETURN;
import static net.bytebuddy.jar.asm.Opcodes.FRETURN;
import static net.bytebuddy.jar.asm.Opcodes.GETFIELD;
import static net.bytebuddy.jar.asm.Opcodes.IRETURN;
import static net.bytebuddy.jar.asm.Opcodes.RETURN;
import static net.bytebuddy.jar.asm.Type.getMethodDescriptor;
import static net.bytebuddy.jar.asm.Type.getType;
import static org.apache.commons.lang3.StringUtils.capitalize;

import java.lang.reflect.Type;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.pool.TypePool;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.utils.AsmUtils;

/**
 * A class adapter designed for adding accessor methods for properties to a class.
 *
 * @author TG Team
 *
 */
public class AddPropertyAccessorAdapter extends ClassVisitor {
    private String propName;
    private Type propType;
    private String ownerTypeName;
    
    /**
     * Returns an ASM wrapper to be used with ByteBuddy API.
     * @param propName
     * @param propType
     * @param ownerTypeName binary name (separated by "/") of the property owner
     * @return
     */
    public static AsmVisitorWrapper wrapper(final String propName, final Type propType, final String ownerTypeName) {
        return new AsmVisitorWrapper.AbstractBase() {
            @Override
            public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, 
                    Context implementationContext, TypePool typePool, 
                    FieldList<InDefinedShape> fields, MethodList<?> methods, 
                    int writerFlags, int readerFlags) 
            {
                return new AddPropertyAccessorAdapter(classVisitor, propName, propType, ownerTypeName);
            }
        };
    }

    public AddPropertyAccessorAdapter(final ClassVisitor cv, final String propName, final Type propType, final String ownerTypeName) {
        super(ASM9, cv);
        this.propName = propName;
        this.propType = propType;
        this.ownerTypeName = ownerTypeName;
    }

    /**
     * This is where new fields and their mutators are added.
     */
    @Override
    public void visitEnd() {
        addPropertyAccessor(cv);
        super.visitEnd();
    }

    private void addPropertyAccessor(ClassVisitor cv) {
        final net.bytebuddy.jar.asm.Type fieldType = getType(PropertyTypeDeterminator.classFrom(propType));
        final String fieldDescriptor = fieldType.getDescriptor();
        final String methodDescriptor = getMethodDescriptor(fieldType);
        final String methodSignature = getMethodSignature();
        final String prefix = propType.equals(Boolean.class) || propType.equals(boolean.class) ? 
                Accessor.IS.startsWith : Accessor.GET.startsWith;
        final String methodName = prefix + capitalize(propName);
        final MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, methodName, methodDescriptor, methodSignature, null);

        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, ownerTypeName, propName, fieldDescriptor);

        // areturn : reference
        // dreturn : double
        // freturn : float 
        // return  : void
        // ireturn : boolean|byte|char|short|int
        final Class<?> clazz = PropertyTypeDeterminator.classFrom(propType);
        if (clazz.isPrimitive()) {
            final int opcode = clazz.equals(double.class) ? DRETURN :
                               clazz.equals(float.class)  ? FRETURN :
                               clazz.equals(void.class)   ? RETURN  :
                               IRETURN;
            mv.visitInsn(opcode);
        }
        else mv.visitInsn(ARETURN);
        
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    private String getMethodSignature() {
        // accessor methods accept 0 arguments
        final StringBuilder sb = new StringBuilder("()");
        sb.append(AsmUtils.getTypeSignature(propType));
        return sb.toString();
    }
    
}