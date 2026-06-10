package org.scijava.ui.paramUI.visitors.gui.elements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.scijava.ui.paramUI.utils.GuiUtils;
import org.scijava.ui.paramUI.visitors.gui.elements.colormap.Colormap;

public class StyleElements
{
	public static DecimalFormat format = new DecimalFormat( "#.###" );

	public static Font FONT = UIManager.getFont( "Label.font" );

	public static Font SMALL_FONT = FONT.deriveFont( FONT.getSize() * 0.8f );

	public static Separator separator()
	{
		return new Separator();
	}

	public static LabelElement label( final String label )
	{
		return new LabelElement( label );
	}

	public static StringElement stringElement( final String label, final Supplier< String > get, final Consumer< String > set )
	{
		return new StringElement( label )
		{

			@Override
			public String get()
			{
				return get.get();
			}

			@Override
			public void set( final String s )
			{
				set.accept( s );
			}
		};
	}

	public static BooleanElement booleanElement( final String label, final BooleanSupplier get, final Consumer< Boolean > set )
	{
		return new BooleanElement( label )
		{
			@Override
			public boolean get()
			{
				return get.getAsBoolean();
			}

			@Override
			public void set( final boolean b )
			{
				set.accept( b );
			}
		};
	}

	public static ColorElement colorElement( final String label, final Supplier< Color > get, final Consumer< Color > set )
	{
		return new ColorElement( label )
		{
			@Override
			public Color getColor()
			{
				return get.get();
			}

			@Override
			public void setColor( final Color c )
			{
				set.accept( c );
			}
		};
	}

	public static ColormapElement colormapElement( final String label, final Supplier< Colormap > get, final Consumer< Colormap > set )
	{
		return new ColormapElement( label )
		{

			@Override
			public Colormap get()
			{
				return get.get();
			}

			@Override
			public void set( final Colormap v )
			{
				set.accept( v );
			}
		};
	}

	public static BoundedDoubleElement boundedDoubleElement( final String label, final double rangeMin, final double rangeMax, final DoubleSupplier get, final Consumer< Double > set )
	{
		return new BoundedDoubleElement( label, rangeMin, rangeMax )
		{
			@Override
			public double get()
			{
				return get.getAsDouble();
			}

			@Override
			public void set( final double v )
			{
				set.accept( v );
			}
		};
	}

	public static DoubleElement doubleElement( final String label, final DoubleSupplier get, final Consumer< Double > set )
	{
		return new DoubleElement( label )
		{
			@Override
			public double get()
			{
				return get.getAsDouble();
			}

			@Override
			public void set( final double v )
			{
				set.accept( v );
			}
		};
	}

	public static IntElement intElement( final String label, final int rangeMin, final int rangeMax, final IntSupplier get, final Consumer< Integer > set )
	{
		return new IntElement( label, rangeMin, rangeMax )
		{
			@Override
			public int get()
			{
				return get.getAsInt();
			}

			@Override
			public void set( final int v )
			{
				set.accept( v );
			}
		};
	}

	public static < E > EnumElement< E > enumElement( final String label, final E[] values, final Supplier< E > get, final Consumer< E > set )
	{
		return new EnumElement< E >( label, values )
		{

			@Override
			public E getValue()
			{
				return get.get();
			}

			@Override
			public void setValue( final E e )
			{
				set.accept( e );
			}
		};
	}

	public static < E > ListElement< E > listElement( final String label, final List< E > values, final Supplier< E > get, final Consumer< E > set )
	{
		return new ListElement< E >( label, values )
		{

			@Override
			public E getValue()
			{
				return get.get();
			}

			@Override
			public void setValue( final E e )
			{
				set.accept( e );
			}
		};
	}

	/*
	 * Visitor interface.
	 */

