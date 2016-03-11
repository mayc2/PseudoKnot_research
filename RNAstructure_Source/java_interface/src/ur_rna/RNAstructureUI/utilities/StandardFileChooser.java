package ur_rna.RNAstructureUI.utilities;

import ur_rna.Utilities.AppLog;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by Richard on 10/23/2015.
 */
public class StandardFileChooser extends JFileChooser {
    private boolean fileShouldExist;
    private boolean modifyHomeDir = true, isCurDirSet;
    private String context;
    private static HashMap<String,File> _homeDirs;

    static {
        _homeDirs = new HashMap<>();
        loadPaths(StandardFileChooser.class);
    }

    public static void loadPaths(Class cls) {
        try {
            Preferences p = Preferences.userNodeForPackage(cls);
            if (p.nodeExists("user.home")) {
                String path = p.get("user.home",  System.getProperty("user.home"));
                if (path != null && new File(path).exists() )
                    System.setProperty( "user.home", path );
            }
            p = p.node("RecentPaths");
            for (String key : p.keys())
                _homeDirs.put(key, new File(p.get(key, "")));
        } catch (BackingStoreException ex) {
            AppLog.getDefault().error("Failed to load recent paths.", ex);
        }
    }
    public static void savePaths() { savePaths(StandardFileChooser.class); }
    public static void savePaths(Class cls) {
        try {
            Preferences p = Preferences.userNodeForPackage(cls);
            p.put("user.home",System.getProperty("user.home"));
            p = p.node("RecentPaths");
            for (String key : _homeDirs.keySet()) {
                File f = _homeDirs.get(key);
                if (f != null) p.put(key, f.toString());
            }
        } catch (Exception ex) {
            AppLog.getDefault().error("Failed to save recent paths.", ex);
        }
    }

    public static File getHomeDir(String context) {
        File f = _homeDirs.get(context);
        if (f == null || !f.exists())
            f = new File( System.getProperty( "user.home" ) );
        if (!f.exists())
            f = null;
        return f;
    }
    public static void setHomeDir(String context, File value) {
        _homeDirs.put(context, value);
        if (context.equals("home") || context.equals(""))
            System.setProperty("user.home", value.getPath());
        savePaths();
    }

    public StandardFileChooser() { this(CUSTOM_DIALOG, null, null); }
    public StandardFileChooser(boolean isSaveDialog, String filters, String title) { this (isSaveDialog ? SAVE_DIALOG : OPEN_DIALOG, filters, title); }
    public StandardFileChooser(int dialogType, String filters, String title) {
        super.setDialogType(dialogType);
        super.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (filters == null || filters.trim().length() == 0)
            super.setAcceptAllFileFilterUsed(true);
        else {
            super.setAcceptAllFileFilterUsed(false);
            addFilters(filters);
        }
        if (title != null && title.length() != 0)
            super.setDialogTitle(title);
        setFileShouldExist(dialogType == OPEN_DIALOG);
    }
    public StandardFileChooser setDefaultFile(String path) {
        if (path == null) return setDefaultFile((File)null);
        return setDefaultFile(new File(path));
    }
    public StandardFileChooser setDefaultFile(File f) {
          if (f == null || f.isDirectory())
            setStartDir(f);
        else {
            super.setSelectedFile(f);
            String parent = f.getParent();
            if (parent != null) {
                File parentDir = new File(parent);
                if (parentDir.exists() && !parentDir.equals(getCurrentDirectory()))
                    setStartDir(parentDir);
            }
        }
        return this;
    }
    public StandardFileChooser setStartDir(String path) { if (path == null) return setStartDir((File)null); return setStartDir(new File(path)); }
    public StandardFileChooser setStartDir(File dir) {
        isCurDirSet = true;
        super.setCurrentDirectory(dir);
        return this;
    }

