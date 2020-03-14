package com.bioraft.rundeck.filelookup;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    static final String OPT_PATH = "Path";
    static final String OPT_GROUP = "Group";
    static final String OPT_NAME = "Name";
    static final String OPT_PATTERN = "Pattern";
    static final String OPT_FIELD_NAME = "Field Name";
    static final String OPT_GLOBAL = "Make global?";

    static final String OPT_PATH_DESCRIPTION =
            "Path to the file to search";
    static final String OPT_GROUP_DESCRIPTION =
            "Variable group (i.e., ${group.x}}";
    static final String OPT_NAME_DESCRIPTION =
            "Variable name (i.e., ${group.name}) [ignored when Pattern has 2 capture fields]";
    static final String OPT_PATTERN_DESCRIPTION =
            "Regular expression to find, with one or two capture fields";
    static final String OPT_FIELD_NAME_DESCRIPTION =
            "Field name to lookup in JSON";
    static final String OPT_GLOBAL_DESCRIPTION =
            "\"Elevate this variable to global scope (default: false)";

}
