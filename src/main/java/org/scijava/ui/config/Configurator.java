package org.scijava.ui.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.scijava.ui.config.Parameters.BooleanParam;
import org.scijava.ui.config.Parameters.BoundedValueParameter;
import org.scijava.ui.config.Parameters.ChoiceParam;
import org.scijava.ui.config.Parameters.DoubleParam;
import org.scijava.ui.config.Parameters.EnumParam;
import org.scijava.ui.config.Parameters.IntParam;
import org.scijava.ui.config.Parameters.Parameter;
import org.scijava.ui.config.Parameters.PathParam;
import org.scijava.ui.config.Parameters.StringParam;
import org.scijava.ui.config.utils.StringUtils;

/**
 * Base class for configurator tools. The implementation of a configurator is
 * made by subclassing this class (or one of its specialization) and specifying
 * parameters with the <code>addXYX()</code> builders.
 *
 * @author Jean-Yves Tinevez
 */
public abstract class Configurator implements Iterable< Parameter< ?, ? > >
{

	protected final List< Parameter< ?, ? > > params = new ArrayList<>();

	protected final List< ParameterGroup > groups = new ArrayList<>();

	/**
	 * Contains Parameters and ParameterGroups in the order they were added, for
	 * UI display.
	 */
	final List< Object > orderedElements = new ArrayList<>();

	protected final List< SelectableParameters > selectables = new ArrayList<>();

	/**
	 * The translators that will be applied to the value before displaying it in
	 * the UI.
	 */
	protected final Map< Parameter< ?, ? >, Function< ?, ? > > forwardUITranslators = new HashMap<>();

	/**
	 * The translators that will be applied to a value read from the UI, before
	 * storing it in the Parameter object.
	 */
	protected final Map< Parameter< ?, ? >, Function< ?, ? > > backwardUITranslators = new HashMap<>();

	/**
	 * The name of configurations created by this configurator. For instance it
	 * can be the plugin name displayed at the top of the UI.
	 */
	protected String name;

	/**
	 * A help text describing the configuration created by this configurator. If
	 * it is a valid URL, it will be used as a link to open the URL when
	 * clicking on the help button in the UI.
	 */
	protected String help;

	/*
	 * CONSTRUCTOR
	 */

	protected Configurator( final String name, final String help )
	{
		this.name = name;
		this.help = help;
	}

	/*
	 * GETTERS
	 */

	public String getName()
	{
		return name;
	}

	public String getHelp()
	{
		return help;
	}

	/**
	 * Returns the list of parameters (plus the command) in this config. All
	 * parameters are present, regardless of whether they are in
	 * {@link SelectableParameters}, {@link Parameter#visible} or not.
	 *
	 * @return the list of parameters.
	 */
	public List< Parameter< ?, ? > > getParameters()
	{
		return Collections.unmodifiableList( params );
	}

	/**
	 * Returns the list of {@link SelectableParameters} in this config.
	 *
	 * @return the list of {@link SelectableParameters}.
	 */
	public List< SelectableParameters > getSelectables()
	{
		return Collections.unmodifiableList( selectables );
	}

	/**
	 * Returns the list of parameters set in this config. The list contains only
	 * the parameters that are selected if they are in a
	 * {@link SelectableParameters}, and those who are not in a
	 * {@link SelectableParameters}.
	 *
	 * @return the selected parameters.
	 */
	public List< Parameter< ?, ? > > getSelectedParameters()
	{
		final List< Parameter< ?, ? > > selectedParams = new ArrayList<>( params );
		for ( final SelectableParameters selectable : selectables )
			selectable.filter( selectedParams );
		return selectedParams;
	}

	/**
	 * Returns the map of translators that will be applied to the value before
	 * displaying it in the UI.
	 * 
	 * @return the map of forward translators.
	 */
	public Map< Parameter< ?, ? >, Function< ?, ? > > getForwardUITranslators()
	{
		return Collections.unmodifiableMap( forwardUITranslators );
	}

	public Map< Parameter< ?, ? >, Function< ?, ? > > getBackwardUITranslators()
	{
		return Collections.unmodifiableMap( backwardUITranslators );
	}

	/*
	 * SELECTABLE PARAMETER GROUPS.
	 */

	/**
	 * Creates a 'one or the other' relationships. The parameters that will be
	 * passed to the {@link SelectableParameters} will be flagged as as not to
	 * be used concurrently in the same command. This will be used when creating
	 * UIs.
	 *
	 * @return a new {@link SelectableParameters} instance.
	 */
	protected SelectableParamAdder addSelectableParameters()
	{
		return new SelectableParamAdder();
	}

