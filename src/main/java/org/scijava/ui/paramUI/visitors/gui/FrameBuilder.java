package org.scijava.ui.paramUI.visitors.gui;

import static org.scijava.ui.paramUI.utils.GuiUtils.isLikelyUrl;
import static org.scijava.ui.paramUI.utils.GuiUtils.openInBrowser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
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

import org.scijava.prefs.DefaultPrefService;
import org.scijava.ui.paramUI.Configurator;
import org.scijava.ui.paramUI.utils.EverythingDisablerAndReenabler;
import org.scijava.ui.paramUI.utils.Icons;
import org.scijava.ui.paramUI.visitors.Maps;
import org.scijava.ui.paramUI.visitors.Strings;
import org.scijava.ui.paramUI.visitors.gui.GuiBuilder.ConfigPanel;

/**
 * Mother class for building frames for Configurators. Builds a {@link JFrame}
 * with a UI that can configure the parameters in a Configurator.
 * <p>
 * The frame shows a config panel (created by {@link ConfigPanel}) and various
 * buttons to interact with the config or an action that is parameterized by the
 * config.
 * <p>
 * Can be subclassed to change the default callbacks for the buttons.
 */
public final class FrameBuilder< C extends Configurator >
{

	@FunctionalInterface
	public interface ProgressTask
	{
		void run( ConfigFrame.Progress progress ) throws Exception;

		default void cancel()
		{ /* no-op by default */ }
	}


	protected final C config;

	protected final ProgressTask onRunTask;

	protected final Runnable onStore;

	protected final Runnable onReload;

	protected final Runnable onReset;

	protected final Runnable onDisplay;

	protected final Runnable onStop;

	protected final ConfigFrame frame;

	/**
	 * Base constructor for FrameBuilder.
	 * 
	 * @param config
	 *            the config to build the frame for. This object will be
	 *            modified by the UI created by this FrameBuilder. If it has a
	 *            non-empty help string, a help button will be shown in the
	 *            frame.
	 * @param onRun
	 *            callback for the "Run" button. If <code>null</code> the button
	 *            will not be shown. This callback will be run in a separate
	 *            thread, and the UI will be disabled until it finished or is
	 *            stopped.
	 * @param onStop
	 *            callback for the "Stop" button. If not <code>null</code>,
	 *            after the user pressed the play button (which must be not
	 *            <code>null</code> as well), the "Run" button will be replaced
	 *            by a "Stop" button, which triggers this callback when pressed.
	 *            If <code>null</code>, no "Stop" button will be shown and the
	 *            "Run" button will remain as is.
	 * @param onStore
	 *            callback for the "Store" button, that serializes the current
	 *            config in the persistence system. If <code>null</code> the
	 *            button will not be shown.
	 * @param onReload
	 *            callback for the "Reload" button, that reloads the config from
	 *            the persistence system, overwriting any unsaved changes. If
	 *            <code>null</code> the button will not be shown.
	 * @param onReset
	 *            callback for the "Reset to default" button, that resets the
	 *            config to the default values. If <code>null</code> the button
	 *            will not be shown.
	 * @param onDisplay
	 *            callback for the "Display" button, that display the current
	 *            config in a human-readable form (e.g. in the console or a
	 *            popup). If <code>null</code> the button will not be shown.
	 */
	protected FrameBuilder(
			final C config,
			final ProgressTask onRunTask,
			final Runnable onStop,
			final Runnable onStore,
			final Runnable onReload,
			final Runnable onReset,
			final Runnable onDisplay )
	{
		this.config = config;
		this.onRunTask = onRunTask;
		this.onStop = onStop;
		this.onStore = onStore;
		this.onReload = onReload;
		this.onReset = onReset;
		this.onDisplay = onDisplay;

		this.frame = new ConfigFrame();

		frame.configPanel = GuiBuilder.build( config );
		final JPanel buttonPanel = buttonPanel();
		final JPanel south = new JPanel( new BorderLayout() );

		frame.progressBar = new JProgressBar( 0, 1000 );
		frame.progressBar.setStringPainted( true );
		frame.progressBar.setBorder( BorderFactory.createEmptyBorder( 4, 8, 8, 8 ) );
		frame.progressBar.setString( "" );
		// remember default color so we can restore it later
		frame.defaultProgressForeground = frame.progressBar.getForeground();

		frame.setTitle( config.getName() );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setLayout( new BorderLayout() );

		south.add( buttonPanel, BorderLayout.NORTH );
		south.add( frame.progressBar, BorderLayout.CENTER );

		frame.add( frame.configPanel, BorderLayout.CENTER );
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

		// Run and stop buttons
		if ( onRunTask != null )
		{
			frame.btnRun = flatButton( Icons.PLAY, "Run the plugin", runner() );
			frame.btnStop = flatButton( Icons.STOP, "Stop", stopper() );
			frame.btnStop.setVisible( false );

			frame.runStop = new JPanel( new java.awt.CardLayout() );
			frame.runStop.add( frame.btnRun, "RUN" );
			frame.runStop.add( frame.btnStop, "STOP" );
			row.add( frame.runStop );
		}

		// Store, reload and reset buttons
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

		// Display button
		if ( onDisplay != null )
		{
			final JButton btnDisplay = flatButton( Icons.COMMENT, "Display the current configuration", e -> onDisplay.run() );
			row.add( btnDisplay );
		}

		// Help text or link
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
		b.setText( null ); // icon-only
		b.putClientProperty( "JButton.buttonType", "toolBarButton" );
		return b;
	}

