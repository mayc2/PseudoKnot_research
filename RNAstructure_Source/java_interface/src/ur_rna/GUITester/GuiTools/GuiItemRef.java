package ur_rna.GUITester.GuiTools;

import abbot.finder.Matcher;
import ur_rna.Utilities.ObjTools;
import ur_rna.Utilities.annotation.ToFriendlyString;

import java.awt.*;

@ToFriendlyString
public class GuiItemRef {
    public final Matcher matcher;
    public Component lastFound;
    public GuiItemRef relative;
    public GuiRelative relationship;

    public GuiItemRef(final Matcher matcher) {
        this.matcher = matcher;
    }
    public GuiItemRef clearFound(final boolean clearRelatives) {
        lastFound = null;
        if (clearRelatives && relative != null)
            relative.clearFound(true);
        return this;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(final boolean includeFullRelativeDescription) {
        return String.format(
                "{ GUI-Reference Criteria: %s %s }",
                ObjTools.toStr(matcher, "none"),
                relativeToString(includeFullRelativeDescription)
        );
    }

    public String relativeToString(boolean includeFullRelativeDescription) {
        if (relationship == null)
            return "";
        return relationship.name() + " of " + (
                includeFullRelativeDescription ?
                        this.relative :
                        "another component"
        );
    }
}
