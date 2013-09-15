package jp.gr.java_conf.youkinjoh.android.LeapMotionProxy;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefActivity extends PreferenceActivity {

	private final String TAG = this.getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setting);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		{
			EditTextPreference pref = (EditTextPreference)findPreference(getText(R.string.preference_key_port_number));
			String  defVal = getString(R.string.default_value_port_number);
			String prefKey = getApplicationContext().getString(R.string.preference_key_port_number);
			pref.setSummary(prefs.getString(prefKey, defVal));
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String val = (String)newValue;
					int port = Integer.parseInt(val, 10);
					try {
						InetSocketAddress address = new InetSocketAddress(port);
					} catch (IllegalArgumentException e) {
						Log.i(TAG, getText(R.string.message_port_number_is_illegal_value).toString(), e);
						Builder alert = new AlertDialog.Builder(PrefActivity.this);
						alert.setMessage(R.string.message_port_number_is_illegal_value);
						alert.setPositiveButton(android.R.string.ok, null);
						alert.create().show();
						return false;
					}
					preference.setSummary(Integer.toString(port, 10));
					return true;
				}
			});
		}

		{
			EditTextPreference pref = (EditTextPreference)findPreference(getText(R.string.preference_key_forwarding_url));
			String  defVal = getString(R.string.default_value_forwarding_url);
			String prefKey = getApplicationContext().getString(R.string.preference_key_forwarding_url);
			pref.setSummary(prefs.getString(prefKey, defVal));
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					String val = (String)newValue;
					URI uri = null;
					try {
						uri = new URI(val);
					} catch (URISyntaxException e) {
						Log.i(TAG, getText(R.string.message_url_is_syntax_error).toString(), e);
						Builder alert = new AlertDialog.Builder(PrefActivity.this);
						alert.setMessage(R.string.message_url_is_syntax_error);
						alert.setPositiveButton(android.R.string.ok, null);
						alert.create().show();
						return false;
					}
					String scheme = uri.getScheme();
					if (!"ws".equals(scheme) && !"wss".equals(scheme)) {
						Log.w(TAG, getText(R.string.message_this_scheme_is_not_allowed).toString());
						Builder alert = new AlertDialog.Builder(PrefActivity.this);
						alert.setMessage(R.string.message_this_scheme_is_not_allowed);
						alert.setPositiveButton(android.R.string.ok, null);
						alert.create().show();
						return false;
					}
					preference.setSummary(val);
					return true;
				}
			});
		}

	}

}
