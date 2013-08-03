package in.co.praveenkumar.mugshot;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	public final static String DEBUG_TAG = "MainActivity";
	AppSettings settings;
	DropboxHandler dropbox;

	// Android widgets
	TextView unlockCountText;
	Button dropboxButton;
	SeekBar AttemptsCountBar;
	TextView frontCamStatus;
	ToggleButton systemAdminSwitch;
	TextView deviceAdminStatusText;
	EditText[] contactText = new EditText[3];
	EditText contactsMessageText;
	Button contactsUpdateButton;
	CheckBox contactsCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Read settings
		settings = new AppSettings(getApplicationContext());

		// Dropbox
		dropbox = new DropboxHandler(getApplicationContext());
		dropbox.checkAppKeySetup();

		// Assign widgets -- Assigns widgets to widget variables
		AssignWidgets();

		// Status
		CheckStatusValues();

		// Settings
		layoutValuesFromSettings();
	}

	@Override
	protected void onResume() {
		super.onResume();
		dropbox.onResume();
		CheckStatusValues();

	}

	public void layoutValuesFromSettings() {
		// Setting values to existing preferences
		int attemptsCount = settings.getAttemptsCount();

		unlockCountText.setText("failed attempts before capture : "
				+ attemptsCount);

		AttemptsCountBar.setProgress(attemptsCount - 1);// -1 because it starts
														// from 0
		AttemptsCountBar.setOnSeekBarChangeListener(seekBarChangeListener);

		// Contacts and message
		for (int i = 1; i < 3; i++) {
			String contactNumber = settings.getPrefs("contact" + i);
			if (contactNumber.compareTo("") != 0)
				contactText[i - 1].setText(contactNumber);
		}
		String contactsMessage = settings.getPrefs("message");
		if (contactsMessage.compareTo("") != 0)
			contactsMessageText.setText(contactsMessage);

		contactsUpdateButton.setOnClickListener(contactsUpdateButtonListener);

	}

	public void CheckStatusValues() {
		// Front camera check //
		int cameraId = -1;
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				break;
			}
		}
		if (cameraId < 0) {
			// No front cam :(
			frontCamStatus.setText("Front camera not found !");
			frontCamStatus.setTextColor(Color.parseColor("#FF0000"));
		} else {
			// Front cam found
			frontCamStatus.setText("Front camera found");
			frontCamStatus.setTextColor(Color.parseColor("#00EE00"));
		}

		// Device Admin check //
		DevicePolicyManager DPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName deviceAdminComponentName = new ComponentName(this,
				MugShotDeviceAdminReceiver.class);
		boolean isActive = DPM.isAdminActive(deviceAdminComponentName);

		if (isActive) {
			systemAdminSwitch.setChecked(true);
			deviceAdminStatusText.setText("Device admin enabled !");
			deviceAdminStatusText.setTextColor(Color.parseColor("#00EE00"));
		} else {
			systemAdminSwitch.setChecked(false);
			deviceAdminStatusText.setText("Device admin not enabled");
			deviceAdminStatusText.setTextColor(Color.parseColor("#FF0000"));
		}
		systemAdminSwitch
				.setOnCheckedChangeListener(systemAdminSwitchChangeListener);

		// Dropbox integration check //
		dropboxButton.setOnClickListener(dropboxButtonListener);
		if (dropbox.getLoggedInStatus()) {
			dropboxButton.setText("Unlink dropbox");
		} else {
			dropboxButton.setText("Link dropbox");
		}

		// Contacts status
		Log.d("CheckStatusValues",
				"Contacts pref : " + settings.getMessagingState());
		if (!settings.getMessagingState()) {
			contactsMessageText.setEnabled(false);
			for (int i = 0; i < 3; i++) {
				contactText[i].setEnabled(false);
			}
			contactsCheckBox.setChecked(false);
		} else {
			contactsMessageText.setEnabled(true);
			for (int i = 0; i < 3; i++) {
				contactText[i].setEnabled(true);
			}
			contactsCheckBox.setChecked(true);
		}
		contactsCheckBox
				.setOnCheckedChangeListener(contactsStateChangeListener);
	}

	private void AssignWidgets() {
		unlockCountText = (TextView) findViewById(R.id.unlockCountText);
		dropboxButton = (Button) findViewById(R.id.dropboxButton);
		AttemptsCountBar = (SeekBar) findViewById(R.id.failedAttemptsCountBar);
		frontCamStatus = (TextView) findViewById(R.id.frontCamStatus);
		systemAdminSwitch = (ToggleButton) findViewById(R.id.deviceAdminSwitch);
		deviceAdminStatusText = (TextView) findViewById(R.id.deviceAdminStatusText);
		contactText[0] = (EditText) findViewById(R.id.contact1);
		contactText[1] = (EditText) findViewById(R.id.contact2);
		contactText[2] = (EditText) findViewById(R.id.contact3);
		contactsMessageText = (EditText) findViewById(R.id.contactsMessage);
		contactsUpdateButton = (Button) findViewById(R.id.contactsButton);
		contactsCheckBox = (CheckBox) findViewById(R.id.contactsState);

	}

	private OnCheckedChangeListener contactsStateChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton checkbox, boolean value) {
			Boolean state = checkbox.isChecked();
			settings.setMessagingState(state);
			contactsMessageText.setEnabled(state);
			for (int i = 0; i < 3; i++) {
				contactText[i].setEnabled(state);
			}
			contactsCheckBox.setChecked(state);
		}
	};

	private OnClickListener contactsUpdateButtonListener = new OnClickListener() {

		@Override
		public void onClick(View dropboxAuthButton) {
			String[] contactDetails = new String[4];
			contactDetails[0] = contactsMessageText.getText().toString();
			for (int i = 1; i < 3; i++) {
				contactDetails[i] = contactText[i - 1].getText().toString();
			}
			settings.savePrefs(contactDetails);
		}
	};

	private OnClickListener dropboxButtonListener = new OnClickListener() {

		@Override
		public void onClick(View dropboxAuthButton) {
			System.out.println("Dropbox requested !");
			// This logs you out if you're logged in, or vice versa
			if (dropbox.getLoggedInStatus()) {
				dropbox.logOut();
				dropboxButton.setText("Link dropbox");
			} else {
				dropbox.logIn();
				dropboxButton.setText("Unlink dropbox");
			}
		}
	};

	private OnCheckedChangeListener systemAdminSwitchChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton adminStatusSwitch,
				boolean value) {
			adminStatusSwitch.setChecked(!value);
			openAdminIntent();
		}
	};

	private OnSeekBarChangeListener seekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar AttemptsCountBar) {
			int progressValue = AttemptsCountBar.getProgress() + 1;

			TextView unlockCountText = (TextView) findViewById(R.id.unlockCountText);
			unlockCountText.setTextColor(Color.parseColor("#000000"));
			unlockCountText.setText("failed attempts before capture : "
					+ progressValue);

			// Saving settings
			settings = new AppSettings(getApplicationContext());
			settings.saveAttemptsCount(progressValue);
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgressChanged(SeekBar AttemptsCountBar, int value,
				boolean arg2) {

			TextView unlockCountText = (TextView) findViewById(R.id.unlockCountText);
			unlockCountText.setTextColor(Color.parseColor("#3F9BBF"));
			unlockCountText.setText("failed attempts before capture : "
					+ (value + 1));

		}
	};

	public void openAdminIntent() {
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		ComponentName deviceAdminComponentName = new ComponentName(this,
				MugShotDeviceAdminReceiver.class);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
				deviceAdminComponentName);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				"We need this to monitor failed unlock attempts only");
		startActivityForResult(intent, 5);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
