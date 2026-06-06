package org.scijava.ui.paramUI.utils;

import java.util.Iterator;

public class StringUtils
{
	public static String join( final Object[] array, String separator )
	{
		if ( array == null )
			return "";
		if ( separator == null )
			separator = "";

		final StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < array.length; i++ )
		{
			if ( i > 0 )
				sb.append( separator );
			sb.append( array[ i ] != null ? array[ i ].toString() : "" );
		}
		return sb.toString();
	}

	public static String join( final Iterable< ? > iterable, final String separator )
	{
		if ( iterable == null )
			return "";
		return join( iterable.iterator(), separator );
	}

	public static String join( final Iterator< ? > iterator, String separator )
	{
		if ( iterator == null )
			return "";
		if ( separator == null )
			separator = "";

		final StringBuilder sb = new StringBuilder();
		while ( iterator.hasNext() )
		{
			if ( sb.length() > 0 )
				sb.append( separator );
			final Object obj = iterator.next();
			sb.append( obj != null ? obj.toString() : "" );
		}
		return sb.toString();
	}
}
