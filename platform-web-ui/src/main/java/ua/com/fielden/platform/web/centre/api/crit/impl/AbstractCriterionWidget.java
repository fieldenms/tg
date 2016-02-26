package ua.com.fielden.platform.web.centre.api.crit.impl;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.not;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.interfaces.IImportable;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 * The base implementation box for generic information for all criterion widgets.
 *
 * All criterion widget implementations should be based on this one and should be extended by criterion-specific configuration data.
 *
 * @author TG Team
 *
 */
public abstract class AbstractCriterionWidget implements IRenderable, IImportable {

    private static final String sufixFrom = " (From)";
    private static final String sufixTo = " (To)";
    private static final String sufixYes = " (Yes)";
    private static final String sufixNo = " (No)";

    private final String propertyName;
    private final String widgetName;
    private final String widgetPath;
    private boolean debug = false;
    private final Pair<AbstractWidget, AbstractWidget> editors;

    /**
     * Creates {@link AbstractCriterionWidget} from <code>entityType</code> type and <code>propertyName</code> and the name&path of widget.
     *
     * @param criteriaType
     * @param propertyName
     */
    public AbstractCriterionWidget(final String widgetPath, final String propertyName, final AbstractWidget... editors) {
        this.widgetName = extractNameFrom(widgetPath);
        this.widgetPath = widgetPath;
        this.propertyName = propertyName;
        this.editors = new Pair<>(editors[0].markAsCriterionEditor(), null);
        if (editors.length > 1) {
            this.editors.setValue(editors[1].markAsCriterionEditor());
        }
    }

    private List<AbstractWidget> editors0() {
        return editors.getValue() == null ? Arrays.asList(editors.getKey()) : Arrays.asList(editors.getKey(), editors.getValue());
    }

    private DomElement[] editors() {
        final List<AbstractWidget> editors = editors0();
        final DomElement[] editorsDOM = new DomElement[editors.size()];
        for (int editorIndex = 0; editorIndex < editors.size(); editorIndex++) {
            final DomElement editorElement = editors.get(editorIndex).render();
            editorElement.clazz("flex", true);
            if (editors.size() > 1 && editorIndex == 0) {
                editorElement.style("margin-right:20px");
            }
            editorsDOM[editorIndex] = editorElement;
        }
        return editorsDOM;
    }

    public List<String> editorsImportPaths() {
        return editors0().stream().map(widget -> widget.importPath()).collect(Collectors.toList());
    }

    /**
     * The name of the property to which this editor will be bound.
     *
     * Please, note that if the "property itself" is used -- the method returns "THIS".
     *
     * @return
     */
    protected String propertyName() {
        return "".equals(propertyName) ? "THIS" : propertyName.replace(".", ":"); // dots are converted to colons to conform to the 'propertyModel' naming convention
    }

    /**
     * Creates an attributes that will be used for widget component generation (generic attributes).
     *
     * @return
     */
    private Map<String, Object> createAttributes() {
        final LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();
        if (isDebug()) {
            attrs.put("debug", "true");
        }
        attrs.put("validation-callback", "[[validate]]");
        return attrs;
    }

    /**
     * Creates an attributes that will be used for widget component generation.
     * <p>
     * Please, implement this method in descendants (for concrete widgets) to extend the attributes set by widget-specific attributes.
     *
     * @return
     */
    protected Map<String, Object> createCustomAttributes() {
        return new LinkedHashMap<>();
    };

    @Override
    public final DomElement render() {
        return new DomElement(widgetName).attrs(createAttributes()).attrs(createCustomAttributes()).add(editors());
    }

    /**
     * Extracts widget name from its path.
     *
     * @param path
     * @return
     */
    public static String extractNameFrom(final String path) {
        final int lastSlashInd = path.lastIndexOf('/');
        if (lastSlashInd < 0) {
            return path;
        } else {
            return path.substring(lastSlashInd + 1);
        }
    }

    @Override
    public String importPath() {
        return widgetPath;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public static Pair<String, String> generateNames(final Class<?> root, final Class<?> managedType, final String propertyName) {
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, propertyName);

        final String firstPropertyName, secondPropertyName;
        if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType, propertyName)) {
            firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, EntityUtils.isBoolean(propertyType) ? is(propertyName) : from(propertyName));
            secondPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, EntityUtils.isBoolean(propertyType) ? not(propertyName) : to(propertyName));
        } else {
            firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, propertyName);
            secondPropertyName = null;
        }
        return new Pair<>(firstPropertyName, secondPropertyName);
    }

    public static Pair<Pair<String, String>, Pair<String, String>> generateTitleDesc(final Class<?> root, final Class<?> managedType, final String propertyName) {
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, propertyName);
        //        final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, propertyName);
        //        final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        //        final List<NewProperty> generatedProperties = new ArrayList<NewProperty>();
        //
        //        if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType, propertyName)) {
        //            generatedProperties.addAll(generateRangeCriteriaProperties(root, managedType, propertyType, propertyName, titleAndDesc));
        //        } else {
        //            generatedProperties.add(generateSingleCriteriaProperty(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation));
        //        }
        //        return generatedProperties;

        //        final Pair<String, String> _firstTitleDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        //        final Pair<String, String> firstTitleDesc = Pair.pair(_firstTitleDesc.getKey() + sufixFrom, _firstTitleDesc.getValue());
        //
        //        final Pair<String, String> _secondTitleDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        //        final Pair<String, String> secondTitleDesc = Pair.pair(_secondTitleDesc.getKey() + sufixTo, _secondTitleDesc.getValue());

        //        return new Pair<>(firstTitleDesc, secondTitleDesc);
        if(EntityUtils.isBoolean(propertyType)){
            return generateTitleDescWithSufixes(sufixYes, sufixNo, managedType, propertyName);
        } else {
            return generateTitleDescWithSufixes(sufixFrom, sufixTo, managedType, propertyName);
        }

    }

    public static Pair<Pair<String, String>, Pair<String, String>> generateTitleDescWithSufixes(final String firstSufix, final String secondSufix, final Class<?> managedType, final String propertyName) {
        final Pair<String, String> _firstTitleDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        final Pair<String, String> firstTitleDesc = Pair.pair(_firstTitleDesc.getKey() + firstSufix, _firstTitleDesc.getValue());

        final Pair<String, String> _secondTitleDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        final Pair<String, String> secondTitleDesc = Pair.pair(_secondTitleDesc.getKey() + secondSufix, _secondTitleDesc.getValue());
        return new Pair<>(firstTitleDesc, secondTitleDesc);
    }

    public static String generateSingleName(final Class<?> root, final Class<?> managedType, final String propertyName) {
        return generateNames(root, managedType, propertyName).getKey();
    }

    public static Pair<String, String> generateSingleTitleDesc(final Class<?> root, final Class<?> managedType, final String propertyName) {
        final boolean isEntityItself = "".equals(propertyName);
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, propertyName);
        final Pair<String, String> _first = generateTitleDesc(root, managedType, propertyName).getKey();
        final String title = _first.getKey();
        final int suffixLength = EntityUtils.isBoolean(propertyType) ? sufixYes.length() : sufixFrom.length();

        return Pair.pair(title.substring(0, title.length() - suffixLength), _first.getValue());
    }
}
