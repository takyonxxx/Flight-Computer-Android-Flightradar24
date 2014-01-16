package com.flightcomputer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Random;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.util.Log;
public class LeonardoLiveWriter implements PositionWriter {
	private String ourVersion;
	private int expectedIntervalSecs;
	private String phoneName = android.os.Build.MODEL;
	private String programName = "Flight Computer";
	private String vehicleName;
	private int vehicleType = 1;
	private String gpsType = "Internal GPS";
	private String userName;
	private String password;
	private String trackURL, clientURL;
	public int sessionId = (new Random()).nextInt(0x7fffffff);
	private int packetNum = 1;	
	Context basecontext;
	public LeonardoLiveWriter(Context context, String serverURL, String userName, String password,
			String vehicleName, int vehicleType, int expectedInterval) throws Exception {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);

			ourVersion = pi.versionName;
		} catch (NameNotFoundException eNnf) {
			throw new RuntimeException(eNnf); // We better be able to find the
			// info about our own package
		}

		URL url = new URL(serverURL + "/track.php");
		trackURL = url.toString();
		url = new URL(serverURL + "/client.php");
		clientURL = url.toString();

		this.userName = userName;
		this.password = password;
		this.vehicleType = vehicleType;
		this.vehicleName = vehicleName;
		expectedIntervalSecs = expectedInterval;
		this.basecontext=context;
		doLogin(); // Login here, so we can find out about bad passwords ASAP
	}

	static int PACKET_START = 2; // FIXME, lookup java const syntax
	static int PACKET_END = 3;
	static int PACKET_POINT = 4;
	static int PACKET_Sessionless = 1;
	static String normalizeURL(String url) {
		return url.replace(' ', '+'); // FIXME, do a better job of this
	}
	void sendPacket(int packetType, String options) throws IOException {
		try {
			String urlstr = String.format(Locale.US,
					"%s?leolive=%d&sid=%d&pid=%d&%s", trackURL, packetType,
					sessionId, packetNum, options);

			URL url = new URL(normalizeURL(urlstr));

			url.openStream().close();
		} catch (MalformedURLException ex) {
			// We should have caught this in the constructor
			throw new RuntimeException(ex);
		}
		packetNum++;
	}
	@Override
	public void emitEpilog() {
		try {
			// FIXME - add support for end of track types (need retrieve etc...)
			sendPacket(PACKET_END, "prid=0");
		} catch (IOException ex) {
			System.out.println("Ignoring on epilog: " + ex);
		}
	}
	long lastUpdateTime = SystemClock.elapsedRealtime();
	
	@Override
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed) {
		try {
			int groundKmPerHr = (int) groundSpeed;
			int unixTimestamp = (int) (time / 1000); // Convert from msecs to
			// secs
			long now = SystemClock.elapsedRealtime();
			if (lastUpdateTime + (expectedIntervalSecs *1000) < now)
			{
				String opts = String.format(Locale.US,
						"lat=%f&lon=%f&alt=%d&sog=%d&cog=%d&tm=%d", latitude,
						longitude, Float.isNaN(altitude) ? 0 : (int) altitude, groundKmPerHr, bearing,
						unixTimestamp);
				Log.d("XXX", opts);
				sendPacket(PACKET_POINT, opts);
				lastUpdateTime = SystemClock.elapsedRealtime();
			}
		} catch (IOException ex) {
			System.out.println("Ignoring on epilog: " + ex);
		}
	}
	@Override
	public void emitSessionlessPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed) {
		try {
			int groundKmPerHr = (int) groundSpeed;
			int unixTimestamp = (int) (time / 1000); // Convert from msecs to			
			long now = SystemClock.elapsedRealtime();
			if (lastUpdateTime + (expectedIntervalSecs *1000) < now)
			{
				String opts = String
						.format(
								Locale.US,
								"client=%s&v=%s&lat=%f&lon=%f&alt=%d&sog=%d&cog=%d&tm=%d&user=%s&pass=%s",
								programName,ourVersion,latitude,
								longitude, Float.isNaN(altitude) ? 0 : (int) altitude, groundKmPerHr, bearing,
								unixTimestamp,userName, password);			
				sendPacket(PACKET_Sessionless, opts);
				lastUpdateTime = SystemClock.elapsedRealtime();
			}
		} catch (IOException ex) {
			System.out.println("Ignoring on epilog: " + ex);
		}
	}
	private void doLogin() throws Exception {		
		String urlstr = String.format("%s?op=login&user=%s&pass=%s", clientURL, userName, password);

		URL url = new URL(normalizeURL(urlstr));

		InputStream responseStream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
		String response = reader.readLine();

		try {
			int userID = Integer.parseInt(response);

			if (userID == 0)
				throw new Exception("Invalid username or password");

			Random a = new Random(System.currentTimeMillis());
			int rnd = Math.abs(a.nextInt());
			// we make an int with leftmost bit=1 ,
			// the next 7 bits random
			// (so that the same userID can have multiple active sessions)
			// and the next 3 bytes the userID
			sessionId = (rnd & 0x7F000000) | (userID & 0x00ffffff) | 0x80000000;			
		} catch (NumberFormatException ex) {
			throw new Exception("Unexpected server response");
		}
	}
	@Override
	public int getLWCount() {		
		return packetNum;		
	}
	@Override
	public void emitProlog() {
		try {
			String opts = String
					.format(
							Locale.US,
							"client=%s&v=%s&user=%s&pass=%s&phone=%s&gps=%s&trk1=%d&vtype=%d&vname=%s",
							programName, ourVersion, userName, password, phoneName, gpsType,
							expectedIntervalSecs, vehicleType, vehicleName);
			Log.d("XXX", opts);
			sendPacket(PACKET_START, opts);
		} catch (IOException ex) {
			System.out.println("FIXME, rethrow on connect failed " + ex);
		}
	}
	
}

