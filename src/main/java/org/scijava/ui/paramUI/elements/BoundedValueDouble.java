package org.scijava.ui.paramUI.elements;

/**
 * A {@code double} variable that can take any value in a given range. A
 * {@link #setUpdateListener(UpdateListener) listener} is notified when the
 * value or its allowed range is changed.
 *
 * @author Tobias Pietzsch
 */
public class BoundedValueDouble
{
	private double rangeMin;

	private double rangeMax;

	private double currentValue;

	public interface UpdateListener
	{
		void update();
	}

	private UpdateListener updateListener;

	public BoundedValueDouble( final double rangeMin, final double rangeMax, final double currentValue )
	{
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		this.currentValue = currentValue;
		updateListener = null;
	}

	public double getRangeMin()
	{
		return rangeMin;
	}

	public double getRangeMax()
	{
		return rangeMax;
	}

	public double getCurrentValue()
	{
		return currentValue;
	}

	public void setRange( final double min, final double max )
	{
		assert min <= max;
		rangeMin = min;
		rangeMax = max;
		currentValue = Math.min( Math.max( currentValue, min ), max );

		if ( updateListener != null )
			updateListener.update();
	}

	public void setCurrentValue( final double value )
	{
		currentValue = value;

		if ( currentValue < rangeMin )
			currentValue = rangeMin;
		else if ( currentValue > rangeMax )
			currentValue = rangeMax;

		if ( updateListener != null )
			updateListener.update();
	}

	public void setUpdateListener( final UpdateListener l )
	{
		updateListener = l;
	}
}
