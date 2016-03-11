package ur_rna.Utilities;

import ur_rna.Utilities.annotation.MagicConstant;
import ur_rna.Utilities.annotation.ToFriendlyString;

import java.awt.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class ObjTools {
    private ObjTools(){} //prevent instantiation

    public static String fmt(String format, Object... args) {
        return String.format(format, args);
    }

    @SuppressWarnings("unchecked") // getMethod results in a warning about using unchecked or unsafe operations.
    public static String toDisplayString(Object value) {
        if (value == null)
            return "null";
        if (value instanceof String)
            return "\"" + value + "\"";
        if (value instanceof Double || value instanceof Integer)
            return value.toString(); // + value.getClass().getSimpleName().substring(0,1); // D,F,I,B

        Class cls =value.getClass();
        String name = cls.getSimpleName();

        if (value instanceof Number)
            return value.toString() + name.substring(0, 1).toLowerCase(); // D,F,I,B
        if (value instanceof Component) {
            Component c = (Component) value;
            Rectangle r = c.getBounds();
            return "[GUI " + name + " \"" + c.getName() + "\" Bounds: (x:" + r.x + ", y:" + r.y + ", w:" + r.width + ", h:" + r.height + ") ]";
        }
        if (value instanceof Throwable) {
            return "[ERROR: " + ((Throwable) value).getMessage() + "]";
        }
        if (value == Objects.MISSING)
            return "[MISSING]";

        if (value instanceof Displayable)
            return ((Displayable) value).toDisplayString();

		/* List classes here that can be converted by using toString() directly, without prepending the class name */
        Class[] knownTypes = new Class[]{Boolean.class};
        for (Class c : knownTypes) {
            if (c.isInstance(value))
                return value.toString();
        }

        Annotation[] notes = cls.getAnnotationsByType(ToFriendlyString.class);
        if (notes.length != 0) {
            ToFriendlyString a = (ToFriendlyString)notes[0];
            String method = a.method();
            if ("toString".equals(method))
                return value.toString();
            if (method != null && !method.isEmpty()) {
                try {
                    Method m = cls.getMethod(method);
                    if (m != null)
                        return m.invoke(value).toString();
                } catch (Exception ex) {
                    // do nothing. use another method.
                }
            }
        }

        String s = value.toString();
        if (s.startsWith("[") && s.endsWith("]") || s.startsWith("{") && s.endsWith("}"))
            return s;
        return "[" + name + ": " + s + "]";
    }
    public static String toStr(Object value) {
        return toStr(value, "");
    }
    public static String toStr(Object value, String nullValue) {
        if (value == null) return nullValue;
        return value.toString();
    }
    /**
     * Used to convert raw characters to their escaped version
     * when these raw version cannot be used as part of a Java
     * string literal.
     * <p>
     * Escapes NULL, BKSP, TAB, CR, LF, FF, backslash, double (") and single (') quotes
     * to their java escape codes. Also escapes all other control characters (1 to 31) and
     * all characters above 127 to the corresponding Java unicode escape code (\u0001, \u26BD etc. )
     * </p>
     *
     * @param subject The string to escape.
     * @return The escaped string.
     * @see #escapeStringLiteral(String,int)
     */
    public static String escapeStringLiteral(String subject) {
        return escapeStringLiteral(subject, CharType.ALL);
    }

    /**
     * Used to convert raw characters to their escaped version
     * when these raw version cannot be used as part of a Java
     * string literal.
     * <p>
     * Escapes NULL, BKSP, TAB, CR, LF, FF, backslash, double (") and single (') quotes
     * to their java escape codes. Also escapes all other control characters (1 to 31) and
     * all characters above 127 to the corresponding Java unicode escape code (\u0001, \u26BD etc. )
     * </p>
     *
     * @param subject The string to escape.
     *
     * @param escapeOptions
     *   A bitfield of {@link CharType} values indicating which types
     *   of characters to escape.
     *   <p>
     *   However note {@link CharType#CONTROL} characters are ALWAYS escaped, while
     *   {@link CharType#ALPHA}, {@link CharType#DIGITS}, and most {@link CharType#SYMBOLS}
     *   are NEVER escaped.
     *   The only character types for which escaping can be enabled or disabled are the following:
     *   {@link CharType#TAB}, {@link CharType#NEWLINE}, {@link CharType#DOUBLE_QUOTES},
     *   {@link CharType#SINGLE_QUOTE}, {@link CharType#BACKSLASH}, and {@link CharType#EXT_ASCII}.
     *   </p>
     *
     * @return The escaped string.
     * @see #escapeStringLiteral(String, int)
     */
    public static String escapeStringLiteral(String subject,
            @MagicConstant(flagsFromClass = CharType.class) int escapeOptions) {
        boolean bDQ = isSet(CharType.DOUBLE_QUOTES, escapeOptions);
        boolean bSQ = isSet(CharType.SINGLE_QUOTE, escapeOptions);
        boolean bEA = isSet(CharType.EXT_ASCII, escapeOptions);
        boolean bSL = isSet(CharType.SLASH, escapeOptions);

        StringBuilder sb = new StringBuilder(subject.length());
        char c;
        for (int i = 0; i < subject.length(); i++) {
            switch (c = subject.charAt(i)) {
                case 0:
                    sb.append("\\0");
                    continue;
                case CharConstants.BKSP:
                    sb.append("\\b");
                    continue;
                case CharConstants.TAB:
                    sb.append("\\t");
                    continue;
                case CharConstants.LF:
                    sb.append("\\n");
                    continue;
                case CharConstants.FF:
                    sb.append("\\f");
                    continue;
                case CharConstants.CR:
                    sb.append("\\r");
                    continue;
                case CharConstants.DBL_QUOT:
                    sb.append(bDQ ? "\\\"" : c);
                    continue;
                case CharConstants.SNG_QUOT:
                    sb.append(bSQ ? "\\\'" : c);
                    continue;
                case CharConstants.BACK_SLASH:
                    sb.append(bSL ? "\\\\" : c);
                    continue;
                case CharConstants.DEL:
                    sb.append("\\u007f");
                default:
                    if (c < CharConstants.SP || bEA && c >= CharConstants.UPPER_ASCII || c > CharConstants.LAST_ASCII) {
                        String s = "000" + Integer.toString(c, Constants.HEX_RADIX);
                        sb.append("\\u");
                        sb.append(s, s.length() - 4, s.length());
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static boolean isSet(final int flagToFind, final int valueToSearch) {
        return (valueToSearch & flagToFind) == flagToFind;
    }
    public static boolean isAnySet(final int flagToFind, final int valueToSearch) {
        return (valueToSearch & flagToFind) != 0;
    }
    /**
     * Returns true if the string is null or empty ("").
     * @param str
     * @return
     */
    public static boolean isEmpty(final String str) {
        return str == null || str.isEmpty();
    }
}
