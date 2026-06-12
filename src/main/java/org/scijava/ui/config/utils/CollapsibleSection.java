package org.scijava.ui.config.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class CollapsibleSection extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final ToggleTitleBorder titled;

	final JPanel body;

	private boolean expanded;

	public CollapsibleSection( final String title, final Font font, final boolean collapsed )
	{
		super( new BorderLayout() );
		this.expanded = !collapsed;
		this.titled = new ToggleTitleBorder( title, font, expanded );
		setBorder( BorderFactory.createCompoundBorder(
				titled,
				BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) ) );

		// Body
		body = new JPanel( new GridBagLayout() );
		body.setVisible( expanded );
		add( body, BorderLayout.CENTER );

		// Click on the arrow to toggle
		addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				if ( titled.isInToggle( e.getX(), e.getY(), CollapsibleSection.this ) )
					setExpanded( !expanded );
			}
		} );

		// Hand cursor when hovering the arrow
		addMouseMotionListener( new MouseMotionAdapter()
		{
			@Override
			public void mouseMoved( final MouseEvent e )
			{
				setCursor( titled.isInToggle( e.getX(), e.getY(), CollapsibleSection.this )
						? Cursor.getPredefinedCursor( Cursor.HAND_CURSOR )
						: Cursor.getDefaultCursor() );
			}
		} );
	}

	/**
	 * Exposes the body panel to allow adding content to the collapsible
	 * section.
	 * 
	 * @return the body panel.
	 */
	public JPanel getBody()
	{
		return body;
	}

	private void setExpanded( final boolean show )
	{
		if ( this.expanded == show )
			return;
		this.expanded = show;
		titled.setExpanded( show );
		body.setVisible( show );
		revalidate();
		repaint();

		final Window w = SwingUtilities.getWindowAncestor( this );
		if ( w instanceof Frame )
			w.pack();
	}

	@Override
	public Dimension getPreferredSize()
	{
		// Base (current LAF/layout calculation)
		final Dimension base = super.getPreferredSize();
		// Always include the body’s preferred width, even if it's invisible
		final Dimension bodyPref = body.getPreferredSize();

		// Total insets (titled border + inner padding)
		final Insets ins = getInsets();

		// Ensure width is large enough to fit the (expanded) content
		int prefW = Math.max( base.width, bodyPref.width + ins.left + ins.right );

		// Optional: also consider title text width so it doesn't get clipped
		int titleW = 0;
		final String t = titled.getTitle();
		final Font tf = titled.getTitleFont() != null ? titled.getTitleFont() : getFont();
		if ( t != null && tf != null )
		{
			final FontMetrics fm = getFontMetrics( tf );
			if ( fm != null )
				titleW = fm.stringWidth( t ) + 12; // small padding
		}
		prefW = Math.max( prefW, titleW + ins.left + ins.right );

		// Height: expanded -> body height + insets; collapsed -> only title
		// band height + bottom inset
		final int titleBand = Math.max( 0, titled.getBorderInsets( this ).top );
		final int prefH = ( body.isVisible() ? bodyPref.height + ins.top + ins.bottom
				: Math.max( base.height, titleBand + ins.bottom ) );

		return new Dimension( prefW, prefH );
	}

	/**
	 * Custom border with an invisible toggle button that is used to
	 * collapse/expand the section when clicked.
	 */
	private static class ToggleTitleBorder extends TitledBorder
	{
		private static final long serialVersionUID = 1L;

		private static final int ICON_W = 10;

		private static final int ICON_H = 10;

		// left margin before arrow
		private static final int LEFT_PAD = 6;

		// gap between arrow and title
		private static final int GAP_AFTER = 6;

		private final String baseTitle;

		private boolean expanded;

		private final Rectangle toggleBounds = new Rectangle();

		ToggleTitleBorder( final String title, final Font font, final boolean expanded )
		{
			super( BorderFactory.createLineBorder( Color.LIGHT_GRAY, 1, true ),
					title, TitledBorder.LEFT, TitledBorder.TOP, font, null );
			this.baseTitle = title;
			this.expanded = expanded;
			updateTitle();
		}

		void setExpanded( final boolean expanded )
		{
			if ( this.expanded != expanded )
			{
				this.expanded = expanded;
				updateTitle();
			}
		}

		boolean isInToggle( final int x, final int y, final JComponent c )
		{
			return toggleBounds.contains( x, y );
		}

		private void updateTitle()
		{
			setTitle( ( expanded ? "▼ " : "▶ " ) + baseTitle );
		}

		@Override
		public void paintBorder( final Component c, final Graphics g, final int x, final int y, final int width, final int height )
		{
			// Let the default titled border draw the line and the title
			super.paintBorder( c, g, x, y, width, height );

			final Insets ins = getBorderInsets( c );
			// Title band height
			final int band = Math.max( 0, ins.top );

			// Compute arrow position on the title band
			final int ax = x + LEFT_PAD;
			final int ay = y + Math.max( 0, ( band - ICON_H ) / 2 );

			// Remember clickable bounds for hit-testing
			toggleBounds.setBounds( ax, ay, ICON_W, ICON_H );
		}

		@Override
		public Insets getBorderInsets( final Component c, final Insets insets )
		{
			// Start from default insets and then add room on the left for
			// the arrow + gap
			final Insets i = super.getBorderInsets( c, insets );
			i.left += LEFT_PAD + ICON_W + GAP_AFTER;
			return i;
		}

		@Override
		public Insets getBorderInsets( final Component c )
		{
			final Insets i = super.getBorderInsets( c );
			i.left += LEFT_PAD + ICON_W + GAP_AFTER;
			return i;
		}
	}

	@Override
	public Dimension getMinimumSize()
	{
		// Prevent pack() from squeezing narrower than the expanded content
		final Dimension pref = getPreferredSize();
		return new Dimension( pref.width, Math.min( pref.height, 1 + titled.getBorderInsets( this ).top ) );
	}
}
