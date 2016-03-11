package ur_rna.GUITester.GuiTools.Matchers;

 import abbot.util.ExtendedComparator;

 import javax.accessibility.AccessibleContext;
 import javax.swing.*;
 import javax.swing.text.JTextComponent;
 import java.awt.*;

public class DescriptionMatcher extends ComposableBase {
    private String _find = null;
    private int scope;

    public final static int SEARCH_NONE = (0);
    public final static int SEARCH_TRUE_NAME = (1<<0);
    public final static int SEARCH_TRUE_INPUT = (1<<1);
    public final static int SEARCH_TRUE_CAPTION = (1<<2);
    public final static int SEARCH_TRUE_TEXT = SEARCH_TRUE_CAPTION | SEARCH_TRUE_INPUT;
    public final static int SEARCH_TOOLTIP = (1<<10);
    public final static int SEARCH_SELECTION = (1<<14);
    public final static int SEARCH_ACCESSIBLE_NAME = (1<<20);
    public final static int SEARCH_ACCESSIBLE_TEXT = (1<<21);
    public final static int SEARCH_ACCESSIBLE_DESC = (1<<22);

    public final static int SEARCH_ACCESSIBLE = SEARCH_ACCESSIBLE_NAME | SEARCH_ACCESSIBLE_TEXT | SEARCH_ACCESSIBLE_DESC;
    public final static int SEARCH_NAME = SEARCH_TRUE_NAME | SEARCH_ACCESSIBLE_NAME | SEARCH_ACCESSIBLE_DESC;
    public final static int SEARCH_INPUT = SEARCH_TRUE_INPUT | SEARCH_ACCESSIBLE_TEXT;
    public final static int SEARCH_CAPTION = SEARCH_TRUE_CAPTION | SEARCH_ACCESSIBLE_DESC;
    public final static int SEARCH_TEXT = SEARCH_INPUT | SEARCH_CAPTION;
    public static final int SEARCH_COMMENT = SEARCH_TOOLTIP | SEARCH_ACCESSIBLE_DESC;

    public final static int SEARCH_ALL = -1;

    public DescriptionMatcher(String text) {
    	this(text, SEARCH_ALL);
    }

    public DescriptionMatcher(String text, int searchScope) {
    	this._find = text;
        this.scope = searchScope;
    }    

    @Override
    public boolean matches(Component c) {
    	if (0 != (scope & SEARCH_TRUE_NAME) &&
                isMatch(c.getName())) return true;

        if (0 != (scope & SEARCH_TOOLTIP) &&
   		    c instanceof JComponent &&
                isMatch(((JComponent) c).getToolTipText())) return true;

        if (0 != (scope & SEARCH_TRUE_INPUT)) {
            if (c instanceof JTextComponent && isMatch(((JTextComponent) c).getText())) return true;
            if (c instanceof TextComponent && isMatch(((TextComponent) c).getText())) return true;
        }
        if (0 != (scope & SEARCH_TRUE_CAPTION)) {
            if (c instanceof AbstractButton && isMatch(((AbstractButton) c).getText())) return true; // includes JButton, JMenuItem
            if (c instanceof Button && isMatch(((Button) c).getLabel())) return true;
            if (c instanceof JLabel && isMatch(((JLabel) c).getText())) return true;
            if (c instanceof Label && isMatch(((Label) c).getText())) return true;
            if (c instanceof Frame && isMatch(((Frame) c).getTitle())) return true;
            if (c instanceof Dialog && isMatch(((Dialog) c).getTitle())) return true;
            if (c instanceof JMenuItem && isMatch(getMenuPath((JMenuItem) c))) return true;
        }

        if (0 != (scope & SEARCH_SELECTION)) {
            if (c instanceof JList && isMatch("" + ((JList) c).getSelectedValue())) return true;
            if (c instanceof JComboBox && isMatch("" + ((JComboBox) c).getSelectedItem())) return true;
        }

        if (0 != (scope &  SEARCH_ACCESSIBLE)){
            AccessibleContext ac = c.getAccessibleContext();
            if (ac != null) {
                if (0 != (scope & SEARCH_ACCESSIBLE_NAME) &&
                    isMatch(ac.getAccessibleName())) return true;

                if (0 != (scope & SEARCH_ACCESSIBLE_DESC) &&
                    isMatch(ac.getAccessibleDescription())) return true;

                if (0 != (scope & SEARCH_ACCESSIBLE_TEXT) &&
                    isMatch("" + ac.getAccessibleText())) return true;
            }
        }
		return false;
    }

    boolean isMatch(String actual) {
        return stringsMatch(_find, actual);
    }

    static boolean stringsMatch(String expected, String actual) {
        return ExtendedComparator.stringsMatch(expected, actual);
    }

//    public boolean matchesMenu(JMenuItem mi) {
//        return  stringsMatch(_find, getMenuPath(mi));
//    }
//
    public static String getMenuPath(JMenuItem item) {
        Object parent = item.getParent();
        if(parent instanceof JPopupMenu) {
            parent = ((JPopupMenu)parent).getInvoker();
        }
        return parent instanceof JMenuItem ? getMenuPath((JMenuItem) parent) + "|" + item.getText() : item.getText();
    }
//
//    public static java.util.List splitMenuPath(String path) {
//        int lastFoundIndex = -1;
//        ArrayList selectionPath;
//        for(selectionPath = new ArrayList(); (lastFoundIndex = path.indexOf(124, lastFoundIndex)) != -1; ++lastFoundIndex) {
//            selectionPath.add(path.substring(0, lastFoundIndex));
//        }
//        selectionPath.add(path);
//        return selectionPath;
//    }

	public String toString() {
        return "[ Description Matcher:  \"" + _find + "\" (in " + scopeToString(scope) + ")]";
    }

    public static String scopeToString(final int scope) {
        switch (scope) {
            case SEARCH_NONE:
                return "nowhere";
            case SEARCH_ALL:
                return "any field";
            case SEARCH_TRUE_NAME:
                return "true name";
            case SEARCH_TRUE_INPUT:
                return "true input";
            case SEARCH_TRUE_CAPTION:
                return "true caption";
            case SEARCH_TRUE_TEXT:
                return "true text";
            case SEARCH_TOOLTIP:
                return "tooltip";
            case SEARCH_SELECTION:
                return "selection";
            case SEARCH_ACCESSIBLE_NAME:
                return "accessible name";
            case SEARCH_ACCESSIBLE_TEXT:
                return "accessible text";
            case SEARCH_ACCESSIBLE_DESC:
                return "accessible desc";
            case SEARCH_ACCESSIBLE:
                return "accessible";
            case SEARCH_NAME:
                return "name";
            case SEARCH_INPUT:
                return "input";
            case SEARCH_CAPTION:
                return "caption";
            case SEARCH_TEXT:
                return "text";
            case SEARCH_COMMENT:
                return "tooltip or desc";
            default:
                return Integer.toString(scope);
        }

    }
}
