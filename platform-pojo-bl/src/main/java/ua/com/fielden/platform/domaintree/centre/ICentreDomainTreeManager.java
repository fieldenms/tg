package ua.com.fielden.platform.domaintree.centre;

import java.util.List;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * This interface defines how domain tree can be managed for <b>entity centres</b>. <br>
 * <br>
 *
 * Domain tree consists of a tree of properties. <br>
 * <br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 *
 * 1. The domain can be enhanced by <i>calculated properties</i> using {@link IDomainTreeEnhancer} instance ({@link #getEnhancer()} method).<br>
 * 2. Each property has two "tick" managers (refer to {@link IAddToCriteriaTickManager} and {@link IAddToResultTickManager}), which include tick checking logic, criteria values,
 * result property ordering etc.<br>
 * 3. The rules of tree representation (properties disablement, immutable checking etc.) can be changed by {@link IDomainTreeRepresentation} instance ({@link #getRepresentation()}
 * method).<br>
 *
 * @author TG Team
 *
 */
public interface ICentreDomainTreeManager extends IDomainTreeManager {
    /**
     * A <i>domain tree manager<i> with <i>enhancer</i> inside.
     *
     * @author TG Team
     *
     */
    public interface ICentreDomainTreeManagerAndEnhancer extends IDomainTreeManagerAndEnhancer, ICentreDomainTreeManager {
        /**
         * A type of analysis.
         *
         * @author TG Team
         *
         */
        public enum AnalysisType {
            SIMPLE, PIVOT, LIFECYCLE, SENTINEL, MULTIPLEDEC
        }

        /**
         * Initialises a brand new <b>analysis manager</b> with specified <code>name</code> and <code>analysisType</code>. The initialisation uses raw instance creation. <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis
         * @param analysisType
         *            -- a type of an analysis.
         * @return
         */
        ICentreDomainTreeManagerAndEnhancer initAnalysisManagerByDefault(final String name, final AnalysisType analysisType);

        /**
         * Discards a current version of analysis manager with specified <code>name</code>. If a current version of <b>analysis manager</b> was freezed then it just "discards" the
         * changes after freezing. <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        ICentreDomainTreeManagerAndEnhancer discardAnalysisManager(final String name);

        /**
         * Accepts a current version of analysis manager with specified <code>name</code>. If a current version of <b>analysis manager</b> was freezed then it just "accepts" the
         * current version. <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        ICentreDomainTreeManagerAndEnhancer acceptAnalysisManager(final String name);

        /**
         * Returns <code>true</code> if the current version of analysis manager instance with specified <code>name</code> has been changed since last saving/discard (or since the
         * beginning of analysis history). <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        boolean isChangedAnalysisManager(final String name);

        /**
         * Removes an analysis manager with specified <code>name</code>. Throws {@link DomainTreeException} when analysis does not exist. The analysis to be removed can be
         * persisted or not (but should exist - at least locally). <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        ICentreDomainTreeManagerAndEnhancer removeAnalysisManager(final String name);

        /**
         * Gets a current version of analysis manager with specified <code>name</code>. <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        IAbstractAnalysisDomainTreeManager getAnalysisManager(final String name);

        /**
         * Freezes a current version of analysis manager with specified <code>name</code>. <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then accepted ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        ICentreDomainTreeManagerAndEnhancer freezeAnalysisManager(final String name);

        /**
         * Returns <code>true</code> if the current version of analysis manager instance with specified <code>name</code> is in freezed state. Use
         * {@link #discardAnalysisManager(String)} or {@link #acceptAnalysisManager(String)} to discard / accept the changes that were made after freezing (these actions trigger
         * automatic unfreezing after that). <br>
         * <br>
         *
         * (implementation note) : there should be two sets of analysis managers : persistentAnalyses and currentAnalyses. save = [persistentAnalyses <= currentAnalyses]. discard =
         * [persistentAnalyses => currentAnalyses]. <br>
         * <br>
         *
         * This current version of a analysis manager can be altered by its methods, and then saved ({@link #acceptAnalysisManager(String)} method) or discarded (
         * {@link #discardAnalysisManager(String)} method). <br>
         * <br>
         *
         * @param name
         *            -- a name that defines an analysis.
         * @return
         */
        boolean isFreezedAnalysisManager(final String name);

        /**
         * Returns an <b>ordered</b> list of existent analysis names (which are the keys, that can be used in {@link #getAnalysisManager(String)} method).
         *
         * @return
         */
        List<String> analysisKeys();

        //    /**
        //     * Moves property <code>what</code> to the place <b>before</b> property <code>beforeWhat</code> in an ordered list of checked properties for concrete <code>root</code> type.
        //     *
        //     * @param root -- a root type that contains a checked properties.
        //     * @param what -- a property to move
        //     * @param beforeWhat -- a property before which property "what" will be inserted
        //     */
        //    ICentreDomainTreeManagerAndEnhancer moveAnalysis(final String what, final String beforeWhat);
        //
        //    /**
        //     * Moves property <code>what</code> to the place after all properties in an ordered list of checked properties for concrete <code>root</code> type.
        //     *
        //     * @param root -- a root type that contains a checked properties.
        //     * @param what -- a property to move
        //     */
        //    ICentreDomainTreeManagerAndEnhancer moveAnalysisToTheEnd(final String what);
    }

    /**
     * Returns a domain representation that is able to change domain representation rules. See {@link IDomainTreeRepresentation} documentation for more details.
     *
     * @return
     */
    @Override
    ICentreDomainTreeRepresentation getRepresentation();

    /**
     * Returns a tree manager for a <b>add to criteria</b> tick. See {@link IAddToCriteriaTickManager} for more details.
     */
    @Override
    IAddToCriteriaTickManager getFirstTick();

    /**
     * Returns a tree manager for a <b>add to result-set</b> tick. See {@link IAddToResultTickManager} for more details.
     */
    @Override
    IAddToResultTickManager getSecondTick();

    /**
     * Gets a <i>runAutomatically</i> flag. <br>
     * <br>
     *
     * @return
     */
    boolean isRunAutomatically();

    /**
     * Sets a <i>runAutomatically</i> flag. <br>
     * <br>
     *
     * @param runAutomatically
     *            -- a value to set
     * @return -- a criteria manager
     */
    ICentreDomainTreeManager setRunAutomatically(final boolean runAutomatically);

    /**
     * This interface defines <b>entity centre</b> domain tree can be managed for <b>criteria</b> (property represents as a criteria editor). <br>
     * <br>
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
     * <br>
     *
     * The major aspects of tree management (context-specific) are following: <br>
     * 1. property's checking (adding to criteria);<br>
     * 2. property's [default/____] values/exclusiveness/negation/missingValue;<br>
     * 3. entity-typed property's locators etc...<br>
     *
     * @author TG Team
     *
     */
    public interface IAddToCriteriaTickManager extends ITickManager, ILocatorManager {
        /**
         * A type of meta values.
         * TODO please remove this functionality. Migrate categories to CentreUpdater.
         *
         * @author TG Team
         *
         */
        public enum MetaValueType {
            VALUE, VALUE2, EXCLUSIVE, EXCLUSIVE2, OR_NULL, NOT, DATE_PREFIX, DATE_MNEMONIC, AND_BEFORE, ALL_ORDERING /* TODO Rename to SORTING */, WIDTH, GROW_FACTOR
        }

        /**
         * Returns <code>true</code> whether the meta value is present (when it was explicitly marked as 'present'), <code>false</code> otherwise.
         * TODO please remove this functionality.
         *
         * @param metaValueType
         * @param root
         * @param property
         * @return
         */
        boolean isMetaValuePresent(final MetaValueType metaValueType, final Class<?> root, final String property);

        /**
         * Marks the meta value as 'present'.
         * TODO please remove this functionality.
         *
         * @param metaValueType
         * @param root
         * @param property
         * @return
         */
        IAddToCriteriaTickManager markMetaValuePresent(final MetaValueType metaValueType, final Class<?> root, final String property);

        /**
         * Gets a <i>columnsNumber</i> for criteria editors. <br>
         * <br>
         *
         * @return
         */
        int getColumnsNumber();

        /**
         * Sets a <i>columnsNumber</i> for criteria editors. Should be >= 1, as it represents a columns number in entity criteria (throws {@link DomainTreeException} if trying
         * to set <code>columnsNumber</code> <= 0).
         *
         * @param columnsNumber
         *            -- a value to set
         * @return -- a criteria manager
         */
        IAddToCriteriaTickManager setColumnsNumber(final int columnsNumber);

        /**
         * Returns value that indicates whether <i>main</i> value of a criteria property is empty or not.<br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        boolean isValueEmpty(final Class<?> root, final String property);

        /**
         * Gets a <i>main</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Object getValue(final Class<?> root, final String property);

        /**
         * Sets a <i>main</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param value
         *            -- a value to set
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setValue(final Class<?> root, final String property, final Object value);

        /**
         * Returns value that indicates whether <i>secondary</i> value of a criteria property is empty or not. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is applicable only to <i>double editor (or boolean)</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        boolean is2ValueEmpty(final Class<?> root, final String property);

        /**
         * Gets a <i>secondary</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is is applicable only to <i>double editor (or boolean)</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Object getValue2(final Class<?> root, final String property);

        /**
         * Sets a <i>secondary</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is applicable only to <i>double editor (or boolean)</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param value2
         *            -- a value to set
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setValue2(final Class<?> root, final String property, final Object value2);

        /**
         * Gets an <b>exclusive</b> flag for <i>main</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is applicable only to <i>double editor</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Boolean getExclusive(final Class<?> root, final String property);

        /**
         * Sets an <b>exclusive</b> flag for <i>main</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is applicable only to <i>double editor</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param exclusive
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setExclusive(final Class<?> root, final String property, final Boolean exclusive);

        /**
         * Gets an <b>exclusive</b> flag for <i>secondary</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is applicable only to <i>double editor</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Boolean getExclusive2(final Class<?> root, final String property);

        /**
         * Sets an <b>exclusive</b> flag for <i>secondary</i> value of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
         * It is applicable only to <i>double editor</i> properties. These include date type, money type, number type etc. The conflict will produce an
         * {@link DomainTreeException}.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param exclusive2
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setExclusive2(final Class<?> root, final String property, final Boolean exclusive2);

        /**
         * Gets a date prefix of a criteria <i>date</i> property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method). Throws {@link DomainTreeException} when the
         * property is not of date type.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        DateRangePrefixEnum getDatePrefix(final Class<?> root, final String property);

        /**
         * Sets a date prefix of a criteria <i>date</i> property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method). Throws {@link DomainTreeException} when the
         * property is not of date type.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param datePrefix
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setDatePrefix(final Class<?> root, final String property, final DateRangePrefixEnum datePrefix);

        /**
         * Gets a date mnemonic of a criteria <i>date</i> property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method). Throws {@link DomainTreeException} when the
         * property is not of date type.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        MnemonicEnum getDateMnemonic(final Class<?> root, final String property);

        /**
         * Sets a date mnemonic of a criteria <i>date</i> property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method). Throws {@link DomainTreeException} when the
         * property is not of date type.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param dateMnemonic
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setDateMnemonic(final Class<?> root, final String property, final MnemonicEnum dateMnemonic);

        /**
         * Gets an "and before" flag of a criteria <i>date</i> property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method). Throws {@link DomainTreeException} when the
         * property is not of date type.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Boolean getAndBefore(final Class<?> root, final String property);

        /**
         * Sets an "and before" flag of a criteria <i>date</i> property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method). Throws {@link DomainTreeException} when the
         * property is not of date type.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param andBefore
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setAndBefore(final Class<?> root, final String property, final Boolean andBefore);

        /**
         * Gets an "or null" flag (missing value) of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Boolean getOrNull(final Class<?> root, final String property);

        /**
         * Sets an "or null" flag (missing value) of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param orNull
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setOrNull(final Class<?> root, final String property, final Boolean orNull);

        /**
         * Gets a "not" flag (negation) of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        Boolean getNot(final Class<?> root, final String property);

        /**
         * Sets an "not" flag (negation) of a criteria property. <br>
         * <br>
         *
         * Throws {@link DomainTreeException} when the property is not checked (see {@link #isChecked(Class, String)} method).
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param not
         * @return -- a criteria tick manager
         */
        IAddToCriteriaTickManager setNot(final Class<?> root, final String property, final Boolean not);

        // Boolean getAll(final Class<?> root, final String property);
        // ICriteriaTickManager setAll(final Class<?> root, final String property, final Boolean all);

        /**
         * Marks a concrete property's tick to be <b>mutably</b> checked in domain tree representation. <br>
         * <br>
         *
         * The action should not conflict with a contract of disabled / checked property's ticks. The conflict will produce an {@link DomainTreeException}.
         * <p>
         * <b>IMPORTANT</b> : a list of checked properties also contains "placeholders", which can be swapped, moved exactly as simple property. Placeholder property name has
         * contract "<numberOfPlaceholder>-placeholder-origin-<placeholderOriginalRow>-<placeholderOriginalColumn>", but this is only for information, not for "property-related"
         * usage.
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param check
         *            -- an action to perform (<code>true</code> to check, <code>false</code> to uncheck)
         *
         */
        @Override
        IAddToCriteriaTickManager check(final Class<?> root, final String property, final boolean check);

        /**
         * Returns an <b>ordered</b> list of checked properties for concrete <code>root</code> type. <br>
         * <br>
         *
         * The order of the checked properties should be following (if it was not altered using {@link #swap(Class, String, String)}/{@link #move(Class, String, String)} methods):<br>
         * 1. all checked properties as defined by a) {@link #isChecked(Class, String)} contract b) {@link IDomainTreeRepresentation#includedProperties(Class)} order<br>
         * 2. all manually checked properties (in order that they were checked)
         * <p>
         * <b>IMPORTANT</b> : a list of properties also contains "placeholders", which can be swapped, moved exactly as simple property. Placeholder property name has contract
         * "<numberOfPlaceholder>-placeholder-origin-<placeholderOriginalRow>-<placeholderOriginalColumn>", but this is only for information, not for "property-related" usage.
         *
         * @param root
         *            -- a root type that contains a checked properties.
         * @return
         */
        @Override
        List<String> checkedProperties(final Class<?> root);

        /**
         * Swaps two properties (<code>property1</code> and <code>property2</code>) in an ordered list of checked properties for concrete <code>root</code> type.
         * <p>
         * <b>IMPORTANT</b> : a list of properties also contains "placeholders", which can be swapped, moved exactly as simple property. Placeholder property name has contract
         * "<numberOfPlaceholder>-placeholder-origin-<placeholderOriginalRow>-<placeholderOriginalColumn>", but this is only for information, not for "property-related" usage.
         *
         * @param root
         *            -- a root type that contains a checked properties.
         * @param property1
         *            -- a first property to swap
         * @param property2
         *            -- a second property to swap
         */
        @Override
        IAddToCriteriaTickManager swap(final Class<?> root, final String property1, final String property2);

        /**
         * Throws {@link UnsupportedOperationException}.
         *
         * @param root
         *            -- a root type that contains a checked properties.
         * @param what
         *            -- a property to move
         * @param beforeWhat
         *            -- a property before which property "what" will be inserted
         */
        @Override
        IAddToCriteriaTickManager move(final Class<?> root, final String what, final String beforeWhat);

        /**
         * Throws {@link UnsupportedOperationException}.
         *
         * @param root
         *            -- a root type that contains a checked properties.
         * @param what
         *            -- a property to move
         */
        @Override
        IAddToCriteriaTickManager moveToTheEnd(final Class<?> root, final String what);
    }

    /**
     * This interface defines how <b>entity centre</b> domain tree can be managed for <b>result-set</b> (property represents as a result column). <br>
     * <br>
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
     * <br>
     *
     * @see IOrderingManager
     * @author TG Team
     *
     */
    public interface IAddToResultTickManager extends IOrderingManager, IWidthManager, ITickManager {
        /**
         * Gets a <i>growFactor</i> for property. <br>
         * <br>
         *
         * This contract should not conflict with "checked properties" (or with "used properties" -- more accurately) contract. The conflict will produce an
         * {@link DomainTreeException}.<br>
         * <br>
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @return
         */
        int getGrowFactor(final Class<?> root, final String property);

        /**
         * Sets a <i>growFactor</i> for property. <br>
         * <br>
         *
         * This action should not conflict with "checked properties" (or with "used properties" -- more accurately) contract. The conflict will produce an
         * {@link DomainTreeException}.<br>
         * <br>
         *
         * @param root
         *            -- a root type that contains property.
         * @param property
         *            -- a dot-notation expression that defines a property.
         * @param width
         *            -- a width to set
         * @return -- a result tick representation
         */
        IAddToResultTickManager setGrowFactor(final Class<?> root, final String property, final int growFactor);
        
        /**
         * Returns column widths and grow factors to facilitate exact copy through {@link #setWidthsAndGrowFactors(T2)} method.
         * 
         * @return
         */
        T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> getWidthsAndGrowFactors();
        
        /**
         * Sets column widths and grow factors to facilitate full overriding of that information; need to get information using {@link #getWidthsAndGrowFactors()} method.
         * 
         * @param widthsAndGrowFactors
         */
        void setWidthsAndGrowFactors(final T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> widthsAndGrowFactors);
    }
}