	public static class SelectableParameters
	{

		private final List< Parameter< ?, ? > > params = new ArrayList<>();

		private final String key;

		private int selected = 0;

		private SelectableParameters( final String key, final List< Parameter< ?, ? > > params )
		{
			this.key = key;
			this.params.addAll( params );
		}

		public String getKey()
		{
			return key;
		}

		private void filter( final List< Parameter< ?, ? > > params )
		{
			final Set< Parameter< ?, ? > > toRemove = new HashSet<>();
			for ( final Parameter< ?, ? > arg : params )
			{
				if ( !params.contains( arg ) )
					continue; // Unknown of this selectable, keep it.

				if ( arg.equals( getSelection() ) )
					continue; // The one selected, keep it.

				// Not selected, remove it.
				toRemove.add( arg );
			}

			params.removeAll( toRemove );
		}

		public void select( final int selection )
		{
			this.selected = Math.max( 0, Math.min( params.size() - 1, selection ) );
		}

		public void select( final Parameter< ?, ? > arg )
		{
			final int sel = params.indexOf( arg );
			if ( sel < 0 )
			{
				this.selected = 0;
				return;
			}
			this.selected = sel;
		}

		public void select( final String key )
		{
			for ( int i = 0; i < params.size(); i++ )
			{
				if ( key.equals( params.get( i ).getKey() ) )
				{
					this.selected = i;
					return;
				}
			}
			this.selected = 0;
		}

		public Parameter< ?, ? > getSelection()
		{
			return params.get( selected );
		}

		public int getSelected()
		{
			return selected;
		}

		/**
		 * Exposes all members of the selectable.
		 *
		 * @return the parameters in this selectable.
		 */
		public List< Parameter< ?, ? > > getParameters()
		{
			return params;
		}
	}

	/*
	 * VISITOR INTERFACE.
	 */

	/*
	 * ADDER CLASSES.
	 */

	/**
	 * Base class for builders that can add parameters to this configurator.
	 * 
	 * @param <A>
	 *            the type of parameter this builder creates.
	 * @param <T>
	 *            the type of the builder, for fluent API.
	 */
	@SuppressWarnings( "unchecked" )
	abstract class Adder< A, T extends Adder< A, T > >
	{

		protected String key;

		protected String name;

		/**
		 * Specifies the key to use to persist the value of this parameter. If
		 * <code>null</code>, the parameter will not be persisted.
		 *
		 * @param key
		 *            the parameter key.
		 * @return this adder.
		 */
		public T key( final String key )
		{
			this.key = key;
			return ( T ) this;
		}

		/**
		 * Specifies a user-friendly name for the parameter.
		 *
		 * @param name
		 *            the parameter name.
		 * @return this adder.
		 */
		public T name( final String name )
		{
			this.name = name;
			return ( T ) this;
		}

		/**
		 * Returns the parameter created by this builder.
		 *
		 * @return the parameter.
		 */
		public abstract A get();

		/**
		 * Basic consistency check for the builder.
		 */
		protected void check()
		{
			if ( key == null )
				throw new IllegalArgumentException( "Parameter '" + name + "' must have a key." );
		}
	}

	/**
	 * Base class for builders that can add parameters to this configurator.
	 * 
	 * @param <A>
	 *            the type of parameter this builder creates.
	 * @param <T>
	 *            the type of the builder, for fluent API.
	 * @param <O>
	 *            the type of value the parameter created by this builder
	 *            accepts.
	 */
	@SuppressWarnings( "unchecked" )
	abstract class ParamAdder< A extends Parameter< A, O >, T extends ParamAdder< A, T, O >, O > extends Adder< A, T >
	{

		protected String units;

		protected O defaultValue;

		protected String help;

		protected boolean visible = true; // by default

		/**
		 * Specifies units for values accepted by this parameter.
		 *
		 * @param units
		 *            parameter value units.
		 * @return this adder.
		 */
		public T units( final String units )
		{
			this.units = units;
			return ( T ) this;
		}

		/**
		 * Specifies whether this parameter will be visible in user interfaces
		 * generated from the configurator.
		 *
		 * @param visible
		 *            UI visibility.
		 * @return this adder.
		 */
		public T visible( final boolean visible )
		{
			this.visible = visible;
			return ( T ) this;
		}

		/**
		 * Specifies a help text for the parameter.
		 *
		 * @param help
		 *            the help text.
		 * @return this adder.
		 */
		public T help( final String help )
		{
			this.help = help;
			return ( T ) this;
		}

