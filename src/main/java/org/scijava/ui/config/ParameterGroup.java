package org.scijava.ui.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.scijava.ui.config.Parameters.Parameter;

/**
 * Holds an ordered list of parameters, and possibly sub-groups of parameters.
 * <p>
 * Has fields to store a name, help text and sets whether the group is folded or
 * not when displayed in a UI.
 */
public class ParameterGroup implements Iterable< Parameter< ?, ? > >
{

	private String name = toString();

	private boolean collapsed = true;

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

	/**
	 * Set whether the group is folded (collapsed, default) or unfolded
	 * (expanded) when displayed in a UI.
	 * 
	 * @param collapsed
	 * @return this parameter group.
	 */
	ParameterGroup collapsed( final boolean collapsed )
	{
		this.collapsed = collapsed;
		return this;
	}

	public boolean isCollapsed()
	{
		return collapsed;
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
				+ " - collapsed: " + isCollapsed() + "\n";
	}
}
