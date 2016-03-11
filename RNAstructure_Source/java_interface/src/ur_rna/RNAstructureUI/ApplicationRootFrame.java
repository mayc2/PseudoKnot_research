/*
 * (c) 2009 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program.
 */

package ur_rna.RNAstructureUI;

import ur_rna.RNAstructureUI.menus.DynamicMenuBar;
import ur_rna.RNAstructureUI.utilities.ImageGrabber;
import ur_rna.RNAstructureUI.windows.InternalWindow;

import javax.swing.*;
import java.awt.*;


/**
 * A class that creates the application frame of the RNAstructure GUI.
 *
 * @author Jessica S. Reuter
 */
public class ApplicationRootFrame
	extends JFrame {
	private static final long serialVersionUID = 20120802;

	/**
	 * The main RNAstructure toolbar.
	 */
	private JToolBar bar;
	private JLabel infoLabel;

	/**
	 * Constructor.
	 * <br><br>
	 * Create the frame, set the icon and layout, add the toolbar, back panel,
	 * and info label, add a focus listener that shows the default menus, then
	 * set the size and location before setting the frame to be visible.
	 */
	public ApplicationRootFrame() {
		super("RNAstructure");
		String iconString = "Icon.gif";
		setIconImage(ImageGrabber.tryGetImage(iconString));
		setLayout(new BorderLayout());
		setJMenuBar(new DynamicMenuBar(null));
		buildToolBar();
		add(bar, BorderLayout.PAGE_START);
		add(buildBackPanel(), BorderLayout.CENTER);
		buildInfoLabel();
		resetInfoLabel();
		resetFrame();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024, 730);
		setLocationRelativeTo(null);
		//System.out.println("Before vis");
		setVisible(true);
		//System.out.println("After vis");
	}