		/**
		 * Specifies a default value for this parameter. If the parameter is not
		 * set, it will appear in the command line with this default value.
		 *
		 * @param defaultValue
		 *            the parameter default value.
		 * @return this adder.
		 */
		public T defaultValue( final O defaultValue )
		{
			this.defaultValue = defaultValue;
			return ( T ) this;
		}
	}

	@SuppressWarnings( "unchecked" )
	private abstract class BoundedAdder< A extends BoundedValueParameter< A, O >, T extends BoundedAdder< A, T, O >, O extends Comparable< O > > extends ParamAdder< A, T, O >
	{
		protected O min;

		protected O max;

		/**
		 * Specifies a min for the values accepted by this parameter. If the
		 * user sets a value below this min value, an error is thrown.
		 *
		 * @param min
		 *            the min value.
		 * @return this adder.
		 */
		public T min( final O min )
		{
			this.min = min;
			return ( T ) this;
		}

		/**
		 * Specifies a max for the values accepted by this parameter. If the
		 * user sets a value above this max value, an error is thrown.
		 *
		 * @param max
		 *            the max value.
		 * @return this adder.
		 */
		public T max( final O max )
		{
			this.max = max;
			return ( T ) this;
		}
	}

	protected class IntAdder extends BoundedAdder< IntParam, IntAdder, Integer >
	{
		@Override
		public IntParam get()
		{
			final IntParam arg = new IntParam()
					.name( name )
					.help( help )
					.defaultValue( defaultValue )
					.max( max )
					.min( min )
					.units( units )
					.visible( visible )
					.key( key );
			Configurator.this.params.add( arg );
			Configurator.this.orderedElements.add( arg );
			return arg;
		}
	}

	protected class DoubleAdder extends BoundedAdder< DoubleParam, DoubleAdder, Double >
	{

		private DoubleAdder()
		{}

		@Override
		public DoubleParam get()
		{
			final DoubleParam arg = new DoubleParam()
					.name( name )
					.help( help )
					.defaultValue( defaultValue )
					.max( max )
					.min( min )
					.units( units )
					.visible( visible )
					.key( key );
			Configurator.this.params.add( arg );
			Configurator.this.orderedElements.add( arg );
			return arg;
		}
	}

	protected class BooleanAdder extends ParamAdder< BooleanParam, BooleanAdder, Boolean >
	{

		private BooleanAdder()
		{}

		@Override
		public BooleanParam get()
		{
			check();
			final BooleanParam arg = new BooleanParam()
					.name( name )
					.help( help )
					.defaultValue( defaultValue )
					.units( units )
					.visible( visible )
					.key( key );
			Configurator.this.params.add( arg );
			Configurator.this.orderedElements.add( arg );
			return arg;
		}
	}

	protected class StringAdder extends ParamAdder< StringParam, StringAdder, String >
	{

		private StringAdder()
		{}

		@Override
		public StringParam get()
		{
			check();
			final StringParam arg = new StringParam()
					.name( name )
					.help( help )
					.defaultValue( defaultValue )
					.units( units )
					.visible( visible )
					.key( key );
			Configurator.this.params.add( arg );
			Configurator.this.orderedElements.add( arg );
			return arg;
		}
	}

	protected class PathAdder extends ParamAdder< PathParam, PathAdder, String >
	{

		private PathAdder()
		{}

		@Override
		public PathParam get()
		{
			check();
			final PathParam arg = new PathParam()
					.name( name )
					.help( help )
					.defaultValue( defaultValue )
					.units( units )
					.visible( visible )
					.key( key );
			Configurator.this.params.add( arg );
			Configurator.this.orderedElements.add( arg );
			return arg;
		}
	}

	protected class ChoiceAdder extends ParamAdder< ChoiceParam, ChoiceAdder, String >
	{

		private ChoiceAdder()
		{}

		private final List< String > choices = new ArrayList<>();

		private final List< String > mappeds = new ArrayList<>();

		/**
		 * Adds a selectable item for this choice parameter. The user will be
		 * able to select from the list of choices added by this method.
		 *
		 * @param choice
		 *            the choice to add.
		 * @return this adder.
		 */
		public ChoiceAdder addChoice( final String choice )
		{
			return addChoice( choice, choice );
		}

		/**
		 * Adds a selectable item for this choice parameter. The user will be
		 * able to select from the list of choices added by this method. In the
		 * command line, this choice will be mapped to the specified string.
		 * <p>
		 * Example:
		 *
		 * <pre>
		 * addChoice( "Bacteria phase contrast", "bact_phase_omni" )
		 * </pre>
		 *
		 * will display <i>Bacteria phase contrast</i> in the UI and results in
		 * using <i>bact_phase_omni</i> in the generated command line.
		 *
		 * @param choice
		 *            the choice to add.
		 * @param mapped
		 *            the string the choice will be mapped in the command line
		 *            parameter.
		 * @return this adder.
		 */
		public ChoiceAdder addChoice( final String choice, final String mapped )
		{
			if ( !choices.contains( choice ) )
			{
				choices.add( choice );
				mappeds.add( mapped );
			}
			return this;
		}

