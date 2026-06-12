package org.scijava.ui.config.visitors.gui;

import static org.scijava.ui.config.utils.GuiUtils.isLikelyUrl;
import static org.scijava.ui.config.utils.GuiUtils.openInBrowser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.scijava.Cancelable;
import org.scijava.command.Previewable;
import org.scijava.prefs.DefaultPrefService;
import org.scijava.ui.config.Configurator;
import org.scijava.ui.config.utils.EverythingDisablerAndReenabler;
import org.scijava.ui.config.utils.Icons;
import org.scijava.ui.config.visitors.Maps;
import org.scijava.ui.config.visitors.Strings;
import org.scijava.ui.config.visitors.gui.GuiBuilder.ConfigPanel;

public final class FrameBuilder< C extends Configurator >
{
	@FunctionalInterface
	public interface UserTask
	{
		void run( ConfigFrame.Progress progress ) throws Exception;
	}

	protected final C config;

	protected final UserTask task;

	protected final Runnable onStore;

	protected final Runnable onReload;

	protected final Runnable onReset;

	protected final Runnable onDisplay;

	protected final ConfigFrame frame;

	protected FrameBuilder(
			final C config,
			final UserTask task,
			final Runnable onStore,
			final Runnable onReload,
			final Runnable onReset,
			final Runnable onDisplay )
	{
		this.config = config;
		this.task = task;
		this.onStore = onStore;
		this.onReload = onReload;
		this.onReset = onReset;
		this.onDisplay = onDisplay;

		this.frame = new ConfigFrame();

		frame.configPanel = GuiBuilder.build( config );
		final JPanel buttonPanel = buttonPanel();

		frame.setTitle( config.getName() );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setLayout( new BorderLayout() );

		frame.add( frame.configPanel, BorderLayout.CENTER );

		final JPanel south = new JPanel( new BorderLayout() );
		south.add( buttonPanel, BorderLayout.NORTH );

		frame.progressBar = new JProgressBar( 0, 1000 );
		frame.progressBar.setStringPainted( true );
		frame.progressBar.setString( "" );
		frame.progressBar.putClientProperty( "JComponent.sizeVariant", "large" );
		final int desiredHeight = 24;
		frame.progressBar.setPreferredSize( new Dimension( 10, desiredHeight ) );
		frame.progressBar.setMinimumSize( new Dimension( 10, desiredHeight ) );

		frame.progressBar.setBorder( BorderFactory.createEmptyBorder( 4, 8, 8, 8 ) );
		south.add( frame.progressBar, BorderLayout.CENTER );

		frame.add( south, BorderLayout.PAGE_END );

		frame.pack();
		frame.setLocationByPlatform( true );
	}

	public ConfigFrame get()
	{
		return frame;
	}

	protected JPanel buttonPanel()
	{
		final JPanel row = new JPanel( new GridLayout( 1, 0, 0, 0 ) );
		row.setOpaque( false );

		if ( task != null )
		{
			final boolean cancelable = ( task instanceof Cancelable );
			final boolean previewable = ( task instanceof Previewable );

			frame.btnRun = flatButton( Icons.PLAY, "Run", runner() );

			if ( cancelable )
			{
				frame.btnStop = flatButton( Icons.STOP, "Stop", stopper() );
				frame.btnStop.setVisible( false );

				frame.runStop = new JPanel( new java.awt.CardLayout() );
				frame.runStop.add( frame.btnRun, "RUN" );
				frame.runStop.add( frame.btnStop, "STOP" );
				row.add( frame.runStop );
			}
			else
			{
				row.add( frame.btnRun );
			}

			if ( previewable )
			{
				frame.btnPreview = flatButton( Icons.PREVIEW, "Preview", previewer() );
				frame.btnStopPreview = flatButton( Icons.STOP, "Stop preview", previewStopper() );
				frame.btnStopPreview.setVisible( false );
				frame.previewRunStop = new JPanel( new CardLayout() );
				frame.previewRunStop.add( frame.btnPreview, "PREVIEW" );
				frame.previewRunStop.add( frame.btnStopPreview, "STOP_PREVIEW" );

				row.add( frame.previewRunStop );
			}
		}

		if ( onStore != null )
		{
			final JButton btnStore = flatButton( Icons.STORE, "Store the current configuration", e -> onStore.run() );
			row.add( btnStore );
		}

		if ( onReload != null )
		{
			final JButton btnReload = flatButton( Icons.RELOAD, "Reload the configuration", e -> onReload.run() );
			row.add( btnReload );
		}

		if ( onReset != null )
		{
			final JButton btnReset = flatButton( Icons.RESET, "Reset to default values", e -> onReset.run() );
			row.add( btnReset );
		}

		if ( onDisplay != null )
		{
			final JButton btnDisplay = flatButton( Icons.COMMENT, "Display the current configuration", e -> onDisplay.run() );
			row.add( btnDisplay );
		}

		final String help = config.getHelp();
		if ( help != null && !help.trim().isEmpty() )
		{
			final JButton btnHelp = flatButton( Icons.HELP, "Show help", e -> showHelp( help ) );
			row.add( btnHelp );
		}

		return row;
	}

