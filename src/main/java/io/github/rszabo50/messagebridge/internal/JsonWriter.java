package io.github.rszabo50.messagebridge.internal;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

public final class JsonWriter {
    private JsonWriter() {
    }

    public static String write(Object value) {
        StringBuilder output = new StringBuilder();
        appendValue(output, value);
        return output.toString();
    }

    private static void appendValue(StringBuilder output, Object value) {
        if (value == null) {
            output.append("null");
        } else if (value instanceof String) {
            appendString(output, (String) value);
        } else if (value instanceof Boolean) {
            output.append(value);
        } else if (value instanceof Integer
                || value instanceof Long
                || value instanceof Short
                || value instanceof Byte) {
            output.append(value);
        } else if (value instanceof Double || value instanceof Float) {
            appendFloatingPoint(output, (Number) value);
        } else if (value instanceof Map<?, ?>) {
            appendMap(output, (Map<?, ?>) value);
        } else if (value instanceof Iterable<?>) {
            appendIterable(output, ((Iterable<?>) value).iterator());
        } else if (value.getClass().isArray()) {
            appendArray(output, value);
        } else {
            throw new IllegalArgumentException("unsupported JSON value type: " + value.getClass().getName());
        }
    }

    private static void appendMap(StringBuilder output, Map<?, ?> map) {
        output.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("JSON object keys must be strings");
            }
            String key = (String) entry.getKey();
            if (!first) {
                output.append(',');
            }
            appendString(output, key);
            output.append(':');
            appendValue(output, entry.getValue());
            first = false;
        }
        output.append('}');
    }

    private static void appendIterable(StringBuilder output, Iterator<?> iterator) {
        output.append('[');
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                output.append(',');
            }
            appendValue(output, iterator.next());
            first = false;
        }
        output.append(']');
    }

    private static void appendArray(StringBuilder output, Object array) {
        output.append('[');
        int length = Array.getLength(array);
        for (int index = 0; index < length; index++) {
            if (index > 0) {
                output.append(',');
            }
            appendValue(output, Array.get(array, index));
        }
        output.append(']');
    }

    private static void appendFloatingPoint(StringBuilder output, Number number) {
        double value = number.doubleValue();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("JSON number must be finite");
        }
        output.append(number);
    }

    private static void appendString(StringBuilder output, String value) {
        output.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    output.append("\\\"");
                    break;
                case '\\':
                    output.append("\\\\");
                    break;
                case '\b':
                    output.append("\\b");
                    break;
                case '\f':
                    output.append("\\f");
                    break;
                case '\n':
                    output.append("\\n");
                    break;
                case '\r':
                    output.append("\\r");
                    break;
                case '\t':
                    output.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        output.append(String.format("\\u%04x", (int) character));
                    } else {
                        output.append(character);
                    }
            }
        }
        output.append('"');
    }
}
