package com.asigbe.preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.asigbe.gwakeup.R;

/**
 * @hide
 */
public class SeekBarPreference extends DialogPreference {
	private static final String TAG = "SeekBarPreference";

	private final Drawable myIcon;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDialogLayoutResource(R.layout.seekbar_dialog);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);

		// Steal the XML dialogIcon attribute's value
		this.myIcon = getDialogIcon();
		setDialogIcon(null);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		final ImageView iconView = (ImageView) view
				.findViewById(R.id.icon_seekbar);
		if (this.myIcon != null) {
			iconView.setImageDrawable(this.myIcon);
		} else {
			iconView.setVisibility(View.GONE);
		}
	}

	protected static SeekBar getSeekBar(View dialogView) {
		return (SeekBar) dialogView.findViewById(R.id.seekbar);
	}
}
