package org.scijava.ui.paramUI.visitors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.ui.paramUI.Configurator;
import org.scijava.ui.paramUI.Configurator.SelectableParameters;
import org.scijava.ui.paramUI.Parameters.Parameter;

public class Maps
{

	/**
	 * Convert a Configurator to a Map of key to value. Parameters without keys
	 * are skipped. Selectables are included.
	 * 
	 * @param config
	 *            the Configurator to convert.
	 * @return a new Map containing the keys and values of the Configurator's
	 *         parameters.
	 */
	public static Map< String, Object > toMap( final Configurator config )
	{
		final Map< String, Object > map = new LinkedHashMap<>();
		for ( final Parameter< ?, ? > param : config )
		{
			final String key = param.getKey();
			if ( key == null )
				continue; // skip parameters without keys
			map.put( key, param.getValue() );
		}
		for ( final SelectableParameters selectable : config.getSelectables() )
		{
			final String key = selectable.getKey();
			if ( key == null )
				continue; // skip selectables without keys
			map.put( key, selectable.getSelection().getKey() );
		}
		return map;
	}

	public static final void fromMap( final Map< String, Object > settings, final Configurator config )
	{
		config.getParameters().forEach( arg -> fromMap( settings, arg ) );
		config.getSelectables().forEach( selectable -> fromMap( settings, selectable ) );
	}

	private static < O > void fromMap( final Map< String, Object > settings, final Parameter< ?, O > param )
	{
		final String key = param.getKey();
		final Object val = settings.get( key );
		if ( val != null )
		{
			@SuppressWarnings( "unchecked" )
			final O castVal = ( O ) val;
			param.set( castVal );
		}
	}

	private static void fromMap( final Map< String, Object > settings, final SelectableParameters selectable )
	{
		final Object val = settings.get( selectable.getKey() );
		if ( val != null )
			selectable.select( ( String ) val );
	}
}
