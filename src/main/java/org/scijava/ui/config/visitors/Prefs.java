package org.scijava.ui.config.visitors;

import java.util.HashMap;
import java.util.Map;

import org.scijava.prefs.DefaultPrefService;
import org.scijava.ui.config.Configurator;

/**
 * Serializes and deserializes Configurator values to and from a PrefService.
 */
public class Prefs
{

	/**
	 * Saves the values of the specified Configurator to the PrefService.
	 * 
	 * @param <C>
	 *            the type of the Configurator to serialize.
	 * @param config
	 *            the Configurator to save the values of.
	 */
	public static < C extends Configurator > void serialize( final C config )
	{
		final DefaultPrefService prefs = new DefaultPrefService();
		Maps.toMap( config ).forEach( ( k, v ) -> {
			final Class< ? extends Object > valClass = v.getClass();
			if ( Double.class.isAssignableFrom( valClass ) || Float.class.isAssignableFrom( valClass ) )
			{
				prefs.put( config.getClass(), k, ( ( Number ) v ).doubleValue() );
			}
			else if ( Integer.class.isAssignableFrom( valClass ) )
			{
				prefs.put( config.getClass(), k, ( ( Number ) v ).intValue() );
			}
			else if ( Boolean.class.isAssignableFrom( valClass ) )
			{
				prefs.put( config.getClass(), k, ( Boolean ) v );
			}
			else if ( String.class.isAssignableFrom( valClass ) )
			{
				prefs.put( config.getClass(), k, ( String ) v );
			}
			else if ( Enum.class.isAssignableFrom( valClass ) )
			{
				prefs.put( config.getClass(), k, ( ( Enum< ? > ) v ).name() );
			}
		} );
	}

	/**
	 * Reloads the values of the specified Configurator from the PrefService,.
	 * 
	 * @param <C>
	 *            the type of the Configurator to deserialize.
	 * @param config
	 *            the Configurator to fill with the values from the PrefService.
	 */
	public static < C extends Configurator > void deserialize( final C config )
	{
		final DefaultPrefService prefs = new DefaultPrefService();
		final Map< String, Object > map = new HashMap<>();
		Maps.toMap( config ).forEach( ( k, v ) -> {
			final Class< ? extends Object > valClass = v.getClass();
			if ( Double.class.isAssignableFrom( valClass ) || Float.class.isAssignableFrom( valClass ) )
			{
				map.put( k, prefs.getDouble( config.getClass(), k, ( ( Number ) v ).doubleValue() ) );
			}
			else if ( Integer.class.isAssignableFrom( valClass ) )
			{
				map.put( k, prefs.getInt( config.getClass(), k, ( ( Number ) v ).intValue() ) );
			}
			else if ( Boolean.class.isAssignableFrom( valClass ) )
			{
				map.put( k, prefs.getBoolean( config.getClass(), k, ( Boolean ) v ) );
			}
			else if ( String.class.isAssignableFrom( valClass ) )
			{
				map.put( k, prefs.get( config.getClass(), k, ( String ) v ) );
			}
			else if ( Enum.class.isAssignableFrom( valClass ) )
			{
				final String str = prefs.get( config.getClass(), k );
				if ( str == null )
					return; // no value saved for this parameter, skip it
				try
				{
					@SuppressWarnings( { "unchecked", "rawtypes" } )
					final Object enumVal = Enum.valueOf( ( Class< ? extends Enum > ) valClass, str );
					map.put( k, enumVal );
				}
				catch ( final IllegalArgumentException exc )
				{
					System.err.println( "Couldn't parse enum value " + str + " for parameter " + k + " of type " + valClass.getName() );
					exc.printStackTrace();
				}
			}
			else
			{
				System.err.println( "Don't know how to reload parameter " + k + " of type " + valClass.getName() );
			}
		} );
		Maps.fromMap( map, config );
	}
}
