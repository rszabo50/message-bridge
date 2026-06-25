package ca.bobszabo.messagebridge.internal;

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
        switch (value) {
            case null -> output.append("null");
            case String string -> appendString(output, string);
            case Boolean bool -> output.append(bool);
            case Integer number -> output.append(number);
            case Long number -> output.append(number);
            case Short number -> output.append(number);
            case Byte number -> output.append(number);
            case Double number -> appendFloatingPoint(output, number);
            case Float number -> appendFloatingPoint(output, number);
            case Map<?, ?> map -> appendMap(output, map);
            case Iterable<?> iterable -> appendIterable(output, iterable.iterator());
            default -> {
                if (value.getClass().isArray()) {
                    appendArray(output, value);
                } else {
                    throw new IllegalArgumentException("unsupported JSON value type: " + value.getClass().getName());
                }
            }
        }
    }

    private static void appendMap(StringBuilder output, Map<?, ?> map) {
        output.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("JSON object keys must be strings");
            }
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
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("JSON number must be finite");
        }
        output.append(number);
    }

    private static void appendString(StringBuilder output, String value) {
        output.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> output.append("\\\"");
                case '\\' -> output.append("\\\\");
                case '\b' -> output.append("\\b");
                case '\f' -> output.append("\\f");
                case '\n' -> output.append("\\n");
                case '\r' -> output.append("\\r");
                case '\t' -> output.append("\\t");
                default -> {
                    if (character < 0x20) {
                        output.append(String.format("\\u%04x", (int) character));
                    } else {
                        output.append(character);
                    }
                }
            }
        }
        output.append('"');
    }
}
