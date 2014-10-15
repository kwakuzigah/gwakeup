package com.asigbe.gwakeup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.asigbe.view.DragAndDropView;
import com.asigbe.view.FixedAnalogClock;
import com.asigbe.view.ViewTools;

/**
 * This activity displayed allows to configure alarms.
 * 
 * @author Delali Zigah
 */
public class MainActivity extends Activity {

	// private DragAndDropView dragAndDropView;
	private final List<AlarmClock> alarmClocks = new ArrayList<AlarmClock>();
	private final List<AlarmClock> removedAlarmClocks = new ArrayList<AlarmClock>();
	private int lastSelectedPosition;
	private ClockViewAdapter clockViewAdapter;
	// private View dialogView;
	private final SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
			.getTimeInstance();
	private ViewGroup container;
	private DragAndDropView dragAndDropView;
	private boolean is24HourView;

	private static final int AUDIO_DIALOG = 1;
	private static final int NO_LICENSE_DIALOG = 2;
	private static final int LICENSE_ERROR_DIALOG = 3;
	// private static final int ARM_SERVICE_NOT_FOUND_DIALOG = 2;
	protected int currentDraggedPosition;

	private final static int EDIT_ID = 0;
	private final static int CLONE_ID = 1;
	private final static int DELETE_ID = 2;

	// private IArmService service;
	// private ArmServiceConnection armCon;
	private String AID = "OA00030795";

	public static final String ALARM = "alarm";
	public static final String INDEX_ALARM = "index_alarm";
	public static final String IS_CLONE = "is_clone";
	public static final String IS_ONE_TIME_ALARM = "is_one_time_alarm";

	private LicenseCheckerCallback licenseCheckerCallback;
	private LicenseChecker checker;
	// A handler on the UI thread.
	private Handler handler;
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtp0eOloWbROCjMe3A5Y8MJ7BCjv7DXuuEftPM9EvjUnbNUjgOABPxdRvGAp5eMl2eU3YAJDQjxiou0jmueMOPTWJZ1/6wyKlVGBFMgnT4xrBEc6dNqAgzKIdJ+J9taKj/J8HXp7AUImxdCNUMEGq/qt8PiRi/fk1BwcnBJUygDAP3sk4NJtBseser0x5McwLEtkYp0fujFLmc7GNeEpRrSz+LLVDu/uMmOrA9/lQDY0tSqnYcqIw83vr44/gjRsc36rtuoehF7rdgv7x6rWkVG3heKqr44TR1pdP9w2iwWsKMpxwAqc4Xaaj6OUgbF6iLbs7MFW3Un2fZNVhVU+Q4wIDAQAB";

	// Generate your own 20 random bytes, and put them here.
	private static final byte[] SALT = new byte[] { 45, 65, 4, -128, -5, -57,
			74, -64, 51, 45, -95, -57, 8, -54, -1, -21, -4, 78, 78, 89 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.handler = new Handler();

		// Try to use more data here. ANDROID_ID is a single point of attack.
		String deviceId = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);

		// Library calls this when it's done.
		this.licenseCheckerCallback = new MyLicenseCheckerCallback();
		// Construct the LicenseChecker with a policy.
		this.checker = new LicenseChecker(this, new ServerManagedPolicy(this,
				new AESObfuscator(SALT, getPackageName(), deviceId)),
				BASE64_PUBLIC_KEY);
		doCheck();

		// Execute ARM Check process
		// if (!runArmService()) {
		// in case the ARMservice is not listed on the phone menu or when it
		// can not be found.
		// showDialog(ARM_SERVICE_NOT_FOUND_DIALOG);
		// }

		setContentView(R.layout.mainview);
		this.is24HourView = DateFormat.is24HourFormat(this);

		// retrieve and lay the thumbnails view and the configuration view
		this.container = (ViewGroup) findViewById(R.id.container);
		this.dragAndDropView = (DragAndDropView) ViewTools.inflateView(
				MainActivity.this, R.layout.alarmsview);
		this.container.addView(this.dragAndDropView);
		this.container
				.setPersistentDrawingCache(ViewGroup.PERSISTENT_ANIMATION_CACHE);

