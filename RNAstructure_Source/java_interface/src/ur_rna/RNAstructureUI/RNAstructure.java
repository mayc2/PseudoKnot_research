/*
 * (c) 2009 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program.
 */

package ur_rna.RNAstructureUI;

import ur_rna.RNAstructureUI.utilities.MacChecker;
import ur_rna.RNAstructureUI.utilities.Resources;
import ur_rna.RNAstructureUI.utilities.SimpleDialogHandler;
import ur_rna.Utilities.*;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

/**
 * A class that starts the RNAstructure interface.
 *
 * @author Jessica S. Reuter
 * @author Richard M. Watson
 */
public class RNAstructure
	implements Serializable {
	private static final long serialVersionUID = 20120802;
	private static final AppLog log = AppLog.getDefault();
	/**
	 * The main method.
	 *
	 * @param args   The command line arguments (ignored).
	 */
	public static void main( String[] args ) {
		try {
			parseCommandArgs(args); //checks for some command-line arguments and sets System properties.

			// If the OS is a type of Mac, certain things need to be set in
			// order to make the native Java more "Mac-like."
			MacChecker.checkOS();
			Resources.verify();

			// Set the look and feel for the current OS.
			String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel( lookAndFeel );

			// Detect the architecture of the Java platform (32 or 64 bits
			// -- this is NOT necessarily the architecture of the OS!!)
			String libExt, jvmBits = System.getProperty("sun.arch.data.model");
			if (jvmBits.equals("64") || jvmBits.equals("32"))
					libExt = "_"+jvmBits;
			else
					libExt = "";

			log.debug("OS: %s Arch: %s Bits: %s%n", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("sun.arch.data.model"));
			log.debug("user.home: " + System.getProperty("user.home"));
			log.debug("CurDir: " + new File(".").getCanonicalPath());
			log.debug("java.library.path: " + System.getProperty("java.library.path"));
			log.debug("java.class.path: " + System.getProperty("java.class.path"));
			if (log.getDebug())
				System.getProperties().list(log.getDbgStream());
			log.debug("Headless: " + java.awt.GraphicsEnvironment.isHeadless());

			// The splash screen window is loaded by a native Java library
			// before the JVM loads (based on the corresponding entry
			// in the Manifest file). It is closed automatically as soon
			// as the first window is displayed by Swing/AWT.
			// So there is no need to manipulate the splash screen directly.
			// If running directly, without compiling to JAR you can specify
			// the splash image by passing the flag  -splash:<path_to_image.gif>
			// on the java command line.

			// Get a reference to the splash screen if it has been shown.
			// getSplashScreen will return null if the splash image is not specified in the
			//   manifest or passed in on the command-line.
			// This would happen for example, if the application is launched
			//   in an IDE directly, as opposed to via a JAR file.
			java.awt.SplashScreen splash = java.awt.SplashScreen.getSplashScreen();
			if (splash != null)
				Thread.sleep(2000); //give the splash screen 2 seconds to display before launching the main window.


            String[] libNames = { "RNAstructure_GUI" + libExt, "RNAstructure_GUI" };
			if (!tryLoadLibrary(libNames, "Unable to load the RNAstructure library!")) {
                System.exit(2);
                return;
            }

			for (String s : ClassLoaderInfo.getMyLoadedLibraries()) {
				final String find = "RNAstructure".toLowerCase();
				String lc = s.toLowerCase();
				if (lc.contains(find))
					log.debug("Loaded Native DLL: " + s);
			}
			verifyDataPath();

			// Start the GUI.
			JFrame frm = new ApplicationRootFrame();
			frm.getRootPane().addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(final PropertyChangeEvent evt) {
					log.debug("PropertyChangeEvent: " + evt.toString());
				}
			});
		} catch( Exception e ) {
			log.error("error loading RNAstructure.", e);
			try {
				java.io.FileWriter w = new java.io.FileWriter("RNAstructure_error.log");
				w.write(DateHelper.getFormattedDate(DateHelper.ISO8601) + "\n");
				e.printStackTrace(new java.io.PrintWriter(w));
				w.flush();
				w.close();
			} catch (Throwable ex) {
				showWarning("Could not write error log:\n" + AppLog.getErrorInfo(ex));
			}
			System.exit( 10 );
		}
	}

	private static void parseCommandArgs(String[] args) {
		try {
			java.util.List<String> list = Arrays.asList(args);
			if (list.contains("-d"))
				System.setProperty("debug", "true");
			int foundVerbose = list.indexOf("-v");
			if (foundVerbose != -1)
				System.setProperty("verbose", args[foundVerbose+1]);
			if (list.contains("-s"))
				System.setProperty("silentLaunch","true");
		} catch (Throwable e) {
			log.error("error parsing command line.", e);
		}
	}

	static String findDataPath() {
		String paths[] = { ".", "..", "./resources", "../Resources" };
		String fullPaths[] = { "{path}/", "{path}/data_tables", "{app}/{path}/", "{app}/{path}/data_tables" };

		String appPath = PathTools.getAppPath(RNAstructure.class);
        String filter = "*.dat";
        String realpath;
		for (String path : paths) {
			for (String full : fullPaths) {
                String fullPath = full.replace("{app}", appPath).replace("{path}", path);
                File dir = new File(fullPath);
                //System.out.println("Trying: " + fullPath + " ==> " + dir.getAbsolutePath());
                if (dir.exists())
                    try {
                        realpath = dir.getCanonicalPath();
                        if (PathTools.listFiles(realpath, filter).size() != 0)
                            return realpath;
                    } catch (Exception ex) {
                        //Do nothing. continue the loop.
                    }
            }
		}
		return null;
	}

	static boolean verifyDataPath() {
		final String vardef = "The DATAPATH environment variable";

		String dataPath = System.getenv("DATAPATH");
		String dataPathWarning = null;
		String absPath;

		if (dataPath == null || dataPath == "") {
			dataPathWarning = "DATAPATH is undefined!";
		} else {
            final String APP_PATH = "{APP_PATH}";
            if (dataPath.contains(APP_PATH)) {
                String appPath = PathTools.getAppPath(RNAstructure.class);
                System.out.println(APP_PATH + "=" + appPath);
                if (appPath == null) appPath = ".";
                dataPath = dataPath.replace(APP_PATH, appPath);
                RNAstructureBackendCalculator.setEnvVar("DATAPATH="+dataPath);
            }
            File dataPathFile = new File(dataPath);

			try {
				absPath = dataPathFile.getCanonicalPath();
			} catch (IOException ex) {
				absPath = "(error - Could not obtain absolute path: " + ex.getMessage() + ")";
			}
			String info = String.format("DATAPATH = '%s' ==> %s", dataPath, absPath);
			log.debug(info);
			if (!dataPathFile.exists())
				dataPathWarning = "The directory indicated by DATAPATH does not exist!\n" + info;
		}

		if (dataPathWarning == null)
			return true;

		//DATAPATH was either NULL or not found. In either case, search for one in standard directories.
		dataPath = findDataPath();
		if (dataPath == null) {
			showWarning(dataPathWarning + "\n\nThe DATAPATH environment variable must be set to the directory in which thermodynamic files are stored.");
			return false;
		}
		log.warn("The DATAPATH environment variable was not set correctly, but a suitable default was found at \"" + dataPath + "\".");
		RNAstructureBackendCalculator.setEnvVar("DATAPATH=" + dataPath);
		return true;
	}

	static String arrJoin(Object[] arr, String sep) {
		StringBuilder sbStr = new StringBuilder();
		for (Object o : arr) {
			sbStr.append(o.toString());
			sbStr.append(sep);
		}
		if (sbStr.length() > 0)
			sbStr.setLength(sbStr.length()-sep.length());
		return sbStr.toString();
	}

	static boolean tryLoadLibrary(String[] alternateLibNames, String errorMessage) {
        Throwable[] errors = new Throwable[alternateLibNames.length];
        int i = 0;
        for (String name : alternateLibNames) {
            try {
                System.loadLibrary(name);
                return true;
            } catch (Throwable ex) {
                // Error is usually a UnsatisfiedLinkError, but let's catch all of them so we can show them to the user.
                errors[i++] = ex;
            }
        }
        if (errorMessage != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(errorMessage).append("\n\n").append("Attempted Library Names and Error Details:\n\n");
            for(i = 0; i < errors.length; i++)
                sb.append(i+1).append(")  ").append(alternateLibNames[i])
                .append("\n      ").append(errors[i].getMessage()).append("\n\n");
            showWarning(sb.toString());
        }
        return false;
	}

	static void showWarning(String message) {
		log.error("Warning: " + message);
		if (!Convert.ToBool(System.getProperty("silentLaunch")))
			new SimpleDialogHandler().makeWarningDialog(message);

	}
	static void showInfo(String message) {
		log.info("Information: " + message);
		if (!Convert.ToBool(System.getProperty("silentLaunch")))
			new SimpleDialogHandler().makeMessageDialog(message);
	}
}
