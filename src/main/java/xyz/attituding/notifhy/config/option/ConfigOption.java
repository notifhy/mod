package xyz.attituding.notifhy.config.option;

public class ConfigOption<T> {
    private final String key;
    private T value;

    public ConfigOption(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
