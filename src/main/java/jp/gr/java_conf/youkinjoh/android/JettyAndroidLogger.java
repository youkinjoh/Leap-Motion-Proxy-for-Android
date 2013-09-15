package jp.gr.java_conf.youkinjoh.android;

import org.eclipse.jetty.util.log.Logger;

import android.util.Log;

public class JettyAndroidLogger implements Logger {

	private final String TAG;

	public JettyAndroidLogger(String tag) {
		TAG = tag;
	}

	@Override
	public String getName() {
		return this.getName();
	}

	@Override
	public void warn(String msg, Object... args) {
		Log.w(TAG, msg);
	}

	@Override
	public void warn(Throwable thrown) {
		Log.w(TAG, thrown);
	}

	@Override
	public void warn(String msg, Throwable thrown) {
		Log.w(TAG, msg, thrown);
	}

	@Override
	public void info(String msg, Object... args) {
		Log.i(TAG, msg);
	}

	@Override
	public void info(Throwable thrown) {
		Log.i(TAG, thrown.getMessage(), thrown);
	}

	@Override
	public void info(String msg, Throwable thrown) {
		Log.i(TAG, msg, thrown);
	}

	@Override
	public boolean isDebugEnabled() {
		//TODO no need
		return false;
	}

	@Override
	public void setDebugEnabled(boolean enabled) {
		//TODO no need
	}

	@Override
	public void debug(String msg, Object... args) {
		Log.d(TAG, msg);
	}

	@Override
	public void debug(Throwable thrown) {
		Log.d(TAG, thrown.getMessage(), thrown);
	}

	@Override
	public void debug(String msg, Throwable thrown) {
		Log.d(TAG, msg, thrown);
	}

	@Override
	public Logger getLogger(String name) {
		//TODO no need
		return null;
	}

	@Override
	public void ignore(Throwable ignored) {
		//TODO no need
	}

}
