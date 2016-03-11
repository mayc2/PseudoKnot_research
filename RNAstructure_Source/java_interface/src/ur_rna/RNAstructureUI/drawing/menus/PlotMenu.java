/*
 * (c) 2012 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program and its related applications.
 */

package ur_rna.RNAstructureUI.drawing.menus;

import ur_rna.RNAstructureUI.drawing.dialogs.PlotDialog;
import ur_rna.RNAstructureUI.utilities.FileFilters;
import ur_rna.RNAstructureUI.utilities.RNAstructureMenu;
import ur_rna.RNAstructureUI.utilities.StandardFileChooser;

/**
 * A class that handles a menu which can manipulate a drawn dot plot.
 *
 * @author Jessica S. Reuter
 */
public class PlotMenu
	extends RNAstructureMenu {
	private static final long serialVersionUID = 20120615;

	/**
	 * The plot dialog connected to this menu.
	 */
	private PlotDialog dialog;

	/**
	 * Constructor.
	 *
	 * @param dialog   The structure dialog connected to this menu.
	 */
	public PlotMenu( PlotDialog dialog ) {

		// Create the menu.
		super( "Output Plot" );
		addMenuItem(
			"Write Dot Plot File",
			"Write visible dots to a text file." );
		addMenuItem(
			"Write Postscript File",
			"Write the visible plot to a Postscript image file." );
		addMenuItem(
			"Write SVG File",
			"Write the visible plot to an SVG image file." );
		if( dialog.getFile().endsWith( "pfs" ) ) {
			addSeparator();
			addMenuItem(
				"Write Probable Structures File",
				"Write a CT file containing structures which are composed of " +
					"probable pairs of different levels." );
		}

		// Connect the plot dialog.
		this.dialog = dialog;
	}

	@Override
	protected void doMenuActions( String command ) {

		// Write a dot plot file, if necessary.
		if( command.contains( "Dot Plot" ) ) {
			String file = StandardFileChooser.getSaveName(FileFilters.DotPlot);
			if( file != null ) { dialog.writeTextFile( file ); }
		}

		// Write a Postscript file, if necessary.
		else if( command.contains( "Postscript" ) ) {
			String file = StandardFileChooser.getSaveName(FileFilters.Postscript);
			if( file != null ) { dialog.writePostscriptFile( file ); }
		}

		// Write a probable structures file, if necessary.
		else if( command.equals( "Write Probable Structures File" ) ) {
			String file = StandardFileChooser.getSaveName(FileFilters.CT);
			if( file != null ) {
				dialog.writeStructuresFile( file, true );
			}
		}

		// Write an SVG file, if necessary.
		else if( command.contains( "SVG" ) ) {
			String file = StandardFileChooser.getSaveName(FileFilters.SVG);
			if(file != null ) { dialog.writeSVGFile( file ); }
		}
	}
}
