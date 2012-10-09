package ua.com.fielden.platform.reflection.development;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
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
 * @author TG Team
 *
 */
public class EntityDescriptor {
    private final Class<?> rootType;
    private final List<String> properties = new ArrayList<String>();
    private final Map<String, Pair<String, String>> mapByNames = new HashMap<String, Pair<String, String>>(); // [name, (title, desc)]

    private String directPropertyName(final String name) {
	return name.isEmpty() ? AbstractEntity.KEY : name; // "key" property should be used for empty "" properties
    }

    public EntityDescriptor(final Class<?> rootType, final List<String> properties) {
	this.rootType = rootType;
	this.properties.addAll(properties);
	final Map<String, Pair<String, String>> mapByTitles = new HashMap<String, Pair<String, String>>(); // [title, (name, desc)]

	// create map in which entry's key should be "property title":
	for (final String name : properties) {
	    Pair<String, String> ftad;
	    try {
		ftad = TitlesDescsGetter.getFullTitleAndDesc(directPropertyName(name), rootType);
	    } catch (final Exception e) {
		ftad = null;
	    }

	    if (name.contains("()") || ftad == null) { //
		mapByNames.put(name, null);
	    } else {
		final String shortTitle = TitlesDescsGetter.getTitleAndDesc(directPropertyName(name), rootType).getKey();
		final Pair<String, String> neww = new Pair<String, String>(name, ftad.getValue());

		if (mapByTitles.containsKey(shortTitle)) { // this short titled property already exists!
		    final Pair<String, String> old = mapByTitles.get(shortTitle);
		    final String fullTitleNew = ftad.getKey();
		    final String fullTitleOld = TitlesDescsGetter.getFullTitleAndDesc(directPropertyName(old.getKey()), rootType).getKey();
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
     * Returns value that indicates whether the passed entity class has description or not.
     *
     * @param klass
     * @return
     */
    public static boolean hasDesc(final Class<?> klass){
	return AnnotationReflector.isAnnotationPresent(DescTitle.class, klass);
    }

}