	public interface StyleElementVisitor
	{
		public default void visit( final Separator element )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final LabelElement label )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final ColorElement colorElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final BooleanElement booleanElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final BoundedDoubleElement doubleElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final DoubleElement doubleElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final IntElement intElement )
		{
			throw new UnsupportedOperationException();
		}

		public default < E > void visit( final EnumElement< E > enumElement )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final ColormapElement element )
		{
			throw new UnsupportedOperationException();
		}

		public default void visit( final StringElement stringElement )
		{
			throw new UnsupportedOperationException();
		}

		public default < E > void visit( final ListElement< E > listElement )
		{
			throw new UnsupportedOperationException();
		}
	}

	/*
	 *
	 * ===============================================================
	 *
	 */

	public interface StyleElement
	{
		public default void update()
		{}

		public void accept( StyleElementVisitor visitor );
	}

	public static class Separator implements StyleElement
	{
		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static class LabelElement implements StyleElement
	{
		private final String label;

		public LabelElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static abstract class StringElement implements StyleElement
	{

		private final ArrayList< Consumer< String > > onSet = new ArrayList<>();

		private final String label;

		private String value;

		public StringElement( final String label )
		{
			this.label = label;
			this.value = "";
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract String get();

		public abstract void set( String s );

		public void onSet( final Consumer< String > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			if ( get() != value )
				value = get();
			onSet.forEach( c -> c.accept( get() ) );
		}
	}

	public static abstract class EnumElement< E > implements StyleElement
	{
		private final ArrayList< Consumer< E > > onSet = new ArrayList<>();

		private final String label;

		private final E[] values;

		public EnumElement( final String label, final E[] values )
		{
			this.label = label;
			this.values = values;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< E > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( getValue() ) );
		}

		public abstract E getValue();

		public abstract void setValue( E e );

		public E[] getValues()
		{
			return values;
		}
	}

	public static abstract class ListElement< E > implements StyleElement
	{
		private final ArrayList< Consumer< E > > onSet = new ArrayList<>();

		private final String label;

		private final List< E > values;

		public ListElement( final String label, final List< E > values )
		{
			this.label = label;
			this.values = values;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< E > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( getValue() ) );
		}

		public abstract E getValue();

		public abstract void setValue( E e );

		public List< E > getValues()
		{
			return values;
		}
	}

	public static abstract class ColorElement implements StyleElement
	{
		private final ArrayList< Consumer< Color > > onSet = new ArrayList<>();

		private final String label;

		public ColorElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< Color > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( getColor() ) );
		}

		public abstract Color getColor();

		public abstract void setColor( Color c );
	}

	public static abstract class BooleanElement implements StyleElement
	{
		private final String label;

		private final ArrayList< Consumer< Boolean > > onSet = new ArrayList<>();

		public BooleanElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public void onSet( final Consumer< Boolean > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( get() ) );
		}

		public abstract boolean get();

		public abstract void set( boolean b );
	}

	public static abstract class BoundedDoubleElement implements StyleElement
	{
		private final BoundedValueDouble value;

		private final String label;

		public BoundedDoubleElement( final String label, final double rangeMin, final double rangeMax )
		{
			final double currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
			value = new BoundedValueDouble( rangeMin, rangeMax, currentValue )
			{
				@Override
				public void setCurrentValue( final double value )
				{
					super.setCurrentValue( value );
					if ( get() != getCurrentValue() )
						set( getCurrentValue() );
				}
			};
			this.label = label;
		}

		public BoundedValueDouble getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract double get();

		public abstract void set( double v );

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}
	}

	public static abstract class DoubleElement implements StyleElement
	{

		private final ArrayList< Consumer< Double > > onSet = new ArrayList<>();

		private double value;

		private final String label;

		public DoubleElement( final String label )
		{
			value = 0.;
			this.label = label;
		}

		public double getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract double get();

		public abstract void set( double v );

		public void onSet( final Consumer< Double > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			if ( get() != value )
				value = get();
			onSet.forEach( c -> c.accept( get() ) );
		}
	}

	public static abstract class IntElement implements StyleElement
	{
		private final BoundedValue value;

		private final String label;

		public IntElement( final String label, final int rangeMin, final int rangeMax )
		{
			final int currentValue = Math.max( rangeMin, Math.min( rangeMax, get() ) );
			value = new BoundedValue( rangeMin, rangeMax, currentValue )
			{
				@Override
				public void setCurrentValue( final int value )
				{
					super.setCurrentValue( value );
					if ( get() != getCurrentValue() )
						set( getCurrentValue() );
				}
			};
			this.label = label;
		}

		public BoundedValue getValue()
		{
			return value;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract int get();

		public abstract void set( int v );

		@Override
		public void update()
		{
			if ( get() != value.getCurrentValue() )
				value.setCurrentValue( get() );
		}
	}

	public static abstract class ColormapElement implements StyleElement
	{
		private final ArrayList< Consumer< Colormap > > onSet = new ArrayList<>();

		private final String label;

		public ColormapElement( final String label )
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public void accept( final StyleElementVisitor visitor )
		{
			visitor.visit( this );
		}

		public abstract Colormap get();

		public abstract void set( Colormap v );

		public void onSet( final Consumer< Colormap > set )
		{
			onSet.add( set );
		}

		@Override
		public void update()
		{
			onSet.forEach( c -> c.accept( get() ) );
		}
	}

	/*
	 *
	 * ===============================================================
	 *
	 */

	public static JLabel linkedLabel( final LabelElement element )
	{
		return new JLabel( element.getLabel() );
	}

	public static JComboBox< Colormap > linkedColormapChooser( final ColormapElement element )
	{
		final JComboBox< Colormap > cb = new JComboBox< Colormap >(
				Colormap.getAvailableLUTs().toArray( new Colormap[] {} ) );
		cb.setRenderer( new ColormapRenderer() );
		cb.setSelectedItem( element.get() );
		cb.addActionListener( e -> element.set( ( Colormap ) cb.getSelectedItem() ) );
		element.onSet( cm -> {
			if ( cm != cb.getSelectedItem() )
				cb.setSelectedItem( cm );
		} );
		return cb;
	}

	private static final class ColormapRenderer extends JPanel implements ListCellRenderer< Colormap >
	{

		private static final long serialVersionUID = 1L;

		private Colormap lut = Colormap.Jet;

		private final DefaultListCellRenderer lbl;

		public ColormapRenderer()
		{
			setPreferredSize( new Dimension( 150, 20 ) );
			final BoxLayout itemlayout = new BoxLayout( this, BoxLayout.LINE_AXIS );
			this.lbl = new DefaultListCellRenderer();
			setLayout( itemlayout );
			add( lbl );
			add( Box.createHorizontalGlue() );
			add( new JComponent()
			{

				private static final long serialVersionUID = 1L;

				@Override
				public void paint( final Graphics g )
				{

					final int width = getWidth();
					final int height = getHeight();
					for ( int i = 0; i < width; i++ )
					{
						final double beta = ( double ) i / ( width - 1 );
						g.setColor( lut.getPaint( beta ) );
						g.drawLine( i, 0, i, height );
					}
					g.setColor( this.getParent().getBackground() );
					g.drawRect( 0, 0, width, height );
				}

				@Override
				public Dimension getMaximumSize()
				{
					return new Dimension( 100, 20 );
				}

				@Override
				public Dimension getPreferredSize()
				{
					return getMaximumSize();
				}
			} );
		}

		@Override
		public Component getListCellRendererComponent(
				final JList< ? extends Colormap > list,
				final Colormap value,
				final int index,
				final boolean isSelected,
				final boolean cellHasFocus )
		{
			this.lut = value;
			lbl.getListCellRendererComponent( list, value.getName(), index, isSelected, cellHasFocus );
			setBackground( lbl.getBackground() );
			return this;
		}
	}

	public static JCheckBox linkedCheckBox( final BooleanElement element, final String label )
	{
		final JCheckBox checkbox = new JCheckBox( label, element.get() );
		checkbox.addActionListener( ( e ) -> element.set( checkbox.isSelected() ) );
		element.onSet( b -> {
			if ( b != checkbox.isSelected() )
				checkbox.setSelected( b );
		} );
		return checkbox;
	}

	public static JButton linkedColorButton( final ColorElement element, final JColorChooser colorChooser )
	{
		final ColorIcon icon = new ColorIcon( element.getColor(), 16, 0 );
		final JButton button = new JButton( icon );
		button.setOpaque( false );
		button.setContentAreaFilled( false );
		button.setBorderPainted( false );
		button.setFont( new JButton().getFont() );
		button.setMargin( new Insets( 0, 0, 0, 0 ) );
		button.setBorder( new EmptyBorder( 2, 5, 2, 2 ) );
		button.setHorizontalAlignment( SwingConstants.LEFT );
		button.addActionListener( e -> {
			colorChooser.setColor( element.getColor() );
			final JDialog d = JColorChooser.createDialog( button, "Choose a color", true, colorChooser, new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent arg0 )
				{
					final Color c = colorChooser.getColor();
					if ( c != null )
					{
						icon.setColor( c );
						button.repaint();
						element.setColor( c );
					}
				}
			}, null );
			d.setVisible( true );
		} );
		element.onSet( icon::setColor );
		return button;
	}

	public static SliderPanel linkedSliderPanel( final IntElement element, final int tfCols )
	{
		final SliderPanel slider = new SliderPanel( null, element.getValue(), 1 );
		slider.setNumColummns( tfCols );
		slider.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		return slider;
	}

	public static JSpinner linkedSpinner( final IntElement element )
	{
		final BoundedValue value = element.getValue();
		final SpinnerNumberModel model = new SpinnerNumberModel( element.get(), value.getRangeMin(), value.getRangeMax(), 1 );
		final JSpinner spinner = new JSpinner( model );
		spinner.setMaximumSize( new Dimension( 80, spinner.getMaximumSize().height ) );
		model.addChangeListener( e -> element.set( ( ( Number ) model.getValue() ).intValue() ) );
		return spinner;
	}

	public static SliderPanelDouble linkedSliderPanel( final BoundedDoubleElement element, final int tfCols )
	{
		return linkedSliderPanel( element, tfCols, 1. );
	}

	public static SliderPanelDouble linkedSliderPanel( final BoundedDoubleElement element, final int tfCols, final double stepSize )
	{
		final SliderPanelDouble slider = new SliderPanelDouble( null, element.getValue(), stepSize );
		slider.setDecimalFormat( "0.####" );
		slider.setNumColummns( tfCols );
		slider.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		return slider;
	}

	@SuppressWarnings( "unchecked" )
	public static < E > JSpinner linkedSpinnerEnumSelector( final EnumElement< E > element )
	{
		final SpinnerListModel model = new SpinnerListModel( element.getValues() );
		final JSpinner spinner = new JSpinner( model );
		spinner.setFont( SMALL_FONT );
		( ( DefaultEditor ) spinner.getEditor() ).getTextField().setEditable( false );
		model.setValue( element.getValue() );
		model.addChangeListener( e -> element.setValue( ( E ) model.getValue() ) );
		element.onSet( e -> {
			if ( e != model.getValue() )
				model.setValue( e );
		} );
		return spinner;
	}

	@SuppressWarnings( "unchecked" )
	public static < E > JComboBox< E > linkedComboBoxEnumSelector( final EnumElement< E > element )
	{
		final DefaultComboBoxModel< E > model = new DefaultComboBoxModel<>( element.values );
		final JComboBox< E > cb = new JComboBox<>( model );
		cb.setFont( SMALL_FONT );
		cb.addActionListener( e -> element.setValue( ( E ) model.getSelectedItem() ) );
		element.onSet( e -> {
			if ( e != model.getSelectedItem() )
				model.setSelectedItem( e );
		} );
		return cb;
	}

	@SuppressWarnings( "unchecked" )
	public static < E > JComboBox< E > linkedComboBoxSelector( final ListElement< E > element )
	{
		final DefaultComboBoxModel< E > model = new DefaultComboBoxModel<>( new Vector<>( element.values ) );
		final JComboBox< E > cb = new JComboBox<>( model );
		cb.setFont( SMALL_FONT );
		cb.addActionListener( e -> element.setValue( ( E ) model.getSelectedItem() ) );
		element.onSet( e -> {
			if ( e != model.getSelectedItem() )
				model.setSelectedItem( e );
		} );
		return cb;
	}

	/**
	 * Create a JFormattedTextField linked to a DoubleElement. The value of the
	 * text field is updated when the element is updated and vice versa. The
	 * text field commits the edit on focus lost and on enter.
	 * 
	 * @param element
	 *            the DoubleElement to link to the text field.
	 * @param min
	 *            the minimum value of the text field (inclusive). The text
	 *            field will not allow values less than this. If null, no
	 *            minimum is enforced.
	 * @param max
	 *            the maximum value of the text field (inclusive). The text
	 *            field will not allow values greater than this. If null, no
	 *            maximum is enforced.
	 * @return a JFormattedTextField linked to the given DoubleElement.
	 */
	public static JFormattedTextField linkedFormattedTextField( final DoubleElement element, final Double min, final Double max )
	{
		final JFormattedTextField ftf = new JFormattedTextField( format );
		ftf.setHorizontalAlignment( JFormattedTextField.RIGHT );
		ftf.setValue( Double.valueOf( element.get() ) );

		final Runnable validateAndSet = () -> {
			double value = ( ( Number ) ftf.getValue() ).doubleValue();
			if ( min != null && value < min )
			{
				value = min;
				ftf.setValue( Double.valueOf( value ) );
			}
			if ( max != null && value > max )
			{
				value = max;
				ftf.setValue( Double.valueOf( value ) );
			}
			element.set( value );
		};

		ftf.addActionListener( e -> validateAndSet.run() );
		ftf.addFocusListener( new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				try
				{
					ftf.commitEdit();
					validateAndSet.run();
				}
				catch ( final ParseException e1 )
				{}
			}
		} );
		GuiUtils.selectAllOnFocus( ftf );
		element.onSet( d -> {
			if ( d != ( ( Number ) ftf.getValue() ).doubleValue() )
				ftf.setValue( Double.valueOf( element.value ) );
		} );

		return ftf;
	}

	public static JTextField linkedTextField( final StringElement element )
	{
		final JTextField tf = new JTextField( element.get() );
		tf.setHorizontalAlignment( JFormattedTextField.LEFT );

		tf.addActionListener( e -> element.set( tf.getText() ) );
		GuiUtils.selectAllOnFocus( tf );
		tf.addFocusListener( new FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent e )
			{
				element.set( tf.getText() );
			}
		} );
		element.onSet( d -> {
			if ( d != ( tf.getText() ) )
				tf.setText( element.value );
		} );

		return tf;
	}
}