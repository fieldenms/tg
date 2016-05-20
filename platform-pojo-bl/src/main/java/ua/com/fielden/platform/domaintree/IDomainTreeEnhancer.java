package ua.com.fielden.platform.domaintree;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.CustomProperty;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.utils.Pair;

/**
 * This interface defines how domain can be enhanced via <b>calculated properties</b> management. <br>
 * <br>
 *
 * Domain consists of a tree of properties. A set of properties could be extended by <b>calculated properties</b> in particular branch of hierarchy. <br>
 * <br>
 *
 * The <b>calculated property</b> represents an abstraction for an expression which could be used in queries and their results exactly as simple property. <b>Calculated
 * property</b> could be added / removed / mutated from this interface. Mutated root domain entities meta-information could be obtained from {@link #getManagedType(Class)} method. <br>
 * <br>
 *
 * For example (root entity is Vehicle.class):<br>
 * 1. <i>Vehicle.doubleTanksQty := "2 * SUM([Vehicle.fuelUsages.oilQuantity]) / [Vehicle.techDetails.tankCapasity]"</i> (collectional aggregation, could contain an <i>aggregated
 * collectional</i> atoms or simple atoms -- adds as domain tree extension, could be queried/resulted)<br>
 * 2. <i>Vehicle.readingWarrantyBalance := "3 * [Vehicle.lastReading] - [Vehicle.techDetails.warrantyKms]"</i> (a couple of properties inside expression -- adds as domain tree
 * extension, could be queried/resulted)<br>
 * 2a.<i>Vehicle.yearsFrom1970OfInitDate := "YEAR([Vehicle.initDate]) - 1970"</i> (date or another property inside expression -- adds as domain tree extension, could be
 * queried/resulted)<br>
 * 3. <i>Vehicle.averageReading := "AVG([Vehicle.readingWarrantyBalance - 100000])"</i> (totals / analysisAggregation expression -- calculated property strictly <b>assigned</b> to
 * a property from which it is originated, appears in "origination" property context only)<br>
 *
 * @author TG Team
 *
 */
public interface IDomainTreeEnhancer extends IRootTyped {
    /**
     * Applies (commits) all domain changes. <b>Apply</b> action is <b>necessary</b> to complete domain enhancements and to load adjusted domain into memory.
     */
    void apply();

    /**
     * Discards (removes, rollbacks, cancels) all domain changes. <b>Discard</b> action gives an ability to start enhancements again.
     */
    void discard();

    /**
     * Returns an actual (possibly mutated with additional calculated properties) type for passed <code>type</code>. Returns the same type when <code>type</code> is not "root"
     * type.
     *
     * @param type
     *            -- an entity type, which "real" type is asked and which hierarchy is enhanced perhaps
     * @return
     */
    Class<?> getManagedType(final Class<?> type);

    /**
     * Returns a byte arrays that define an "actual" type hierarchy (possibly mutated with additional calculated properties) for passed <code>type</code>. Returns empty list if no
     * calculated properties exist.
     *
     * @param type
     *            -- an entity type, which "actual" type's byte arrays are asked
     * @return
     */
    List<ByteArray> getManagedTypeArrays(final Class<?> type);
    
    /**
     * Adjusts managed type name for <code>root</code> with a new name. This method is strictly applicable only to the roots which {@link #getManagedType(Class)} is generated and 
     * is used to provide correspondence between server-side and client side generated types naming in case where server-side type didn't exist and was generated from [user; miType; saveAsName] 
     * returned from client.
     * 
     * @param root
     * @param clientGeneratedTypeNameSuffix -- the suffix of generated type name from client application after '$$TgEntity_' part
     * @return
     */
    Class<?> adjustManagedTypeName(final Class<?> root, final String clientGeneratedTypeNameSuffix);

    /**
     * Adds the <code>calculatedProperty</code> to root type's {@link ICalculatedProperty#getRoot()} hierarchy. Throws {@link IncorrectCalcPropertyException} when the calculated
     * property is incorrect.<br>
     * <br>
     *
     * <i>Important</i> : the <code>calculatedProperty</code> should strictly be expressed in context of {@link ICalculatedProperty#contextType()} hierarchy.
     *
     * @param calculatedProperty
     *            -- fully defined calculated property to be added.
     */
    ICalculatedProperty addCalculatedProperty(final ICalculatedProperty calculatedProperty);

