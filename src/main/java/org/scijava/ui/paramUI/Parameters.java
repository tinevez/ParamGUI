package org.scijava.ui.paramUI;

import java.util.ArrayList;
import java.util.List;

import org.scijava.ui.paramUI.utils.StringUtils;

public class Parameters
{

	/**
	 * Base class for parameters.
	 *
	 * @param <T>
	 *            the implementing type of the parameter.
	 * @param <O>
	 *            the type of value this parameter accepts.
	 */
	@SuppressWarnings( "unchecked" )
	public static abstract class Parameter< T extends Parameter< T, O >, O >
	{

		protected boolean visible = true;

		protected String name;

		protected String help;

		private String key;

		private O value;

		private O defaultValue;

		private String units;

		T units( final String units )
		{
			this.units = units;
			return ( T ) this;
		}

		public String getUnits()
		{
			return units;
		}

		T defaultValue( final O defaultValue )
		{
			this.defaultValue = defaultValue;
			return ( T ) this;
		}

		public O getDefaultValue()
		{
			return defaultValue;
		}

		public void set( final O value )
		{
			this.value = value;
		}

		/**
		 * Returns the value of this parameter, or the default value if no value
		 * has been set.
		 *
		 * @return the value of this parameter, or the default value if no value
		 *         has been set.
		 */
		public O getValue()
		{
			return ( value == null ) ? defaultValue : value;
		}

		/**
		 * If <code>false</code>, this parameter won't be shown in UIs. It will
		 * be used for the command line builder nonetheless.
		 *
		 * @param visible
		 *            whether this parameter should be visible in the UI or not.
		 *            By default: <code>true</code>.
		 * @see CliGuiBuilder
		 * @return the parameter.
		 */
		T visible( final boolean visible )
		{
			this.visible = visible;
			return ( T ) this;
		}

		public boolean isVisible()
		{
			return visible;
		}

		T name( final String name )
		{
			this.name = name;
			return ( T ) this;
		}

		T help( final String help )
		{
			this.help = help;
			return ( T ) this;
		}

		/**
		 * Sets the String key to of this parameter, which will be used mainly
		 * for de / serialization purposes. The key must be unique within a
		 * config.
		 * 
		 *
		 * @param key
		 *            the key to use.
		 * @return the parameter.
		 */
		T key( final String key )
		{

			this.key = key;
			return ( T ) this;
		}

		public String getName()
		{
			return name;
		}

		public String getHelp()
		{
			return help;
		}

		public String getKey()
		{
			return key;
		}

		public abstract void accept( final ParameterVisitor visitor );

		@Override
		public String toString()
		{
			return getName()
					+ " (" + this.getClass().getSimpleName() + ")\n"
					+ ( ( getHelp() == null )
							? " - no help\n"
							: " - help: " + getHelp() + "\n" )
					+ " - key: " + getKey() + "\n"
					+ " - value: " + getValue() + "\n"
					+ ( ( getUnits() == null )
							? ""
							: " - units: " + getUnits() + "\n" )
					+ " - default value: " + getDefaultValue() + "\n"
					+ " - visible: " + isVisible() + "\n";
		}
	}

	public static class BooleanParam extends Parameter< BooleanParam, Boolean >
	{
		BooleanParam()
		{}

