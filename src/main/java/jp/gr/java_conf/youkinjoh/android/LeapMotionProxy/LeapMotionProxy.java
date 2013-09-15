package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LeapMotionProxy extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Button buttonStartStop = (Button)findViewById(R.id.button_start_stop);
		buttonStartStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LeapMotionProxy.this, WSServerService.class);
				if (!isRunningService(WSServerService.class)) {
					startService(intent);
				} else {
					stopService(intent);
				}
				displayServiceStatus();
			}
		});
		displayServiceStatus();
		displayPreferenceValue();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.optionmenu_preferences:
			if (!isRunningService(WSServerService.class)) {
				startActivityForResult(new Intent(this,PrefActivity.class), 0);
			} else {
				Builder alert = new AlertDialog.Builder(this);
				alert.setMessage(R.string.message_only_when_a_server_is_stoped);
				alert.setPositiveButton(android.R.string.ok, null);
				alert.create().show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		displayPreferenceValue();
	}

	private void displayServiceStatus() {
		Button buttonStartStop = (Button)findViewById(R.id.button_start_stop);
		TextView displayAreaServerStatus = (TextView)findViewById(R.id.display_area_server_status);
		if (!isRunningService(WSServerService.class)) {
			buttonStartStop.setText(getString(R.string.button_text_start));
			displayAreaServerStatus.setText(R.string.button_text_stop);
		} else {
			buttonStartStop.setText(getString(R.string.button_text_stop));
			displayAreaServerStatus.setText(R.string.button_text_start);
		}
	}

	private void displayPreferenceValue() {

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		{
			String  defVal    = getString(R.string.default_value_port_number);
			String  prefKey   = getApplicationContext().getString(R.string.preference_key_port_number);
			int     curVal    = Integer.valueOf(pref.getString(prefKey, defVal), 10);
			TextView textView = (TextView)findViewById(R.id.display_area_port_number);
			textView.setText(String.valueOf(curVal));
		}

		{
			String  defVal    = getString(R.string.default_value_forwarding_url);
			String  prefKey   = getApplicationContext().getString(R.string.preference_key_forwarding_url);
			String  curVal    = pref.getString(prefKey, defVal);
			TextView textView = (TextView)findViewById(R.id.display_area_forwarding_url);
			textView.setText(curVal);
		}

	}

	public boolean isRunningService(Class<? extends Service> clazz) {
		String serviceName = clazz.getCanonicalName();
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo info : infos) {
			if (serviceName.equals(info.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
