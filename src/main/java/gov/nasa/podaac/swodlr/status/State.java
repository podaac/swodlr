package gov.nasa.podaac.swodlr.status;

import java.util.HashMap;
import java.util.Map;

public enum State {
    NEW, UNAVAILABLE, GENERATING, ERROR, READY, AVAILABLE;

    private static Map<String, State> map = new HashMap<>() {{
        put("NEW", NEW);
        put("UNAVAILABLE", UNAVAILABLE);
        put("GENERATING", GENERATING);
        put("ERROR", ERROR);
        put("READY", READY);
        put("AVAILABLE", AVAILABLE);
    }};

    public static State fromString(String state) {
        return map.get(state);
    }
}
