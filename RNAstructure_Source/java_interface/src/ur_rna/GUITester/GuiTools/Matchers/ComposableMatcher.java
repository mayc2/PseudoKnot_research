package ur_rna.GUITester.GuiTools.Matchers;

 import abbot.finder.Matcher;

 public interface ComposableMatcher extends Matcher {
    ComposableMatcher and(Matcher other);
    ComposableMatcher or(Matcher other);
    ComposableMatcher xor(Matcher other);
    ComposableMatcher inverse();
    ComposableMatcher simplify();
    Matcher self();
}
