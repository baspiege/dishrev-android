package com.appspot.dishrev;

import com.appspot.dishrev.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Web view to show dish rev web site.
 * 
 */
public class DishRevWebView extends Activity {

	private static final int MENU_REFRESH = 1;
	private WebView mWebView;

	private ValueCallback<Uri> mUploadMessage;
	private final static int FILECHOOSER_RESULTCODE = 1;

	@Override
	public void onCreate(Bundle aBundle) {

		super.onCreate(aBundle);
		setContentView(R.layout.main);

		mWebView = (WebView) findViewById(R.id.webview);

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);

		// Local Storage
		webSettings.setDatabaseEnabled(true);
		webSettings.setDatabasePath(getDir("databases", 0).getPath());
		webSettings.setDomStorageEnabled(true);

		// Geo location
		webSettings.setGeolocationEnabled(true);
		webSettings.setGeolocationDatabasePath(getDir("geolocation", 0)
				.getPath());

		// App cache
		webSettings.setAppCacheEnabled(true);
		webSettings.setAppCachePath(getDir("cache", 0).getPath());
		webSettings.setAllowFileAccess(true);

		// Welcome message
		Toast.makeText(getBaseContext(), "Welcome!", Toast.LENGTH_SHORT).show();

		mWebView.setWebChromeClient(new DishRevWebChromeClient());
		mWebView.setWebViewClient(new DishRevViewClient());

		// Main page
		mWebView.loadUrl("http://dishrev.appspot.com");
	}

	/**
	 * Override for file upload.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null
					: intent.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;

		}
	}

	/**
	 * Override configuration to avoid re-render when changing orientation.
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Invoked during init to give the Activity a chance to set up its Menu.
	 * 
	 * @param menu
	 *            the Menu to which entries may be added
	 * @return true
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_REFRESH, 0, R.string.menu_refresh);
		return true;
	}

	/**
	 * Invoked when the user selects an item from the Menu.
	 * 
	 * @param item
	 *            the Menu entry which was selected
	 * @return true if the Menu item was legit (and we consumed it), false
	 *         otherwise
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			mWebView.reload();
			return true;
		}

		return false;
	}

	/**
	 * Override onKeyDown for back button support.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		mWebView.setFocusable(true);
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Chrome client to link to device.
	 */
	private class DishRevWebChromeClient extends WebChromeClient {

		/**
		 * Geo location permission.
		 */
		public void onGeolocationPermissionsShowPrompt(String origin,
				GeolocationPermissions.Callback callback) {
			callback.invoke(origin, true, false);
		}

		/**
		 * Database quota.
		 */
		public void onExceededDatabaseQuota(String url,
				String databaseIdentifier, long currentQuota,
				long estimatedSize, long totalUsedQuota,
				WebStorage.QuotaUpdater quotaUpdater) {
			quotaUpdater.updateQuota(5 * 1024 * 1024);
		}

		// The undocumented magic method override
		// Eclipse will give error if @Override here
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {

			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("image/*");
			startActivityForResult(Intent.createChooser(i, "Image Browser"),
					FILECHOOSER_RESULTCODE);
		}
	}

	private class DishRevViewClient extends WebViewClient {

		/**
		 * Override to keep links inside in the webview.
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
	}
}