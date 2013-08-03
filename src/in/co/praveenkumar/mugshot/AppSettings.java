package in.co.praveenkumar.mugshot;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class AppSettings {
	private static final String APP_SHARED_PREFS = "mdroid_preferences";
	private SharedPreferences appSharedPrefs;
	private Editor prefsEditor;

	// Preferences
	public AppSettings(Context context) {
		this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,
				Activity.MODE_PRIVATE);
		this.prefsEditor = appSharedPrefs.edit();
	}

	public int getAttemptsCount() {
		int value = appSharedPrefs.getInt("attemptsCount", 2);
		return value;
	}

	public void saveAttemptsCount(int prefValue) {
		prefsEditor.putInt("attemptsCount", prefValue);
		prefsEditor.commit();
	}

	public String getPrefs(String name) {
		String value = "";
		if (name.compareTo("message") == 0) {
			value = appSharedPrefs.getString(name, "");
		} else {
			value = appSharedPrefs.getString(name, "");
		}
		return value;
	}

	public void savePrefs(String... prefValues) {
		prefsEditor.putString("message", prefValues[0]);
		prefsEditor.putString("contact1", prefValues[1]);
		prefsEditor.putString("contact2", prefValues[2]);
		prefsEditor.putString("contact3", prefValues[3]);
		prefsEditor.commit();
	}

	public Boolean getMessagingState() {
		return appSharedPrefs.getBoolean("messagingState", true);
	}

	public void setMessagingState(Boolean state) {
		prefsEditor.putBoolean("messagingState", state);
		prefsEditor.commit();
	}
}