		/**
		 * Adds the specified items for this choice parameter. The user will be
		 * able to select from the list of choices added by this method.
		 *
		 * @param c
		 *            the choices to add.
		 * @return this adder.
		 */
		public ParamAdder< ChoiceParam, ChoiceAdder, String > addChoiceAll( final Collection< String > c )
		{
			for ( final String in : c )
				addChoice( in );
			return this;
		}

		/**
		 * Specifies a default value for this parameter. If the parameter is not
		 * set, it will appear in the command line with this default value.
		 * <p>
		 * The value specified must belong to the list of choices set with
		 * {@link #addChoice(String)} or {@link #addChoiceAll(Collection)}.
		 *
		 * @param defaultChoice
		 *            the parameter default value.
		 * @return this adder.
		 */
		@Override
		public ChoiceAdder defaultValue( final String defaultChoice )
		{
			final int sel = choices.indexOf( defaultChoice );
			if ( sel < 0 )
				throw new IllegalArgumentException( "Unknown selection '" + defaultChoice + "' for parameter '"
						+ name + "'. Must be one of " + StringUtils.join( choices, ", " ) + "." );
			return super.defaultValue( defaultChoice );
		}

		/**
		 * Specifies a default value for this parameter, by selecting the
		 * possible value in order or addition. If the parameter is not set, it
		 * will appear in the command line with this default value.
		 *
		 * @param selected
		 *            the index of the default value in the list of possible
		 *            choices.
		 * @return this adder.
		 */
		public ChoiceAdder defaultValue( final int selected )
		{
			if ( selected < 0 || selected >= choices.size() )
				throw new IllegalArgumentException( "Invalid index for selection of parameter '"
						+ name + "'. Must be in scale " + 0 + " to " + ( choices.size() - 1 ) + " in "
						+ StringUtils.join( choices, ", " ) + "." );
			return defaultValue( choices.get( selected ) );
		}

		@Override
		public ChoiceParam get()
		{
			check();
			final ChoiceParam arg = new ChoiceParam()
					.name( name )
					.help( help )
					.units( units )
					.visible( visible )
					.key( key );
			for ( int i = 0; i < choices.size(); i++ )
				arg.addChoice( choices.get( i ), mappeds.get( i ) );

			arg.defaultValue( defaultValue );
			Configurator.this.orderedElements.add( arg );
			Configurator.this.params.add( arg );
			return arg;
		}
	}

	protected class EnumAdder< E extends Enum< E > > extends ParamAdder< EnumParam< E >, EnumAdder< E >, E >
	{

		private final Class< E > enumClass;

		public EnumAdder( final Class< E > enumClass )
		{
			this.enumClass = enumClass;
		}

		@Override
		public EnumParam< E > get()
		{
			check();
			final EnumParam< E > arg = new EnumParam<>( enumClass )
					.name( name )
					.help( help )
					.defaultValue( defaultValue )
					.units( units )
					.visible( visible )
					.key( key );
			Configurator.this.params.add( arg );
			Configurator.this.orderedElements.add( arg );
			return arg;
		}
	}
	
	protected class SelectableParamAdder extends Adder< SelectableParameters, SelectableParamAdder >
	{

		private final List< Parameter< ?, ? > > params = new ArrayList<>();

		public < T extends Parameter< T, O >, O > SelectableParamAdder add( final T param )
		{
			if ( !params.contains( param ) )
				params.add( param );
			return this;
		}

		@Override
		public SelectableParameters get()
		{
			check();
			final SelectableParameters sp = new SelectableParameters( key, params );
			selectables.add( sp );
			return sp;
		}
	}

	protected class GroupAdder extends Adder< ParameterGroup, GroupAdder >
	{

		protected final List< Parameter< ?, ? > > params = new ArrayList<>();

		protected boolean collapsed = true; // by default

		public < T extends Parameter< T, O >, O > GroupAdder add( final T param )
		{
			params.add( param );
			return this;
		}

		/**
		 * Set whether the group is folded (collapsed, default) or unfolded
		 * (expanded) when displayed in a UI.
		 * 
		 * @param collapsed
		 *            whether the group is collapsed when displayed in a UI.
		 * @return this group adder.
		 */
		public GroupAdder collapsed( final boolean collapsed )
		{
			this.collapsed = collapsed;
			return this;
		}

