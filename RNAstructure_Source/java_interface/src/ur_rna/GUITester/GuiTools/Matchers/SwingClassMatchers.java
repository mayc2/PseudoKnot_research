package ur_rna.GUITester.GuiTools.Matchers;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public abstract class SwingClassMatchers {
    public static final ClassMatcher menu = ClassMatcher.of(MenuElement.class);
    public static final ClassMatcher menuItem = ClassMatcher.of(JMenuItem.class);
    public static final ClassMatcher button = ClassMatcher.of(JButton.class);
    public static final ClassMatcher text = ClassMatcher.of(JTextComponent.class);
    public static final ClassMatcher field = ClassMatcher.of(JTextField.class);
    public static final ClassMatcher textArea = ClassMatcher.of(JTextArea.class);
    public static final ClassMatcher check = ClassMatcher.of(JCheckBox.class);
    public static final ClassMatcher toggle = ClassMatcher.of(JToggleButton.class);
    public static final ClassMatcher radio = ClassMatcher.of(JRadioButton.class);
    public static final ClassMatcher list = ClassMatcher.of(JList.class);
    public static final ClassMatcher combo = ClassMatcher.of(JComboBox.class);
    public static final ClassMatcher label = ClassMatcher.of(JLabel.class);
    public static final ClassMatcher panel = ClassMatcher.of(JPanel.class);
    public static final ClassMatcher dialog = ClassMatcher.of(Dialog.class);
    public static final ClassMatcher window = ClassMatcher.of(Window.class);
    public static ClassMatcher ofType(String typeName) {
        Class cls = findClass(typeName);
        return cls == null ? null : ClassMatcher.of(cls);
    }
    public static Class findClass(String typeName) {
        String[] packages = { "javax.swing", "java.awt", "javax.swing.text",
                "javax.swing.filechooser", "javax.swing.text.html", "javax.swing.text.rtf",
                "javax.swing.tree"};
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException ex) {
            //ignore
        }
        for (String p : packages) {
            try {
                return Class.forName(p + "." + typeName);
            } catch (ClassNotFoundException ex) {
                //ignore
            }
        }
        return null;
    }
}
