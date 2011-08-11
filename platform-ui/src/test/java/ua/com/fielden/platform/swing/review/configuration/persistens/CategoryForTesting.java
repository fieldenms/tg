package ua.com.fielden.platform.swing.review.configuration.persistens;

import java.awt.Color;

import ua.com.fielden.platform.types.ICategory;

public enum CategoryForTesting implements ICategory {
    FIRST_CATEGORY("name1", "desc1", Color.RED, true, true), //
    SECOND_CATEGORY("name2", "desc2", Color.GREEN, true, false);

    private final String name;
    private final String desc;
    private final Color color;
    private final boolean normal;
    private final boolean uncategorized;

    private CategoryForTesting(final String name, final String desc, final Color color, final boolean normal, final boolean uncategorized) {
	this.name = name;
	this.desc = desc;
	this.color = color;
	this.normal = normal;
	this.uncategorized = uncategorized;
    }

    @Override
    public boolean isNormal() {
	return normal;
    }

    @Override
    public boolean isUncategorized() {
	return uncategorized;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getDesc() {
	return desc;
    }

    @Override
    public Color getColor() {
	return color;
    }

}
