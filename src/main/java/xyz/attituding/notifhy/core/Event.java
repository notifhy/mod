package xyz.attituding.notifhy.core;

import java.util.HashMap;

public class Event {
    public enum Type {
        SERVER_CONNECTED(0),
        SERVER_DISCONNECTED(1);

        private final int num;

        Type(int num) {
            this.num = num;
        }

        public int getType() {
            return num;
        }
    }

    public final int type;

    private final HashMap<String, Object> data = new HashMap<>();

    public Event(int type) {
        this.type = type;
    }

    public void putData(String key, Object value) {
        data.put(key, value);
    }
}