		this.gridView = (GridView) findViewById(R.id.gridview);
		Rect rect = new Rect();
		this.container.getDrawingRect(rect);
		this.clockViewAdapter = new ClockViewAdapter(this.alarmClocks);
		this.gridView.setAdapter(this.clockViewAdapter);
		// update the position of the last selected alarm when
		this.gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MainActivity.this.lastSelectedPosition = position;
				MainActivity.this.dragAndDropView.setDraggedView(view);
			}
		});
		this.gridView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				MainActivity.this.lastSelectedPosition = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// edit the alarm on a double click
		final GestureDetector gestureDetector = new GestureDetector(
				new SimpleOnGestureListener() {
					@Override
					public boolean onSingleTapConfirmed(MotionEvent e) {
						int pointToPosition = MainActivity.this.gridView
								.pointToPosition((int) e.getX(), (int) e.getY());
						if (pointToPosition != -1) {
							editClock(pointToPosition);
						}
						return super.onSingleTapConfirmed(e);
					}
				});
		this.gridView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gestureDetector.onTouchEvent(event);
				return false;
			}
		});

		// add the contextual menu
		registerForContextMenu(this.gridView);

		// configure the add button used to add an alarm
		ImageButton addButton = (ImageButton) findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addClock(false);
			}
		});

		ImageButton addOneTimeButton = (ImageButton) findViewById(R.id.addOneTimeButton);
		addOneTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addClock(true);
			}
		});

		// configure the copy button used to clone an alarm
		final ImageButton copyButton = (ImageButton) findViewById(R.id.copyButton);

		// configure the remove button used to remove an alarm
		final ImageButton removeButton = (ImageButton) findViewById(R.id.removeButton);

		this.dragAndDropView
				.setTouchDelegate(new DragAndDropView.TouchDelegate() {
					// private AlarmClock alarmClock = null;
					boolean isDragging = false;

					@Override
					public void onTouchEvent(View view, MotionEvent event) {
						Rect rect = new Rect();

						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							int location[] = new int[2];
							ArrayList<View> touchables = MainActivity.this.gridView
									.getTouchables();
							MainActivity.this.gridView
									.getLocationOnScreen(location);
							int x = (int) event.getRawX() - location[0];
							int y = (int) event.getRawY() - location[1];
							int pointToPosition = MainActivity.this.gridView
									.pointToPosition(x, y);
							View draggedView = null;
							if (pointToPosition != AbsListView.INVALID_POSITION) {
								MainActivity.this.currentDraggedPosition = pointToPosition;
								for (View childView : touchables) {
									int positionForView = MainActivity.this.gridView
											.getPositionForView(childView);
									if (positionForView == pointToPosition) {
										draggedView = childView;
									}
								}
								MainActivity.this.dragAndDropView
										.setDraggedView(draggedView);
							}
							break;
						case MotionEvent.ACTION_MOVE:
							this.isDragging = true;
							// identify if the cursor is on the copy button
							copyButton.getHitRect(rect);
							if (rect.contains((int) event.getX(), (int) event
									.getY())) {
								copyButton.setColorFilter(Color.RED,
										PorterDuff.Mode.SRC_ATOP);
							} else {
								copyButton.clearColorFilter();
							}
							// identify if the cursor is on the remove button
							removeButton.getHitRect(rect);
							if (rect.contains((int) event.getX(), (int) event
									.getY())) {
								removeButton.setColorFilter(Color.RED,
										PorterDuff.Mode.SRC_ATOP);
							} else {
								removeButton.clearColorFilter();
							}
							break;

						case MotionEvent.ACTION_UP:
							if (this.isDragging
									&& MainActivity.this.currentDraggedPosition < MainActivity.this.alarmClocks
											.size()) {
								AlarmClock alarmClock = MainActivity.this.alarmClocks
										.get(MainActivity.this.currentDraggedPosition);
								copyButton.getHitRect(rect);
								if (rect.contains((int) event.getX(),
										(int) event.getY())) {
									// add new alarm
									cloneClock(MainActivity.this.currentDraggedPosition);
								}
								copyButton.clearColorFilter();

								removeButton.getHitRect(rect);
								if (rect.contains((int) event.getX(),
										(int) event.getY())) {
									// remove existing alarm
									MainActivity.this.clockViewAdapter
											.removeAlarmClock(alarmClock);
								}
								removeButton.clearColorFilter();
								alarmClock = null;
							}
							this.isDragging = false;
							break;
						}
					}
				});

	}

	private void doCheck() {
		// mCheckLicenseButton.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
		// mStatusText.setText(R.string.checking_license);
		this.checker.checkAccess(this.licenseCheckerCallback);
	}

	private void displayResult() {
		this.handler.post(new Runnable() {
			@Override
			public void run() {
				// mStatusText.setText(result);
				setProgressBarIndeterminateVisibility(false);
				// mCheckLicenseButton.setEnabled(true);
			}
		});
	}

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		@Override
		public void allow() {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// Should allow user access.
			displayResult();
		}

		@Override
		public void dontAllow() {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			displayResult();
			// Should not allow access. In most cases, the app should assume
			// the user has access unless it encounters this. If it does,
			// the app should inform the user of their unlicensed ways
			// and then either shut down the app or limit the user to a
			// restricted set of features.
			// In this example, we show a dialog that takes the user to Market.
			showDialog(NO_LICENSE_DIALOG);
		}

		@Override
		public void applicationError(ApplicationErrorCode errorCode) {
			if (isFinishing()) {
				// Don't update UI if Activity is finishing.
				return;
			}
			// This is a polite way of saying the developer made a mistake
			// while setting up or calling the license checker library.
			// Please examine the error code and fix the error.
			// String result = String.format(
			// getString(R.string.application_error), errorCode);
			displayResult();
			showDialog(LICENSE_ERROR_DIALOG);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.closestDate = AlarmScheduler.loadFromFile(this, this.alarmClocks);
		this.clockViewAdapter.notifyDataSetChanged();
	}

	// private boolean runArmService() {
	// try {
	// if (armCon == null) {
	// // bindService execution
	// armCon = new ArmServiceConnection();
	// boolean conRes = bindService(new Intent(IArmService.class
	// .getName()), armCon, Context.BIND_AUTO_CREATE);
	//
	// if (conRes) {
	// return true;
	// }
	// }
	// } catch (Exception e) {
	// }
	// releaseService();
	// return false;
	// }
	//
	// private void releaseService() {
	// if (armCon != null) {
	// unbindService(armCon);
	// armCon = null;
	// service = null;
	// }
	// }
	//
	// class ArmServiceConnection implements ServiceConnection {
	// public void onServiceConnected(ComponentName name, IBinder boundService)
	// {
	// // after bindService, onServiceConnected() event takes place
	// if (service == null)
	// service = IArmService.Stub.asInterface((IBinder) boundService);
	// try {
	// // AID variable is sent to ARM Service.
	// int res = service.executeArm(AID);
	// switch (res) {
	// case 1:
	// // in case of success
	// // Application is properly executed
	// break;
	// default:
	// // in case of failure
	// // Application is terminated after an error message is
	// // printed out
	// // Refer to the error codes (Appendix)
	// showDialog(res);
	// break;
	// }
	// } catch (Exception e) {
	// releaseService();
	// return;
	// }
	// // ARM Service gets disconnected
	// releaseService();
	// }
	//
	// public void onServiceDisconnected(ComponentName name) {
	// service = null;
	// }
	// }

	private void removeClock(long position) {
		if (this.lastSelectedPosition < this.gridView.getCount()) {
			Object selectedItem = this.gridView
					.getItemAtPosition(this.lastSelectedPosition);
			if (selectedItem != null) {
				this.clockViewAdapter
						.removeAlarmClock((AlarmClock) selectedItem);
			}
		}
	}

	private void addClock(boolean isOneTimeAlarm) {
		Intent intent = new Intent(getApplicationContext(),
				AlarmConfigurationActivity.class);
		intent.putExtra(MainActivity.IS_ONE_TIME_ALARM, isOneTimeAlarm);
		startActivity(intent);
	}

	private void cloneClock(long position) {
		if (this.lastSelectedPosition < this.gridView.getCount()) {
			Object selectedItem = this.gridView
					.getItemAtPosition(this.lastSelectedPosition);
			if (selectedItem != null) {
				// call the intent
				Intent intent = new Intent(getApplicationContext(),
						AlarmConfigurationActivity.class);
				intent.putExtra(MainActivity.INDEX_ALARM,
						this.lastSelectedPosition);
				intent.putExtra(MainActivity.IS_CLONE, true);
				startActivity(intent);
			}
		}
	}

	private void editClock(long position) {
		this.lastSelectedPosition = (int) position;
		Intent intent = new Intent(getApplicationContext(),
				AlarmConfigurationActivity.class);
		intent.putExtra(MainActivity.INDEX_ALARM, this.lastSelectedPosition);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, EDIT_ID, 0, getResources().getString(R.string.edit));
		menu.add(0, CLONE_ID, 0, getResources().getString(R.string.clone));
		menu.add(0, DELETE_ID, 0, getResources().getString(R.string.delete));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		this.lastSelectedPosition = info.position;
		switch (item.getItemId()) {
		case EDIT_ID:
			editClock(this.lastSelectedPosition);
			break;
		case CLONE_ID:
			cloneClock(this.lastSelectedPosition);
			break;
		case DELETE_ID:
			removeClock(this.lastSelectedPosition);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private class ClockViewAdapter extends BaseAdapter {
		private final List<AlarmClock> alarmClocks;
		public View inflateView;

		public ClockViewAdapter(List<AlarmClock> alarmClocks) {
			this.alarmClocks = alarmClocks;
		}

		/**
		 * Removes an alarm clock from the list.
		 */
		public void removeAlarmClock(AlarmClock alarmClock) {
			MainActivity.this.removedAlarmClocks.add(alarmClock);
			this.alarmClocks.remove(alarmClock);
			notifyDataSetChanged();

			AlarmScheduler.saveToFile(MainActivity.this, this.alarmClocks);
			AlarmScheduler.scheduleAlarm(MainActivity.this, true, closestDate);
		}

		@Override
		public int getCount() {
			return MainActivity.this.alarmClocks.size();
		}

		@Override
		public Object getItem(int position) {
			return MainActivity.this.alarmClocks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			AlarmClock alarmClock = MainActivity.this.alarmClocks.get(position);
			if (convertView == null) { // if it's not recycled, initialize some
				this.inflateView = ViewTools.inflateView(MainActivity.this,
						R.layout.alarmthumbnailview);
				this.inflateView.setDrawingCacheEnabled(true);
			} else {
				this.inflateView = convertView;
			}
			View view = this.inflateView.findViewById(R.id.nameSeparator);
			if (alarmClock.isEnabled()) {
				this.inflateView
						.setBackgroundResource(R.drawable.green_border_32);
				view.setBackgroundColor(getResources().getColor(
						R.color.soft_green));
			} else {
				this.inflateView
						.setBackgroundResource(R.drawable.red_border_32);
				view.setBackgroundColor(getResources().getColor(
						R.color.soft_red));
			}

			TextView textView = ((TextView) this.inflateView
					.findViewById(R.id.digitalClockDisplay));
			textView.setText(dateToString(alarmClock));
			View daysThumbnailTableLayout = this.inflateView
					.findViewById(R.id.daysThumbnailTableLayout);
			if (alarmClock.isOneTimeAlarm()) {
				daysThumbnailTableLayout.setVisibility(View.GONE);
				textView.setLineSpacing(10, 1);
			} else {
				textView.setLineSpacing(0, 1);
				daysThumbnailTableLayout.setVisibility(View.VISIBLE);
				updateButton(this.inflateView, R.id.mondayButton, alarmClock,
						Calendar.MONDAY);
				updateButton(this.inflateView, R.id.tuesdayButton, alarmClock,
						Calendar.TUESDAY);
				updateButton(this.inflateView, R.id.wednesdayButton,
						alarmClock, Calendar.WEDNESDAY);
				updateButton(this.inflateView, R.id.thursdayButton, alarmClock,
						Calendar.THURSDAY);
				updateButton(this.inflateView, R.id.fridayButton, alarmClock,
						Calendar.FRIDAY);
				updateButton(this.inflateView, R.id.saturdayButton, alarmClock,
						Calendar.SATURDAY);
				updateButton(this.inflateView, R.id.sundayButton, alarmClock,
						Calendar.SUNDAY);
			}

			TextView nameTextView = ((TextView) this.inflateView
					.findViewById(R.id.nameTextView));
			nameTextView.setText(alarmClock.getName());
			FixedAnalogClock fixedAnalogClock = ((FixedAnalogClock) this.inflateView
					.findViewById(R.id.analogClockDisplay));
			fixedAnalogClock.setDate(alarmClock.getDate());

			ImageView fadingImageView = (ImageView) this.inflateView
					.findViewById(R.id.fadingImageView);
			ImageView vibrateImageView = (ImageView) this.inflateView
					.findViewById(R.id.vibrateImageView);

			fadingImageView
					.setVisibility(alarmClock.getFading() != 0 ? View.VISIBLE
							: View.INVISIBLE);
			vibrateImageView
					.setVisibility(alarmClock.isVibrate() ? View.VISIBLE
							: View.INVISIBLE);

			return this.inflateView;
		}

		private void updateButton(View parentView, int viewId,
				AlarmClock alarmClock, int day) {
			ImageView imageView = ((ImageView) parentView.findViewById(viewId));
			if (alarmClock.isDayActive(day)) {
				imageView
						.setImageResource(R.drawable.green_button_transparent_16);
			} else {
				imageView
						.setImageResource(R.drawable.red_button_transparent_16);
			}
		}
	}

	private String dateToString(AlarmClock alarmClock) {
		String pattern = new String();
		if (alarmClock.isOneTimeAlarm()) {
			if (this.is24HourView) {
				pattern = "EEE d MMM \n";
			} else {
				pattern = "EEE, d MMM \n";
			}
		}
		if (this.is24HourView) {
			pattern += "HH:mm";
		} else {
			pattern += "KK:mm a";
		}
		this.dateFormat.applyPattern(pattern);
		return this.dateFormat.format(alarmClock.getDate());
	}

	// Create runnable for posting
	// final Runnable mUpdateResults = new Runnable() {
	// public void run() {
	// updateResultsInUi();
	// }
	// };
	//
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		String errorMessage = null;
		switch (id) {
		// case ARM_SERVICE_NOT_FOUND_DIALOG:
		// errorMessage = getString(R.string.armServiceNotFound);
		// break;
		// case 0xF0000004:
		// errorMessage = getString(R.string.licenseNotRequested);
		// break;
		// case 0xF0000008:
		// errorMessage = getString(R.string.licenseIssueFailure);
		// break;
		// case 0xF000000E:
		// errorMessage = getString(R.string.wrongCommunication);
		// break;
		// case 0xF0000009:
		// errorMessage = getString(R.string.failedVerifyPurchase);
		// break;
		// case 0xF000000A:
		// errorMessage = getString(R.string.nonRegisteredUser);
		// break;
		// case 0xF000000C:
		// errorMessage = getString(R.string.licenseNotIssued);
		// break;
		// case 0xF000000D:
		// errorMessage = getString(R.string.licenseNotVerified);
		// break;
		// case 0xF0000011:
		// errorMessage = getString(R.string.unknownPhoneNumber);
		// break;
		// case 0xF0000012:
		// errorMessage = getString(R.string.applicationInfoNotFound);
		// break;
		// case 0xF0000013:
		// errorMessage = getString(R.string.dataCommunicationNotInService);
		// break;
		// case 0xF0000014:
		// errorMessage = getString(R.string.tStoreNotInstalled);
		// break;
		// case 0xF0000030:
		// errorMessage = getString(R.string.licenseFileNotFound);
		// break;
		// case 0xF0000031:
		// errorMessage = getString(R.string.licenseVersionError);
		// break;
		// case 0xF0000032:
		// errorMessage = getString(R.string.wrongLicenseMacValue);
		// break;
		// case 0xF0000033:
		// errorMessage = getString(R.string.MDNLicenseValueDoesNotExist);
		// break;
		// case 0xF0000034:
		// errorMessage = getString(R.string.failedMatchedLicense);
		// break;
		// case 0xF0000035:
		// errorMessage = getString(R.string.matchingAIDLicenseFailure);
		// break;
		case NO_LICENSE_DIALOG:

			// We have only one dialog.
			return new AlertDialog.Builder(this).setTitle(
					R.string.unlicensed_dialog_title).setMessage(
					R.string.unlicensed_dialog_body).setOnCancelListener(
					new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					}).setPositiveButton(R.string.buy_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent marketIntent = new Intent(
									Intent.ACTION_VIEW,
									Uri
											.parse("http://market.android.com/details?id="
													+ getPackageName()));
							startActivity(marketIntent);
						}
					}).setNegativeButton(R.string.quit_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create();
		case LICENSE_ERROR_DIALOG:

			// We have only one dialog.
			return new AlertDialog.Builder(this).setTitle(R.string.error)
					.setMessage(R.string.error_during_check_license)
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					}).setNegativeButton(R.string.quit_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							}).create();
		default:
			errorMessage = getString(R.string.unknownError);
			break;
		}
		if (errorMessage != null) {
			AlertDialog appNotValidDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.licenseError).setMessage(errorMessage)
					// .setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							}).setOnCancelListener(
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									finish();

								}
							}).create();
			return appNotValidDialog;
		}
		return null;
	}

	private AlertDialog aboutDialog;
	private AlertDialog helpDialog;
	private GridView gridView;
	private Date closestDate;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_help:
			if (this.helpDialog == null) {
				View layout = ViewTools.inflateView(this, R.layout.helpview);
				this.helpDialog = new AlertDialog.Builder(this).setView(layout)
						.create();
			}
			this.helpDialog.show();
			return true;
		case R.id.menu_about:
			if (this.aboutDialog == null) {
				View layout = ViewTools.inflateView(this, R.layout.aboutview);
				// Retrieve the version
				PackageManager pm = getPackageManager();
				PackageInfo pi;
				try {
					pi = pm.getPackageInfo(getPackageName(), 0);
					TextView versionTextView = (TextView) layout
							.findViewById(R.id.versionTextView);
					versionTextView.setText(pi.versionName);
				} catch (NameNotFoundException e) {
				}
				this.aboutDialog = new AlertDialog.Builder(this)
						.setView(layout).create();
			}
			this.aboutDialog.show();
			return true;
		case R.id.menu_preferences:

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return false;
	}
}
