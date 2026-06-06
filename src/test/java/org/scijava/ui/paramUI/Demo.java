package org.scijava.ui.paramUI;

import org.scijava.ui.paramUI.Parameters.DoubleParam;
import org.scijava.ui.paramUI.Parameters.EnumParam;
import org.scijava.ui.paramUI.Parameters.IntParam;
import org.scijava.ui.paramUI.Parameters.PathParam;

/**
 * Demo with a UI that would configure Cellpose 3.
 */
public class Demo
{

	public static class Cellpose3Config extends Configurator
	{
		private final EnumParam< Cellpose3BuiltinModels > builtinModel;

		private final PathParam customModel;

		private final SelectableArguments builtinOrCustom;

		private final DoubleParam diameter;

		private IntParam chan1;

		private IntParam chan2;

		private DoubleParam flowThreshold;

		private DoubleParam cellprobThreshold;

		private IntParam minSize;

		public Cellpose3Config()
		{
			super( "Cellpose 3", "https://imagej.net/plugins/cellpose-appose#usage" );

			// Choice among an enum.
			this.builtinModel = addEnumArgument( Cellpose3BuiltinModels.class )
					.key( "BUILTIN_MODEL" )
					.defaultValue( Cellpose3BuiltinModels.CYTO3 )
					.name( "Builtin model" )
					.get();

			// File path.
			this.customModel = addPathArgument()
					.key( "CUSTOM_MODEL_PATH" )
					.name( "Path to custom model" )
					.help( "Path to a custom Cellpose 3 model. " )
					.get();

			// One or the other, but not both.
			this.builtinOrCustom = addSelectableArguments()
					.add( builtinModel )
					.add( customModel );

			// Channels, two int params.
			final int nChannels = 3;

			this.chan1 = addIntArgument()
					.key( "CHAN1" )
					.name( "Main channel" )
					.help( "The main channel to segment. Select 0 to use a grayscale blend of all channels." )
					.defaultValue( 1 )
					.min( 0 )
					.max( nChannels )
					.get();
			this.chan2 = addIntArgument()
					.key( "CHAN2" )
					.name( "Optional channel" )
					.help( "The second channel to segment. Select 0 to skip using a second channel." )
					.defaultValue( 0 )
					.min( 0 )
					.max( nChannels )
					.get();

			// Diameter param is in pixel, but we want to display it in physical
			// units. So we set a translator that converts between the two.

			final double pixelSize = 0.2;
			final String units = "µm";

			this.diameter = addDoubleArgument()
					.key( "DIAMETER" )
					.name( "Diameter" )
					.units( units )
					.defaultValue( 30. )
					.min( 0. ) // But no max
					.get();

			setDisplayTranslator( diameter, d -> d * pixelSize, d -> d / pixelSize );

			/*
			 * Advanced parameters.
			 */

			this.flowThreshold = addDoubleArgument()
					.key( "FLOW_THRESHOLD" )
					.name( "Flow threshold" )
					.help( "<html>Threshold for flow error filtering. Lower = more masks (permissive), Higher = fewer masks (strict).</html>" )
					.defaultValue( 0.4 )
					.min( 0. )
					.max( 3. )
					.get();

			this.cellprobThreshold = addDoubleArgument()
					.key( "CELPROB_THRESHOLD" )
					.name( "Cell probability threshold" )
					.help( "<html>Threshold for cell probability. Increase to filter low-confidence detections.</html>" )
					.defaultValue( 0.0 )
					.min( -6. )
					.max( 6. )
					.get();

			this.minSize = addIntArgument()
					.key( "MIN_SIZE" )
					.name( "Minimum size" )
					.help( "Objects smaller than this are removed." )
					.defaultValue( 15 )
					.min( 0 )
					.units( "pixels" )
					.get();

			addGroup( "Advanced parameters" )
					.add( flowThreshold )
					.add( cellprobThreshold )
					.add( minSize )
					.visible( false )
					.get();
		}
	}

	public static void main( final String[] args )
	{
		final Cellpose3Config config = new Cellpose3Config();

		config.builtinModel.set( Cellpose3BuiltinModels.CYTO3 );
		config.builtinOrCustom.select( config.builtinModel );
		config.chan1.set( 2 );
		config.chan2.set( 1 );
		config.diameter.set( 40. );

		System.out.println( config );
	}

}
