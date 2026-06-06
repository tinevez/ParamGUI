package org.scijava.ui.paramUI.visitors;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ui.paramUI.Configurator;
import org.scijava.ui.paramUI.ConfiguratorIterator;
import org.scijava.ui.paramUI.Parameters.Parameter;

/**
 * Echoes a {@link Configurator} to a String.
 */
public class Strings
{


	private static final String H = "─";

	private static final String V = "│";

	private static final String TL = "┌";

	private static final String TR = "┐";

	private static final String BL = "└";

	private static final String BR = "┘";

	private static final String LH = "├";

	private static final String RH = "┤";

	private static final String SEPARATOR = " " + V + " ";

	/**
	 * Pretty-print the configuration as a table.
	 * 
	 * @param config
	 *            the configuration to print.
	 * @return a string representation of the configuration.
	 */
	public static String echo( final Configurator config )
	{
		// Collect rows so we can compute column widths first
		final List< String[] > rows = new ArrayList<>();
		final List< String > groupHeaders = new ArrayList<>();

		final ConfiguratorIterator it = config.iterator();
		String currentGroupName = null;

		while ( it.hasNext() )
		{
			final Parameter< ?, ? > p = it.next();

			// If we just entered a group, insert a header marker
			if ( it.groupEntered() && it.getCurrentGroup() != null )
			{
				currentGroupName = it.getCurrentGroup().getName();
				groupHeaders.add( currentGroupName );
				// We'll add a virtual header row; actual printing later
			}

			final String name = p.getName();
			final Object value = p.getValue();
			final String valueStr = formatValue( value );

			// Store row with current group context (null means standalone)
			rows.add( new String[] { currentGroupName, name, valueStr } );

			// If we just exited the group, clear context
			if ( it.groupExited() )
			{
				currentGroupName = null;
			}
		}

		// Compute column width for the "name" column (with indentation when in a group)
		int nameWidth = 0;
		for ( final String[] row : rows )
		{
			final boolean inGroup = row[ 0 ] != null;
			final int len = ( inGroup ? 2 : 0 ) + ( row[ 1 ] != null ? row[ 1 ].length() : 0 );
			if ( len > nameWidth )
				nameWidth = len;
		}

		// Compute widest value
		int valueWidth = 0;
		for ( final String[] row : rows )
		{
			final String value = row[ 2 ] != null ? row[ 2 ] : "";
			if ( value.length() > valueWidth )
				valueWidth = value.length();
		}

		// Widest group header (ensure table is at least as wide as the longest
		// group label)
		int groupHeaderWidth = 0;
		final java.util.HashSet< String > groupsSeen = new java.util.HashSet<>( groupHeaders );
		for ( final String g : groupsSeen )
		{
			if ( g != null )
				groupHeaderWidth = Math.max( groupHeaderWidth, g.length() + 2 );
		}

		// Compute inner table width (without vertical borders)
		final String title = config.getName() != null ? config.getName() : "";
		final int tableWidth = Math.max(
				Math.max( title.length(), nameWidth + SEPARATOR.length() + valueWidth ),
				groupHeaderWidth );
		final int innerWidth = Math.max( 3, tableWidth );
		final String dashLine = H.repeat( innerWidth );

		// Build the table
		final StringBuilder out = new StringBuilder();
		out.append( TL ).append( dashLine ).append( TR + "\n" );
		out.append( V ).append( center( title, innerWidth ) ).append( V + "\n" );
		out.append( LH ).append( dashLine ).append( RH + "\n" );
		String printedGroup = null;
		for ( final String[] row : rows )
		{
			final String rowGroup = row[ 0 ];
			final boolean inGroup = rowGroup != null;

			// Print group header when we encounter the first row of that group
			if ( inGroup && ( printedGroup == null || !printedGroup.equals( rowGroup ) ) )
			{
				final String label = " " + rowGroup + " ";
				final int before = Math.max( 0, ( innerWidth - label.length() ) / 2 );
				final int after = Math.max( 0, innerWidth - label.length() - before );
				out
						.append( V )
						.append( H.repeat( before ) )
						.append( label )
						.append( H.repeat( after ) )
						.append( V ).append( "\n" );
				printedGroup = rowGroup;
			}

			// Leave group header area blank line after the last row is printed
			// by detecting group transitions above.
			// (Optional: you can add a separator when printedGroup changes to
			// null.)

			// Print row
			final String indent = inGroup ? "  " : "";
			final String name = row[ 1 ] != null ? row[ 1 ] : "";
			final String value = row[ 2 ] != null ? row[ 2 ] : "";

			// Left-pad to align arrows
			final int pad = nameWidth - ( indent.length() + name.length() );
			out.append( V );
			out.append( indent ).append( name );
			if ( pad > 0 )
				out.append( " ".repeat( pad ) );
			out.append( SEPARATOR ).append( value );
			// pad to right edge
			final int fill = innerWidth - ( indent.length() + name.length() + Math.max( 0, pad ) + SEPARATOR.length() + value.length() );
			if ( fill > 0 )
				out.append( " ".repeat( fill ) );
			out.append( V + "\n" );

			// If we just left a group (next row belongs to a different group or
			// no group), add a blank line
			// This keeps things airy without being verbose
		}

		out.append( BL ).append( dashLine ).append( BR );
		return out.toString();
	}

	private static String center( final String s, final int width )
	{
		final String text = s == null ? "" : s;
		final int len = text.length();
		if ( len >= width )
			return text;
		final int left = ( width - len ) / 2;
		final int right = width - len - left;
		return " ".repeat( left ) + text + " ".repeat( right );
	}

	private static String formatValue( final Object value )
	{
		if ( value == null )
			return "null";

		return String.valueOf( value );
	}
}
