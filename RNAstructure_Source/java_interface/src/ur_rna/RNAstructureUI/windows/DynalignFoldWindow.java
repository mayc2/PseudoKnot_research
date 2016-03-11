/*
 * (c) 2009 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program.
 */

package ur_rna.RNAstructureUI.windows;

import ur_rna.RNAstructureUI.menus.ConstraintsMenu;
import ur_rna.RNAstructureUI.menus.RolloverMenu;
import ur_rna.RNAstructureUI.utilities.*;
import ur_rna.RNAstructureUI.utilities.FieldPanel.FilePanel;
import ur_rna.RNAstructureUI.utilities.FieldPanel.NumberPanel;
import ur_rna.RNAstructureUI.utilities.NumberField.DoubleField;
import ur_rna.RNAstructureUI.utilities.NumberField.FloatField;
import ur_rna.RNAstructureUI.utilities.NumberField.IntegerField;

/**
 * A class responsible for initializing and running the Dynalign module, which
 * calculates common structures for two strands of nucleic acids.
 *
 * @author Jessica S. Reuter
 */
public class DynalignFoldWindow
	extends ModuleWindow {
	private static final long serialVersionUID = 20120802;

	/**
	 * Constructor.
	 *
	 * @param acid   The nucleic acid type.
	 */
	public DynalignFoldWindow( String acid ) {
		super( acid, acid + " Dynalign" );
	}

	@Override
	protected void executeModuleAction( String command ) {

		// Get any input controls from the window that handle actions.
		FilePanel files = (FilePanel)getInputControl( 1 );
		NumberPanel params = (NumberPanel)getInputControl( 2 );

		// If the action comes from the "Alignment File" button, try to
		// select an alignment file, and if one was selected set its name.
		if( command.startsWith( "Alignment File" ) ) {
			int index = 5;
			String file = StandardFileChooser.getSaveName(FileFilters.Alignment, files.getFile(index));
			if( file != null ) { files.setFile( index, file ); }
		}

		// If the action comes from one of the "CT File" buttons, try to
		// select a CT file, and if one was selected set its name, based on
		// its index.
		else if( command.startsWith( "CT File" ) ) {
			int index = ( command.endsWith( "1" ) ) ? 2 : 4;
			String file = StandardFileChooser.getSaveName(FileFilters.CT, files.getFile(index));
			if( file != null ) { files.setFile( index, file ); }
		}

		// If the action comes from one of the "Sequence File" button, get a
		// sequence file for input.
		else if( command.startsWith( "Sequence File" ) ) {

			// Attempt to select the file.
			// If no file was selected, return.
			String file = StandardFileChooser.getOpenName(FileFilters.Sequence);
			if( file == null ) { return; }

			// Set the sequence file name and its default CT file name in the
			// input panel based on the index.
			int index = ( command.endsWith( "1" ) ) ? 1 : 3;
			String defaultOut = replaceExtension( file, "ct" );
			files.setFile( index, file );
			files.setFile( index + 1, defaultOut );

			// If the command comes from the second sequence file button, try
			// to create a back end data structure.
			if( command.equals( "Sequence File 2" ) ) {

				// Get the two sequence file names.
				String seq1 = files.getFile( 1 );
				String seq2 = files.getFile( 3 );

				// Set the combined alignment file name.
				String ali = combineFileNames( seq1, seq2, "ali" );
				files.setFile( 5, ali );

				// Create a data structure.
				// If an error occurred creating the data structure, show an
				// error and return.
				String result =
					backend.buildDynalignDataStructure( seq1, seq2, isRNA );
				if( !verifyBackendResult(result, "Seq1: %s\nSeq2: %s\nRNA: %s", seq1, seq2, isRNA) ) { return; }

				// Reset the structure window size text field based on the
				// sequence length.
				int size1 = backend.getDynalignStructureWindowSize();
				((IntegerField)params.getField( 3 ))
					.resetField( size1, 0, Integer.MAX_VALUE );

				// Reset the alignment window size text field based on the
				// sequence length.
				int size2 = backend.getDynalignAlignmentWindowSize();
				((IntegerField)params.getField( 4 ))
					.resetField( size2, 0, Integer.MAX_VALUE );

				// Enable the menus.
				menuBar.enableMenus();
			}
		}
	}

	@Override
	protected void makeInputControls() {

		// Create the file input panel.
		FileField seq1 = FileField.createDisabled( "Sequence File 1" );
		FileField ct1 = FileField.createEnabled( "CT File 1" );
		FileField seq2 = FileField.createDisabled( "Sequence File 2" );
		FileField ct2 = FileField.createEnabled( "CT File 2" );
		FileField ali = FileField.createEnabled( "Alignment File" );
		FilePanel files = new FilePanel( this, seq1, ct1, seq2, ct2, ali );
		files.setPanelWidth( 350 );
		files.makePanel();

		// Create the parameter panel.
		DoubleField energy =
			new DoubleField( "Max % Energy Difference", 20, 0 );
		IntegerField structures =
			new IntegerField( "Max Number of Structures", 20, 1 );
		IntegerField windowStruct =
			new IntegerField( "Structure Window Size", 0, 0 );
		IntegerField windowAlign =
			new IntegerField( "Alignment Window Size", 0, 0 );
		NumberPanel params =
			new NumberPanel( energy, structures, windowStruct, windowAlign );
		params.setPanelWidth( 250 );
		params.makePanel();

		// Create the gap penalty panel.
		FloatField gapField = new FloatField( "Gap Penalty", 0.4 );
		NumberPanel gap = new NumberPanel( gapField );
		gap.setPanelWidth( 100 );
		gap.makePanel();

		// Create the base pair inserts box and the save file generation box.
		HTMLCheckBox insertBox =
			HTMLCheckBox.createSelectedBox( "Single BP Inserts<br>Allowed" );
		HTMLCheckBox saveBox =
			HTMLCheckBox.createEmptyBox( "Generate Save File" );

		// Add the components in their proper places.
		setGrid( 2, 1 );
		setFillHorizontal(); 
		placeComponent( 0, 0, files );
		setGrid( 1, 3 );
		placeComponent( 0, 1, params );
		setGrid( 1, 1 );
		placeComponent( 1, 1, gap );
		setInsets( -10, 0, -10, 0 );
		placeComponent( 1, 2, insertBox );
		setInsets( 0, 0, 0, 0 );
		placeComponent( 1, 3, saveBox );
		setGrid( 2, 1 );
		makeStartButton( 0, 4 );
	}

	@Override
	protected void runMainCalculation() {

		// Get all input controls from the window.
		FilePanel files = (FilePanel)getInputControl( 1 );
		NumberPanel params = (NumberPanel)getInputControl( 2 );
		NumberPanel gap = (NumberPanel)getInputControl( 3 );
		HTMLCheckBox pairsBox = (HTMLCheckBox)getInputControl( 4 );
		HTMLCheckBox saveBox = (HTMLCheckBox)getInputControl( 5 );

		// Get the data from the file input panel.
		// If an error occurred while retrieving data, return.
		files.getFile( 1 );
		String ct1 = files.getFile( 2 );
		files.getFile( 3 );
		String ct2 = files.getFile( 4 );
		String align = files.getFile( 5 );
		if( files.isError() ) { return; }

		// Get the data from the parameters panel.
		Double percent = ((DoubleField)params.getField( 1 )).getValue();
		Integer structures = ((IntegerField)params.getField( 2 )).getValue();
		Integer windowStructure =
			((IntegerField)params.getField( 3 )).getValue();
		Integer windowAlign = ((IntegerField)params.getField( 4 )).getValue();

		// Get the data from the gap penalty panel and check boxes.
		Float penalty = ((FloatField)gap.getField( 1 )).getValue();
		boolean isInsert = pairsBox.isSelected();
		String saveFile =
			( saveBox.isSelected() ) ? replaceExtension( align, "dsv" ) : "";

		// Run the calculation.
		// If an error occurred during calculation, return.
		showProgressBarDeterminate();
		String result =
			backend.runDynalign(
				ct1, ct2, saveFile, align, percent, structures,
				windowStructure, windowAlign, penalty, isInsert );
		finishModule( result );
		if( !result.equals( "" ) ) { return; }

		// If the user wants to draw structures, draw them.
		String query = "Do you want to draw structures?";
		String doDraw = new SimpleDialogHandler().makeTwoChoicesDialog( query );
		if( doDraw.equals( "OK" ) ) {
			DrawingWindow one = new DrawingWindow( ct1 );
			DrawingWindow two = new DrawingWindow( ct2 );
			if( ( one.isError() == false ) && ( two.isError() == false ) ) {
				one.viewWindow();
				two.viewWindow();
			}
		}
	}

	@Override
	protected RolloverMenu[] setMenus() {

		// Create the temperature menu.
		ConstraintsMenu temperature = new ConstraintsMenu( backend );
		temperature.buildTemperatureMenu();

		// Create the constraint menu for sequence 1.
		ConstraintsMenu seq1 = new ConstraintsMenu( backend );
		seq1.setText( "Constraints for Sequence 1" );
		seq1.addGeneralSection();
		seq1.addShowResetSection();
		seq1.addSaveRestoreSection();

		// Create the constraint menu for sequence 2.
		ConstraintsMenu seq2 = new ConstraintsMenu( backend );
		seq2.setText( "Constraints for Sequence 2" );
		seq2.addGeneralSection();
		seq2.addShowResetSection();
		seq2.addSaveRestoreSection();

		// Create the Dynalign alignment constraints menu.
		ConstraintsMenu alignments = new ConstraintsMenu( backend );
		alignments.buildDynalignAlignmentMenu();

		// Return the menus in an array.
		return new ConstraintsMenu[]{ temperature, seq1, seq2, alignments };
	}
}
