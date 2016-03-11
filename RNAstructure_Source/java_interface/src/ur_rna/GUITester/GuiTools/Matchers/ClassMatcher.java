package ur_rna.GUITester.GuiTools.Matchers;

import java.awt.*;

public abstract class ClassMatcher extends ComposableBase {
    @SuppressWarnings("unchecked")
    public static boolean equalsClass(Class expected, Class actual) {
        return expected.isAssignableFrom(actual);
    }

    public static ClassMatcher of(Class c) { return new SingleClassMatcher(c); }
    public static ClassMatcher of(Class... classes) {
        if (classes.length == 0)
            throw new IllegalArgumentException("At least one class is required.");
        if (classes.length == 1)
            return new SingleClassMatcher(classes[0]);
        return new MultiClassMatcher(classes);
    }

    public abstract String getClassNames();

    @Override
    public String toString() {
        return "{ Class Matcher: " + getClassNames() + " }";
    }

    public static class SingleClassMatcher extends ClassMatcher {
        private Class _cls;

        public SingleClassMatcher(Class cls) {
            _cls = java.util.Objects.requireNonNull(cls);
        }

        @Override
        public boolean matches(Component c) {
            return equalsClass(_cls, c.getClass());
        }

        @Override
        public String getClassNames() {
            return _cls.getSimpleName();
        }
    }

    public static class MultiClassMatcher extends ClassMatcher {
        private Class[] _classes;
        private int _len;

        public MultiClassMatcher(Class... classes) {
            _classes = java.util.Objects.requireNonNull(classes);
            _len = _classes.length;
        }

        @Override
        public boolean matches(Component cmp) {
            Class c = cmp.getClass();
            for (int i = 0; i < _len; i++)
                if (equalsClass(_classes[i], c))
                    return true;
            return false;
        }

        @Override
        public String getClassNames() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < _len; i++) {
                if (i != 0)
                    sb.append(" or ");
                sb.append(_classes[i].getSimpleName());
            }
            return sb.toString();
        }
    }
}