	private static JButton flatButton( final Icon icon, final String tip, final ActionListener al )
	{
		final JButton b = new JButton( icon );
		if ( tip != null )
			b.setToolTipText( tip );
		if ( al != null )
			b.addActionListener( al );
		b.setText( null );
		b.putClientProperty( "JButton.buttonType", "toolBarButton" );
		return b;
	}

	protected ActionListener stopper()
	{
		return e -> {
			// Signal cancellation to the task
			frame.markCanceled( true );

			// Notify the task it should cancel
			if ( task instanceof Cancelable )
			{
				try
				{
					( ( Cancelable ) task ).cancel( "User canceled" );
				}
				catch ( final Throwable t )
				{
					t.printStackTrace();
				}
			}

			// Disable the button - the task will handle stopping
			frame.btnStop.setEnabled( false );
		};
	}

	protected ActionListener runner()
	{
		return e -> {
			frame.disabler.disable();
			frame.markCanceled( false );

			final boolean cancelable = task instanceof Cancelable;
			if ( cancelable )
			{
				frame.btnRun.setVisible( false );
				frame.btnStop.setVisible( true );
				frame.btnStop.setEnabled( true );
				SwingUtilities.invokeLater( () -> ( ( CardLayout ) frame.runStop.getLayout() ).show( frame.runStop, "STOP" ) );
			}
			else
			{
				frame.btnRun.setEnabled( false );
			}

			final Thread t = new Thread( () -> {
				Throwable error = null;
				try
				{
					if ( task != null )
						task.run( frame.getProgress() );
				}
				catch ( final Throwable ex )
				{
					error = ex;
				}
				finally
				{
					final Throwable err = error;
					SwingUtilities.invokeLater( () -> {
						if ( task instanceof Cancelable && ( ( Cancelable ) task ).isCanceled() )
						{
							// Was canceled by the user - task handled it
						}
						else if ( err != null )
						{
							// An unexpected error occurred
							frame.progressBar.setString( "Failed: " + err.getMessage() );
							err.printStackTrace();
						}
						else
						{
							// Completed successfully
						}

						if ( cancelable )
						{
							( ( CardLayout ) frame.runStop.getLayout() ).show( frame.runStop, "RUN" );
							frame.btnRun.setVisible( true );
							if ( frame.btnStop != null )
								frame.btnStop.setVisible( false );
						}
						else
						{
							frame.btnRun.setEnabled( true );
						}

						if ( frame.disabler.disableHasBeenCalled() )
							frame.disabler.reenable();
					} );
				}
			}, "FrameBuilderRunThread" );
			t.start();
		};
	}

