package in.co.praveenkumar.mugshot;

import in.co.praveenkumar.mugshot.MyLocation.LocationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class sendMessages {
	private String Location = "";
	private String Lat = "";
	private String Lng = "";
	private Context context = null;

	public sendMessages(Context contextReceived) {
		context = contextReceived;

		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {
				// Got the location!
				AppSettings appSettings = new AppSettings(context);
				getAddress(location);

				// Get preferences and send message
				String prefMessage = appSettings.getPrefs("message");
				if (prefMessage.compareTo("") == 0)
					prefMessage = "Looks like someone from below address got hold of my mobile.";
				String message = prefMessage + "\n" + "Location : " + Location;
				Log.d("Sent message", message);

				for (int i = 1; i < 6; i++) {
					String contactNumber = appSettings.getPrefs("contact" + i);
					if (contactNumber.compareTo("") != 0)
						sendSMS(contactNumber, message);
				}
			}
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(context, locationResult);
	}

	public void sendSMS(String phoneNumber, String message) {
		Toast.makeText(context, "Sending a message to \n" + phoneNumber,
				Toast.LENGTH_SHORT).show();
		try {
			SmsManager sms = SmsManager.getDefault();
			ArrayList<String> parts = sms.divideMessage(message);
			sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();

		}

	}

	public void getAddress(Location location) {
		Lat = location.getLatitude() + "";
		Lng = location.getLongitude() + "";
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> listAddresses = geocoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);
			if (null != listAddresses && listAddresses.size() > 0) {
				Location = listAddresses.get(0).getAddressLine(0)
						+ "\n"
						+ listAddresses.get(0).getAddressLine(1)
						+ "\n"
						+ listAddresses.get(0).getAddressLine(2)
						+ "\n"
						+ "Latitude : "
						+ Lat
						+ "\n"
						+ "Longitude : "
						+ Lng
						+ "\n"
						+ "A pic of the person uploaded to owners dropbox"
						+ "\n\n"
						+ "By : MugShot "
						+ "https://play.google.com/store/apps/details?id=in.co.praveenkumar.mugshot";
			}
		} catch (IOException e) {
			e.printStackTrace();
			Location = "Failed to fetch address. No internet connection :( \n"
					+ "Latitude : "
					+ Lat
					+ "\n"
					+ "Longitude : "
					+ Lng
					+ "\n\n"
					+ "By : MugShot "
					+ "https://play.google.com/store/apps/details?id=in.co.praveenkumar.mugshot";
		}
	}
}