	protected ActionListener stopper()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( onStop != null )
				{
					frame.markCanceled( true );
					new Thread( () -> {
						try
						{
							frame.btnStop.setEnabled( false );
							onStop.run();
						}
						finally
						{
							if ( frame.disabler.disableHasBeenCalled() )
								frame.disabler.reenable();
							frame.btnStop.setEnabled( true );
							frame.btnRun.setVisible( true );
							frame.btnStop.setVisible( false );
							( ( CardLayout ) frame.runStop.getLayout() ).show( frame.runStop, "RUN" );
						}
					}, "FrameBuilderOnStopThread" ).start();
				}
			}
		};
	}

	/**
	 * Creates an ActionListener for the "Run" button.
	 */
	protected ActionListener runner()
	{
		return new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				frame.disabler.disable();
				frame.btnRun.setVisible( false );
				if ( frame.btnStop != null )
				{
					frame.btnStop.setVisible( true );
					frame.btnStop.setEnabled( true );
				}
				frame.markCanceled( false );
				new Thread( () -> {
					try
					{
						( ( CardLayout ) frame.runStop.getLayout() ).show( frame.runStop, "STOP" );
						onRunTask.run( frame.getProgress() );
					}
					catch ( final Exception e1 )
					{
						e1.printStackTrace();
					}
					finally
					{
						( ( CardLayout ) frame.runStop.getLayout() ).show( frame.runStop, "RUN" );
						frame.btnRun.setVisible( true );
						if ( frame.btnStop != null )
							frame.btnStop.setVisible( false );
						if ( frame.disabler.disableHasBeenCalled() )
							frame.disabler.reenable();
					}
				}, "FrameBuilderOnRunThread" ).start();
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
		// Plain text, wrapped, scrollable as needed
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

		// Cap preferred height; width will follow container
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

		// Throttling parameters - 50 ms
		private static final long PROGRESS_MIN_UPDATE_NANOS = 50_000_000L;

		private static final double PROGRESS_MIN_DELTA = 0.01; // 1%

		private long lastProgressUpdateNanos = 0L;

		private double lastProgressValue = Double.NaN;

		public Color defaultProgressForeground;

		public JProgressBar progressBar;

		public JPanel runStop;

		private static final long serialVersionUID = 1L;

		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( this,
				new Class[] { JLabel.class, JProgressBar.class } );

		public ConfigPanel configPanel;

		public JButton btnStop;

		public JButton btnRun;

		/**
		 * Update progress (0..1). Throttled by time and value delta.
		 * 
		 * @param fraction
		 */
		public void setProgress( final double fraction )
		{
			setProgress( fraction, null );
		}

		public void setProgress( final double fraction, final String text )
		{
			final double f = Math.max( 0d, Math.min( 1d, fraction ) );
			final long now = System.nanoTime();
			final boolean largeJump = Double.isNaN( lastProgressValue )
					|| Math.abs( f - lastProgressValue ) >= PROGRESS_MIN_DELTA
					|| f == 0d || f == 1d;
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

		/**
		 * Switch to/from indeterminate mode, optionally updating displayed
		 * text.
		 * 
		 * @param indeterminate
		 * @param text
		 */
		public void setProgressIndeterminate( final boolean indeterminate, final String text )
		{
			SwingUtilities.invokeLater( () -> {
				progressBar.setIndeterminate( indeterminate );
				if ( text != null )
					progressBar.setString( text );
			} );
		}

		/**
		 * Clear progress and text; restore default color.
		 */
		public void clearProgress()
		{
			lastProgressValue = Double.NaN;
			lastProgressUpdateNanos = 0L;
			SwingUtilities.invokeLater( () -> {
				progressBar.setIndeterminate( false );
				progressBar.setValue( 0 );
				progressBar.setString( null );
				if ( defaultProgressForeground != null )
					progressBar.setForeground( defaultProgressForeground );
			} );
		}

		/**
		 * Show a status message inside the progress bar (default color).
		 * 
		 * @param message
		 */
		public void setStatusMessage( final String message )
		{
			setStatusMessage( message, null );
		}

		/**
		 * Show a status message with an optional custom color (applies to
		 * bar/text depending on LAF).
		 */
		public void setStatusMessage( final String message, final Color color )
		{
			SwingUtilities.invokeLater( () -> {
				progressBar.setString( message == null ? null : message );
				if ( color != null )
					progressBar.setForeground( color );
				else if ( defaultProgressForeground != null )
					progressBar.setForeground( defaultProgressForeground );
			} );
		}

		public interface Progress
		{
			/** 0 to 1. */
			void set( double fraction ); // 0..1

			void set( double fraction, String text );

			void indeterminate( boolean on, String text );

			void message( String text );

			void message( String text, Color color );

			void clear();

			boolean isCanceled();
		}

		private final AtomicBoolean canceled = new AtomicBoolean( false );

		void markCanceled( final boolean v )
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
			public void message( final String t, final java.awt.Color c )
			{
				setStatusMessage( t, c );
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

		public Progress getProgress()
		{
			return progress;
		}
	}

	/**
	 * Default build for a FrameBuilder specifying all the callbacks.
	 * 
	 * @param config
	 *            the config to build the frame for. This object will be
	 *            modified by the UI created by this FrameBuilder. If it has a
	 *            non-empty help string, a help button will be shown in the
	 *            frame.
	 * @param onRun
	 *            callback for the "Run" button. If <code>null</code> the button
	 *            will not be shown. This callback will be run in a separate
	 *            thread, and the UI will be disabled until it finished or is
	 *            stopped.
	 * @param onStop
	 *            callback for the "Stop" button. If not <code>null</code>,
	 *            after the user pressed the play button (which must be not
	 *            <code>null</code> as well), the "Run" button will be replaced
	 *            by a "Stop" button, which triggers this callback when pressed.
	 *            If <code>null</code>, no "Stop" button will be shown and the
	 *            "Run" button will remain as is.
	 * @param onStore
	 *            callback for the "Store" button, that serializes the current
	 *            config in the persistence system. If <code>null</code> the
	 *            button will not be shown.
	 * @param onReload
	 *            callback for the "Reload" button, that reloads the config from
	 *            the persistence system, overwriting any unsaved changes. If
	 *            <code>null</code> the button will not be shown.
	 * @param onReset
	 *            callback for the "Reset to default" button, that resets the
	 *            config to the default values. If <code>null</code> the button
	 *            will not be shown.
	 * @param onDisplay
	 *            callback for the "Display" button, that display the current
	 *            config in a human-readable form (e.g. in the console or a
	 *            popup). If <code>null</code> the button will not be shown.
	 * @return a new ConfigFrame with the specified config and callbacks.
	 */
	public static < C extends Configurator > ConfigFrame build(
			final C config,
			final ProgressTask onRun,
			final Runnable onStop,
			final Runnable onStore,
			final Runnable onReload,
			final Runnable onReset,
			final Runnable onDisplay )
	{
		return new FrameBuilder<>( config, onRun, onStop, onStore, onReload, onReset, onDisplay ).get();
	}

	public static < C extends Configurator > ConfigFrame build(
			final C config,
			final ProgressTask onRun,
			final Runnable onStop,
			final C defaultValues )
	{
		// Work around referencing the config panel for refresh.
		final AtomicReference< ConfigPanel > ref = new AtomicReference<>();
		final Runnable refresh = () -> ref.get().refresh();
		final ConfigFrame frame = build(
				config,
				onRun,
				onStop,
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
			// Switch according to v type.
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
				// Store the name of the enum constant.
				prefs.put( config.getClass(), k, ( ( Enum< ? > ) v ).name() );
			}
		} );
	}

	private static < C extends Configurator > void defaultReload( final C config, final Runnable refresh )
	{
		final DefaultPrefService prefs = new DefaultPrefService();
		// Map that will be deserialized to.
		final Map< String, Object > map = new HashMap<>();
		// Loop over param we know of.
		Maps.toMap( config ).forEach( ( k, v ) -> {
			// Switch according to v type.
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
		// Apply deserialized values to config.
		Maps.fromMap( map, config );
		// Refresh UI to reflect any changes.
		refresh.run();
	}
}
