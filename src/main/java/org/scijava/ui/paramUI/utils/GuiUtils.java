/*-
 * #%L
 * TrackMate: your buddy for everyday tracking.
 * %%
 * Copyright (C) 2010 - 2026 TrackMate developers.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package org.scijava.ui.paramUI.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GuiUtils
{

	private static final FocusListener selectAllFocusListener = new FocusListener()
	{

		@Override
		public void focusLost( final FocusEvent e )
		{}

		@Override
		public void focusGained( final FocusEvent fe )
		{
			if ( !( fe.getSource() instanceof JTextField ) )
				return;
			final JTextField txt = ( JTextField ) fe.getSource();
			SwingUtilities.invokeLater( () -> txt.selectAll() );
		}
	};

	public static final void selectAllOnFocus( final JTextField tf )
	{
		tf.addFocusListener( selectAllFocusListener );
	}

	public static final void setFont( final JComponent panel, final Font font )
	{
		for ( final Component c : panel.getComponents() )
			c.setFont( font );
	}

	/**
	 * Positions a JFrame more or less cleverly next a {@link Component}.
	 *
	 * @param gui
	 *            the window to position.
	 * @param component
	 *            the component to position next to.
	 */
	public static void positionWindow( final Window gui, final Component component )
	{

		if ( null != component )
		{
			// Get total size of all screens
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final GraphicsDevice[] gs = ge.getScreenDevices();
			int screenWidth = 0;
			for ( int i = 0; i < gs.length; i++ )
			{
				final DisplayMode dm = gs[ i ].getDisplayMode();
				screenWidth += dm.getWidth();
			}

			final Point windowLoc = component.getLocation();
			final Dimension windowSize = component.getSize();
			final Dimension guiSize = gui.getSize();
			if ( guiSize.width > windowLoc.x )
			{
				if ( guiSize.width > screenWidth - ( windowLoc.x + windowSize.width ) )
				{
					gui.setLocationRelativeTo( null ); // give up
				}
				else
				{
					// put it to the right
					gui.setLocation( windowLoc.x + windowSize.width, windowLoc.y );
				}
			}
			else
			{
				// put it to the left
				gui.setLocation( windowLoc.x - guiSize.width, windowLoc.y );
			}

		}
		else
		{
			gui.setLocationRelativeTo( null );
		}
	}
}