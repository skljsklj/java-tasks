package eindex.net;

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success;
    private String message;
    private Object payload;

    public static Response ok(String message, Object payload) {
        Response r = new Response();
        r.success = true;
        r.message = message;
        r.payload = payload;
        return r;
    }

    public static Response error(String message) {
        Response r = new Response();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getPayload() { return payload; }
}

