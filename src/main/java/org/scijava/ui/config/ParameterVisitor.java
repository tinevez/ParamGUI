package org.scijava.ui.config;

import org.scijava.ui.config.Parameters.BooleanParam;
import org.scijava.ui.config.Parameters.ChoiceParam;
import org.scijava.ui.config.Parameters.DoubleParam;
import org.scijava.ui.config.Parameters.EnumParam;
import org.scijava.ui.config.Parameters.IntParam;
import org.scijava.ui.config.Parameters.PathParam;
import org.scijava.ui.config.Parameters.StringParam;

/**
 * Visitor interface for {@link Parameter} objects.
 */
public interface ParameterVisitor
{
	public default void visit( final BooleanParam flag )
	{
		throw new UnsupportedOperationException();
	}

	public default void visit( final StringParam stringArgument )
	{
		throw new UnsupportedOperationException();
	}

	public default void visit( final DoubleParam doubleArgument )
	{
		throw new UnsupportedOperationException();
	}

	public default void visit( final IntParam intArgument )
	{
		throw new UnsupportedOperationException();
	}

	public default void visit( final ChoiceParam choiceArgument )
	{
		throw new UnsupportedOperationException();
	}

	public default < E extends Enum< E > > void visit( final EnumParam< E > enumParam )
	{
		throw new UnsupportedOperationException();
	}

	public default void visit( final PathParam pathArgument )
	{
		throw new UnsupportedOperationException();
	}
}
