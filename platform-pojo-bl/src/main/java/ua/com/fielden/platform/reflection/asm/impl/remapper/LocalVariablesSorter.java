package ua.com.fielden.platform.reflection.asm.impl.remapper;

import com.google.inject.asm.Label;
import com.google.inject.asm.MethodAdapter;
import com.google.inject.asm.MethodVisitor;
import com.google.inject.asm.Opcodes;
import com.google.inject.asm.Type;

/**
 * A {@link MethodAdapter} that renumbers local variables in their order of
 * appearance. This adapter allows one to easily add new local variables to a
 * method. It may be used by inheriting from this class, but the preferred way
 * of using it is via delegation: the next visitor in the chain can indeed add
 * new locals when needed by calling {@link #newLocal} on this adapter (this
 * requires a reference back to this {@link LocalVariablesSorter}).
 *
 * @author Chris Nokleberg
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public class LocalVariablesSorter extends MethodAdapter {

    private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

    /**
     * Mapping from old to new local variable indexes. A local variable at index
     * i of size 1 is remapped to 'mapping[2*i]', while a local variable at
     * index i of size 2 is remapped to 'mapping[2*i+1]'.
     */
    private int[] mapping = new int[40];

    /**
     * Array used to store stack map local variable types after remapping.
     */
    private Object[] newLocals = new Object[20];

    /**
     * Index of the first local variable, after formal parameters.
     */
    protected final int firstLocal;

    /**
     * Index of the next local variable to be created by {@link #newLocal}.
     */
    protected int nextLocal;

    /**
     * Indicates if at least one local variable has moved due to remapping.
     */
    private boolean changed;

    /**
     * Creates a new {@link LocalVariablesSorter}.
     *
     * @param access access flags of the adapted method.
     * @param desc the method's descriptor (see {@link Type Type}).
     * @param mv the method visitor to which this adapter delegates calls.
     */
    public LocalVariablesSorter(
        final int access,
        final String desc,
        final MethodVisitor mv)
    {
        super(mv);
        final Type[] args = Type.getArgumentTypes(desc);
        nextLocal = (Opcodes.ACC_STATIC & access) == 0 ? 1 : 0;
        for (int i = 0; i < args.length; i++) {
            nextLocal += args[i].getSize();
        }
        firstLocal = nextLocal;
    }

    public void visitVarInsn(final int opcode, final int var) {
        Type type;
        switch (opcode) {
            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
                type = Type.LONG_TYPE;
                break;

            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                type = Type.DOUBLE_TYPE;
                break;

            case Opcodes.FLOAD:
            case Opcodes.FSTORE:
                type = Type.FLOAT_TYPE;
                break;

            case Opcodes.ILOAD:
            case Opcodes.ISTORE:
                type = Type.INT_TYPE;
                break;

            default:
            // case Opcodes.ALOAD:
            // case Opcodes.ASTORE:
            // case RET:
                type = OBJECT_TYPE;
                break;
        }
        mv.visitVarInsn(opcode, remap(var, type));
    }

    public void visitIincInsn(final int var, final int increment) {
        mv.visitIincInsn(remap(var, Type.INT_TYPE), increment);
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        mv.visitMaxs(maxStack, nextLocal);
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
        final int newIndex = remap(index, Type.getType(desc));
        mv.visitLocalVariable(name, desc, signature, start, end, newIndex);
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
        if (type != Opcodes.F_NEW) { // uncompressed frame
            throw new IllegalStateException("ClassReader.accept() should be called with EXPAND_FRAMES flag");
        }

        if (!changed) { // optimization for the case where mapping = identity
            mv.visitFrame(type, nLocal, local, nStack, stack);
            return;
        }

        // creates a copy of newLocals
        final Object[] oldLocals = new Object[newLocals.length];
        System.arraycopy(newLocals, 0, oldLocals, 0, oldLocals.length);

        // copies types from 'local' to 'newLocals'
        // 'newLocals' already contains the variables added with 'newLocal'

        int index = 0; // old local variable index
        int number = 0; // old local variable number
        for (; number < nLocal; ++number) {
            final Object t = local[number];
            final int size = t == Opcodes.LONG || t == Opcodes.DOUBLE ? 2 : 1;
            if (t != Opcodes.TOP) {
                Type typ = OBJECT_TYPE;
                if (t == Opcodes.INTEGER) {
                    typ = Type.INT_TYPE;
                } else if (t == Opcodes.FLOAT) {
                    typ = Type.FLOAT_TYPE;
                } else if (t == Opcodes.LONG) {
                    typ = Type.LONG_TYPE;
                } else if (t == Opcodes.DOUBLE) {
                    typ = Type.DOUBLE_TYPE;
                } else if (t instanceof String) {
                    typ = Type.getObjectType((String) t);
                }
                setFrameLocal(remap(index, typ), t);
            }
            index += size;
        }

        // removes TOP after long and double types as well as trailing TOPs

        index = 0;
        number = 0;
        for (int i = 0; index < newLocals.length; ++i) {
            final Object t = newLocals[index++];
            if (t != null && t != Opcodes.TOP) {
                newLocals[i] = t;
                number = i + 1;
                if (t == Opcodes.LONG || t == Opcodes.DOUBLE) {
                    index += 1;
                }
            } else {
                newLocals[i] = Opcodes.TOP;
            }
        }

        // visits remapped frame
        mv.visitFrame(type, number, newLocals, nStack, stack);

        // restores original value of 'newLocals'
        newLocals = oldLocals;
    }

    // -------------

    /**
     * Creates a new local variable of the given type.
     *
     * @param type the type of the local variable to be created.
     * @return the identifier of the newly created local variable.
     */
    public int newLocal(final Type type) {
        Object t;
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                t = Opcodes.INTEGER;
                break;
            case Type.FLOAT:
                t = Opcodes.FLOAT;
                break;
            case Type.LONG:
                t = Opcodes.LONG;
                break;
            case Type.DOUBLE:
                t = Opcodes.DOUBLE;
                break;
            case Type.ARRAY:
                t = type.getDescriptor();
                break;
            // case Type.OBJECT:
            default:
                t = type.getInternalName();
                break;
        }
        final int local = nextLocal;
        nextLocal += type.getSize();
        setLocalType(local, type);
        setFrameLocal(local, t);
        return local;
    }

    /**
     * Sets the current type of the given local variable. The default
     * implementation of this method does nothing.
     *
     * @param local a local variable identifier, as returned by {@link #newLocal
     *        newLocal()}.
     * @param type the type of the value being stored in the local variable
     */
    protected void setLocalType(final int local, final Type type) {
    }

    private void setFrameLocal(final int local, final Object type) {
        final int l = newLocals.length;
        if (local >= l) {
            final Object[] a = new Object[Math.max(2 * l, local + 1)];
            System.arraycopy(newLocals, 0, a, 0, l);
            newLocals = a;
        }
        newLocals[local] = type;
    }

    private int remap(final int var, final Type type) {
        if (var + type.getSize() <= firstLocal) {
            return var;
        }
        final int key = 2 * var + type.getSize() - 1;
        final int size = mapping.length;
        if (key >= size) {
            final int[] newMapping = new int[Math.max(2 * size, key + 1)];
            System.arraycopy(mapping, 0, newMapping, 0, size);
            mapping = newMapping;
        }
        int value = mapping[key];
        if (value == 0) {
            value = newLocalMapping(type);
            setLocalType(value, type);
            mapping[key] = value + 1;
        } else {
            value--;
        }
        if (value != var) {
            changed = true;
        }
        return value;
    }

    protected int newLocalMapping(final Type type) {
        final int local = nextLocal;
        nextLocal += type.getSize();
        return local;
    }
}