    /**
     * Show the File Chooser and return the selected file path, if successful, or null if the user
     * cancels.
     * @return The selected file path, if successful, or null if the user cancels.
     */
    public File showFileDialog(Component parent) {
        // Get the current file chooser home directory as a file.
        String dirContext = context == null ? "" : context;
        File cd = StandardFileChooser.getHomeDir(dirContext);
        if (!isCurDirSet && cd != null && cd.exists()) setCurrentDirectory( cd );
//        StandardFileChooser fc = new StandardFileChooser(dialogType, filters, title);
//        fc.setAcceptAllFileFilterUsed(showAll);
//
//        if( !defaultFile.equals( "" ) ) {
//            chooser.setSelectedFile( new File( defaultFile ) );
//        }

//        // Add the file filter(s).
//        int length = fileTypes.length;
//        for( int i = 1; i <= length; i++ ) {
//            String fullDescription =
//                    fileTypes[i-1] + " Files (*." + extensions[i-1] + " )";
//            chooser.addChoosableFileFilter( new FileNameExtensionFilter(
//                    fullDescription, extensions[i-1] ) );
//        }

        // Show the file dialog, and then if no file was selected, return the
        // empty string.
//        int approvalValue = ( selectorType == Types.OPEN ) ?
//                this.showOpenDialog( null ) : chooser.showSaveDialog( null );
//        if( approvalValue != JFileChooser.APPROVE_OPTION ) { return "|"; }
        int success = super.showDialog(parent, this.getApproveButtonText());
        if( success != JFileChooser.APPROVE_OPTION ) { return null; }

        if (modifyHomeDir)
            StandardFileChooser.setHomeDir(dirContext, getCurrentDirectory());
        return getSelectedFile();
    }

    /**
     * Set the context for default startup folder.
     * @param context
     * @return Returns this StandardFileChooser for chained calls during configuration.
     */
    public StandardFileChooser setContext(String context) {
        this.context = context;
        return this;
    }

    public String getContext() {
        return context;
    }

    /**
     * Set the text that appears on the accept (OK) button.
     * @param text The text to display on the button.
     * @return Returns this StandardFileChooser for chained calls during configuration.
     */
    public StandardFileChooser setButtonText(String text) {
        this.setApproveButtonText(text);
        return this;
    }

    /**
     *  Adds a set of filter-specs to the list of file filters for the FileChooser.
     *  A "filter-spec" is a string containing a File Type Description followed by a bar ({@code |}) and one or more
     *  File Extensions, separated by semi-colons ({@code ;}). I.e.: {@code Description|ext1[;ext2...]}
     *  Both the description and at least one extension are required and cannot be blank.
     *  A "set" of filter-specs consists of one or more filter-specs separated by a bar. E.g.:
     *  {@code filter-spec_1[|filter-spec_2...]} i.e. {@code Description|ext1_1[;ext1_2...]|Description2|ext2_1[;ext2_2...] }
     *  A blank filter-spec is allowed and will simply be ignored. So the following will result in
     *  the addition of exactly two filters (despite the empty file-specs at the start, middle and end).
     *  ${ code |Description|ext1;ext2||Description2|ext1;ext2| }
     *  However the following filter-spec set would raise an error, because the list of extensions is effectively blank.
     *  (the ext1... would be interpreted as the start of the <em>second</em> filter-spec).
     *  ${ code Description||ext1;ext2 }
     *  As a special case, the filter-specs "*" or "*|*" are both interpreted as the accept-all-files filter, i.e.
     *  "All Files (*.*)"
     * @param filterSpecSet
     * @return Returns this StandardFileChooser for chained calls during configuration.
     */
    public StandardFileChooser addFilters(String filterSpecSet) {
        String[] parts = filterSpecSet.split("\\|");
        for (int i = 1; i < parts.length; i++) {
            String desc = parts[i-1].trim();
            if (desc.length() == 0)
                continue; //ignore blanks between file-specs
            if (desc.equals("*")) {
                super.setAcceptAllFileFilterUsed(true);
                if (parts[i].equals("*"))
                    i++;
                continue;
            }
            String[] ext = parts[i].split(";");

            desc += String.format(" Files (*.%s)", String.join(", *.", ext));
            this.addChoosableFileFilter(new FileNameExtensionFilter(desc, ext));
            i++; // move to the next filter-spec (two elements are consumed for each filter-spec)
        }
        return this;
    }

