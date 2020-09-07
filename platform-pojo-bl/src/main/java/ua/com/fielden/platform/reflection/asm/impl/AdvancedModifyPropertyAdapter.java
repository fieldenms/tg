package ua.com.fielden.platform.reflection.asm.impl;

import static ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService.nextTypeName;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.asm5.AnnotationVisitor;
import org.kohsuke.asm5.Attribute;
import org.kohsuke.asm5.ClassVisitor;
import org.kohsuke.asm5.FieldVisitor;
import org.kohsuke.asm5.MethodVisitor;
import org.kohsuke.asm5.Opcodes;
import org.kohsuke.asm5.Type;

import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;

/**
 * A class adapter designed for modification of existing fields based on the new specification.
 *
 * @author TG Team
 *
 */
public class AdvancedModifyPropertyAdapter extends ClassVisitor implements Opcodes {

    /**
     * Mapping between the type names, property names and their respective new types.
     */
    private final Map<String, NewProperty> propertiesToAdapt;
    /**
     * Type name that is being put through the adapter.
     */
    private String owner;
    private String enhancedName;

    public AdvancedModifyPropertyAdapter(final ClassVisitor cv, final Map<String, NewProperty> propertiesToAdapt) {
        super(Opcodes.ASM5, cv);
        this.propertiesToAdapt = propertiesToAdapt;
    }

