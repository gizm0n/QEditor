package com.hipipal.texteditor;

import java.io.File;

import com.hipipal.texteditor.common.Constants;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import fr.xgouchet.androidlib.ui.activity.BrowsingActivity;

public class TedOpenActivity extends BrowsingActivity implements
		OnClickListener, Constants {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int request;
		Bundle extras;

		// Setup content view
		setContentView(R.layout.m_list_with_bb);

		// buttons
		//findViewById(R.id.buttonCancel).setOnClickListener(this);

		// set default result
		setResult(RESULT_CANCELED, null);

		// show the title as toast
		extras = getIntent().getExtras();
		if (extras != null)
			request = extras.getInt(EXTRA_REQUEST_CODE);
		else
			request = -1;

		switch (request) {
		case REQUEST_OPEN:
			Toast.makeText(this, R.string.toast_open_select, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_open_select, Style.INFO);
			break;
		case REQUEST_HOME_PAGE:
			Toast.makeText(this, R.string.toast_home_page_select, Toast.LENGTH_SHORT).show();

			//Crouton.showText(this, R.string.toast_home_page_select, Style.INFO);
			break;
		}

	}

	/**
	 * @see Activity#onKeyUp(int, KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// navigate to parent folder
			File parent = mCurrentFolder.getParentFile();
			if ((parent != null) && (parent.exists())) {
				fillFolderView(parent);
				return true;
			} else {
				finish();
				return false;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buttonCancel) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowserActivity#onFolderClick(java.io.File)
	 */
	@Override
	protected boolean onFolderClick(File folder) {
		return true;
	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowsingActivity#onFolderViewFilled()
	 */
	@Override
	protected void onFolderViewFilled() {

	}

	/**
	 * @see fr.xgouchet.androidlib.ui.activity.BrowserActivity#onFileClick(java.io.File)
	 */
	@Override
	protected void onFileClick(File file) {
		if (setOpenResult(file))
			finish();
	}

	/**
	 * Set the result of this activity to open a file
	 * 
	 * @param file
	 *            the file to return
	 * @return if the result was set correctly
	 */
	protected boolean setOpenResult(File file) {
		Intent result;

		if (!file.canRead()) {
			Toast.makeText(this, R.string.toast_file_cant_read, Toast.LENGTH_SHORT).show();

			//Crouton.showText(this, R.string.toast_file_cant_read, Style.ALERT);
			return false;
		}

		result = new Intent();
		result.putExtra("path", file.getAbsolutePath());

		setResult(RESULT_OK, result);
		return true;
	}
	
	public void onHome(View v) {
		finish();
	}


}