		@Override
		public ParameterGroup get()
		{
			final ParameterGroup group = new ParameterGroup()
					.name( name )
					.collapsed( collapsed );
			for ( final Parameter< ?, ? > param : params )
				group.add( param );
			Configurator.this.groups.add( group );
			Configurator.this.orderedElements.add( group );
			return group;
		}
	}

	/*
	 * ADDER METHODS.
	 */

	/**
	 * Adds a boolean parameter to the config, via a builder.
	 *
	 * @return a new flag parameter builder.
	 */
	protected BooleanAdder addFlag()
	{
		return new BooleanAdder();
	}

	/**
	 * Adds a string parameter to the config, via a builder.
	 *
	 * @return new string parameter builder.
	 */
	protected StringAdder addStringParameter()
	{
		return new StringAdder();
	}

	/**
	 * Adds a path parameter to the config, via a builder.
	 *
	 * @return new path parameter builder.
	 */
	protected PathAdder addPathParameter()
	{
		return new PathAdder();
	}

	/**
	 * Adds a integer parameter to the config, via a builder.
	 *
	 * @return new integer parameter builder.
	 */
	protected IntAdder addIntParameter()
	{
		return new IntAdder();
	}

	/**
	 * Adds a double parameter to the config, via a builder.
	 *
	 * @return new double parameter builder.
	 */
	protected DoubleAdder addDoubleParameter()
	{
		return new DoubleAdder();
	}

	/**
	 * Adds a choice parameter to the config, via a builder. Such parameters can
	 * accept a series of discrete values (specified by addChoice() method in
	 * the builder).
	 *
	 * @return new choice parameter builder.
	 */
	protected ChoiceAdder addChoiceParameter()
	{
		return new ChoiceAdder();
	}

	/**
	 * Adds an enum parameter to the config, via a builder. Such parameters can
	 * accept a series of discrete values, that are the values of the specified
	 * enum class.
	 * 
	 * @param <E>
	 *            the type of the enum values.
	 * @param enumClass
	 *            the class of the enum values.
	 * @return new enum parameter builder.
	 */
	protected < E extends Enum< E > > EnumAdder< E > addEnumParameter( final Class< E > enumClass )
	{
		return new EnumAdder<>( enumClass );
	}

	/**
	 * Adds a group of parameters to the config, via a builder. Such groups are
	 * used to group parameters together in the UI.
	 * 
	 * @param name
	 *            the name of the group.
	 * @return a new group builder.
	 */
	protected GroupAdder addGroup( final String name )
	{
		return new GroupAdder().name( name );
	}

	@Override
	public String toString()
	{
		final StringBuilder str = new StringBuilder();
		str.append( name + " (" + hashCode() + ")\n" );
		str.append( "-----------------------\n" );
		str.append( help + "\n" );
		str.append( "-----------------------\n" );

		final ConfiguratorIterator it = iterator();
		while ( it.hasNext() )
		{
			final Parameter< ?, ? > param = it.next();
			if (it.groupEntered())
			{
				str.append( "> -----------------------\n" );
				str.append( "> " + it.getCurrentGroup() );
				str.append( "> -----------------------\n" );
			}
			if (it.groupExited())
			{
				str.append( "> -----------------------\n" );
			}
			str.append( param );
		}
		return str.toString();
	}

	/**
	 * Decorates the specified parameter with a translator, that will modify the
	 * value <b>displayed</b> in the user-interfaces built with this
	 * configurator.
	 * <p>
	 * This can be used to translate the value of an parameter into a more
	 * user-friendly value, for instance to translate a radius into a diameter.
	 * <p>
	 * Warning: display translation is not supported for {@link BooleanParam}
	 * and {@link ChoiceParam}.
	 *
	 * @param arg
	 *            the parameter to decorate.
	 * @param forward
	 *            the function to apply to the value to display it in the UI.
	 * @param backward
	 *            the function to apply to the value to get the value back from
	 *            the UI.
	 * @param <O>
	 *            the type of value the parameter accepts.
	 */
	protected < O > void setDisplayTranslator( final Parameter< ?, O > arg, final Function< O, O > forward, final Function< O, O > backward )
	{
		if ( arg instanceof ChoiceParam )
			throw new IllegalArgumentException( "ChoiceParam does not support display translators." );
		if ( arg instanceof BooleanParam )
			throw new IllegalArgumentException( "BooleanParam does not support display translators." );

		forwardUITranslators.put( arg, forward );
		backwardUITranslators.put( arg, backward );
	}

	@Override
	public ConfiguratorIterator iterator()
	{
		return new ConfiguratorIterator( this );
	}
}