    /**
     * Creates a new calculated property based on provided meta-information and adds it to the root type's {@link ICalculatedProperty#getRoot()} hierarchy. Throws
     * {@link IncorrectCalcPropertyException} when the calculated property is incorrect.<br>
     * <br>
     *
     * @param root
     * @param contextPath
     * @param contextualExpression
     * @param title
     * @param desc
     * @param attribute
     * @param originationProperty
     */
    ICalculatedProperty addCalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty);

    /**
     * Creates a new calculated property based on provided meta-information and adds it to the root type's {@link ICalculatedProperty#getRoot()} hierarchy. Throws
     * {@link IncorrectCalcPropertyException} when the calculated property is incorrect.<br>
     * <br>
     *
     * @param root
     * @param contextPath
     * @param customPropertyName -- the desired property name for new property
     * @param contextualExpression
     * @param title
     * @param desc
     * @param attribute
     * @param originationProperty
     */
    ICalculatedProperty addCalculatedProperty(final Class<?> root, final String contextPath, final String customPropertyName, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty);

    /**
     * Removes the calculated property with a name <code>calculatedPropertyName</code>(dot-notation expression) from <code>rootType</code> hierarchy. Throws
     * {@link IncorrectCalcPropertyException} when the calculated property is incorrect.<br>
     * <br>
     *
     * @param rootType
     *            -- type of <b>root</b> entity, from which the calculated property should be removed (not derived type)
     * @param calculatedPropertyName
     *            -- the dot-notation expression name of calculated property to be removed
     */
    void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName);

    /**
     * Gets the calculated property with a name <code>calculatedPropertyName</code>(dot-notation expression) from <code>rootType</code> hierarchy. Throws
     * {@link IncorrectCalcPropertyException} when the calculated property name is incorrect.<br>
     * <br>
     *
     * @param rootType
     *            -- type of <b>root</b> entity, from which the calculated property should be obtained (not derived type).
     * @param calculatedPropertyName
     *            -- the dot-notation expression name of calculated property to be obtained.
     */
    ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName);

    /**
     * Copies the calculated property with a name <code>calculatedPropertyName</code>(dot-notation expression) from <code>rootType</code> hierarchy. Throws
     * {@link IncorrectCalcPropertyException} when the calculated property name is incorrect.<br>
     * <br>
     *
     * @param rootType
     *            -- type of <b>root</b> entity, from which the calculated property should be copied (not derived type).
     * @param calculatedPropertyName
     *            -- the dot-notation expression name of calculated property to be copied.
     */
    ICalculatedProperty copyCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName);

    /**
     * Indicates a situation when the name of calculated property is incorrect (for e.g. the place does not exist or name is not unique in the hierarchy).
     *
     * @author TG Team
     *
     */
    public class IncorrectCalcPropertyException extends Result {
        private static final long serialVersionUID = 435410515344805056L;

        public IncorrectCalcPropertyException(final String s) {
            super(null, new Exception(s));
        }
    }

    /**
     * Indicates a situation when the calculated property is correct but some warning exists.
     *
     * @author TG Team
     *
     */
    public class CalcPropertyWarning extends Warning {
        private static final long serialVersionUID = 435410515344805056L;

        public CalcPropertyWarning(final String s) {
            super(s);
        }
    }

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();

    Map<Class<?>, List<CalculatedProperty>> calculatedProperties();

    Map<Class<?>, List<CustomProperty>> customProperties();

    Map<Class<?>, Pair<Class<?>, Map<String, ByteArray>>> originalAndEnhancedRootTypesAndArrays();

    EntityFactory getFactory();

    /**
     * Creates a new 'custom' property based on provided meta-information and adds it to the root type's hierarchy.
     * <p>
     * Throws {@link IncorrectCalcPropertyException} when the 'custom' property is incorrect in context of other properties.<br>
     * <br>
     *
     * @param root
     * @param contextPath
     * @param name
     * @param title
     * @param desc
     * @param type
     */
    IDomainTreeEnhancer addCustomProperty(final Class<?> root, final String contextPath, final String name, final String title, final String desc, final Class<?> type);
}