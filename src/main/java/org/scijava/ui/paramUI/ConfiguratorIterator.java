package org.scijava.ui.paramUI;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.scijava.ui.paramUI.Parameters.Parameter;

/**
 * An iterator that tracks parameter groups and provides consistent group
 * transition information.
 */
public class ConfiguratorIterator implements Iterator< Parameter< ?, ? > >
{
	private final Iterator< Object > mainIterator;

	// Iteration state for the group we are currently reading "next" from
	private ParameterGroup currentGroup; // group for upcoming parameters (if
											// any)

	private Iterator< Parameter< ?, ? > > currentGroupIterator; // iterator over
																// that group's
																// parameters

	// Prepared next parameter and its group-position flags
	private Parameter< ?, ? > nextParameter;

	private boolean nextIsFirstInGroup = false;

	private boolean nextIsLastInGroup = false;

	// Info about the last parameter returned by next()
	private ParameterGroup lastGroup = null;

	private boolean lastEntered = false;

	private boolean lastExited = false;

	public ConfiguratorIterator( final Configurator configurator )
	{
		this.mainIterator = configurator.orderedElements.iterator();
	}

	@Override
	public boolean hasNext()
	{
		if ( nextParameter != null )
			return true;

		// If we just exhausted a group, clear that state before searching
		if ( currentGroupIterator != null && !currentGroupIterator.hasNext() )
		{
			currentGroupIterator = null;
			currentGroup = null;
		}

		// If we're inside a group with remaining parameters, yield from it
		if ( currentGroupIterator != null && currentGroupIterator.hasNext() )
		{
			nextParameter = currentGroupIterator.next();
			nextIsFirstInGroup = false;
			nextIsLastInGroup = !currentGroupIterator.hasNext();
			return true;
		}

		// Otherwise iterate through ordered elements
		while ( mainIterator.hasNext() )
		{
			final Object element = mainIterator.next();

			if ( element instanceof ParameterGroup )
			{
				// Start iterating this group (skip if empty)
				final ParameterGroup group = ( ParameterGroup ) element;
				final Iterator< Parameter< ?, ? > > it = group.parameters.iterator();
				if ( !it.hasNext() )
					continue; // empty group: ignore

				currentGroup = group;
				currentGroupIterator = it;

				nextParameter = currentGroupIterator.next();
				nextIsFirstInGroup = true;
				nextIsLastInGroup = !currentGroupIterator.hasNext();
				return true;
			}
			else if ( element instanceof Parameter )
			{
				// Standalone parameter
				nextParameter = ( Parameter< ?, ? > ) element;
				nextIsFirstInGroup = false;
				nextIsLastInGroup = false;
				return true;
			}
		}

		return false;
	}

	@Override
	public Parameter< ?, ? > next()
	{
		if ( !hasNext() )
			throw new NoSuchElementException( "No more parameters available" );

		final Parameter< ?, ? > result = nextParameter;

		// Record "last returned" state for caller queries
		lastGroup = currentGroup; // null if standalone parameter
		lastEntered = ( currentGroup != null ) && nextIsFirstInGroup;
		lastExited = ( currentGroup != null ) && nextIsLastInGroup;

		// Consume prepared next
		nextParameter = null;

		return result;
	}

	// Was the last returned parameter the first in its group?
	public boolean groupEntered()
	{
		return lastEntered;
	}

	// Was the last returned parameter the last in its group?
	public boolean groupExited()
	{
		return lastExited;
	}

	// Did the last returned parameter belong to a group?
	public boolean inGroup()
	{
		return lastGroup != null;
	}

	// The group of the last returned parameter (or null if standalone)
	public ParameterGroup getCurrentGroup()
	{
		return lastGroup;
	}
}
