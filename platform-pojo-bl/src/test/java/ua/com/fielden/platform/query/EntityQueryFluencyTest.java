package ua.com.fielden.platform.query;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;

public class EntityQueryFluencyTest extends TestCase {

    private static List<Method> objectMethods = new ArrayList<Method>();

    private static Class<String> STRING = String.class;
    private static Class<String[]> STRINGS = String[].class;
    private static Class<IQueryModel> IQUERY = IQueryModel.class;
    private static Class<IQueryModel[]> IQUERIES = IQueryModel[].class;
    private static Class<Object> OBJECT = Object.class;
    private static Class<Object[]> OBJECTS = Object[].class;

    /**
     * Constants for all methods that are part of entity query fluent interface.
     */
    private static String select = "select";
    private static String from = "from";
    private static String where = "where";
    private static String join = "join";
    private static String leftJoin = "leftJoin";
    private static String joinFetch = "joinFetch";
    private static String leftJoinFetch = "leftJoinFetch";
    private static String fetchWith = "fetchWith";
    private static String orderBy = "orderBy";
    private static String produceModel = "produceModel";
    private static String the = "the";
    private static String gs = "gs";
    private static String notGs = "notGs";
    private static String and = "and";
    private static String or = "or";
    private static String exists = "exists";
    private static String notExists = "notExists";
    private static String isNull = "isNull";
    private static String isNotNull = "isNotNull";
    private static String isTrue = "isTrue";
    private static String isFalse = "isFalse";
    private static String inParams = "inParams";
    private static String in = "in";
    private static String notInParams = "notInParams";
    private static String notIn = "notIn";
    private static String likeParams = "likeParams";
    private static String like = "like";
    private static String notLikeParams = "notLikeParams";
    private static String notLike = "notLike";
    private static String eqParams = "eqParams";
    private static String eq = "eq";
    private static String eqThe = "eqThe";
    private static String ne = "ne";
    private static String neParams = "neParams";
    private static String ltParams = "ltParams";
    private static String lt = "lt";
    private static String gtParams = "gtParams";
    private static String gt = "gt";
    private static String leParams = "leParams";
    private static String le = "le";
    private static String geParams = "geParams";
    private static String ge = "ge";

    /**
     * List of the conditional methods names with their parameter classes.
     */
    private static Object[] conditions = new Object[] { eq, OBJECT, OBJECTS, eqParams, STRING, eqParams, STRING, OBJECT, eqThe, STRING, ne, OBJECT, OBJECTS, neParams, STRING,
	    neParams, STRING, OBJECT, gt, OBJECT, gtParams, STRING, gtParams, STRING, OBJECT, lt, OBJECT, ltParams, STRING, ltParams, STRING, OBJECT, ge, OBJECT, geParams, STRING,
	    geParams, STRING, OBJECT, le, OBJECT, leParams, STRING, leParams, STRING, OBJECT, isNull, isNotNull, isTrue, isFalse, in, OBJECT, OBJECTS, in, IQUERY, IQUERIES,
	    inParams, STRING, inParams, STRING, OBJECTS, notIn, OBJECT, OBJECTS, notIn, IQUERY, IQUERIES, notInParams, STRING, notInParams, STRING, OBJECTS, like, OBJECT, OBJECTS,
	    likeParams, STRING, likeParams, STRING, OBJECT, notLike, OBJECT, OBJECTS, notLikeParams, STRING, notLikeParams, STRING, OBJECT };

    /**
     * Represents name and parameter classes of the class method.
     * 
     * @author nc
     * 
     */
    class MethodEntry {
	String name;
	Class<?>[] paramClasses = null;

	public MethodEntry(final String name, final Class<?>... paramClasses) {
	    this.name = name;
	    this.paramClasses = (paramClasses != null && paramClasses.length != 0) ? paramClasses : null;
	}

	public MethodEntry(final Method method) {
	    this.name = method.getName();
	    this.paramClasses = method.getParameterTypes().length != 0 ? method.getParameterTypes() : null;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj) {
		return true;
	    }
	    if (!(obj instanceof MethodEntry)) {
		return false;
	    }

	    return (name.equals(((MethodEntry) obj).getName()) && Arrays.equals(paramClasses, ((MethodEntry) obj).getParamClasses()));
	}

	@Override
	public int hashCode() {
	    return getName().hashCode() * 23;
	}

	public String getName() {
	    return name;
	}

	public Class<?>[] getParamClasses() {
	    return paramClasses;
	}

