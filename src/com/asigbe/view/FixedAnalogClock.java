package com.asigbe.view;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.asigbe.gwakeup.R;

/**
 * This class displays an an analog clock which time can be configured.
 * 
 * @author Delali Zigah
 */
public class FixedAnalogClock extends View {

	private Drawable mHourHand;
	private Drawable mMinuteHand;
	private Drawable mDial;

	private int mDialWidth;
	private int mDialHeight;

	private boolean mChanged;
	private Calendar calendar;

	/**
	 * Creates a standard clock with no attributes.
	 */
	public FixedAnalogClock(Context context) {
		this(context, null, 0);
	}

	/**
	 * Creates a standard clock with the given attributes.
	 */
	public FixedAnalogClock(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Creates a standard clock with the given attributes and the given style.
	 */
	public FixedAnalogClock(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Resources r = getContext().getResources();

		this.mDial = r.getDrawable(R.drawable.clock_transparent);
		this.mHourHand = r.getDrawable(R.drawable.clock_hand_hour);
		this.mMinuteHand = r.getDrawable(R.drawable.clock_hand_minute);
		this.mDialWidth = this.mDial.getIntrinsicWidth();
		this.mDialHeight = this.mDial.getIntrinsicHeight();
		this.calendar = new GregorianCalendar();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		float hScale = 1.0f;
		float vScale = 1.0f;

		if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
			hScale = (float) widthSize / (float) mDialWidth;
		}

		if (heightMode != MeasureSpec.UNSPECIFIED
				&& heightSize < this.mDialHeight) {
			vScale = (float) heightSize / (float) this.mDialHeight;
		}

		float scale = Math.min(hScale, vScale);

		setMeasuredDimension(resolveSize((int) (this.mDialWidth * scale),
				widthMeasureSpec), resolveSize(
				(int) (this.mDialHeight * scale), heightMeasureSpec));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.mChanged = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		boolean changed = this.mChanged;
		if (changed) {
			this.mChanged = false;
		}

		int availableWidth = getRight() - getLeft();
		int availableHeight = getBottom() - getTop();

		int x = availableWidth / 2;
		int y = availableHeight / 2;

		final Drawable dial = this.mDial;
		int w = dial.getIntrinsicWidth();
		int h = dial.getIntrinsicHeight();

		boolean scaled = false;

		if (availableWidth < w || availableHeight < h) {
			scaled = true;
			float scale = Math.min((float) availableWidth / (float) w,
					(float) availableHeight / (float) h);
			canvas.save();
			canvas.scale(scale, scale, x, y);
		}

		if (changed) {
			dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
		}
		dial.draw(canvas);

		canvas.save();
		canvas.rotate(this.calendar.get(Calendar.HOUR) / 12.0f * 360.0f, x, y);
		final Drawable hourHand = this.mHourHand;
		if (changed) {
			w = hourHand.getIntrinsicWidth();
			h = hourHand.getIntrinsicHeight();
			hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
					+ (h / 2));
		}
		hourHand.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas
				.rotate(this.calendar.get(Calendar.MINUTE) / 60.0f * 360.0f, x,
						y);

		final Drawable minuteHand = this.mMinuteHand;
		if (changed) {
			w = minuteHand.getIntrinsicWidth();
			h = minuteHand.getIntrinsicHeight();
			minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
					+ (h / 2));
		}
		minuteHand.draw(canvas);
		canvas.restore();

		if (scaled) {
			canvas.restore();
		}
	}

	/**
	 * Sets the date of the clock and change the display according to it.
	 */
	public void setDate(Date date) {
		this.calendar.setTime(date);
		this.mChanged = true;
		invalidate();
	}
}