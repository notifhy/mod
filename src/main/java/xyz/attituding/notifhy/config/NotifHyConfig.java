package xyz.attituding.notifhy.config;

public class NotifHyConfig {
    public static final String CATEGORY_NONE = "none";
    public static final String CATEGORY_ADVANCED = "advanced";

    public static String authentication;

    public static class Advanced {
        public static String server;
        public static String[] hosts;
    }
}