    /**
     * {@inheritDoc}
     *
     * Additionally, modifies and records the name of the currently being traversed class.
     */
    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
        owner = name;
        enhancedName = nextTypeName(name);
        super.visit(version, access, enhancedName, signature, superName, interfaces);
    }

    /**
     * Visits fields declaration with either description of the replacing type (if propertiesToAdapt contains such an entry) or the supplied one.
     */
    @Override
    public FieldVisitor visitField(final int access, final String fieldName, final String desc, final String signature, final Object value) {
        // the value of signature represents generic type parameter information
        // in case the property needs to be modified and it is collectional (i.e. Collection, Set etc.) then may need to replace its signature
        // the same goes for the corresponding setter and getter

        final NewProperty newProperty = propertiesToAdapt.get(fieldName);

        if (newProperty != null) {
            if (newProperty.changeSignature) {
                final String signatureMode = signature.substring(0, signature.indexOf("<") + 1) + Type.getDescriptor(newProperty.type)
                        + signature.substring(signature.indexOf(">"));
                return new CollectionalFieldVisitor(super.visitField(access, fieldName, desc, signatureMode, value), newProperty);
            } else {
                return super.visitField(access, fieldName, Type.getDescriptor(newProperty.type), signature, value);
            }
        } else {
            return super.visitField(access, fieldName, desc, signature, value);
        }
    }

    /**
     * Replaces references to the owner in methods with the enhancedName. Also, changes setter and getter to reference a property replacement type if appropriate.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final String descMod;
        final String signatureMod;
        if (Mutator.isMutator(name) || name.startsWith("get")) {
            final String fieldName = name.startsWith("get") ? name.substring(3, 4).toLowerCase() + name.substring(4) : Mutator.deducePropertyNameFromMutator(name);
            final NewProperty newProperty = propertiesToAdapt.get(fieldName);
            if (newProperty != null) {
                // get: ()Lua/com/fielden/platform/reflection/asm/impl/entities/EntityBeingEnhanced;
                // set: (Lua/com/fielden/platform/reflection/asm/impl/entities/EntityBeingEnhanced;)V
                if (newProperty.changeSignature) { // collectional property
                    //collectional get desc and signature: ()Ljava/util/Collection;                 ()Ljava/util/Collection<Lua/com/fielden/platform/reflection/asm/impl/entities/EntityBeingEnhanced;>;
                    //collectional set desc and signature: (Ljava/util/Collection;)V                 (Ljava/util/Collection<Lua/com/fielden/platform/reflection/asm/impl/entities/EntityBeingEnhanced;>;)V
                    if (name.startsWith("get") || name.startsWith(Mutator.SETTER.startsWith)) {
                        descMod = desc;
                        final String paramTypeReplacement = Type.getDescriptor(newProperty.type);
                        signatureMod = signature.substring(0, signature.indexOf("<") + 1) + paramTypeReplacement + signature.substring(signature.indexOf(">"));
                    } else {
                        descMod = "(" + Type.getDescriptor(newProperty.type) + ")" + Type.getReturnType(desc).getDescriptor();
                        signatureMod = signature;
                    }
                } else {
                    descMod = name.startsWith("get") ? // are we handling getter?
                    ("()" + Type.getDescriptor(newProperty.type))
                            : // getter
                            "(" + Type.getDescriptor(newProperty.type) + ")" + Type.getReturnType(desc).getDescriptor(); // setter
                    signatureMod = signature;
                }
            } else {
                descMod = desc;
                signatureMod = signature;
            }
        } else {
            descMod = desc;
            signatureMod = signature;
        }
        final MethodVisitor mv = cv.visitMethod(access, name, fix(descMod), fix(signatureMod), exceptions);
        // check if the method is not abstract
        return mv != null && (access & ACC_ABSTRACT) == 0 ? new MethodRenamer(mv) : mv;
    }

    public String getOwner() {
        return owner;
    }

    public String getEnhancedName() {
        return enhancedName;
    }

    /**
     * Changes all the occurrences of <code>owner<code> with <code>enhancedName</code>.
     */
    private String fix(String signiture) {

        if (signiture != null) {
            if (signiture.indexOf(owner + ";") != -1) {
                signiture = signiture.replaceAll(Pattern.quote(owner + ";"), Matcher.quoteReplacement(enhancedName + ";"));
            }
        }

        return signiture;
    }

    /**
     * MethodAdapter is a CodeVisitor ie a visitor to visit the bytecode instructions of a Java method.
     *
     */
    private class MethodRenamer extends MethodVisitor {

        public MethodRenamer(final MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        /**
         * Visits method type instruction. A type instruction is an instruction that takes a type descriptor as parameter.
         *
         * @param opcode
         *            - the opcode of the type instruction to be visited. This opcode is either NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
         *
         * @param desc
         *            - the operand of the instruction to be visited. This operand is must be a fully qualified class name in internal form, or the type descriptor of an array type
         *            (see Type).
         */
        @Override
        public void visitTypeInsn(final int opcode, String desc) {
            if (owner.equals(desc)) {
                desc = enhancedName;
            }
            mv.visitTypeInsn(opcode, desc);
        }

        /**
         * Visits method field instruction. A field instruction is an instruction that loads or stores the value of a field of an object.
         *
         * @param opcode
         *            - the opcode of the type instruction to be visited. This opcode is either GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
         * @param owner
         *            - the internal name of the field's owner class (see getInternalName).
         * @param name
         *            - the field's name.
         * @param desc
         *            - the field's descriptor (see Type).
         */
        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
            final NewProperty newProperty = propertiesToAdapt.get(name);

            final String descMod = newProperty == null || newProperty.changeSignature ? desc : Type.getDescriptor(newProperty.type);

            if (AdvancedModifyPropertyAdapter.this.owner.equals(owner)) {
                mv.visitFieldInsn(opcode, enhancedName, name, fix(descMod));
            } else {
                mv.visitFieldInsn(opcode, owner, name, fix(descMod));
            }
        }

        /**
         * Visits method instruction. A method instruction is an instruction that invokes a method.
         *
         * @param opcode
         *            - the opcode of the type instruction to be visited. This opcode is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or INVOKEINTERFACE.
         * @param owner
         *            - the internal name of the method's owner class (see getInternalName).
         * @param name
         *            - the method's name.
         * @param desc
         *            - the method's descriptor (see Type).
         */
        @Override
        public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
            if (AdvancedModifyPropertyAdapter.this.owner.equals(owner)) {
                mv.visitMethodInsn(opcode, enhancedName, name, fix(desc));
            } else {
                mv.visitMethodInsn(opcode, owner, name, fix(desc));
            }
        }

    }

    /**
     * Field visitor for collectional properties, which modifies value for {@link IsProperty} annotation according to the specified new property information, and assigns
     * <code>linkProperty</code> attribute with its original value in case annotation was present originally.
     *
     */
    private class CollectionalFieldVisitor extends FieldVisitor {

        private final FieldVisitor fv;
        private final NewProperty np;

        public CollectionalFieldVisitor(final FieldVisitor fv, final NewProperty np) {
            super(org.kohsuke.asm5.Opcodes.ASM5, fv);
            this.fv = fv;
            this.np = np;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
            return desc.equals(Type.getDescriptor(IsProperty.class)) ? null : fv.visitAnnotation(desc, visible);
        }

        @Override
        public void visitAttribute(final Attribute arg0) {
            fv.visitAttribute(arg0);
        }

        @Override
        public void visitEnd() {
            try {
                final Class<?> originalType = ClassLoader.getSystemClassLoader().loadClass(owner.replace('/', '.'));
                final Field field = Finder.findFieldByName(originalType, np.name);

                final AnnotationVisitor av = fv.visitAnnotation(Type.getDescriptor(IsProperty.class), true);
                av.visit("value", Type.getType(np.type));

                if (AnnotationReflector.isAnnotationPresent(field, IsProperty.class)) {
                    av.visit("linkProperty", AnnotationReflector.getAnnotation(field, IsProperty.class).linkProperty());
                }

                av.visitEnd();
                fv.visitEnd();

            } catch (final Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

    }
}
