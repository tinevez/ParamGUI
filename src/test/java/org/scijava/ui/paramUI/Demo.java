package org.scijava.ui.paramUI;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.scijava.Cancelable;
import org.scijava.command.Previewable;
import org.scijava.ui.paramUI.Parameters.BooleanParam;
import org.scijava.ui.paramUI.Parameters.DoubleParam;
import org.scijava.ui.paramUI.Parameters.EnumParam;
import org.scijava.ui.paramUI.Parameters.IntParam;
import org.scijava.ui.paramUI.Parameters.PathParam;
import org.scijava.ui.paramUI.visitors.Maps;
import org.scijava.ui.paramUI.visitors.Strings;
import org.scijava.ui.paramUI.visitors.gui.FrameBuilder;
import org.scijava.ui.paramUI.visitors.gui.FrameBuilder.ConfigFrame;
import org.scijava.ui.paramUI.visitors.gui.FrameBuilder.ConfigFrame.Progress;
import org.scijava.ui.paramUI.visitors.gui.FrameBuilder.UserTask;

/**
 * Demo with a UI that would configure Cellpose 3.
 */
public class Demo
{

	public static class Cellpose3Config extends Configurator
	{
		private final EnumParam< Cellpose3BuiltinModels > builtinModel;

		private final PathParam customModel;

		private final SelectableParameters builtinOrCustom;

		private final DoubleParam diameter;

		private IntParam chan1;

		private IntParam chan2;

		private DoubleParam flowThreshold;

		private DoubleParam cellprobThreshold;

		private IntParam minSize;

		private BooleanParam exportROIs;

		private BooleanParam exportLabels;

		private BooleanParam exportFlows;

		public Cellpose3Config( final Integer nChannels, final double pixelSize, final String units )
		{
			super( "Cellpose 3", "https://imagej.net/plugins/cellpose-appose#usage" );

			// Choice among an enum.
			this.builtinModel = addEnumParameter( Cellpose3BuiltinModels.class )
					.key( "BUILTIN_MODEL" )
					.defaultValue( Cellpose3BuiltinModels.CYTO3 )
					.name( "Builtin model" )
					.help( "https://cellpose.readthedocs.io/en/v3.1.1.1/models.html#full-built-in-models" )
					.get();

			// File path.
			this.customModel = addPathParameter()
					.key( "CUSTOM_MODEL_PATH" )
					.defaultValue( "" ) // Better than null.
					.name( "Path to custom model" )
					.help( "Path to a custom Cellpose 3 model. " )
					.get();

			// One or the other, but not both.
			this.builtinOrCustom = addSelectableParameters()
					.key( "BUILTIN_OR_CUSTOM" )
					.add( builtinModel )
					.add( customModel )
					.get();

			// Channels, two int params.
			this.chan1 = addIntParameter()
					.key( "CHAN1" )
					.name( "Main channel" )
					.help( "The main channel to segment. Select 0 to use a grayscale blend of all channels." )
					.defaultValue( 1 )
					.min( 0 )
					.max( nChannels )
					.get();
			this.chan2 = addIntParameter()
					.key( "CHAN2" )
					.name( "Optional channel" )
					.help( "The second channel to segment. Select 0 to skip using a second channel." )
					.defaultValue( 0 )
					.min( 0 )
					.max( nChannels )
					.get();

			// Diameter param is in pixel, but we want to display it in physical
			// units. So we set a translator that converts between the two.

			this.diameter = addDoubleParameter()
					.key( "DIAMETER" )
					.name( "Diameter" )
					.help( "<html>Estimated diameter of objects, in physical units "
							+ "(stored in pixel size internally). " +
							"Set to 0 to let Cellpose estimate it automatically.</html>" )
					.units( units )
					.defaultValue( 30. )
					.min( 0. ) // But no max
					.get();

			setDisplayTranslator( diameter, d -> d * pixelSize, d -> d / pixelSize );

			/*
			 * Advanced parameters.
			 */

			this.flowThreshold = addDoubleParameter()
					.key( "FLOW_THRESHOLD" )
					.name( "Flow threshold" )
					.help( "<html>Threshold for flow error filtering. Lower = more masks (permissive), Higher = fewer masks (strict).</html>" )
					.defaultValue( 0.4 )
					.min( 0. )
					.max( 3. )
					.get();

			this.cellprobThreshold = addDoubleParameter()
					.key( "CELPROB_THRESHOLD" )
					.name( "Cell probability threshold" )
					.help( "<html>Threshold for cell probability. Increase to filter low-confidence detections.</html>" )
					.defaultValue( 0.0 )
					.min( -6. )
					.max( 6. )
					.get();

			this.minSize = addIntParameter()
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
					.collapsed( true )
					.get();

			/*
			 * Export group.
			 */

			this.exportROIs = addFlag()
					.key( "EXPORT_ROIS" )
					.name( "Export ROIs" )
					.help( "If set, ROIs will be computed from the labels output and added to the input image." )
					.defaultValue( true )
					.get();

			this.exportLabels = addFlag()
					.key( "EXPORT_LABELS" )
					.name( "Export label image" )
					.help( "If set, the label image will be shown." )
					.defaultValue( false )
					.get();

			this.exportFlows = addFlag()
					.key( "EXPORT_FLOWS" )
					.name( "Export flows" )
					.help( "If set, the Cellpose flows will be shown as a 3-channel image" )
					.defaultValue( false )
					.get();

			addGroup( "Export options" )
					.add( exportROIs )
					.add( exportLabels )
					.add( exportFlows )
					.collapsed( false )
					.get();
		}
	}

