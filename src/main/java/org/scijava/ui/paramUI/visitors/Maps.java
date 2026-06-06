package org.scijava.ui.paramUI.visitors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.ui.paramUI.Configurator;
import org.scijava.ui.paramUI.Configurator.SelectableArguments;
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
		for ( final SelectableArguments selectable : config.getSelectables() )
		{
			final String key = selectable.getKey();
			if ( key == null )
				continue; // skip selectables without keys
			map.put( key, selectable.getSelection().getKey() );
		}
		return map;
	}
}