	protected ActionListener previewer()
	{
		return e -> {
			if ( !( task instanceof Previewable ) )
				return;

			frame.disabler.disable();
			final Previewable previewable = ( Previewable ) task;

			if ( frame.previewRunStop != null && frame.btnStopPreview != null && frame.btnPreview != null )
			{
				frame.btnPreview.setVisible( false );
				frame.btnStopPreview.setVisible( true );
				frame.btnStopPreview.setEnabled( true );
				( ( CardLayout ) frame.previewRunStop.getLayout() ).show( frame.previewRunStop, "STOP_PREVIEW" );
			}
			else if ( frame.btnPreview != null )
			{
				frame.btnPreview.setEnabled( false );
			}

			// In previewer() action listener — we ARE on the EDT here
			frame.progressBar.setIndeterminate( false );
			frame.progressBar.setValue( frame.progressBar.getMaximum() );
			frame.progressBar.paintImmediately( frame.progressBar.getBounds() );

			// Defer setIndeterminate(true) to NEXT EDT cycle so native renders value=max first
			SwingUtilities.invokeLater( () -> {
				frame.progressBar.setIndeterminate( true );
				frame.progressBar.setString( "Preview…" );
			} );

			final Thread t = new Thread( () -> {
				try
				{
					previewable.preview();

					SwingUtilities.invokeLater( () -> {
						frame.progressBar.setIndeterminate( false );
						frame.progressBar.setString( "Preview done" );
					} );
				}
				catch ( final Throwable ex )
				{
					SwingUtilities.invokeLater( () -> {
						frame.progressBar.setIndeterminate( false );
						frame.progressBar.setString( "Preview failed: " + ex.getMessage() );
						ex.printStackTrace();
					} );
				}
				finally
				{
					SwingUtilities.invokeLater( () -> {
						if ( frame.previewRunStop != null && frame.btnStopPreview != null && frame.btnPreview != null )
						{
							( ( CardLayout ) frame.previewRunStop.getLayout() ).show( frame.previewRunStop, "PREVIEW" );
							frame.btnPreview.setVisible( true );
							frame.btnStopPreview.setVisible( false );
						}
						else if ( frame.btnPreview != null )
						{
							frame.btnPreview.setEnabled( true );
						}

						if ( frame.disabler.disableHasBeenCalled() )
							frame.disabler.reenable();
					} );
				}
			}, "FrameBuilderPreviewThread" );
			t.start();
		};
	}

	protected ActionListener previewStopper()
	{
		return e -> {
			if ( !( task instanceof Previewable ) )
				return;

			if ( frame.btnStopPreview != null )
				frame.btnStopPreview.setEnabled( false );

			// Signal cancellation to the preview task
			final Previewable previewable = ( Previewable ) task;
			try
			{
				previewable.cancel();
			}
			catch ( final Throwable t )
			{
				t.printStackTrace();
			}

			// If the task is also Cancelable, notify it as well
			if ( task instanceof Cancelable )
			{
				try
				{
					( ( Cancelable ) task ).cancel( "User canceled preview" );
				}
				catch ( final Throwable t )
				{
					t.printStackTrace();
				}
			}
		};
	}

	protected void showHelp( final String help )
	{
		final String text = help.trim();
		if ( isLikelyUrl( text ) )
		{
			openInBrowser( text, frame );
			return;
		}
		showHelpText( help );
	}

	protected void showHelpText( final String helpText )
	{
		final JTextArea ta = new JTextArea( helpText, 5, 40 );
		ta.setEditable( false );
		ta.setLineWrap( true );
		ta.setWrapStyleWord( true );
		ta.setOpaque( false );
		ta.setBorder( BorderFactory.createEmptyBorder() );
		ta.setFont( UIManager.getFont( "Label.font" ) );

		final JScrollPane sp = new JScrollPane(
				ta,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		sp.setBorder( BorderFactory.createEmptyBorder() );
		sp.getViewport().setOpaque( false );
		sp.setOpaque( false );

		ta.setSize( new Dimension( 420, Integer.MAX_VALUE ) );
		final Dimension pref = ta.getPreferredSize();
		final int maxHeight = 180;
		sp.setPreferredSize( new Dimension( Math.min( pref.width, 600 ), Math.min( pref.height, maxHeight ) ) );

		final JPanel panel = new JPanel( new BorderLayout() );
		panel.add( sp, BorderLayout.CENTER );
		panel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		final JFrame helpFrame = new JFrame( "Help: " + config.getName() );
		helpFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		helpFrame.setLayout( new BorderLayout() );
		helpFrame.add( panel, BorderLayout.CENTER );
		helpFrame.pack();
		helpFrame.setLocationRelativeTo( frame );
		helpFrame.setVisible( true );
	}

	public static class ConfigFrame extends JFrame
	{
		public JPanel previewRunStop;

		public JButton btnStopPreview;

		public interface Progress
		{
			void set( double fraction );

			void set( double fraction, String text );

			void indeterminate( boolean on, String text );

			void message( String text );

			void clear();

			boolean isCanceled();
		}

		public JPanel runStop;

		private static final long serialVersionUID = 1L;

		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class, JProgressBar.class } );

		public ConfigPanel configPanel;

		public JButton btnStop;

