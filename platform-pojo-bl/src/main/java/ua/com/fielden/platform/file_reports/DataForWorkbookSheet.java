package ua.com.fielden.platform.file_reports;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

public class DataForWorkbookSheet<E extends AbstractEntity<?>> {
    private final String sheetTitle;
    private final List<E> entities;
    private final List<String> propNames = new ArrayList<>();
    private final List<String> propTitles = new ArrayList<>();
    
    public DataForWorkbookSheet(String sheetTitle, List<E> entities, List<Pair<String, String>> propertyNamesAndTitles) {
        super();
        this.sheetTitle = sheetTitle;
        this.entities = entities;
        for (Pair<String, String> pair : propertyNamesAndTitles) {
            propNames.add(pair.getKey());
            propTitles.add(pair.getValue());
        }
    }

    public String getSheetTitle() {
        return sheetTitle;
    }

    public List<E> getEntities() {
        return entities;
    }
    
    public List<String> getPropNames() {
        return propNames;
    }

    public List<String> getPropTitles() {
        return propTitles;
    }
}