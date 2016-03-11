/*
 * (c) 2011 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program.
 */

package ur_rna.RNAstructureUI.menus;

import ur_rna.RNAstructureUI.utilities.FileFilters;
import ur_rna.RNAstructureUI.utilities.StandardFileChooser;
import ur_rna.RNAstructureUI.windows.DrawingWindow;
import ur_rna.RNAstructureUI.windows.DynalignRefoldWindow;
import ur_rna.RNAstructureUI.windows.InternalWindow;
import ur_rna.RNAstructureUI.windows.OligoScreenWindow;
import ur_rna.RNAstructureUI.windows.RefoldWindow;
import ur_rna.RNAstructureUI.windows.SequenceDisplayWindow;

/**
 * A class that creates a "File" menu.
 * <br><br>
 * Most File menus are identical, but items can be added to or subtracted from
 * them in specific contexts.
 *
 * @author Jessica S. Reuter
 */
public class FileMenu
	extends RolloverMenu {
	private static final long serialVersionUID = 20120802;

	/**
	 * Constructor.
	 *
	 * @param window   The window this menu is attached to.
	 */
	public FileMenu( InternalWindow window ) {
		super( "File" );
		addMenuItem( "New Sequence", "Create a new sequence.", 'N' );
		addMenuItem( "Open Sequence", "Open an existing sequence.", 'O' );
		addSeparator();
		addMenuItem( "OligoScreen",
			"Calculate thermodynamic parameters for a set of " +
			"oligonucleotides." );
		addSeparator();
		addMenuItem( "Draw",
			"Draw a secondary structure." );
		addMenuItem( "Dot Plot",
			"Display the energy dot plot for a sequence that was previously " +
			"folded." );
		addMenuItem( "Dot Plot Partition Function",
			"Display base pairing probabilities for a previously calculated " +
			"sequence." );
		addMenuItem( "Dot Plot Dynalign",
			"Generate a Dynalign dot plot for two sequences." );
		addMenuItem( "Dot Plot From Text File",
			"Draw a dot plot from a text file." );
		addSeparator();
		addMenuItem( "Refold From Save File",
			"Refold a sequence from its save file." );
		addMenuItem( "Refold From Dynalign Save File",
			"Refold from a Dynalign calculation." );
		addSeparator();
		addMenuItem( "Exit", "Exit the RNAstructure application." );
	}

	@Override
	protected void doMenuActions( String command ) {

		// If the command is a drawing command, prepare a drawing window.
		if( command.startsWith( "D" ) ) {

			// Show an energy dot plot window, if possible.
			if( command.equals( "Dot Plot" ) ) {
				String file = StandardFileChooser.getOpenName(FileFilters.Folding);
				if( file != null ) {
					DrawingWindow window = new DrawingWindow( file );
					if(!window.isError()) { window.viewWindow(); }
				}
			}

			// Show a Dynalign dot plot window, if possible.
			else if( command.equals( "Dot Plot Dynalign" ) ) {
				String file = StandardFileChooser.getOpenName(FileFilters.Dynalign);
				if( file != null ) {
					DrawingWindow window1 = new DrawingWindow( file, 1 );
					DrawingWindow window2 = new DrawingWindow( file, 2 );
					boolean validWindows =
						(!window1.isError()) &&
						(!window2.isError());
					if( validWindows ) {
						window1.viewWindow();
						window2.viewWindow();
					}
				}
			}

			// Show a text dot plot window, if possible.
			else if( command.equals( "Dot Plot From Text File" ) ) {
				String file = StandardFileChooser.getOpenName(FileFilters.DotPlot);
				if( file != null ) {
					DrawingWindow window = new DrawingWindow( file );
					if(!window.isError()) { window.viewWindow(); }
				}
			}

			// Show a probability dot plot window, if possible.
			else if( command.equals( "Dot Plot Partition Function" ) ) {
				String file = StandardFileChooser.getOpenName(FileFilters.Partition);
				if( file != null ) {
					DrawingWindow window = new DrawingWindow( file );
					if(!window.isError()) { window.viewWindow(); }
				}
			}

			// Initialize a structure window, if possible.
			else if( command.equals( "Draw" ) ) {
				String file = StandardFileChooser.getOpenName(FileFilters.CT);
				if( file != null ) {
					DrawingWindow window = new DrawingWindow( file );
					if(!window.isError()) { window.viewWindow(); }
				}
			}
		}

		// If the command is to exit the application, do so.
		else if( command.equals( "Exit" ) ) { System.exit( 0 ); }

		// If the command is to build a new sequence, show a blank sequence
		// display window.
		else if( command.equals( "New Sequence" ) ) {
			new SequenceDisplayWindow().viewWindow();
		}

		// If the command is to run OligoScreen, show an OligoScreen window.
		else if( command.equals( "OligoScreen" ) ) {
			new OligoScreenWindow().viewWindow();
		}

		// If the command is to open an existing sequence, show a filled
		// sequence display window.
		else if( command.equals( "Open Sequence" ) ) {
			String file = StandardFileChooser.getOpenName(FileFilters.SequenceExtended);
			if( file != null ) {
				new SequenceDisplayWindow( file ).viewWindow();
			}
		}

		// If the command is to refold from a Dynalign save file, open a
		// Dynalign refolding window.
		else if( command.equals( "Refold From Dynalign Save File" ) ) {
			new DynalignRefoldWindow().viewWindow();
		}

		// If the command is to refold from a folding save file, open a
		// refolding window.
		else if( command.equals( "Refold From Save File" ) ) {
			new RefoldWindow().viewWindow();
		}
	}
}
