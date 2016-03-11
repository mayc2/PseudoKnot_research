/*
 * (c) 2011 Mathews Lab, University of Rochester Medical Center.
 *
 * This software is part of a group specifically designed for the RNAstructure
 * secondary structure prediction program and its related applications.
 */

package ur_rna.RNAstructureUI.utilities;

import javax.swing.*;
import java.awt.*;

/**
 * A class that creates a text field which holds a file name.
 *
 * @author Jessica S. Reuter
 */
public class FileField
	extends JTextField {
	private static final long serialVersionUID = 20120802;

	/**
	 * Constructor.
	 * 
	 * @param text      The name of the field.
	 * @param enabled   Whether the field is enabled.
	 */
	private FileField( String text, boolean enabled ) {
		setName( text );
		//System.out.println("FileField Role: " + this.getAccessibleContext().getAccessibleRole().toDisplayString());
		//System.out.println("FileField RoleClass: " + this.getAccessibleContext().getAccessibleRole().getClass().getTypeName());
		//System.out.println("FileField Name: " + this.getAccessibleContext().getAccessibleName());
		//System.out.println("FileField Desc: " + this.getAccessibleContext().getAccessibleDescription());
		this.getAccessibleContext().setAccessibleName("Input " + text);
		this.getAccessibleContext().setAccessibleDescription("text");
		//System.out.println("FileField Name: " + this.getAccessibleContext().getAccessibleName());
		//System.out.println("FileField Desc: " + this.getAccessibleContext().getAccessibleDescription());
		//System.out.println("FileField role-after: " + this.getAccessibleContext().getAccessibleRole().toDisplayString());

		if( !enabled ) {
			setEditable( false );
			setBackground( Color.WHITE );
		}
	}

	/**
	 * Create a disabled file input field.
	 *
	 * @return   The field.
	 */
	public static FileField createDisabled( String text ) {
		return new FileField( text, false );
	}

	/**
	 * Create an enabled file input field.
	 *
	 * @return   The field.
	 */
	public static FileField createEnabled( String text ) {
		return new FileField( text, true );
	}

	/**
	 * Get the file name in the field.
	 *
	 * @return   The file name.
	 */
	public String getFile() { return getText().trim(); }
}
