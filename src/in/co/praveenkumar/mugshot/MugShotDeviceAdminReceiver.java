package in.co.praveenkumar.mugshot;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

public class MugShotDeviceAdminReceiver extends DeviceAdminReceiver implements
		PictureCallback {
	static final String TAG = "MugShotDeviceAdminReceiver";
	static Context OnPswdFailedContext = null;
	private final String DEBUG_TAG = "SavePhoto";
	private Camera camera;
	private int cameraId = 0;
	private static int callBackTester = 0;
	private AppSettings settings;

	/** Called when this application is approved to be a device administrator. */
	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		Toast.makeText(context, "Device admin enabled", Toast.LENGTH_LONG)
				.show();
		Log.d(TAG, "onEnabled");
	}

	/** Called when this application is no longer the device administrator. */
	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		Toast.makeText(context, "Device admin disabled", Toast.LENGTH_LONG)
				.show();
		Log.d(TAG, "onDisabled");
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		super.onPasswordFailed(context, intent);
		Log.d(TAG, "onPasswordFailed");
		OnPswdFailedContext = context;

		// Intent i = new Intent(OnPswdFailedContext, photoTaker.class);
		// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// OnPswdFailedContext.startActivity(i);

		// Get failed attempts from system
		DevicePolicyManager DPM = getManager(OnPswdFailedContext);
		int failCount = DPM.getCurrentFailedPasswordAttempts();
		System.out.println("Fail count : " + failCount);

		// Get failed attempts preference
		settings = new AppSettings(OnPswdFailedContext);
		int failCountPref = settings.getAttemptsCount();

		if (failCount >= failCountPref) {

			System.gc();
			TakePicture();
			if (settings.getMessagingState()) {
				// Location obtained and messages sent by just instantiating
				@SuppressWarnings("unused")
				sendMessages smsWithLocation = new sendMessages(
						OnPswdFailedContext);
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// while (sravan == 0) {
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// }
			System.out.println("callBackTester value : " + callBackTester);
		} else {
			// Wait for count to match
			System.out.println("Waiting for counts match !");
			System.out.println("Failed count : " + failCount + " Pref count : "
					+ failCountPref);
		}
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		super.onPasswordSucceeded(context, intent);
		Log.d(TAG, "onPasswordSucceeded");
	}

	public void TakePicture() {
		if (!OnPswdFailedContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			Toast.makeText(OnPswdFailedContext, "No camera on this device",
					Toast.LENGTH_LONG).show();
		} else {
			cameraId = findFrontFacingCamera();
			if (cameraId < 0) {
				Toast.makeText(OnPswdFailedContext,
						"No front facing camera found.", Toast.LENGTH_LONG)
						.show();
			} else {
				try {
					if (camera == null)
						camera = Camera.open(cameraId);
					System.out.println("Camera opened ! Camera Id : "
							+ cameraId);

					SurfaceView view = new SurfaceView(OnPswdFailedContext);
					camera.setPreviewDisplay(view.getHolder());
					camera.startPreview();
					camera.takePicture(null, null, this);
					System.out.println("Taking picture using camera Id : "
							+ cameraId);
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(DEBUG_TAG,
							"Failed to open camera or take picture ! Camera Id : "
									+ cameraId);
					if (camera != null) {
						camera.release();
						camera = null;
					}
					callBackTester = 1;
				}

			}

		}
	}

	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				Log.d(DEBUG_TAG, "Camera found");
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}

	private File getDir() {
		File sdDir = Environment.getExternalStorageDirectory();
		return new File(sdDir, "MugShot");
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		System.out.println("Reached !");
		File pictureFileDir = getDir();

		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

			Log.d(DEBUG_TAG, "Can't create directory to save image.");
			Toast.makeText(OnPswdFailedContext,
					"Can't create directory to save image.", Toast.LENGTH_LONG)
					.show();
			return;

		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh.mm.ss");
		String date = dateFormat.format(new Date());
		String photoFile = "Mugger_" + date + ".jpg";

		String filename = pictureFileDir.getPath() + File.separator + photoFile;

		File pictureFile = new File(filename);

		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(data);
			fos.close();
			Log.d(DEBUG_TAG, "Image saved as " + filename);
			dropboxUpload(pictureFile);
		} catch (Exception error) {
			Log.d(DEBUG_TAG,
					"File" + filename + "not saved: " + error.getMessage());
			Toast.makeText(OnPswdFailedContext, "Image could not be saved.",
					Toast.LENGTH_LONG).show();
		}
		camera.release();
		camera = null;
		callBackTester = 1;
	}

	private void dropboxUpload(File file) {
		DropboxHandler dropbox = new DropboxHandler(OnPswdFailedContext);
		dropbox.checkAppKeySetup();
		if (dropbox.getLoggedInStatus()) {
			dropbox.uploadPicture(file);
			Log.d("Dropbox uploader", "File upload request sent !");
		} else {
			Log.d("Dropbox uploader", "Dropbox integration not found :(");
		}
	}

}