	public static void main( final String[] args )
	{
		final int nChannels = 3;
		final double pixelSize = 0.2;
		final String units = "µm";

		final Cellpose3Config config = new Cellpose3Config( nChannels, pixelSize, units );

		config.builtinModel.set( Cellpose3BuiltinModels.CYTO3 );
		config.builtinOrCustom.select( config.builtinModel );
		config.chan1.set( 2 );
		config.chan2.set( 1 );
		config.diameter.set( 40. );

		System.out.println( "------------------------------" );
		System.out.println( "Original config" );
		System.out.println( "------------------------------" );
		System.out.println( config );
		System.out.println( "------------------------------" );

		System.out.println();
		System.out.println( "------------------------------" );
		System.out.println( "As a map:" );
		System.out.println( "------------------------------" );
		final Map< String, Object > map = Maps.toMap( config );
		map.forEach( ( k, v ) -> System.out.println( " - " + k + " -> " + v ) );
		System.out.println( "------------------------------" );

		// Modify the map.
		map.put( "CUSTOM_MODEL_PATH", "Trololo" );
		map.put( "BUILTIN_OR_CUSTOM", "CUSTOM_MODEL_PATH" );
		map.put( "CHAN2", 0 );

		// Re-read the map into a new config.
		final Cellpose3Config config2 = new Cellpose3Config( nChannels, pixelSize, units );
		Maps.fromMap( map, config2 );
		System.out.println();
		System.out.println( "------------------------------" );
		System.out.println( "After modifying the map" );
		System.out.println( "------------------------------" );
		System.out.println( Strings.echo( config2 ) );
		System.out.println( "------------------------------" );

		/*
		 * GUI
		 */

		final DummyRunner dummyRunner = new DummyRunner( config2 );
		final Configurator defaultValues = new Cellpose3Config( nChannels, pixelSize, units );

		final ConfigFrame frame = FrameBuilder.build( config2, dummyRunner, defaultValues );

		frame.setVisible( true );
	}

	private static class DummyRunner implements UserTask, Cancelable, Previewable
	{

		private final Cellpose3Config config;

		private final AtomicBoolean cancelRequested = new AtomicBoolean( false );

		private String cancelReason;

		public DummyRunner( final Cellpose3Config config )
		{
			this.config = config;
		}

		@Override
		public void run( final Progress p ) throws Exception
		{
			cancelRequested.set( false );
			p.indeterminate( false, "Preparing..." );
			Thread.sleep( 500 );
			final int steps = 20;
			for ( int i = 1; i <= steps; i++ )
			{
				if ( isCanceled() )
				{
					p.message( "Canceled:" + getCancelReason() );
					return;
				}
				Thread.sleep( 100 );
				p.set( i / ( double ) steps, "Running " + config.builtinModel.getValue() );
			}
			p.message( "Model run finished." );
		}

		@Override
		public boolean isCanceled()
		{
			return cancelRequested.get();
		}

		@Override
		public void cancel( final String reason )
		{
			this.cancelReason = reason;
			cancelRequested.set( true );
		}

		@Override
		public String getCancelReason()
		{
			return cancelReason;
		}

		@Override
		public void preview()
		{
			cancelRequested.set( false );
			System.out.println( "Previewing with config: " + config );
			try
			{
				Thread.sleep( 2500 );
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
			if ( cancelRequested.get() )
			{
				System.out.println( "Preview was canceled." );
				return;
			}
			System.out.println( "Preview done." );
		}

		@Override
		public void cancel()
		{
			cancelRequested.set( true );
			System.out.println( "Preview canceled." );
		}
	}
}
