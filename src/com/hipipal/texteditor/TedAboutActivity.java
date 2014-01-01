package com.hipipal.texteditor;

import android.os.Bundle;
import fr.xgouchet.androidlib.ui.activity.AboutActivity;

public class TedAboutActivity extends AboutActivity {

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_about);
	}
}
