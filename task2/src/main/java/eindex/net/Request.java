package eindex.net;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private Action action;
    private Map<String, Object> data = new HashMap<>();

    public Request() {}

    public Request(Action action) { this.action = action; }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public Map<String, Object> getData() { return data; }
    public void put(String key, Object value) { data.put(key, value); }
}