	@Override
	public String toString() {
	    return "Method: " + getName();
	}
    }

    {
	for (int i = 0; i < Object.class.getMethods().length; i++) {
	    objectMethods.add(Object.class.getMethods()[i]);
	}
    }

    public void testSelect() throws SecurityException, NoSuchMethodException {

	//	final Object[] initialStatement = new Object[] {select, STRING, STRINGS};
	//
	//	final List<MethodEntry> validMethods = transform(from, STRING, from, STRING, STRING);
	//
	//	performTest(initialStatement, validMethods);
    }

    //
    //    public void testFrom() throws SecurityException, NoSuchMethodException {
    //
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING};
    //
    //	final List<MethodEntry> validMethods = transform(where,
    //							join, STRING,
    //							join, STRING, STRING,
    //							leftJoin, STRING,
    //							leftJoin, STRING, STRING,
    //							joinFetch, STRING, STRING,
    //							leftJoinFetch, STRING, STRING,
    //							fetchWith, STRING, IQUERY,
    //							orderBy, STRING, STRINGS,
    //							produceModel);
    //	performTest(initialStatement, validMethods);
    //    }
    //
    //    public void testWhere() throws SecurityException, NoSuchMethodException {
    //
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING, where};
    //
    //	final List<MethodEntry> validMethods = transform(the, STRING,
    //							gs,
    //							exists, IQUERY,
    //							notGs,
    //							notExists, IQUERY
    //							);
    //	performTest(initialStatement, validMethods);
    //    }
    //
    //    public void testThe() throws SecurityException, NoSuchMethodException {
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING, where, the, STRING};
    //
    //	final  List<MethodEntry> validMethods = transform(conditions);
    //
    //	performTest(initialStatement, validMethods);
    //    }
    //
    //    public void testGroupStart() throws SecurityException, NoSuchMethodException {
    //
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING, where, gs};
    //
    //	final  List<MethodEntry> validMethods = transform(the, STRING,
    //							gs,
    //							notGs,
    //							notExists, IQUERY,
    //							exists, IQUERY);
    //
    //	performTest(initialStatement, validMethods);
    //    }
    //
    //    public void testExists() throws SecurityException, NoSuchMethodException {
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING, where, exists, IQUERY};
    //
    //	final  List<MethodEntry> validMethods = transform(and,
    //							or,
    //							orderBy, STRING, STRINGS,
    //							produceModel);
    //
    //	performTest(initialStatement, validMethods);
    //    }
    //
    //    public void testPropertyCondition() throws SecurityException, NoSuchMethodException {
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING, where, the, STRING, not, isNull};
    //
    //	final  List<MethodEntry> validMethods = transform(and,
    //							or,
    //							orderBy, STRING, STRINGS,
    //							produceModel
    //							);
    //
    //	System.out.println("testPropertyCondition");
    //	performTest(initialStatement, validMethods);
    //    }
    //
    //    public void testAndAfterConditionOnProperty() throws SecurityException, NoSuchMethodException {
    //	final Object[] initialStatement = new Object[] {select, STRING, STRINGS, from, STRING, where, the, STRING, not, isNull, and};
    //
    //	final  List<MethodEntry> validMethods = transform(exists, IQUERY,
    //							notExists, IQUERY,
    //							the, STRING,
    //							gs,
    //							notGs
    //							);
    //	validMethods.addAll(transform(conditions));
    //
    //	performTest(initialStatement, validMethods);
    //    }

    /**
     * Get list of the given class methods except those that are inherited from Object
     * 
     * @param inspectedClass
     * @return
     */
    private List<Method> dissectObjectMethods(final Class<?> inspectedClass) {
	final List<Method> result = new ArrayList<Method>();
	for (int i = 0; i < inspectedClass.getMethods().length; i++) {
	    final Method method = inspectedClass.getMethods()[i];
	    if (!objectMethods.contains(method)) {
		result.add(method);
	    }
	}

	return result;
    }

    /**
     * Get first method that fails during chaining
     * 
     * @param inspectedClass
     * @param methods
     * @return
     */
    private MethodEntry getFirstMethodThatFailsDuringChaining(final Class<?> inspectedClass, final MethodEntry[] methods) {
	Class<?> currentReturnType = inspectedClass;
	for (final MethodEntry methodEntry : methods) {
	    try {
		currentReturnType = currentReturnType.getMethod(methodEntry.name, methodEntry.paramClasses).getReturnType();
	    } catch (final SecurityException e) {
		e.printStackTrace();
	    } catch (final NoSuchMethodException e) {
		return methodEntry;
	    }
	}
	return null;
    }

    /**
     * Gets the class, which is returned as a result of chaining the provided list of methods.
     * 
     * @param inspectedClass
     * @param methods
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private Class<?> getChainedMethodsClass(final Class<?> inspectedClass, final List<MethodEntry> methods) throws SecurityException, NoSuchMethodException {
	Class<?> currentReturnClass = inspectedClass;
	Type currentReturnType = inspectedClass;
	for (final MethodEntry methodEntry : methods) {
	    //currentReturnType = currentReturnClass.getMethod(methodEntry.name, methodEntry.paramClasses).getReturnType();
	    currentReturnType = currentReturnClass.getMethod(methodEntry.name, methodEntry.paramClasses).getGenericReturnType();

	    if (currentReturnType instanceof ParameterizedType) {
		System.out.println("   ParameterizedType: " + currentReturnType);
		final ParameterizedType type = (ParameterizedType) currentReturnType;
		final Type[] typeArguments = type.getActualTypeArguments();
		currentReturnClass = (Class) typeArguments[0];
	    } else {
		currentReturnClass = currentReturnClass.getMethod(methodEntry.name, methodEntry.paramClasses).getReturnType();
	    }

	}
	return currentReturnClass;
    }

    private Class<?> getChainedMethodsClasses(final Class<?> inspectedClass, final Object... objects) throws SecurityException, NoSuchMethodException {
	return getChainedMethodsClass(inspectedClass, transform(objects));
    }

    /**
     * Check that all not-valid (those that exist but are not contained in the list of valid) are really invalid (fail during chaining)
     */
    private MethodEntry getFirstMethodThatShouldFailButSucceeded(final Class<?> inspectedClass, final List<MethodEntry> validMethods) {

	for (final Method method : dissectObjectMethods(inspectedClass)) {
	    final MethodEntry methodEntry = new MethodEntry(method);
	    if (!validMethods.contains(methodEntry)) {
		if (getFirstMethodThatFailsDuringChaining(inspectedClass, new MethodEntry[] { methodEntry }) == null
			|| (getFirstMethodThatFailsDuringChaining(inspectedClass, new MethodEntry[] { methodEntry }) != null && !getFirstMethodThatFailsDuringChaining(inspectedClass, new MethodEntry[] { methodEntry }).equals(methodEntry))) {
		    return methodEntry;
		}
	    }
	}

	return null;
    }

    /**
     * Check that all valid methods are really valid (are contained in all methods and are successfully chained
     * 
     * @param inspectedClass
     * @param methods
     * @return
     */
    private MethodEntry getFirstMethodThatShouldSucceedButFailed(final Class<?> inspectedClass, final List<MethodEntry> validMethods) {

	final List<MethodEntry> allMethods = new ArrayList<MethodEntry>();
	for (final Method method : dissectObjectMethods(inspectedClass)) {
	    allMethods.add(new MethodEntry(method));
	}

	for (final MethodEntry validMethodEntry : validMethods) {
	    if (allMethods.contains(validMethodEntry)) {
		if (getFirstMethodThatFailsDuringChaining(inspectedClass, new MethodEntry[] { validMethodEntry }) != null) {
		    return validMethodEntry;
		}
	    } else {
		return validMethodEntry;
	    }
	}

	return null;
    }

    /**
     * Produces sequence of {@link MethodEntry} instances out of the sequence of their names followed by parameter classes if such exist.
     * 
     * @param objects
     * @return
     */
    private List<MethodEntry> transform(final Object... objects) {
	final List<MethodEntry> result = new ArrayList<MethodEntry>();
	String currentMethodName = (String) objects[0];
	final List<Class<?>> currClasses = new ArrayList<Class<?>>(0);
	final Class<?>[] a = new Class[] {};
	for (int i = 1; i < objects.length; i++) {
	    final Object object = objects[i];

	    if (object instanceof String) {
		result.add(new MethodEntry(currentMethodName, currClasses.toArray(a)));
		currentMethodName = (String) object;
		currClasses.clear();
	    } else if (object instanceof Class) {
		currClasses.add((Class<?>) object);
	    }
	}
	result.add(new MethodEntry(currentMethodName, currClasses.toArray(a)));

	return result;
    }

    /**
     * Produces string representation for initial statements sequence. The intention is to provide test fail message with maximum details.
     * 
     * @param initialStatement
     * @return
     */
    private String getStringRepresentation(final Object[] initialStatement) {
	final StringBuffer initialStatementRepresentation = new StringBuffer();

	for (final Object object : initialStatement) {
	    if (object instanceof String) {
		initialStatementRepresentation.append(object.toString());
		initialStatementRepresentation.append(",");
	    } else {
		initialStatementRepresentation.append(",");
	    }
	}

	return initialStatementRepresentation.toString().replace(",,", "(..).").replace(",", "().");
    }

    /**
     * Performs actual testing - tests that list of the methods available from the type returned as a result of the initial statement is identical to the list of provided valid
     * methods. In case of any mismatch the test failure is reported.
     * 
     * @param initialStatement
     * @param validMethods
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    //    private void performTest(final Object[] initialStatement, final List<MethodEntry> validMethods) throws SecurityException, NoSuchMethodException {
    //	final Class<?> inspectedClass = getChainedMethodsClasses(IFrom.class, initialStatement);
    //	System.out.println(inspectedClass);
    //
    //	final MethodEntry methodThatShouldHaveFailed = getFirstMethodThatShouldFailButSucceeded(inspectedClass, validMethods);
    //	assertNull(methodThatShouldHaveFailed + " should not be accessible from: " + getStringRepresentation(initialStatement), methodThatShouldHaveFailed);
    //
    //	final MethodEntry methodThatShouldHaveSucceeded = getFirstMethodThatShouldSucceedButFailed(inspectedClass, validMethods);
    //	assertNull(methodThatShouldHaveSucceeded + " should be accessible from: " + getStringRepresentation(initialStatement), methodThatShouldHaveSucceeded);
    //    }
}
