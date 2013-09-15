package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

import java.util.ArrayList;

import jp.gr.java_conf.youkinjoh.android.JettyAndroidLogger;
import jp.gr.java_conf.youkinjoh.android.LeapMotionProxy.WSServlet.OnConnectionListener;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Logger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class WSServerService extends Service {

	private Context serviceContext = this;
	private android.os.Handler h = new android.os.Handler();

	private final String TAG = this.getClass().getSimpleName();

	private Logger logger = new JettyAndroidLogger("JettyLog");
	private Server server = null;

	OnConnectionListener onConnectionListener = new OnConnectionListener() {
		@Override
		public void onConnection(String message) {
			showMessage(message);
		}
	};

	@Override
	public void onCreate() {

		super.onCreate();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		int port = -1;
		{
			String  defVal = getString(R.string.default_value_port_number);
			String prefKey = getApplicationContext().getString(R.string.preference_key_port_number);
			port = Integer.valueOf(prefs.getString(prefKey, defVal), 10);
		}

		String url = null;
		{
			String  defVal = getString(R.string.default_value_forwarding_url);
			String prefKey = getApplicationContext().getString(R.string.preference_key_forwarding_url);
			url = prefs.getString(prefKey, defVal);
		}

		startServer(port, url);

		int icon = R.drawable.notification_icon;
		CharSequence tickerText = "Start Server Service";
		long when = System.currentTimeMillis();
		Context context = getApplicationContext();
		CharSequence contentTitle = getText(R.string.app_name);
		CharSequence contentText = "running server";

		Intent notificationIntent = new Intent(this, LeapMotionProxy.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		startForeground(1, notification);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return START_REDELIVER_INTENT;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopServer();
	}

	private void startServer(int portNumber, String forwardingUrl) {

		server = new Server(portNumber);
		ArrayList<Handler> handlers = new ArrayList<Handler>();

		try {
			WSServlet servlet = new WSServlet(forwardingUrl);
			servlet.setOnConnectionListener(onConnectionListener);
			ServletHolder sh = new ServletHolder(servlet);
			ServletContextHandler sch = new ServletContextHandler();
			sch.setLogger(logger);
			sch.addServlet(sh, "/*");
			handlers.add(sch);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			showMessage(e.getMessage());
		}

		HandlerList hl = new HandlerList();
		hl.setHandlers(handlers.toArray(new Handler[]{}));
		server.setHandler(hl);
		try {
			server.start();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			showMessage(e.getMessage());
		}

	}

	private void stopServer() {
		try {
			server.stop();
			server = null;
			stopForeground(true);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			showMessage(e.getMessage());
		}
	}

	private void showMessage(final String message) {
		final String appName = getText(R.string.app_short_name).toString();
		new Thread(new Runnable() {
			@Override
			public void run() {
				h.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(serviceContext, appName + " | " + message, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}).start();
	}

}
