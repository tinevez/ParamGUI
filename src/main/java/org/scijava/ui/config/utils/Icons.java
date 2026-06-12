package org.scijava.ui.config.utils;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class Icons
{

	public static int DEFAULT_ICON_SIZE = 16;

	public static final ImageIcon RELOAD = get( "cached" );

	public static final ImageIcon COMMENT = get( "comment" );

	public static final ImageIcon HELP = get( "help_outline" );

	public static final ImageIcon PLAY = get( "play_circle_filled_white" );

	public static final ImageIcon STOP = get( "stop" );

	public static final ImageIcon RESET = get( "settings_backup_restore" );

	public static final ImageIcon STORE = get( "turned_in_not" );

	public static final ImageIcon PREVIEW = get( "preview" );

	public static final ImageIcon STOP_PREVIEW = get( "preview_off" );

	private static final ImageIcon get( final String name )
	{
		return resize( load( "/icons/" + name + ".png" ), DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
	}

	private static final ImageIcon resize( final ImageIcon icon, final int width, final int height )
	{
		final Image img = icon.getImage();
		final Image resizedImage = img.getScaledInstance( width, height, Image.SCALE_SMOOTH );
		return new ImageIcon( resizedImage );
	}

	private static final ImageIcon load( final String path )
	{
		try
		{
			return new ImageIcon( Icons.class.getResource( path ) );
		}
		catch ( final Exception e )
		{
			System.err.println( "Could not load icon: " + path );
			// Return default "missing image" icon.
			return asImageIcon( UIManager.getIcon( "OptionPane.errorIcon" ) );
		}
	}

	private static ImageIcon asImageIcon( final Icon icon )
	{
		if ( icon instanceof ImageIcon )
			return ( ImageIcon ) icon;
		if ( icon == null )
			return new ImageIcon(
					new java.awt.image.BufferedImage( 16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB ) );
		final int w = Math.max( 1, icon.getIconWidth() );
		final int h = Math.max( 1, icon.getIconHeight() );
		final java.awt.image.BufferedImage img = new java.awt.image.BufferedImage( w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB );
		final java.awt.Graphics2D g = img.createGraphics();
		try
		{
			icon.paintIcon( null, g, 0, 0 );
		}
		finally
		{
			g.dispose();
		}
		return new ImageIcon( img );
	}
}
