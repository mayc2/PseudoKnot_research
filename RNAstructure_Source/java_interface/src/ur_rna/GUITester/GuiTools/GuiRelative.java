package ur_rna.GUITester.GuiTools;

/**
 * Describes the relationship between two GUI Components.
 * Relationships are directional, so it is important to note the order of the components.
 * Given components A and B, the relationship {@code A --[RELATION]--> B} is interpreted as "B is a RELATION of A".
 * e.g. {@code A --[Child]--> B}  means that B is the Child of A. Similarly {@code A --[PrevSibling]--> B} means that B is the previous sibling of A.
 */
public enum GuiRelative {
    Sibling,
    Parent,
    Child,
    Ancestor,
    Descendant,
    LabelTarget
}
