/*
 * (c) 2011 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program.
 */

package ur_rna.RNAstructureUI.windows;

import ur_rna.RNAstructureUI.ApplicationRootFrame;
import ur_rna.RNAstructureUI.menus.FileMenu;
import ur_rna.RNAstructureUI.menus.RolloverMenu;
import ur_rna.RNAstructureUI.utilities.*;
import ur_rna.Utilities.AppLog;
import ur_rna.Utilities.ObjTools;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * A class responsible for displaying a window that allows a user to examine
 * raw sequence data.
 * <br><br>
 *
 * @author Jessica S. Reuter
 * @author Richard M. Watson
 *
 */
public class SequenceDisplayWindow
	extends InternalWindow {
	private static final long serialVersionUID = 20120802;
    private final static String allowedSequenceChars = "ACGTUXN acgtux \r\n\t - 1";
    private final static String unpairedSequenceChars = "acgtux";
    private final static String specialSequenceChars = "1XxN";

    private Font defaultFont = new Font("Monospaced", 0, 12);
	/**
	 * The array of text areas in this window.
	 */
	private JTextArea titleBox;
    private JTextArea commentBox;
    private JTextPane sequenceBox;
    private StyledDocument sequenceDoc;

	/**
	 * A boolean, true if the sequence has been edited, false if not.
	 */
	private boolean edited = false;
    private boolean userAcceptedInvalidSequence = false;

	/**
	 * The position at which speaking is occurring.
	 */
	private int speakPosition = 0;

	/**
	 * The button which can be clicked to start or stop speaking.
	 */
	private JButton speakButton;

	/**
	 * A boolean, true if speaking while typing is active, false if not.
	 */
	private boolean speakWhileTyping = false;
    private boolean speakingFullSequence = false;

	/**
	 * The timer that aids in speaking nucleotides in rhythm.
	 */
	private Timer speakTimer;

    /**
     * The timer that updates syntax highlighting
     */
    private Timer styleTimer;

	/**
	 * Default Constructor.
	 * <br><br>
	 * This constructor is used when a blank display window is opened.
	 */
	public SequenceDisplayWindow() {
		// Set the title of this window and the main frame to reflect the
		// creation of a new sequence. Also, enable the menus and stop the
		// frame from closing automatically if the close button is clicked.
		setTitles( "New Sequence" );
		menuBar.enableMenus();
		setDefaultCloseOperation( SequenceDisplayWindow.DO_NOTHING_ON_CLOSE );

		// Insert the sequence display specific menu items in the file menu.
		FileMenu fileMenu = (FileMenu)menuBar.getMenu( 0 );
		fileMenu.insertMenuItem("Save Sequence",
                "Save a sequence with its existing name.", this, 3);
		fileMenu.insertMenuItem("Save Sequence As...",
                "Save a sequence with a new name.", this, 4);
		fileMenu.insertSeparator(4);

		// Create the input regions.

        titleBox = new JTextArea();
		JPanel titlePane = buildTextBoxPanel(40, "Title:", titleBox, "titleBox");
        commentBox = new JTextArea();
		JPanel commentPane = buildTextBoxPanel(50, "Comment:", commentBox, "commentBox");
        sequenceDoc = CreateStyledDocument();
        sequenceBox = new JTextPane(sequenceDoc);
		JPanel sequencePane = buildTextBoxPanel(200,
                "Sequence: (Nucleotides in lower case are forced single " +
                        "stranded in structure predictions.)", sequenceBox, "sequenceBox");

		// Create the entire layout in a box and add the box to the window.
		Box box = Box.createVerticalBox();
		box.add( titlePane );
		box.add( commentPane );
		box.add( sequencePane );
		box.add(buildButtonPanel());
		add(box);

        setResizable(true);

		// Add a key listener to the sequence data input region so it restricts
		// nucleotides, and can read nucleotides out loud, if desired.
		sequenceBox.addKeyListener(new KeyAdapter() {
            char[] speakChars;

            {
                speakChars = new char[]{'A', 'C', 'G', 'T', 'U', 'X'};
                Arrays.sort(speakChars);
            }

            /**
             * Check to make sure a key is valid to be in the sequence region.
             *
             * @param e   The key event.
             */
            public void checkKey(KeyEvent e) {
                // Get the next character.
                char c = e.getKeyChar();
                int mod = e.getModifiers();

                // If speaking while typing is turned on, and the character is
                // a valid nucleotide, read it out.
                if (speakWhileTyping && ! speakingFullSequence) {
                    // Check to see if the character is a valid nucleotide.
                    int found = Arrays.binarySearch(speakChars, Character.toUpperCase(c));
                    if (found != -1)
                        playClip(c);
                }
//				// If the character is not valid, don't output it.
//				if( !( okSpace || okNuc || okMove ) ) { e.consume(); }
            }

            //@Override
            //public void keyPressed(KeyEvent e) { checkKey(e); }

            @Override
            public void keyTyped(KeyEvent e) { checkKey(e); }
        });

		// Add an internal frame listener that adds the following specialized
		// capabilities to this window:
		// 1. When the frame is activated, activate the save button.
		// 2. As the frame is closing, check if the sequence has been edited.
		// 3. When the frame is deactivated, deactivate the save button.
		final SequenceDisplayWindow saveActionHandler = this;
		final JButton save =
			(JButton)ApplicationRootFrame.getFrame().getToolBar()
			.getComponent( 3 );
		addInternalFrameListener( new InternalFrameAdapter() {

			@Override
			public void internalFrameActivated( InternalFrameEvent e ) {
				save.addActionListener( saveActionHandler );
				save.setEnabled( true );
			}

			@Override
			public void internalFrameClosing( InternalFrameEvent e ) {
				// If the window hasn't been edited, just dispose of the frame.
				if( !edited ) {
					e.getInternalFrame().dispose();
					return;
				}

				// Ask the user if they want to save the edited sequence.
				String msg =
					"The sequence has been modified.<br>Save Changes?";
				String response = dialogHandler.makeThreeChoicesDialog( msg );

				// If the sequence should be saved, save it.
				if( response.equals( "YES" ) ) {
					String file = saveSequence();
					if( ObjTools.isEmpty(file) ) { e.getInternalFrame().dispose(); }
				}

				// If the sequence shouldn't be saved, close the window.
				// Note that receiving a response of "CANCEL" does not close
				// the window; it only closes the dialog box opened above, as
				// no other action is attached to it.
				else if( response.equals( "NO" ) ) {
					e.getInternalFrame().dispose();
				}
			}

			@Override
			public void internalFrameDeactivated( InternalFrameEvent e ) {
				save.removeActionListener( saveActionHandler );
				save.setEnabled( false );
			}
		});

		// Initialize the speaking timer.
		speakTimer = new Timer( 100, this );
		speakTimer.setActionCommand( "SpeakTimer" );

        styleTimer = new Timer (250, this );
        styleTimer.setActionCommand( "UpdateStyles" );
	}

	/**
	 * File Constructor.
	 * <br><br>
	 * This constructor is used when a sequence file is specified.
	 *
	 * @param file   The sequence file to open.
	 */
	public SequenceDisplayWindow( String file ) {

		// Call the blank window constructor to build the window before adding
		// the sequence.
		this();
		// Set the title of this window and the main frame to reflect the name
		// of the data file.
		setTitles( file );

		// Read in the data for the sequence, then set it in the window.
        if (!new File(file).exists()) {
            JOptionPane.showMessageDialog(null, "The specified file does not exist: \n" + file);
        } else {
            userAcceptedInvalidSequence = false;
            backend.readSequenceData( file );
            titleBox.setText(backend.getSequenceTitle().trim());
            commentBox.setText(backend.getSequenceComment().trim());
            // Format the sequence and set it as unedited.
            setSequenceText(backend.getSequenceData().trim());
            markEdited(false);
        }
	}

    private void setSequenceText(final String text) {
        sequenceDoc.setCharacterAttributes(0,sequenceDoc.getLength(), defaultStyle, true);
        sequenceBox.setText(text);
        sequenceDoc.setCharacterAttributes(0,sequenceDoc.getLength(), defaultStyle, true);
    }


    void markEdited(final boolean edited) {
        this.edited = edited;
        //System.out.println("Begin Marked Clean");
    }

    /**
     * View the window.
     */
    @Override
    public void viewWindow() {
        super.viewWindow();
        setSize(680, 480);
    }
    /**
	 * Make the button panel at the bottom of the window.
	 * @return   The button panel.
	 */
	private JPanel buildButtonPanel() {
		// Create the button names array.
		String[] names = {
			"Format Sequence",
			//"Speak Sequence",
			"Fold as DNA",
			"Fold as RNA", "Save Sequence",
		};

		// Create the button panel, and save the second button as the speaking
		// control button.
		JPanel buttonPanel = new JPanel( new GridLayout( 1, 0 ) );
		for( int i = 0; i < names.length; i++ ) {
			JButton button = new JButton( names[i] );
			button.addActionListener( this );
			buttonPanel.add( button );
			//if( i == 2 ) { speakButton = button; }
		}

		// Return the panel.
		return buttonPanel;
	}

	/**
	 * Build a text input region for this window. Each region is a panel
	 * consisting of a label and a text component.
	 *
	 * @param height   The height of the input region.
	 * @param text     The label text.
	 * @return         The input region panel.
	 */
	private JPanel buildTextBoxPanel(final int height, final String text, final JTextComponent textBox, final String name) {

		// Create the input panel and add its description label.
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add( new JLabel( text ), BorderLayout.NORTH );
        textBox.setName(name);
        panel.setName(name + "Panel");

		// Create the text area for this input region and set its defaults.
        textBox.setFont(defaultFont);
        textBox.setBackground(Color.WHITE);
        //region.setWrapStyleWord( true );

		// Add a document listener to the text area so any change to the text
		// component sets the status as edited.
        textBox.getDocument().addDocumentListener( new DocumentListener() {
			public void changedUpdate( DocumentEvent e ) { docUpdate(e); }
			public void insertUpdate( DocumentEvent e ) { docUpdate(e); }
			public void removeUpdate( DocumentEvent e ) { docUpdate(e); }

            public void docUpdate(DocumentEvent e) {
                styleTimer.restart();
//                if (e.getType() != DocumentEvent.EventType.REMOVE)
//                    UpdateSyntaxStyles(e.getOffset(), e.getLength());
                //if (e.getType() != DocumentEvent.EventType.CHANGE ) // a change probably corresponds to style. insert/remove are the only ones that are important to us. Changing a character is actually a remove, followed by an insert.
                edited = true;
                //System.out.println("DocumentEvent: " + e.getType().toString() + " Box: " + textBox.getName());
            }
		});

		// Add the text area to a scroll pane and set its size, then add the
		// scroll pane to the panel and the text area to the array.
		ScrollerPane pane = new ScrollerPane( textBox, 100, height );
		panel.add( pane );

        BorderBuilder borders = new BorderBuilder();
        borders.makeEqualBorder(5, panel);

		// Return the panel.
		return panel;
	}

    @Override
    protected void doActions(String command) {

        // If the command comes from one of the "Fold" buttons, pop up a new
        // single strand folding window with the input already put in.
        if (command.startsWith("Fold")) {

            // Attempt to save the sequence, if edited, and if it wasn't saved,
            // return out of the possible folding.
            String file = getTitle();
            if (edited) {
                file = saveSequence();
                if (file.equals("")) { return; }
            }

            // Check nucleic acid type, then show a window and set its data.
            dispose();
            String acid = (command.endsWith("RNA")) ? "RNA" : "DNA";
            FoldSingleWindow window = new FoldSingleWindow(acid);
            window.viewWindow();
            window.setDataAutomatically(file);
        }

        // If the command comes from the "Format Sequence" button, format the
        // sequence into blocks so it's easier to read.
        else if (command.equals("Format Sequence")) { formatSequenceBox(); }

//		// If the command comes from the "Speak Sequence" button, begin
//		// speaking the sequence.
//		else if( command.equals( "Speak Sequence" ) ) {
//			position = 0;
//			speakingFullSequence = true;
//			timer.start();
//			speakButton.setText("Stop Speaking");
//		}

        // If the command is to save a sequence, save it either under its
        // existing name or under a new name.
        else if (command.startsWith("Save")) {
            boolean saveWithSameName =
                    command.equals("Save Sequence") &&
                            !(getTitle().equals("New Sequence"));
            if (saveWithSameName) { saveSequence(getTitle()); } else { saveSequence(); }
        }

//		// If the command comes from the "Stop Reading" button, stop
//		// speaking the sequence.
//		else if( command.equals( "Stop Speaking" ) ) {
//
//            speakingFullSequence = false;
//			timer.stop();
//			speakButton.setText("Speak Sequence");
//		}

        // If the command comes from the timer, read a single nucleotide.
        else if (command.equals("SpeakTimer")) {
            if (_speakPlayer.isPlaying()) return;
            try {
                // If the caret position is still within the length of the
                // sequence, highlight and read the next nucleotide.
                JEditorPane area = sequenceBox;
                if (speakPosition != area.getText().length()) {
                    // If the window was closed, stop the timer.
                    if (!isVisible()) { speakTimer.stop(); }
                    // Select the next nucleotide and play it if possible.
                    area.getCaret().setSelectionVisible(false);
                    area.select(speakPosition, speakPosition + 1);
                    Character base =
                            area.getSelectedText().toUpperCase().charAt(0);
                    if (base != ' ') {
                        area.getCaret().setSelectionVisible(true);
                        playClip(base);
                    }
                    speakPosition++;
                } else {
                    // Otherwise if end has been reached, terminate the read.
                    speakFullSequence(false);
                }
            }

            // If a problem happened attempting to play a clip, show a
            // stack trace because the error won't be useful for the user.
            catch (Exception ex) { ex.printStackTrace(); }
        }
        else if (command.equals("UpdateStyles")) {
            //styleTimer.stop();
            UpdateSyntaxStyles();
        }
    }

    private Style defaultStyle, errorStyle, unpairedStyle, specialStyle;

    private StyledDocument CreateStyledDocument() {
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);
        defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        defaultStyle.addAttribute(StyleConstants.FontFamily, defaultFont.getFamily());
        defaultStyle.addAttribute(StyleConstants.FontSize, defaultFont.getSize());

        //defaultStyle.addAttribute(StyleConstants.Bold, true);
        // Create and add the style
        errorStyle = sc.addStyle("Error", defaultStyle);
        errorStyle.addAttribute(StyleConstants.Foreground, Color.red);
        //errorStyle.addAttribute(StyleConstants.StrikeThrough, true);
        //errorStyle.addAttribute(StyleConstants.Underline, true);
        unpairedStyle = sc.addStyle("Unpaired", defaultStyle);
        unpairedStyle.addAttribute(StyleConstants.Foreground, Color.blue);
        //unpairedStyle.addAttribute(StyleConstants.Underline, true);
        defaultStyle.addAttribute(StyleConstants.Bold, true);

        specialStyle = sc.addStyle("Special", defaultStyle);
        specialStyle.addAttribute(StyleConstants.Bold, true);
        specialStyle.addAttribute(StyleConstants.Italic, true);
        specialStyle.addAttribute(StyleConstants.Foreground, new Color(0,0xAA,0x33));

        return doc;
    }

    private void UpdateSyntaxStyles() {
        UpdateSyntaxStyles(0, sequenceDoc.getLength());
    }
    private void UpdateSyntaxStyles(int offset, int length) {
        final boolean wasEdited = edited;
        try {
            length = Math.min(sequenceDoc.getLength(), length);
            offset = Math.max(offset, 0);
            char[] text = sequenceDoc.getText(offset, length).toCharArray();
            for (int i = 0; i < length; i++)
                setCharStyle(offset+i,getCharStyle(text[i]));
        } catch (Exception ex) {
            AppLog.getDefault().error("Error updating styles in sequence file.", ex);
        }
        if (!wasEdited) markEdited(false);
    }

    private void setCharStyle(int offset, Style s) {
        if (!s.getName().equals(sequenceDoc.getCharacterElement(offset).getAttributes().getAttribute(StyleConstants.NameAttribute)))
            sequenceDoc.setCharacterAttributes(offset, 1, s, true);
    }

    private Style getCharStyle(char c) {
        if (!isValidSeqChar(c))
            return errorStyle;
        else if (isUnpairedSeqChar(c))
            return unpairedStyle;
        else if (isSpecialSeqChar(c))
            return specialStyle;
        return defaultStyle;
    }

    private boolean isValidSeqChar(char c) {
        return Character.isWhitespace(c) ||
                allowedSequenceChars.indexOf(c) != -1;
    }
    private boolean isUnpairedSeqChar(char c) {
        return unpairedSequenceChars.indexOf(c) != -1;
    }
    private boolean isSpecialSeqChar(char c) {
        return specialSequenceChars.indexOf(c) != -1;
    }

    private void speakFullSequence(final boolean speak) {
        speakPosition = 0;
        speakingFullSequence = speak;
        speakMenu.speakSequence.setText(speak ? "Stop Speaking" : "Speak Sequence");
        if (speak)
            speakTimer.start();
        else
            speakTimer.stop();
    }

    private boolean verifySequence() {
        if (userAcceptedInvalidSequence) return true;
        char[] text = sequenceBox.getText().toCharArray();
        Set<String> invalidChars = null;
        for(int i = 0; i < text.length; i++) {
            char c = text[i];
            if (!isValidSeqChar(c)) {
                if (invalidChars == null) {
                    invalidChars = new HashSet<>();
                    sequenceBox.select(i, i + 1); //select first invalid character
                }
                invalidChars.add(ObjTools.escapeStringLiteral(Character.toString(c)));
            }
        }
        if (invalidChars != null && invalidChars.size() > 0) {
            String prompt = "The sequence contains one or more invalid characters (" + String.join(", ", invalidChars) + ").\nSaving the file with invalid characters could result in unexpected behavior when the file is processed by this or another program.";
            int result = JOptionPane.showOptionDialog(this, prompt + "\n\nDo you want to save the file anyway?" , "Invalid Character(s) in Sequence", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
            sequenceBox.grabFocus();
            if (result != JOptionPane.YES_OPTION) return false;
            userAcceptedInvalidSequence = true;
        }
        return true;
    }

    private void formatSequenceBox() {
        // If speaking while typing is on right now, turn it off.
//		boolean prev_speaking = speakWhileTyping;
//		if(prev_speaking) {
//            speakWhileTyping = false;
//            _speakPlayer.pause(true);
//        }
        // Check if the sequence has already been edited.
        final boolean wasEdited = edited;
        sequenceBox.setEnabled(false);
        setSequenceText(formatSequence(sequenceBox.getText()));
        //area.setCaretPosition( 0 );

        // If speaking while typing was turned off before formatting the
        // sequence, turn it back on.
//		if(prev_speaking) {
//            speakWhileTyping = true;
//            _speakPlayer.pause(false);
//        }
        sequenceBox.setEnabled(true);
        // Set the edit state of the sequence to what it was before the
        // sequence was formatted. (That is, don't count formatting as a
        // sequence edit.)
        markEdited(edited);
    }

	/**
	 * Format the sequence in the text area.
	 */
	private String formatSequence(String text) {
        //text = text.replaceAll("[\\s]+", "");
		//int length = text.length();
		// Put blocks of the sequence 10 nucleotides long, separated by a
		// space, into the area, with a new line between every 5 blocks. If an
		// error occurs during this process, show it.
		try {
			// Create a list of lines 50 nucleotides long.
            int pos = 0, group = 0;
            final int MAX_POS = 10;
            final int MAX_GROUP = 5;
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (Character.isWhitespace(c))
                    continue;
                if (pos == MAX_POS) {
                    pos = 0;
                    group++;
                    if (group == MAX_GROUP) {
                        sb.append(" \n");
                        group=0;
                    } else
                        sb.append(' ');
                }
                sb.append(c);
                pos++;
            }
            return sb.toString();
		} catch( Exception e ) {
			dialogHandler.makeErrorDialog( "error formatting sequence." );
		}
        return text;
	}

    private class SoundPlayer {
        private javax.sound.sampled.Clip _line;
        private InputStream _currentClip;
        private final Deque<InputStream> _queue = new ArrayDeque<>();
        private boolean _paused;
        private boolean _playing;
        public void play(InputStream input) {
            synchronized (_queue) {
                _queue.addFirst(input);
                playNext();
            }
        }
        private Clip createLine(final AudioFormat format) {
            try {
                // If a problem happened attempting to play a clip, show a stack
                // trace because the error won't be useful for the user.
                // Get the audio information for the clip.
                Clip line = (Clip)AudioSystem.getLine(new DataLine.Info(Clip.class, format));
                line.addLineListener(new LineListener() {
                    @Override
                    public void update(final LineEvent event) {
                        if (event.getType() == LineEvent.Type.STOP) {
                            //System.out.println("STOP Signal");
                            try {
                                _line.close();
                                _currentClip.reset();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            playNext();
                        }
                    }
                });
                return line;
            } catch (Exception e) {
                String message = "error speaking sequence.";
                new SimpleDialogHandler().makeErrorDialog(message);
                return null;
            }
        }

        public void pause(boolean pause) {
            if (_paused == pause) return;
            _paused = pause;
            if (_line == null) return;
            if (_paused)
                _line.stop();
            else
                _line.start();
        }

        private void playNext() {
            if (_paused) return;
//            if (_line == null) {
//                System.out.println("isNULL");
//            } else {
//                System.out.println("isRunning: " + _line.isRunning());
//                System.out.println("isOpen: " + _line.isOpen());
//                System.out.println("isActive: " + _line.isActive());
//            }
            if (_line != null && _line.isRunning()) return;

            InputStream input;
            synchronized (_queue) {
                input = _queue.pollLast();
            }
            if (input == null) {
                //System.out.println("end of queue");
                _playing = false;
            } else {
                try {
                    //System.out.println("marking " + input.available());
                    input.mark(input.available());
                    _currentClip = input;
                    AudioInputStream audio = AudioSystem.getAudioInputStream(input);
                    AudioFormat format = audio.getFormat(); //AudioSystem.getAudioFileFormat(input).getFormat();
                    if (_line == null) {
                        _line = createLine(format);
                        //System.out.println("creating");
                        if (_line == null) return; //failed
                    }
//                    } else if (!_line.getFormat().matches(format)) {
//                        System.out.println("closing due to format");
//                        _line.close();
//                    }

                    //if (!_line.isOpen()) {
                    //System.out.println("opening " + input.available());
                    _line.open(audio);
                    //}
                    //input.mark(input.available());
                    //int len;
                    //System.out.println("starting");
                    _playing = true;
                    _line.start();
//                        System.out.println("start writing");
//                        long t = System.currentTimeMillis();
//                        while(0 < (len = input.read(buffer))) {
//                            _line.write(buffer, 0, len);
//                        }
//                        System.out.println("done writing " + (System.currentTimeMillis() - t));
                    input.reset();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        public boolean isPlaying() {
            return _playing && !_paused;
        }
    }

    private SoundPlayer _speakPlayer = new SoundPlayer();
    private HashMap<Character, InputStream> _speakStreams = new HashMap<>();

	/**
	 * Play a sound clip, depending on the nucleotide.
	 *
	 * @param base   The nucleotide to play.
	 */
	private void playClip( char base ) {
        InputStream input = _speakStreams.get(base);
        if (input == null) {
            // The Stream is null, which could either mean the character isn't yet in the Map or that there is no Stream for this base.
            if (!_speakStreams.containsKey(base)) {
                // Get the file name of the clip to play.
                InputStream res = Resources.tryGet("sounds/" + Character.toUpperCase(base) + ".wav");
                if (res != null) {
                    try {
                        int pos = 0, len;
                        byte[] buffer = new byte[res.available()];
                        while (0 < (len = res.read(buffer, pos, res.available())))
                            pos += len;
                        input = new ByteArrayInputStream(buffer);
                    } catch (Exception ex) {
                        AppLog.getDefault().error("Failed to load audio file.", ex);
                    }
                }
            }
            _speakStreams.put(base, input); //input could still be null here if there is no sound for this base or if it cannot be loaded. In either case, add it to the Map so we don't attempt to get it from Resources again.
            if (input == null) return;
        }
        _speakPlayer.play(input);
	}

	/**
	 * Save a sequence.
	 *
	 * @return   The file that was saved.
	 */
	public String saveSequence() {
		String file = StandardFileChooser.getSaveName(FileFilters.Sequence, getTitle());
		if( file == null ) { return ""; }
		return saveSequence( file );
	}

	/**
	 * Save a sequence.
	 *
	 * @param file   The file name to save the sequence with.
	 * @return       The file that was saved.
	 */
	public String saveSequence( String file ) {
		// Format the sequence in the area. 		//formatSequence();
        if (!verifySequence()) return "";

        // Set the data in the back end for writing
        backend.setSequenceTitle( titleBox.getText().trim() );
        backend.setSequenceComment( commentBox.getText().trim() );
        backend.setSequenceData( sequenceBox.getText().trim() );

		// Write the sequence file.
		if( file.toLowerCase().endsWith( ".fasta" ) ) {
			backend.writeFastaFile( file );
		} else {
			backend.writeSequenceFile( file );
		}

		// Set the titles of the window, set the state to be unedited, and
		// return the file.
		setTitles( file);
        markEdited(false);
        return file;
	}

    SpeakMenu speakMenu;

	@Override
	protected RolloverMenu[] setMenus() {
        speakMenu  = new SpeakMenu(this);
		return new RolloverMenu[]{
			new EditMenu(), speakMenu
		};
	}

	/**
	 * Set whether or not the window should be speaking while typing.
	 *
	 * @param isSpeaking   True if speaking, false if not.
	 */
	public void setSpeakWhileTyping(boolean isSpeaking) { speakWhileTyping = isSpeaking; }

	/**
	 * An inner class that creates a sequence editing menu.
	 *
	 * @author Jessica S. Reuter
	 */
	private class EditMenu
		extends RolloverMenu {
		private static final long serialVersionUID = 20120802;

		/**
		 * Constructor.
		 */
		public EditMenu() {
			super( "Edit" );
			addMenuItem(
				"Cut", "Cut a block of text to the clipboard.", 'X' );
			addMenuItem(
				"Copy", "Copy a block of text to the clipboard.", 'C' );
			addMenuItem(
				"Paste", "Paste a block of text from the clipboard.", 'V' );
		}

		@Override
		protected void doMenuActions( String command ) {
			Action action =
				( command.equals( "Cut" ) ) ?
					new DefaultEditorKit.CutAction() :
				( command.equals( "Copy" ) ) ?
					new DefaultEditorKit.CopyAction() :
				new DefaultEditorKit.PasteAction();
			action.actionPerformed( null );
		}
	}

	/**
	 * An inner class that creates a sequence speaking menu.
	 *
	 * @author Jessica S. Reuter
	 */
	private class SpeakMenu
		extends RolloverMenu {
		private static final long serialVersionUID = 20120802;
        public final SequenceDisplayWindow parent;
        public final JCheckBoxMenuItem speakWhileTyping;
        public final JMenuItem speakSequence;
		/**
		 * Constructor.
		 */
		public SpeakMenu(SequenceDisplayWindow parent) {
			super( "Speak" );
            this.parent = parent;
            speakWhileTyping = addCheckBoxMenuItem("Speak While Typing",
                    "Read a sequence out loud as it is typed into the " +
                            "keyboard.");
            speakSequence = addMenuItem("Speak Sequence", "Read the entire sequence out loud.");
		}

		@Override
		protected void doMenuActions( String command ) {
            parent.setSpeakWhileTyping(speakWhileTyping.isSelected());

            if (command.equals("Speak Sequence")) {
                boolean speaking = !parent.speakingFullSequence;
                parent.speakFullSequence(speaking);
            }
        }
	}
}
