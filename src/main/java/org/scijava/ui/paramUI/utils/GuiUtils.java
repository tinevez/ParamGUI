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
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
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

	public static boolean isLikelyUrl( final String s )
	{
		if ( s == null )
			return false;
		final String t = s.trim().toLowerCase();
		return t.startsWith( "http://" ) || t.startsWith( "https://" ) || t.startsWith( "file:" );
	}

	public static void openInBrowser( final String url, final Component parent )
	{
		try
		{
			if ( Desktop.isDesktopSupported() )
			{
				Desktop.getDesktop().browse( new java.net.URI( url.trim() ) );
			}
			else
			{
				JOptionPane.showMessageDialog( parent,
						"Desktop browsing not supported.\n" + url,
						"Help", JOptionPane.INFORMATION_MESSAGE );
			}
		}
		catch ( final Exception ex )
		{
			JOptionPane.showMessageDialog( parent,
					"Could not open:\n" + url + "\n" + ex.getMessage(),
					"Help", JOptionPane.ERROR_MESSAGE );
		}
	}
}