		/**
		 * Sets this flag parameter to <code>true</code>.
		 */
		public void set()
		{
			set( true );
		}

		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	/**
	 * Specialization of {@link StringParam} to be used to enter file of folder
	 * path in a GUI.
	 */
	public static class PathParam extends AbstractStringParam< PathParam >
	{
		PathParam()
		{}

		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static class StringParam extends AbstractStringParam< StringParam >
	{
		StringParam()
		{}

		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	/**
	 * Base class for parameters that use a string as internal value.
	 * 
	 * @param <T>
	 *            the type of the parameter, used for chaining builders.
	 */
	public static abstract class AbstractStringParam< T extends AbstractStringParam< T > > extends Parameter< T, String >
	{}

	public static class IntParam extends BoundedValueParameter< IntParam, Integer >
	{
		IntParam()
		{}

		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static class DoubleParam extends BoundedValueParameter< DoubleParam, Double >
	{
		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static class ChoiceParam extends Parameter< ChoiceParam, String >
	{

		private final List< String > choices = new ArrayList<>();

		private final List< String > displays = new ArrayList<>();

		private int selected = -1; // -1 means no selection.;

		ChoiceParam()
		{}

		ChoiceParam addChoice( final String choice, final String displayed )
		{
			if ( !choices.contains( choice ) )
			{
				choices.add( choice );
				displays.add( displayed );
			}
			return this;
		}

		/**
		 * The list of the display strings corresponding to the possible
		 * choices.
		 *
		 * @return The list of the display strings.
		 */
		public List< String > getDisplays()
		{
			return displays;
		}

		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}

		@Override
		public String getValue()
		{
			return choices.get( selected );
		}

		public int getSelectedIndex()
		{
			return selected;
		}

		@Override
		public void set( final String choice )
		{
			final int sel = choices.indexOf( choice );
			if ( sel < 0 )
				throw new IllegalArgumentException( "Unknown selection '" + choice + "' for parameter '"
						+ name + "'. Must be one of: [ " + StringUtils.join( choices, ", " ) + " ]." );
			this.selected = sel;
		}

		public void set( final int selected )
		{
			if ( selected < 0 || selected >= choices.size() )
				throw new IllegalArgumentException( "Invalid index for selection of parameter '"
						+ name + "'. Must be in scale " + 0 + " to " + ( choices.size() - 1 ) + " in "
						+ StringUtils.join( choices, ", " ) + "." );
			this.selected = selected;
		}

		@Override
		ChoiceParam defaultValue( final String defaultChoice )
		{
			final int sel = choices.indexOf( defaultChoice );
			if ( sel < 0 )
				throw new IllegalArgumentException( "Unknown selection '" + defaultChoice + "' for parameter '"
						+ name + "'. Must be one of " + StringUtils.join( choices, ", " ) + "." );
			super.defaultValue( defaultChoice );
			return this;
		}

		ChoiceParam defaultValue( final int selected )
		{
			if ( selected < 0 || selected >= choices.size() )
				throw new IllegalArgumentException( "Invalid index for selection of parameter '"
						+ name + "'. Must be in scale " + 0 + " to " + ( choices.size() - 1 ) + " in "
						+ StringUtils.join( choices, ", " ) + "." );
			super.defaultValue( choices.get( selected ) );
			return this;
		}

		@Override
		public String toString()
		{
			final String str = super.toString();
			return str
					+ " - choices: " + choices + "\n"
					+ " - display strings: " + displays + "\n";
		}
	}

	public static class EnumParam< E extends Enum< E > > extends Parameter< EnumParam< E >, E >
	{

		private final Class< E > enumClass;

		EnumParam( final Class< E > enumClass )
		{
			this.enumClass = enumClass;
		}

		@Override
		public void accept( final ParameterVisitor visitor )
		{
			visitor.visit( this );
		}

		public Class< E > getEnumClass()
		{
			return enumClass;
		}
	}

	/**
	 * Base class for parameters that accept values that can be bounded by a min
	 * and max.
	 * 
	 * @param <T>
	 *            the implementing type of the parameter.
	 * @param <O>
	 *            the type of value this parameter accepts.
	 */
	@SuppressWarnings( "unchecked" )
	public static abstract class BoundedValueParameter< T extends BoundedValueParameter< T, O >, O > extends Parameter< T, O >
	{

		private BoundedValueParameter()
		{}

		private O min;

		private O max;

		T min( final O min )
		{
			this.min = min;
			return ( T ) this;
		}

		public O getMax()
		{
			return max;
		}

		T max( final O max )
		{
			this.max = max;
			return ( T ) this;
		}

		public O getMin()
		{
			return min;
		}

		public boolean hasMin()
		{
			return min != null;
		}

		public boolean hasMax()
		{
			return max != null;
		}

		@Override
		public String toString()
		{
			final String str = super.toString();
			return str
					+ " - has min: " + hasMin() + "\n"
					+ ( hasMin()
							? " - min: " + getMin() + "\n"
							: "" )
					+ " - has max: " + hasMax() + "\n"
					+ ( hasMax()
							? " - max: " + getMax() + "\n"
							: "" );
		}
	}
}
