package ua.com.fielden.platform.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.utils.Pair;

/**
 * This class is used for correct determining of property titles and descriptions for different purposes (e.g. EGI headers titles/toolTips, criteria label titles/toolTips). This
 * class collects some set of properties and determines their titles and descs respectively to each other (they are dependent on each other).
 *
 * <p>
 * Algorithm : </br>1. description (usually toolTips) : always full description used (see for more details : {@link TitlesDescsGetter#getFullTitleAndDesc(String, Class)}). </br>2.
 * title (usually labels and captions) : commonly short title used (see for more details : {@link TitlesDescsGetter#getTitleAndDesc(String, Class)}) except those ones that
 * duplicate another property titles.
 *
 * @author Jhou
 *
 */
public class EntityDescriptor {

    private static final String _IS = "_is", _NOT = "_not", _FROM = "_from", _TO = "_to";

    private final Class<?> rootType;
    private final List<String> properties = new ArrayList<String>();
    private final Map<String, Pair<String, String>> mapByNames = new HashMap<String, Pair<String, String>>(); // [name, (title, desc)]

    public EntityDescriptor(final Class<?> rootType, final List<String> properties) {
	this.rootType = rootType;
	this.properties.addAll(properties);
	final Map<String, Pair<String, String>> mapByTitles = new HashMap<String, Pair<String, String>>(); // [title, (name, desc)]

	// create map in which entry's key should be "property title":
	for (final String name : properties) {
	    Pair<String, String> ftad;
	    try {
		ftad = TitlesDescsGetter.getFullTitleAndDesc(name, rootType);
	    } catch (final Exception e) {
		ftad = null;
	    }

	    if (name.contains("()") || ftad == null) { //
		mapByNames.put(name, null);
	    } else {
		final String shortTitle = TitlesDescsGetter.getTitleAndDesc(name, rootType).getKey();
		final Pair<String, String> neww = new Pair<String, String>(name, ftad.getValue());

		if (mapByTitles.containsKey(shortTitle)) { // this short titled property already exists!
		    final Pair<String, String> old = mapByTitles.get(shortTitle);
		    final String fullTitleNew = ftad.getKey();
		    final String fullTitleOld = TitlesDescsGetter.getFullTitleAndDesc(old.getKey(), rootType).getKey();
		    if (fullTitleNew.length() > fullTitleOld.length()) {
			mapByTitles.put(fullTitleNew, neww);
		    } else if (fullTitleNew.length() < fullTitleOld.length()) {
			// remove old:
			mapByTitles.remove(shortTitle);
			// put new entry with shorter full title:
			mapByTitles.put(shortTitle, neww);
			// put old entry with longer full title:
			mapByTitles.put(fullTitleOld, old);
		    } else {
			mapByNames.put(name, new Pair<String, String>(fullTitleNew, ftad.getValue()));
		    }
		} else { // simply put new property
		    mapByTitles.put(shortTitle, neww);
		}
	    }
	}
	// convert map into [name, (title, desc)]
	final Set<Entry<String, Pair<String, String>>> entrySet = mapByTitles.entrySet();
	for (final Entry<String, Pair<String, String>> e : entrySet) {
	    mapByNames.put(e.getValue().getKey(), new Pair<String, String>(e.getKey(), e.getValue().getValue()));
	}
    }

    public Class<?> getRootType() {
	return rootType;
    }

    public List<String> getNames() {
	return properties;
    }

    public Pair<String, String> getTitleAndDesc(final String name) {
	if (!mapByNames.containsKey(name)) {
	    throw new RuntimeException("Property " + name + " does not exist in EntityDescriptor.");
	}
	return mapByNames.get(name);
    }

    public String getTitle(final String name) {
	return getTitleAndDesc(name).getKey();
    }

    public String getDesc(final String name) {
	return getTitleAndDesc(name).getValue();
    }

    public String getDescTop(final String name) {
	final String descWithoutHtmlTag = TitlesDescsGetter.removeHtmlTag(getDesc(name));
	return TitlesDescsGetter.addHtmlTag(descWithoutHtmlTag.substring(0, descWithoutHtmlTag.indexOf("<br><i>[")));
    }

    public String getDescBottom(final String name) {
	final String descWithoutHtmlTag = TitlesDescsGetter.removeHtmlTag(getDesc(name));
	return TitlesDescsGetter.addHtmlTag(descWithoutHtmlTag.substring(descWithoutHtmlTag.indexOf("<br><i>[") + 4));
    }

    public Map<String, Pair<String, String>> getTitlesAndDescs() {
	return mapByNames;
    }

    /**
     * Returns normal representation of range property without "_from" or "_to".
     *
     * @param dynamicCriteriaKey
     * @param klass
     * @return
     */
    public static String enhanceDynamicCriteriaPropertyEditorKey(final String dynamicCriteriaKey, final Class klass) {
	return removeSuffixes(dynamicCriteriaKey.replaceFirst(klass.getSimpleName() + ".", ""));
    }

    /**
     * Removes "_from", "_to", "_is", "_not" suffixes from "propertyName".
     *
     * @param propertyName
     * @return
     */
    public static String removeSuffixes(final String propertyName) {
	String s = propertyName;
	s = replaceLast(s, _FROM, "");
	s = replaceLast(s, _TO, "");
	s = replaceLast(s, _IS, "");
	s = replaceLast(s, _NOT, "");
	return s;
    }

    /**
     * Removes ".key" parts from propertyNames.
     *
     * @param propertyNames
     * @return
     */
    public static List<String> getPropertyNamesWithoutKeyParts(final List<String> propertyNames) {
	final List<String> propertyNamesWithoutKeyParts = new ArrayList<String>();
	for (final String propertyName : propertyNames) {
	    propertyNamesWithoutKeyParts.add(getPropertyNameWithoutKeyPart(propertyName));
	}
	return propertyNamesWithoutKeyParts;
    }

    /**
     * Removes ".key" part from propertyName.
     *
     * @param propertyName
     * @return
     */
    public static String getPropertyNameWithoutKeyPart(final String propertyName) {
	return replaceLast(propertyName, ".key", "");
    }

    public static String replaceLast(final String s, final String what, final String byWhat) {
	final int i = s.lastIndexOf(what);
	return i >= 0 ? s.substring(0, i) : s;
    }

}
