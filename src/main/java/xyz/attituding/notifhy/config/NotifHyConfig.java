package xyz.attituding.notifhy.config;

public class NotifHyConfig {
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_ADVANCED = "advanced";

    public static final String KEY_AUTHENTICATION = "general.authentication";
    public static final String KEY_ADVANCED_SERVER = "advanced.server";
    public static final String KEY_ADVANCED_HOSTS = "advanced.hosts";

    public static class General {
        public static String authentication = "";
    }

    public static class Advanced {
        public static String server = "https://notifhy-api.attituding.xyz/v1/event";
        public static String[] hosts = new String[]{"hypixel.net"};
    }
}
