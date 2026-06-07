package org.scijava.ui.paramUI.gui;

import static org.scijava.ui.paramUI.gui.elements.StyleElements.SMALL_FONT;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.booleanElement;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.boundedDoubleElement;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.doubleElement;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.enumElement;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.intElement;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedCheckBox;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedComboBoxEnumSelector;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedComboBoxSelector;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedFormattedTextField;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedSliderPanel;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedSpinner;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.linkedTextField;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.listElement;
import static org.scijava.ui.paramUI.gui.elements.StyleElements.stringElement;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.scijava.ui.paramUI.Configurator;
import org.scijava.ui.paramUI.Configurator.SelectableParameters;
import org.scijava.ui.paramUI.ParameterVisitor;
import org.scijava.ui.paramUI.Parameters;
import org.scijava.ui.paramUI.Parameters.BooleanParam;
import org.scijava.ui.paramUI.Parameters.ChoiceParam;
import org.scijava.ui.paramUI.Parameters.DoubleParam;
import org.scijava.ui.paramUI.Parameters.EnumParam;
import org.scijava.ui.paramUI.Parameters.IntParam;
import org.scijava.ui.paramUI.Parameters.Parameter;
import org.scijava.ui.paramUI.Parameters.PathParam;
import org.scijava.ui.paramUI.Parameters.StringParam;
import org.scijava.ui.paramUI.gui.elements.BoundedValue;
import org.scijava.ui.paramUI.gui.elements.BoundedValue.UpdateListener;
import org.scijava.ui.paramUI.gui.elements.StyleElements.BooleanElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.BoundedDoubleElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.DoubleElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.EnumElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.IntElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.ListElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.StringElement;
import org.scijava.ui.paramUI.gui.elements.StyleElements.StyleElement;
import org.scijava.ui.paramUI.utils.FileChooser;
import org.scijava.ui.paramUI.utils.FileChooser.DialogType;

public class GuiBuilder implements ParameterVisitor
{

	private static final int tfCols = 4;

	private final ConfigPanel panel;

	private final GridBagConstraints c;

	private int topInset = 5;

	private int bottomInset = 5;

	private final Map< Parameter< ?, ? >, Function< ?, ? > > forwardUITranslators;

	private final Map< Parameter< ?, ? >, Function< ?, ? > > backwardUITranslators;

