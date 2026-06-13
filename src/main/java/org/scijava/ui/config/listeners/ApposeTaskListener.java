package org.scijava.ui.config.listeners;

import java.util.function.Consumer;

import org.apposed.appose.Builder.ProgressConsumer;
import org.apposed.appose.TaskEvent;
import org.scijava.ui.config.visitors.gui.FrameBuilder.ConfigFrame.Progress;

public interface ApposeTaskListener
{

	/**
	 * Implementation of ApposeTaskListener that writes messages to the standard
	 * output.
	 */
	public static final ApposeTaskListener STD = new StdApposeTaskListener();

	/**
	 * Implementation of ApposeTaskListener that does nothing.
	 */
	public static final ApposeTaskListener VOID = new VoidApposeTaskListener();

	/**
	 * Creates a new ApposeTaskListener that writes messages to the given
	 * Progress instance.
	 * 
	 * @param progress
	 *            the Progress instance to write messages to.
	 * @return a new ApposeTaskListener.
	 */
	public static ApposeTaskListener of( final Progress progress )
	{
		return new ProgressApposeListener( progress );
	}

	/**
	 * Returns a consumer that will be called with task events related to the
	 * execution of an Appose task, and that writes messages in the outputs
	 * defined in this class.
	 * 
	 * @return a new task event consumer.
	 */
	Consumer< TaskEvent > taskListener();

	/**
	 * Returns a consumer that will be called with output messages related to
	 * the downloading, installation and deployment of an Appose environment,
	 * and that writes messages in the outputs defined in this class.
	 * 
	 * @return a new output message consumer.
	 */
	Consumer< String > outputListener();

	/**
	 * Returns a consumer that will be called with error messages related to the
	 * the downloading, installation and deployment of an Appose environment,
	 * and that writes messages in the outputs defined in this class.
	 * <p>
	 * We have a small issue with current Appose: pixi and other managers always
	 * return an error message that says "✔ The cp4-cpu environment has been
	 * installed." when the environment is ready, even if it was already
	 * installed. So we need to filter out this message to avoid showing an
	 * error dialog.
	 * 
	 * @return a new error message consumer.
	 */
	default Consumer< String > errorListener()
	{
		return str -> {
			if ( str != null && str.contains( "✔ The" ) && str.contains( "environment has been installed." ) )
			{
				final String envName = str.substring( str.indexOf( "✔ The" ) + 5, str.indexOf( "environment" ) );
				message( "Python environment " + envName + " is ready." );
			}
			else
			{
				// Actual error.
				error( "ERROR: " + str );
			}
		};
	}

	/**
	 * Returns a consumer that will be called with progress updates related to
	 * the downloading, installation and deployment of an Appose environment,
	 * and that writes messages in the outputs defined in this class.
	 * 
	 * @return a new progress update consumer.
	 */
	ProgressConsumer progressListener();

	/**
	 * Writes a message to the outputs defined in this class.
	 * 
	 * @param msg
	 *            the message to write.
	 */
	void message( String msg );

	/**
	 * Writes an error message.
	 * 
	 * @param msg
	 *            the error message.
	 */
	void error( String msg );

	/**
	 * Implementation of ApposeTaskListener that does nothing.
	 */
	static class VoidApposeTaskListener implements ApposeTaskListener
	{

		@Override
		public Consumer< TaskEvent > taskListener()
		{
			return event -> {};
		}

		@Override
		public Consumer< String > outputListener()
		{
			return msg -> {};
		}

		@Override
		public Consumer< String > errorListener()
		{
			return msg -> {};
		}

		@Override
		public ProgressConsumer progressListener()
		{
			return ( t, c, m ) -> {};
		}

		@Override
		public void message( final String msg )
		{}

		@Override
		public void error( final String msg )
		{}
	}

	/**
	 * Implementation of ApposeTaskListener that writes messages to the standard
	 * output.
	 */
	static class StdApposeTaskListener implements ApposeTaskListener
	{
		@Override
		public Consumer< TaskEvent > taskListener()
		{
			return event -> {
				if ( event.message != null && !event.message.isEmpty() )
					message( event.responseType + " - " + event.message );
				if ( event.maximum > 0 )
					progress( ( double ) event.current / event.maximum );
			};
		}

		@Override
		public Consumer< String > outputListener()
		{
			return msg -> message( msg );
		}

		@Override
		public Consumer< String > errorListener()
		{
			return msg -> error( msg );
		}

		@Override
		public ProgressConsumer progressListener()
		{
			return ( t, c, m ) -> {
				message( t + ": " + String.format( "%.1f%%", ( double ) c / m ) );
			};
		}

		@Override
		public void message( final String msg )
		{
			System.out.println( msg );
		}

		@Override
		public void error( final String msg )
		{
			System.err.println( msg );
		}

		private void progress( final double d )
		{
			System.out.println( String.format( "%.1f%%", 100. * d ) );
		}
	}

	/**
	 * Implementation of ApposeTaskListener that writes messages to a Progress
	 * instance. Error messages are written to the standard error output.
	 */
	public static class ProgressApposeListener implements ApposeTaskListener
	{

		private final Progress progress;

		public ProgressApposeListener( final Progress progress )
		{
			this.progress = progress;
		}

		@Override
		public Consumer< TaskEvent > taskListener()
		{
			return event -> {
				if ( event.message != null && !event.message.isEmpty() )
					progress.message( event.responseType + " - " + event.message );
				if ( event.maximum > 0 )
					progress.set( ( double ) event.current / event.maximum );
			};
		}

		@Override
		public Consumer< String > outputListener()
		{
			return msg -> progress.message( msg );
		}

		/**
		 * Writes an error message. By default: to the standard error output.
		 * Override this method to change the behavior.
		 * 
		 * @param msg
		 *            the error message to write.
		 */
		@Override
		public void error( final String msg )
		{
			System.err.println( msg );
		}

		@Override
		public ProgressConsumer progressListener()
		{
			return ( t, c, m ) -> progress.set( ( double ) c / m, t );
		}

		@Override
		public void message( final String msg )
		{
			progress.message( msg );
		}
	}
}
