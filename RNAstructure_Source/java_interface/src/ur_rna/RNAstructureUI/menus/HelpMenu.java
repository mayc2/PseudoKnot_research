/*
 * (c) 2011 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program.
 */

package ur_rna.RNAstructureUI.menus;

import ur_rna.RNAstructureUI.utilities.ImageGrabber;
import ur_rna.RNAstructureUI.utilities.SimpleDialogHandler;
import ur_rna.Utilities.AppLog;

import java.awt.*;
import java.net.URI;

/**
 * A class that creates a "Help" menu.
 *
 * @author Jessica S. Reuter
 */
public class HelpMenu
	extends RolloverMenu {
	private static final long serialVersionUID = 20120802;

	/**
	 * Constructor.
	 */
	public HelpMenu() {
		super( "Help" );
		addMenuItem( "Help Topics", "Get online help.", "F1" );
		addSeparator();
		addMenuItem( "About RNAstructure...",
			"Display program information, version number, and copyright." );
	}

	@Override
	protected void doMenuActions( String command ) {
		SimpleDialogHandler dialog = new SimpleDialogHandler();

		// If the command is to show the about screen, do so.
		if( command.startsWith( "About" ) ) {
			try {
				dialog.makeMessageDialog( ImageGrabber.getImageLabel( "logo.png" ) );
			} catch( Exception e ) {
				dialog.makeErrorDialog( "error showing about screen.\n" + AppLog.getErrorInfo(e));
			}
		}

		// Otherwise, show the help in a browser window.
		else {
			try {
				String page =
					"http://rna.urmc.rochester.edu/GUI/html/Contents.html";
				Desktop.getDesktop().browse( new URI( page ) );
			} catch( Exception e ) {
				dialog.makeErrorDialog( "error showing online help." );
			}
		}
	}
}
