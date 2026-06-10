package org.scijava.ui.paramUI.visitors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.ui.paramUI.Configurator;
import org.scijava.ui.paramUI.Configurator.SelectableParameters;
import org.scijava.ui.paramUI.Parameters.Parameter;

public class Maps
{

	/**
	 * Convert a Configurator to a Map of key to value.
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
			map.put( key, param.getValue() );
		}
		for ( final SelectableParameters selectable : config.getSelectables() )
		{
			final String key = selectable.getKey();
			map.put( key, selectable.getSelection().getKey() );
		}
		return map;
	}

	/**
	 * Fills the specified Configurator with the values from the specified Map,
	 * by matching the keys of the Map to the keys of the Configurator's
	 * parameters.
	 * 
	 * @param map
	 *            the Map containing the keys and values to set in the
	 *            Configurator.
	 * @param config
	 *            the Configurator to fill with the values from the Map.
	 */
	public static final void fromMap( final Map< String, Object > map, final Configurator config )
	{
		config.getParameters().forEach( arg -> fromMap( map, arg ) );
		config.getSelectables().forEach( selectable -> fromMap( map, selectable ) );
	}

	private static < O > void fromMap( final Map< String, Object > map, final Parameter< ?, O > param )
	{
		final String key = param.getKey();
		final Object val = map.get( key );
		if ( val != null )
		{
			@SuppressWarnings( "unchecked" )
			final O castVal = ( O ) val;
			param.set( castVal );
		}
	}

	private static void fromMap( final Map< String, ? > map, final SelectableParameters selectable )
	{
		final Object val = map.get( selectable.getKey() );
		if ( val != null )
			selectable.select( ( String ) val );
	}
}
