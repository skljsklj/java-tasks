package eindex.util;

import java.util.*;

public final class JsonUtil {
    private JsonUtil() {}

    public static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return '"' + escape((String) value) + '"';
        if (value instanceof Number) return String.valueOf(value);
        if (value instanceof Boolean) return ((Boolean) value) ? "true" : "false";
        if (value instanceof Map) {
            @SuppressWarnings("unchecked") Map<String, Object> m = (Map<String, Object>) value;
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append('"').append(':').append(toJson(e.getValue()));
            }
            sb.append('}');
            return sb.toString();
        }
        if (value instanceof Iterable) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (Object o : (Iterable<?>) value) {
                if (!first) sb.append(',');
                first = false;
                sb.append(toJson(o));
            }
            sb.append(']');
            return sb.toString();
        }
        throw new IllegalArgumentException("Unsupported type: " + value.getClass());
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static Object parse(String json) { return new Parser(json).parse(); }

    private static final class Parser {
        private final String s; private int i = 0;
        Parser(String s) { this.s = s; }
        Object parse() { skipWS(); Object v = parseValue(); skipWS(); if (i != s.length()) throw new RuntimeException("Trailing content"); return v; }
        private Object parseValue() {
            skipWS(); if (i >= s.length()) throw new RuntimeException("EOF"); char c = s.charAt(i);
            if (c == '"') return parseString();
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            return parseNumber();
        }
        private Map<String,Object> parseObject() {
            Map<String,Object> m = new LinkedHashMap<>(); expect('{'); skipWS(); if (peek('}')) { i++; return m; }
            while (true) { String k = parseString(); skipWS(); expect(':'); Object v = parseValue(); m.put(k, v); skipWS(); if (peek('}')) { i++; break; } expect(','); }
            return m;
        }
        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>(); expect('['); skipWS(); if (peek(']')) { i++; return list; }
            while (true) { Object v = parseValue(); list.add(v); skipWS(); if (peek(']')) { i++; break; } expect(','); }
            return list;
        }
        private String parseString() {
            expect('"'); StringBuilder sb = new StringBuilder();
            while (i < s.length()) { char c = s.charAt(i++); if (c == '"') break; if (c == '\\') { char e = s.charAt(i++);
                switch (e) { case '"': sb.append('"'); break; case '\\': sb.append('\\'); break; case '/': sb.append('/'); break; case 'b': sb.append('\b'); break; case 'f': sb.append('\f'); break; case 'n': sb.append('\n'); break; case 'r': sb.append('\r'); break; case 't': sb.append('\t'); break; case 'u': String hex = s.substring(i, i+4); sb.append((char)Integer.parseInt(hex,16)); i+=4; break; default: throw new RuntimeException("bad escape"); } }
                else sb.append(c); }
            return sb.toString(); }
        private Number parseNumber() { int start=i; if (s.charAt(i)=='-') i++; while (i<s.length() && Character.isDigit(s.charAt(i))) i++; if (i<s.length() && s.charAt(i)=='.') { i++; while (i<s.length() && Character.isDigit(s.charAt(i))) i++; return Double.parseDouble(s.substring(start,i)); } return Long.parseLong(s.substring(start,i)); }
        private void skipWS() { while (i<s.length()) { char c=s.charAt(i); if (c==' '||c=='\n'||c=='\r'||c=='\t') i++; else break; } }
        private boolean peek(char c){ return i<s.length() && s.charAt(i)==c; }
        private void expect(char c){ if (i>=s.length()||s.charAt(i)!=c) throw new RuntimeException("expected '"+c+"'"); i++; }
    }
}

