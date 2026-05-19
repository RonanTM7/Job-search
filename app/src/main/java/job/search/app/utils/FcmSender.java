package job.search.app.utils;

import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FcmSender {
    private static final String TAG = "FcmSender";
    // NOTE: This is a legacy FCM API approach. For a production app, use FCM v1 with OAuth2.
    // WARNING: Storing the server key in the client app is a major security risk.
    // It allows anyone who decompiles the APK to send notifications to all your users.
    // This is implemented here only because of the user's specific request to avoid Cloud Functions costs.
    private static final String SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE";
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    public static void sendNotification(String targetToken, String title, String body) {
        if (SERVER_KEY.equals("YOUR_FCM_SERVER_KEY_HERE")) {
            Log.w(TAG, "FCM Server Key not set. Notification not sent.");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(FCM_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);
                json.put("to", targetToken);
                json.put("notification", notification);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "FCM Response Code: " + responseCode);

                if (responseCode == 200) {
                    Scanner s = new Scanner(conn.getInputStream()).useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";
                    Log.d(TAG, "FCM Response: " + response);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error sending FCM", e);
            }
        }).start();
    }
}
