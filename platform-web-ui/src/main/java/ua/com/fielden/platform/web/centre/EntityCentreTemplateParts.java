package ua.com.fielden.platform.web.centre;

/**
 * Represents all entity centre template parts that needs to be replaced during entity centre generation.
 *
 * @author TG Team
 *
 */
public enum EntityCentreTemplateParts {

    IMPORTS("<!--@imports-->"),
    FULL_ENTITY_TYPE("@full_entity_type"),
    FULL_MI_TYPE("@full_mi_type"),
    MI_TYPE("@mi_type"),
    //egi related properties
    EGI_LAYOUT("@gridLayout"),
    EGI_LAYOUT_CONFIG("//gridLayoutConfig"),
    EGI_SHORTCUTS("@customShortcuts"),
    EGI_TOOLBAR_VISIBLE("@toolbarVisible"),
    EGI_CHECKBOX_VISIBILITY("@checkboxVisible"),
    EGI_CHECKBOX_FIXED("@checkboxesFixed"),
    EGI_CHECKBOX_WITH_PRIMARY_ACTION_FIXED("@checkboxesWithPrimaryActionsFixed"),
    EGI_NUM_OF_FIXED_COLUMNS("@numOfFixedCols"),
    EGI_SECONDARY_ACTION_FIXED("@secondaryActionsFixed"),
    EGI_HEADER_FIXED("@headerFixed"),
    EGI_SUMMARY_FIXED("@summaryFixed"),
    EGI_VISIBLE_ROW_COUNT("@visibleRowCount"),
    EGI_PAGE_CAPACITY("@pageCapacity"),
    EGI_ACTIONS("//generatedActionObjects"),
    EGI_PRIMARY_ACTION("//generatedPrimaryAction"),
    EGI_SECONDARY_ACTIONS("//generatedSecondaryActions"),
    EGI_PROPERTY_ACTIONS("//generatedPropActions"),
    EGI_DOM("<!--@egi_columns-->"),
    EGI_FUNCTIONAL_ACTION_DOM("<!--@functional_actions-->"),
    EGI_PRIMARY_ACTION_DOM("<!--@primary_action-->"),
    EGI_SECONDARY_ACTIONS_DOM("<!--@secondary_actions-->"),
    //Toolbar related
    TOOLBAR_DOM("<!--@toolbar-->"),
    TOOLBAR_JS("//toolbarGeneratedFunction"),
    TOOLBAR_STYLES("/*toolbarStyles*/"),
    //Selection criteria related
    QUERY_ENHANCER_CONFIG("@queryEnhancerContextConfig"),
    CRITERIA_DOM("<!--@criteria_editors-->"),
    SELECTION_CRITERIA_LAYOUT_CONFIG("//@layoutConfig"),
    //Insertion points
    INSERTION_POINT_ACTIONS("//generatedInsertionPointActions"),
    INSERTION_POINT_ACTIONS_DOM("<!--@insertion_point_actions-->"),
    LEFT_INSERTION_POINT_DOM("<!--@left_insertion_points-->"),
    RIGHT_INSERTION_POINT_DOM("<!--@right_insertion_points-->"),
    BOTTOM_INSERTION_POINT_DOM("<!--@bottom_insertion_points-->");

    private final String centreReplacementTemplate;

    private EntityCentreTemplateParts(final String centreReplacementTemplate) {
        this.centreReplacementTemplate = centreReplacementTemplate;
    }

    @Override
    public String toString() {
        return centreReplacementTemplate;
    }
}
