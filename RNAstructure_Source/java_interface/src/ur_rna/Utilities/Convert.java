package ur_rna.Utilities;

/**
 * Created by Richard on 3/19/2015.
 */
public class Convert {
    private Convert() {}
    public static boolean ToBool(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean)
            return ((Boolean)obj).booleanValue();
        if (obj instanceof Number)
            return ((Number)obj).doubleValue() == 0d;
        if (obj instanceof String)
            return ToBool((String)obj);
        if (obj instanceof Void)
            return false;
        if (obj instanceof java.util.Optional<?>)
            return ((java.util.Optional<?>)obj).isPresent();
        return true;
    }
    public static boolean ToBool(String s) {
        if (s == null || s.length() == 0) return false;
        s = s.toUpperCase();
        return !(
                s.equals("F")
                        || s.equals("FALSE")
                        || s.equals("NO")
                        || s.equals("0")
        );
    }
    public static int ToInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s);
        }catch (NumberFormatException e) {
            return def;
        }
    }
    public static double ToDouble(String s, double def) {
        if (s == null) return def;
        try {
            return Double.parseDouble(s);
        }catch (NumberFormatException e) {
            return def;
        }
    }
}