	private GuiBuilder( final Configurator config )
	{
		this.forwardUITranslators = config.getForwardUITranslators();
		this.backwardUITranslators = config.getBackwardUITranslators();
		this.panel = new ConfigPanel();
		final GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] { 0., 1., 0. };
		panel.setLayout( layout );
		panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		this.c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
	}

	private void setCurrentRadioButton( final JRadioButton radioButton )
	{
		if ( radioButton == null || ( panel.rdbtn != radioButton ) )
		{
			topInset = 5;
			bottomInset = 5;
		}
		else
		{
			topInset = 0;
			bottomInset = 0;
		}
		panel.rdbtn = radioButton;
	}

	/*
	 * Parameter VISITOR.
	 */

	@Override
	public void visit( final BooleanParam flag )
	{
		final BooleanElement element = booleanElement( flag.getName(), flag::getValue, flag::set );
		panel.elements.put( flag.getKey(), element );
		final JCheckBox checkbox = linkedCheckBox( element, "" );
		checkbox.setHorizontalAlignment( SwingConstants.LEADING );
		addToLayout(
				flag.getHelp(),
				new JLabel( element.getLabel() ),
				checkbox,
				flag );
	}

	@Override
	public void visit( final IntParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< Integer, Integer > forward = ( Function< Integer, Integer > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< Integer, Integer > backward = ( Function< Integer, Integer > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final IntSupplier valueGetter = () -> {
			final int value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< Integer > valueSetter = ( v ) -> {
			final int value = backward.apply( v );
			arg.set( value );
		};

		// Different UI if there are bounds.
		final JComponent comp;
		if ( arg.hasMin() && arg.hasMax() )
		{
			// JSlider
			final int min = forward.apply( arg.getMin() );
			final int max = forward.apply( arg.getMax() );
			final IntElement element = intElement( arg.getName(), min, max, valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );

			final int largest = Math.max( Math.abs( min ), Math.abs( max ) );
			final String numberString = String.valueOf( largest );
			final int numberOfColumns = numberString.length() + 1;
			comp = linkedSliderPanel( element, numberOfColumns );
		}
		else
		{
			// JSpinner
			final int tmin = arg.hasMin() ? arg.getMin() : Integer.MIN_VALUE;
			final int tmax = arg.hasMax() ? arg.getMax() : Integer.MAX_VALUE;
			final int min= forward.apply( tmin );
			final int max = forward.apply( tmax );
			final IntElement element = intElement( arg.getName(), min, max, valueGetter, valueSetter );
			comp = linkedSpinner( element );
		}
		addToLayout(
				arg.getHelp(),
				new JLabel( arg.getName() ),
				comp,
				arg.getUnits(),
				arg );
	}

	@Override
	public void visit( final DoubleParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< Double, Double > forward = ( Function< Double, Double > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< Double, Double > backward = ( Function< Double, Double > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final DoubleSupplier valueGetter = () -> {
			final double value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< Double > valueSetter = ( v ) -> {
			final double value = backward.apply( v );
			arg.set( value );
		};

		if ( arg.hasMin() && arg.hasMax() )
		{
			final double min = forward.apply( arg.getMin() );
			final double max = forward.apply( arg.getMax() );
			final BoundedDoubleElement element = boundedDoubleElement( arg.getName(),
					min, max, valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );
			addToLayout(
					arg.getHelp(),
					new JLabel( element.getLabel() ),
					linkedSliderPanel( element, tfCols, arg.getMax() / 50 ),
					arg.getUnits(),
					arg );
		}
		else
		{
			final DoubleElement element = doubleElement( arg.getName(), valueGetter, valueSetter );
			panel.elements.put( arg.getKey(), element );
			element.set( forward.apply( arg.getValue() ) );
			addToLayout(
					arg.getHelp(),
					new JLabel( element.getLabel() ),
					linkedFormattedTextField( element ),
					arg.getUnits(),
					arg );
		}
	}

	@Override
	public void visit( final StringParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< String, String > forward = ( Function< String, String > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< String, String > backward = ( Function< String, String > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final Supplier< String > valueGetter = () -> {
			final String value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< String > valueSetter = ( v ) -> {
			final String value = backward.apply( v );
			arg.set( value );
		};

		final StringElement element = stringElement( arg.getName(), valueGetter, valueSetter );
		panel.elements.put( arg.getKey(), element );
		addToLayoutTwoLines(
				arg.getHelp(),
				new JLabel( element.getLabel() ),
				linkedTextField( element ),
				arg );
	}

	@Override
	public void visit( final PathParam arg )
	{
		// Translate
		@SuppressWarnings( "unchecked" )
		final Function< String, String > forward = ( Function< String, String > ) forwardUITranslators.getOrDefault( arg, v -> v );
		@SuppressWarnings( "unchecked" )
		final Function< String, String > backward = ( Function< String, String > ) backwardUITranslators.getOrDefault( arg, v -> v );
		final Supplier< String > valueGetter = () -> {
			final String value = arg.getValue();
			return forward.apply( value );
		};
		final Consumer< String > valueSetter = ( v ) -> {
			final String value = backward.apply( v );
			arg.set( value );
		};

		final StringElement element = stringElement( arg.getName(), valueGetter, valueSetter );
		panel.elements.put( arg.getKey(), element );
		addPathToLayout(
				arg.getHelp(),
				new JLabel( element.getLabel() ),
				linkedTextField( element ),
				arg );
	}

	@Override
	public void visit( final ChoiceParam arg )
	{
		final List< String > displays = arg.getDisplays();
		final Supplier< String > supplier = () -> {
			return displays.get( arg.getSelectedIndex() );
		};
		final Consumer< String > consumer = ( s ) -> arg.set( displays.indexOf( s ) );

		final ListElement< String > element = listElement( arg.getName(), displays, supplier, consumer );
		panel.elements.put( arg.getKey(), element );
		final JComboBox< String > comboBox = linkedComboBoxSelector( element );
		comboBox.setSelectedIndex( arg.getSelectedIndex() );
		addToLayout(
				arg.getHelp(),
				new JLabel( element.getLabel() ),
				comboBox,
				arg.getUnits(),
				arg );
	}

	@Override
	public < E extends Enum< E > > void visit( final EnumParam< E > param )
	{
		final Class< E > enumClass = param.getEnumClass();
		final E[] enumConstants = enumClass.getEnumConstants();
		final Supplier< E > supplier = () -> param.getValue();
		final Consumer< E > consumer = ( s ) -> param.set( s );

		final EnumElement< E > element = enumElement( param.getName(), enumConstants, supplier, consumer );
		panel.elements.put( param.getKey(), element );
		final JComboBox< E > comboBox = linkedComboBoxEnumSelector( element );
		comboBox.setSelectedItem( param.getValue() );
		addToLayout(
				param.getHelp(),
				new JLabel( element.getLabel() ),
				comboBox,
				param.getUnits(),
				param );
	}

	/*
	 * UI STUFF.
	 */

	private void addToLayoutTwoLines( final String help, final JLabel lbl, final JComponent comp, final Parameter< ?, ? > arg )
	{
		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		comp.setFont( SMALL_FONT );
		final JComponent item;
		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			comp.setEnabled( btn.isSelected() );
			item = new JPanel();
			item.setLayout( new BoxLayout( item, BoxLayout.LINE_AXIS ) );
			item.add( btn );
			item.add( Box.createHorizontalGlue() );
			item.add( lbl );
		}
		else
		{
			item = lbl;
		}
		c.insets = new Insets( 5, 0, 0, 0 );
		c.gridwidth = 3;
		panel.add( item, c );
		c.gridy++;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets( 0, 0, 5, 0 );
		panel.add( comp, c );
		c.gridy++;

		if ( help != null )
		{
			lbl.setToolTipText( help );
			comp.setToolTipText( help );
		}
	}

	private void addPathToLayout( final String help, final JLabel lbl, final JTextField tf, final Parameter< ?, ? > arg )
	{
		final JPanel p = new JPanel();
		final BoxLayout bl = new BoxLayout( p, BoxLayout.LINE_AXIS );
		p.setLayout( bl );

		tf.setColumns( 10 ); // Avoid long paths deforming new panels.

		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		tf.setFont( SMALL_FONT );
		final JButton browseButton = new JButton( "browse" );
		browseButton.setFont( SMALL_FONT );
		browseButton.addActionListener( e -> {
			final File file = FileChooser.chooseFile( p, tf.getText(), DialogType.LOAD );
			if ( file == null )
				return;
			tf.setText( file.getAbsolutePath() );
			tf.postActionEvent();
		} );

		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> {
				tf.setEnabled( btn.isSelected() );
				browseButton.setEnabled( btn.isSelected() );
			} );

			tf.setEnabled( btn.isSelected() );
			browseButton.setEnabled( btn.isSelected() );
			p.add( btn );
		}
		p.add( lbl );
		p.add( Box.createHorizontalGlue() );
		p.add( browseButton );

		c.insets = new Insets( topInset, 0, 0, 0 );
		c.gridwidth = 3;
		panel.add( p, c );
		c.gridy++;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets( 0, 0, bottomInset, 0 );
		panel.add( tf, c );
		c.gridy++;

		if ( help != null )
		{
			lbl.setToolTipText( help );
			tf.setToolTipText( help );
			browseButton.setToolTipText( help );
		}
	}

	private void addToLayout( final String help, final JLabel lbl, final JComponent comp, final Parameter< ?, ? > arg )
	{
		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		lbl.setHorizontalAlignment( JLabel.RIGHT );
		comp.setFont( SMALL_FONT );

		final JComponent header;
		if ( arg != null && panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			comp.setEnabled( btn.isSelected() );
			header = new JPanel();
			header.setLayout( new BoxLayout( header, BoxLayout.LINE_AXIS ) );
			header.add( btn );
			header.add( Box.createHorizontalGlue() );
			header.add( lbl );
		}
		else
		{
			header = lbl;
		}

		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add( header, c );

		c.gridx++;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add( comp, c );

		c.gridx = 0;
		c.gridy++;
		c.insets = new Insets( topInset, 0, bottomInset, 0 );

		if ( help != null )
		{
			lbl.setToolTipText( help );
			comp.setToolTipText( help );
		}
	}

	private void addToLayout( final String help, final JLabel lbl, final JComponent comp, final String units, final Parameter< ?, ? > arg )
	{
		if ( units == null )
		{
			addToLayout( help, lbl, comp, arg );
			return;
		}

		lbl.setText( lbl.getText() + " " );
		lbl.setFont( SMALL_FONT );
		lbl.setHorizontalAlignment( JLabel.RIGHT );
		comp.setFont( SMALL_FONT );

		final JComponent header;
		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			header = new JPanel();
			header.setLayout( new BoxLayout( header, BoxLayout.LINE_AXIS ) );
			header.add( btn );
			header.add( Box.createHorizontalGlue() );
			header.add( lbl );
		}
		else
		{
			header = lbl;
		}

		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_END;
		panel.add( header, c );

		c.gridx++;
		c.anchor = GridBagConstraints.LINE_START;
		panel.add( comp, c );

		final JLabel lblUnits = new JLabel( " " + units );
		lblUnits.setFont( SMALL_FONT );
		c.gridx++;
		c.insets = new Insets( topInset, 0, bottomInset, 0 );
		panel.add( lblUnits, c );

		c.gridx = 0;
		c.gridy++;

		if ( help != null )
		{
			lbl.setToolTipText( help );
			comp.setToolTipText( help );
			lblUnits.setToolTipText( help );
		}
	}

	private void addToLayout( final String help, final JComponent comp )
	{
		final JComponent header;
		if ( panel.rdbtn != null )
		{
			final JRadioButton btn = panel.rdbtn;
			btn.addItemListener( e -> comp.setEnabled( btn.isSelected() ) );
			header = new JPanel();
			header.setLayout( new BoxLayout( header, BoxLayout.LINE_AXIS ) );
			header.add( btn );
			header.add( Box.createHorizontalGlue() );
			header.add( comp );
		}
		else
		{
			header = comp;
		}

		c.gridx = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets( topInset, 0, bottomInset, 0 );
		panel.add( header, c );

		c.gridx = 0;
		c.gridy++;

		if ( help != null )
			comp.setToolTipText( help );
	}

	private void addLastRow()
	{
		c.gridx = 0;
		c.gridy++;
		c.weighty = 1.;
		panel.add( new JLabel(), c );
	}

	/**
	 * Creates and returns a basic {@link ConfigPanel} for the specified
	 * {@link Configurator}. The panel UI elements will <i>modify</i> the values
	 * of the parameters in the config.
	 * 
	 * <p>
	 * The panel will contain only the UI elements corresponding to the
	 * {@link Parameters} in the Configurator that are
	 * {@link Parameter#isVisible()}, using the default Swing look and feel.
	 * There is no title nor help.
	 * 
	 * @param config
	 * @return
	 */
	public static ConfigPanel build( final Configurator config )
	{
		final GuiBuilder builder = createBuilder( config );
		return build( config, builder );
	}

	private static GuiBuilder createBuilder( final Configurator config )
	{
		return new GuiBuilder( config );
	}

	private static ConfigPanel build( final Configurator config, final GuiBuilder builder )
	{
		/*
		 * Iterate over Parameters.
		 */

		// Map a selectable group to a button group in the GUI
		final Map< Parameter< ?, ? >, JRadioButton > buttons = new HashMap<>();
		for ( final SelectableParameters selectable : config.getSelectables() )
		{
			final List< Parameter< ?, ? > > args = selectable.getParameters();
			final int nItems = args.size();
			final String label = selectable.getKey();
			final IntSupplier get = selectable::getSelected;
			final Consumer< Integer > set = selectable::select;
			final IntElement element = intElement( label, 0, nItems - 1, get, set );
			builder.panel.elements.put( selectable.getKey(), element );
			final ButtonGroup buttonGroup = linkedButtonGroup( element );
			// Link radio buttons to Parameters.
			final Enumeration< AbstractButton > enumeration = buttonGroup.getElements();
			final Iterator< Parameter< ?, ? > > it = args.iterator();
			while ( enumeration.hasMoreElements() )
			{
				final JRadioButton btn = ( JRadioButton ) enumeration.nextElement();
				final Parameter< ?, ? > arg = it.next();
				buttons.put( arg, btn );
				btn.setSelected( selectable.getSelection().equals( arg ) );
			}
		}

		// Iterate over Parameters, taking care of selectable group.
		for ( final Parameter< ?, ? > arg : config.getParameters() )
		{
			if ( !arg.isVisible() )
				continue;

			builder.setCurrentRadioButton( buttons.get( arg ) );
			arg.accept( builder );
		}

		/*
		 * Last row.
		 */

		builder.addLastRow();
		return builder.panel;
	}

	private static ButtonGroup linkedButtonGroup( final IntElement element )
	{
		final BoundedValue value = element.getValue();
		final ButtonGroup buttonGroup = new ButtonGroup();
		final List< JRadioButton > buttons = new ArrayList<>();
		for ( int i = 0; i <= value.getRangeMax(); i++ )
		{
			final JRadioButton btn = new JRadioButton();
			buttons.add( btn );
			final int selected = i;
			btn.addItemListener( new ItemListener()
			{

				@Override
				public void itemStateChanged( final ItemEvent e )
				{
					if ( btn.isSelected() )
						element.set( selected );
				}
			} );
			buttonGroup.add( btn );
		}
		element.getValue().setUpdateListener( new UpdateListener()
		{

			@Override
			public void update()
			{
				final int selected = element.get();
				buttons.get( selected ).setSelected( true );
			}
		} );
		return buttonGroup;
	}

	public class ConfigPanel extends JPanel
	{

		/**
		 * Map of StyleElements that are created by this builder. The keys are
		 * the corresponding Parameter keys.
		 */
		final Map< String, StyleElement > elements = new LinkedHashMap<>();

		private JRadioButton rdbtn;

		private static final long serialVersionUID = 1L;

		public void refresh()
		{
			elements.values().forEach( e -> e.update() );
		}
	}
}
