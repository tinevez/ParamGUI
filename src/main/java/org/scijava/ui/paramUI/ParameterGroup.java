package org.scijava.ui.paramUI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.scijava.ui.paramUI.Parameters.Parameter;

/**
 * Holds an ordered list of parameters, and possibly sub-groups of parameters.
 * <p>
 * Has fields to store a name, help text and sets whether the group is folded or
 * not when displayed in a UI.
 */
public class ParameterGroup implements Iterable< Parameter< ?, ? > >
{

	private String name = toString();

	private String help = "";

	private boolean visible = false;

	protected List< Parameter< ?, ? > > parameters = new ArrayList<>();

	ParameterGroup name( final String name )
	{
		this.name = name;
		return this;
	}

	public String getName()
	{
		return name;
	}

	ParameterGroup help( final String help )
	{
		this.help = help;
		return this;
	}

	public String getHelp()
	{
		return help;
	}

	ParameterGroup visible( final boolean visible )
	{
		this.visible = visible;
		return this;
	}

	public boolean isVisible()
	{
		return visible;
	}

	void add( final Parameter< ?, ? > param )
	{
		this.parameters.add( param );
	}

	@Override
	public Iterator< Parameter< ?, ? > > iterator()
	{
		return parameters.iterator();
	}

	@Override
	public String toString()
	{
		return getName()
				+ " (" + this.getClass().getSimpleName() + ")\n"
				+ ( ( getHelp() == null )
						? " - no help\n"
						: " - help: " + getHelp() + "\n" )
				+ " - visible: " + isVisible() + "\n";
	}
}