		public JButton btnRun;

		public JButton btnPreview;

		public JProgressBar progressBar;

		private final AtomicBoolean canceled = new AtomicBoolean( false );

		public void markCanceled( final boolean v )
		{
			canceled.set( v );
		}

		private final Progress progress = new Progress()
		{
			@Override
			public void set( final double f )
			{
				setProgress( f );
			}

			@Override
			public void set( final double f, final String t )
			{
				setProgress( f, t );
			}

			@Override
			public void indeterminate( final boolean on, final String t )
			{
				setProgressIndeterminate( on, t );
			}

			@Override
			public void message( final String t )
			{
				setStatusMessage( t );
			}

			@Override
			public void clear()
			{
				clearProgress();
			}

			@Override
			public boolean isCanceled()
			{
				return canceled.get();
			}
		};

		private static final long PROGRESS_MIN_UPDATE_NANOS = 50_000_000L;

		private static final double PROGRESS_MIN_DELTA = 0.01;

		private long lastProgressUpdateNanos = 0L;

		private double lastProgressValue = Double.NaN;

		public Progress getProgress()
		{
			return progress;
		}

		public void setProgress( final double fraction )
		{
			setProgress( fraction, null );
		}

		public void setProgress( final double fraction, final String text )
		{
			final double f = Math.max( 0d, Math.min( 1d, fraction ) );
			final long now = System.nanoTime();
			final boolean largeJump = Double.isNaN( lastProgressValue ) || Math.abs( f - lastProgressValue ) >= PROGRESS_MIN_DELTA || f == 0d || f == 1d;
			final boolean timeOk = now - lastProgressUpdateNanos >= PROGRESS_MIN_UPDATE_NANOS;
			if ( !( largeJump || timeOk ) )
				return;
			lastProgressValue = f;
			lastProgressUpdateNanos = now;
			SwingUtilities.invokeLater( () -> {
				if ( progressBar.isIndeterminate() )
					progressBar.setIndeterminate( false );
				progressBar.setValue( ( int ) Math.round( f * progressBar.getMaximum() ) );
				if ( text != null )
					progressBar.setString( text );
			} );
		}

		public void setProgressIndeterminate( final boolean indeterminate, final String text )
		{
			SwingUtilities.invokeLater( () -> {
				progressBar.setIndeterminate( indeterminate );
				if ( text != null )
					progressBar.setString( text );
			} );
		}

		public void clearProgress()
		{
			lastProgressValue = Double.NaN;
			lastProgressUpdateNanos = 0L;
			SwingUtilities.invokeLater( () -> {
				progressBar.setIndeterminate( false );
				progressBar.setValue( 0 );
				progressBar.setString( null );
			} );
		}

		public void setStatusMessage( final String message )
		{
			SwingUtilities.invokeLater( () -> {
				progressBar.setString( message == null ? null : message );
			} );
		}
	}

	public static < C extends Configurator > ConfigFrame build(
			final C config,
			final UserTask task,
			final Runnable onStore,
			final Runnable onReload,
			final Runnable onReset,
			final Runnable onDisplay )
	{
		return new FrameBuilder<>( config, task, onStore, onReload, onReset, onDisplay ).get();
	}

	public static < C extends Configurator > ConfigFrame build(
			final C config,
			final UserTask task,
			final C defaultValues )
	{
		final AtomicReference< ConfigPanel > ref = new AtomicReference<>();
		final Runnable refresh = () -> ref.get().refresh();
		final ConfigFrame frame = build(
				config,
				task,
				() -> defaultStore( config ),
				() -> defaultReload( config, refresh ),
				() -> defaultReset( config, defaultValues, refresh ),
				() -> defaultDisplay( config ) );
		ref.set( frame.configPanel );
		return frame;
	}

	private static < C extends Configurator > void defaultDisplay( final C config )
	{
		System.out.println( Strings.echo( config ) );
	}

	private static < C extends Configurator > void defaultReset( final C config, final C defaultValues, final Runnable refresh )
	{
		final Map< String, Object > defaultMap = Maps.toMap( defaultValues );
		Maps.fromMap( defaultMap, config );
		refresh.run();
	}

	private static < C extends Configurator > void defaultStore( final C config )
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

	private static < C extends Configurator > void defaultReload( final C config, final Runnable refresh )
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
		refresh.run();
	}
}
