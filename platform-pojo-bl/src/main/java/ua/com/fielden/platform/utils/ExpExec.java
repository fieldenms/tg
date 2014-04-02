package ua.com.fielden.platform.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;

/**
 * Convenience class for handling string expression using JEXL. Added expressions are managed by List and therefore can be easily referred by index.
 * 
 * @author 01es
 * 
 * @param <T>
 */
public class ExpExec<T> {
    private final List<Expression> expressions = new ArrayList<Expression>();
    private final String instanceAlias;

    /**
     * Constructs an instance with instanceAlias parameter, which is used during late binding for naming the actual instance used for expression evaluation.
     * 
     * @param instanceAlias
     */
    public ExpExec(final String instanceAlias) {
        this.instanceAlias = instanceAlias;
    }

    /**
     * Convenience constructor
     * 
     * @param instanceAlias
     */
    public ExpExec() {
        this("obj");
    }

    /**
     * Instantiation JAXL Expression for the specified string expression. Calls to this method can be conveniently chained.
     * 
     * @param expression
     * @return
     * @throws RuntimeException
     */
    public ExpExec<T> add(final String expression) throws RuntimeException {
        try {
            expressions.add(ExpressionFactory.createExpression((instanceAlias + "." + expression.trim())));
            return this;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to create expression " + expression + ": " + e.getMessage());
        }
    }

    /**
     * Evaluates expression stored under index against a passed instance.
     * 
     * @param instance
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
    public Object eval(final T instance, final int index) {
        final JexlContext jc = JexlHelper.createContext();
        jc.getVars().put(instanceAlias, instance);
        try {
            return expressions.get(index).evaluate(jc);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to evaluate expression " + expressions.get(index) + ": " + e.getMessage());
        }
    }

    /**
     * Evaluates expression stored under index against a passed instance in a type safe manner, where parameter <code>klass</code> should specify the return type of the expression
     * being evaluated.
     * 
     * @param <V>
     * @param instance
     * @param index
     * @param klass
     * @return
     */
    public <V> V value(final T instance, final int index, final Class<V> klass) {
        return klass.cast(eval(instance, index));
    }

    /**
     * Invokes {@link #value(Object, int, Class)} with index set to 0. This method was introduced to simplify usage in case when there is only one expression to be evaluated.
     * 
     * @param <V>
     * @param instance
     * @param klass
     * @return
     */
    public <V> V value(final T instance, final Class<V> klass) {
        return klass.cast(eval(instance, 0));
    }

    public static Object eval(final Object instance, final String exp) {
        final JexlContext jc = JexlHelper.createContext();
        jc.getVars().put("obj", instance);
        try {
            return ExpressionFactory.createExpression("obj." + exp.trim()).evaluate(jc);
        } catch (final Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to evaluate expression " + exp + ": " + e.getMessage());
        }
    }
}
