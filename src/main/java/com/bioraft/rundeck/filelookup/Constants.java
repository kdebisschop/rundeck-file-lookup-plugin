package com.bioraft.rundeck.filelookup;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    final static String OPT_PATH = "Path";
    final static String OPT_GROUP = "Group";
    final static String OPT_NAME = "Name";
    final static String OPT_PATTERN = "Pattern";
    final static String OPT_FIELD_NAME = "Field Name";
    final static String OPT_GLOBAL = "Make global?";

    final static String OPT_PATH_DESCRIPTION =
            "Path to the file to search";
    final static String OPT_GROUP_DESCRIPTION =
            "Variable group (i.e., ${group.x}}";
    final static String OPT_NAME_DESCRIPTION =
            "Variable name (i.e., ${group.name}) [ignored when Pattern has 2 capture fields]";
    final static String OPT_PATTERN_DESCRIPTION =
            "Regular expression to find, with one or two capture fields";
    final static String OPT_FIELD_NAME_DESCRIPTION =
            "Field name to lookup in JSON";
    final static String OPT_GLOBAL_DESCRIPTION =
            "\"Elevate this variable to global scope (default: false)";

}