    /**
     * Clear any existing filters.
     * This is useful in any case where a file filter has been added, but is no longer desired.
     * For example when the default constructor is used, the all-files-filter is automatically added.
     * If that filter is not desired, then calling clearFilters will remove it along with any other existing filters.
     * If the only undesired filter is the all-files-filter, then simply calling
     * {@link #setAcceptAllFileFilterUsed(boolean)} with the value false would be more efficient
     * than clearing all filters.
     * @return Returns this StandardFileChooser for chained calls during configuration.
     */
    public StandardFileChooser clearFilters() {
        this.setAcceptAllFileFilterUsed(false);
        super.resetChoosableFileFilters();
        return this;
    }

//    public static class Save extends StandardFileChooser {
//        public Save() {super(true, null, null);}
//        public Save(String filters) { super(true, filters, null); }
//
//    }
//    public static class Open extends StandardFileChooser {
//        public Open() { super(false, null, null);  }
//        public Open(String filters) { super(false, filters, null); }
//    }
    public static String getSaveName(String filters, String defaultPath, String title, String context, Component parent) {
        return getFileName(true, parent, title, defaultPath, filters, context);
    }
    public static String getSaveName(String filters) { return getSaveName(filters, null); }
    public static String getSaveName(String filters, String defaultPath) { return getSaveName(filters, defaultPath, null, null, null); }

    public static String getOpenName(String filters, String defaultPath, String title, String context, Component parent) {
        return getFileName(false, parent, title, defaultPath, filters, context);
    }
    public static String getOpenName(String filters) { return getOpenName(filters, null); }
    public static String getOpenName(String filters, String defaultPath) { return getOpenName(filters, defaultPath, null, null, null); }

    public static String getFileName(boolean forSave, Component parent, String title, String defaultPath, String filters, String context) {
        StandardFileChooser fc = new StandardFileChooser(forSave, filters, title);
        if (context == null) context = title;
        if (context != null)
            fc.setContext(context);
        if (defaultPath != null)
            fc.setDefaultFile(defaultPath);
        File f = fc.showFileDialog(parent);
        if (f == null)
            return null;
        return f.getAbsolutePath();
    }

    public boolean getFileShouldExist() { return fileShouldExist; }
    public void setFileShouldExist(final boolean fileShouldExist) {
        this.fileShouldExist = fileShouldExist;
    }

    public String getDefaultExtension() {
        FileFilter f = this.getFileFilter();
        if (f instanceof FileNameExtensionFilter) {
            FileNameExtensionFilter ff = (FileNameExtensionFilter)f;
            String[] exts = ff.getExtensions();
            if (exts.length != 0)
                return exts[0];
        }
        return "";
    }
    public boolean passesFilter(File f) {
        FileFilter ff = this.getFileFilter();
        return ff == null || ff.accept(f);
    }
    /**
     * Returns the selected file. This can be set either by the
     * programmer via <code>setSelectedFile</code> or by a user action, such as
     * either typing the filename into the UI or selecting the
     * file from a list in the UI.
     *
     * @return the selected file
     * @see #setSelectedFile
     */
    @Override
    public File getSelectedFile() {
        File f = super.getSelectedFile();
        if (f == null || f.exists()) return f;
        // Get the file, and make sure it ends with the proper extension.
        if (!passesFilter(f))
            f = new File(f.getPath() + "." + getDefaultExtension());
        return f;
    }
    @Override
    public void approveSelection(){
        File f = getSelectedFile();
        if (f == null) {
            JOptionPane.showMessageDialog(null, "Please select a file.");
            return;
        }
        if(!f.exists() && getFileShouldExist()){
            JOptionPane.showMessageDialog(null, "The specified file does not exist: \n" + f.getPath());
            return;
        } else if(f.exists() && getDialogType() == SAVE_DIALOG){
            int result = JOptionPane.showConfirmDialog(this,"The file exists, overwrite?\n\nDetails:\nFile: " + f.getPath(),"Existing file",JOptionPane.YES_NO_CANCEL_OPTION);
            switch(result){
                case JOptionPane.YES_OPTION:
                    super.approveSelection();
                    return;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CLOSED_OPTION:
                    return;
                case JOptionPane.CANCEL_OPTION:
                    cancelSelection();
                    return;
            }
        }
        super.approveSelection();
    }
    public boolean getModifyHomeDir() {
        return modifyHomeDir;
    }
    public void setgetModifyHomeDir(boolean value) {
        this.modifyHomeDir= value;
    }
}