//	@Override
//	protected void processWindowEvent(WindowEvent e) {
//		try {
//			System.out.println("Window Event "+ e.getID() + " - " + e.toString());
//			super.processWindowEvent(e);
//		} catch (Throwable t) {
//			System.out.println("error " + t.getMessage());
//			t.printStackTrace();
//		}
//	}


	/**
	 * Create the gray back panel that fills the main frame.
	 *
	 * @return   The back panel.
	 */
	private JDesktopPane buildBackPanel() {
		JDesktopPane pane = new JDesktopPane();
		pane.setBackground( Color.GRAY );
		pane.removeAll();
		return pane;
	}

	/**
	 * Create the label at the bottom of the main frame.
	 * <br><br>
	 * This informative label holds helpful messages about modules or actions
	 * that can be taken by the user.
	 */
	private void buildInfoLabel() {
		JLabel label = new JLabel( "RNAstructure" );
		label.setFont( new Font( label.getFont().getFontName(), 0, 16 ) );
		add( label, BorderLayout.SOUTH );
		infoLabel = label;
	}

	/**
	 * Build the main RNAstructure toolbar.
	 * <br><br>
	 * Note that adding the same action listeners to the buttons as their
	 * corresponding menu items uses some hard-coded numbers, so if the order of
	 * the menu items or tool bar items needs to be changed, those numbers
	 * (indexes) might need to be changed too.
	 */
	private void buildToolBar() {

		// Create the toolbar.
		bar = new JToolBar();
		bar.setFloatable( true );

		// Add the new sequence button to the toolbar.
		JButton newSequence = new JButton();
		newSequence.setToolTipText( "New Sequence" );
		newSequence.setActionCommand( newSequence.getToolTipText() );
		newSequence.setIcon( ImageGrabber.tryGetImageIcon( "NewSequence.gif" ) );
		newSequence.addActionListener(
			getJMenuBar().getMenu( 0 ).getItem( 0 ).getActionListeners()[0] );
		bar.add( newSequence );

		// Add the open sequence button to the toolbar.
		JButton openSequence = new JButton();
		openSequence.setToolTipText( "Open Sequence" );
		openSequence.setActionCommand( openSequence.getToolTipText() );
		openSequence.setIcon( ImageGrabber.tryGetImageIcon( "OpenSequence.gif" ) );
		openSequence.addActionListener(
			getJMenuBar().getMenu( 0 ).getItem( 1 ).getActionListeners()[0] );
		bar.add( openSequence );

		// Add a separator.
		bar.addSeparator();

		// Add the save sequence (as) button to the toolbar.
		// Note that this button initially doesn't have an action associated
		// with it, and is also disabled.
		// Only the SequenceDisplayWindow can activate or deactivate it.
		JButton saveSequence = new JButton();
		saveSequence.setToolTipText( "Save" );
		saveSequence.setName( "Save" );
		saveSequence.setActionCommand( "Save Sequence" );
		saveSequence.setIcon( ImageGrabber.tryGetImageIcon( "Save.gif" ) );
		saveSequence.setEnabled( false );
		saveSequence.setFocusable( false );
		bar.add( saveSequence );

		// Add a separator.
		bar.addSeparator();

		// Add the draw button to the toolbar.
		JButton draw = new JButton();
		draw.setToolTipText( "Draw" );
		draw.setActionCommand( draw.getToolTipText() );
		draw.setIcon( ImageGrabber.tryGetImageIcon( "Draw.gif" ) );
		draw.addActionListener(
			getJMenuBar().getMenu( 0 ).getItem( 5 ).getActionListeners()[0] );
		bar.add( draw );

		// Add the fold RNA single strand button to the toolbar.
		JButton fold = new JButton();
		fold.setToolTipText( "Fold RNA Single Strand" );
		fold.setActionCommand( fold.getToolTipText() );
		fold.setIcon( ImageGrabber.tryGetImageIcon( "FoldRNASingle.gif" ) );
		fold.addActionListener(
			getJMenuBar().getMenu( 1 ).getItem( 0 ).getActionListeners()[0] );
		bar.add( fold );

		// Add the RNA OligoWalk button to the toolbar.
		JButton oligo = new JButton();
		oligo.setToolTipText( "RNA OligoWalk" );
		oligo.setActionCommand( oligo.getToolTipText() );
		oligo.setIcon( ImageGrabber.tryGetImageIcon( "OligoWalk.gif" ) );
		oligo.addActionListener(
			getJMenuBar().getMenu( 1 ).getItem( 17 )
			.getActionListeners()[0] );
		bar.add( oligo );

		// Add the RNA Dynalign button to the toolbar.
		JButton dynalign = new JButton();
		dynalign.setToolTipText( "RNA Dynalign" );
		dynalign.setActionCommand( dynalign.getToolTipText() );
		dynalign.setIcon( ImageGrabber.tryGetImageIcon( "Dynalign.gif" ) );
		dynalign.addActionListener(
			getJMenuBar().getMenu( 1 ).getItem( 12 )
			.getActionListeners()[0] );
		bar.add( dynalign );
	}

	/**
	 * Get the top frame of the application.
	 *
	 * @return  The top frame.
	 */
	public static ApplicationRootFrame getFrame() {
		// RMW: previously this function would return the first Frame in the list of Windows.
		//  If any other dialog windows were open prior to the creation of the ApplicationRootFrame,
		//  this would result in a ClassCastException.  Better to return a frame only
		//  only if it is a true ApplicationRootFrame.
		for (java.awt.Frame f : ApplicationRootFrame.getFrames())
			if (f instanceof ApplicationRootFrame)
				return (ApplicationRootFrame)f;
		return null;
	}

	/**
	 * Get the most recent internal frame in the application.
	 *
	 * @return   The top frame, or null if no frame exists.
	 */
	public InternalWindow getMostRecentFrame() {
		JDesktopPane desk = (JDesktopPane)getContentPane().getComponent( 1 );
		return (InternalWindow)desk.getSelectedFrame();
	}

	/**
	 * Get the toolbar.
	 *
	 * @return   The toolbar.
	 */
	public JToolBar getToolBar() { return bar; }

	/**
	 * Reset the frame to its original state before any windows were opened.
	 */
	public void resetFrame() {
		setTitle( "RNAstructure" );
		setJMenuBar( new DynamicMenuBar( null ) );
		getJMenuBar().revalidate();
		getJMenuBar().repaint();
	}

	/**
	 * Reset the info bar at the bottom of the main window to its default.
	 */
	public void resetInfoLabel() {
		setInfoLabel( "For help, press F1." );
	}

	/**
	 * Set the info bar at the bottom of the main window to contain the
	 * specified text.
	 *
	 * @param text   The text to set on the info bar.
	 */
	public void setInfoLabel( String text ) {
		//RMW: The previous implementation caused bugs in several cases,
		//   related to fact that the index of the LJabel can change
		//   within the container upon certain user events, such as moving
		//   the toolbar etc
		//  BAD CODE: ((JLabel)getContentPane().getComponent( 2 )).setText(" " + text);
		infoLabel.setText(" " + text);
	}
}
