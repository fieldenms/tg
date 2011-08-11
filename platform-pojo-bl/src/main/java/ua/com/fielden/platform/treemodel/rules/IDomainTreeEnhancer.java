package ua.com.fielden.platform.treemodel.rules;



/**
 * This interface defines how domain can be enhanced via <b>calculated properties</b> management. <br><br>
 *
 * Domain consists of a tree of properties. A set of properties could be extended by <b>calculated properties</b> in particular branch of hierarchy. <br><br>
 *
 * The <b>calculated property</b> represents an abstraction for an expression which could be used in queries
 * and their results exactly as simple property. <b>Calculated property</b> could be added / removed / mutated from this interface.
 * Mutated root domain entities meta-information could be obtained from {@link #getManagedType(Class)} method. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * For example (root entity is Vehicle.class):<br>
 * 1. <i>Vehicle.doubleTanksQty := "2 * SUM([Vehicle.fuelUsages.oilQuantity]) / [Vehicle.techDetails.tankCapasity]"</i> (collectional aggregation, could contain an <i>aggregated collectional</i> atoms or simple atoms -- adds as domain tree extension, could be queried/resulted)<br>
 * 2. <i>Vehicle.readingWarrantyBalance := "3 * [Vehicle.lastReading] - [Vehicle.techDetails.warrantyKms]"</i>  (a couple of properties inside expression -- adds as domain tree extension, could be queried/resulted)<br>
 * 2a.<i>Vehicle.yearsFrom1970OfInitDate := "YEAR([Vehicle.initDate]) - 1970"</i>  (date or another property inside expression -- adds as domain tree extension, could be queried/resulted)<br>
 * 3. <i>Vehicle.averageReading := "AVG([Vehicle.readingWarrantyBalance - 100000])"</i>  (totals / analysisAggregation expression -- calculated property strictly <b>assigned</b> to a property from which it is originated, appears in "origination" property context only)<br>
 *
 * @author TG Team
 *
 */
public interface IDomainTreeEnhancer /* extends Serializable */ {
    /**
     * Applies (commits) all domain changes. <b>Apply</b> action is <b>necessary</b> to complete domain enhancements and to load adjusted domain into memory.
     */
    void apply();

    /**
     * Discards (removes, rollbacks, cancels) all domain changes. <b>Discard</b> action gives an ability to start enhancements again.
     */
    void discard();

    /**
     * Returns an actual (possibly mutated with additional calculated properties) type for passed <code>type</code>.
     * Returns the same type when <code>type</code> is not "root" type.
     *
     * @param type -- an entity type, which "real" type is asked and which hierarchy is enhanced perhaps
     * @return
     */
    Class<?> getManagedType(final Class<?> type);

    /**
     * Adds the <code>calculatedProperty</code> to root type's {@link ICalculatedProperty#getRootType()} hierarchy into path {@link ICalculatedProperty#getPath()}.
     * Throws {@link IncorrectPlaceException} when the place (the hierarchy branch) for calculated property is incorrect (for e.g. does not exist).<br><br>
     *
     * <i>Important</i> : the <code>calculatedProperty</code> should strictly be expressed in context of {@link ICalculatedProperty#getParentType()} hierarchy.
     *
     * @param calculatedProperty -- fully defined calculated property to be added.
     */
    void addCalculatedProperty(final ICalculatedProperty calculatedProperty);

    /**
     * Removes the calculated property with a name <code>calculatedPropertyName</code>(dot-notation expression) from <code>rootType</code> hierarchy.
     * Throws {@link IncorrectPlaceException} when the place (the hierarchy branch) for calculated property is incorrect (for e.g. does not exist).<br><br>
     *
     * @param rootType -- type of <b>root</b> entity, from which the calculated property should be removed (not derived type)
     * @param calculatedPropertyName -- the dot-notation expression name of calculated property to be removed
     */
    void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName);

    /**
     * Removes the calculated property from {@link ICalculatedProperty#getRootType()} hierarchy.
     * Throws {@link IncorrectPlaceException} when the place (the hierarchy branch) for calculated property is incorrect (for e.g. does not exist).<br><br>
     *
     * @param calculatedProperty -- the calculated property to be removed
     */
    void removeCalculatedProperty(final ICalculatedProperty calculatedProperty);

    /**
     * Gets the calculated property with a name <code>calculatedPropertyName</code>(dot-notation expression) from <code>rootType</code> hierarchy.
     * Throws {@link IncorrectPlaceException} when the place (the hierarchy branch) for calculated property is incorrect (for e.g. does not exist).<br><br>
     *
     * @param rootType -- type of <b>root</b> entity, from which the calculated property should be removed (not derived type)
     * @param calculatedPropertyName -- the dot-notation expression name of calculated property to be obtained
     */
    ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName);

    /**
     * Indicates a situation when the place (the hierarchy branch) for calculated property is incorrect (for e.g. does not exist).
     *
     * @author TG Team
     *
     */
    public class IncorrectPlaceException extends RuntimeException {
	private static final long serialVersionUID = 435410515344805056L;

	public IncorrectPlaceException(final Exception e) {
	    super(e);
	}

	public IncorrectPlaceException(final String s) {
	    super(s);
	}
    }

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();
}