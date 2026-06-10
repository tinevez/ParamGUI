package org.scijava.ui.paramUI;

import org.scijava.ui.paramUI.Parameters.IntParam;
import org.scijava.ui.paramUI.visitors.gui.FrameBuilder;
import org.scijava.ui.paramUI.visitors.gui.FrameBuilder.UserTask;

public class DemoSimple
{

	public static void main( final String[] args )
	{
		final SimpleConfig config = new SimpleConfig();
		final UserTask task = ( p ) -> p.message( "Value = " + config.param.getValue() );
		FrameBuilder.build( config, task, new SimpleConfig() ).setVisible( true );
	}

	private static class SimpleConfig extends Configurator
	{

		private IntParam param;

		public SimpleConfig()
		{
			super( "Simple Config example", "A simple config demo." );
			this.param = addIntParameter()
					.key( "INT" )
					.defaultValue( 42 )
					.name( "Integer" )
					.help( "An integer parameter." )
					.min( 0 )
					.max( 100 )
					.get();
		}
	}
}
