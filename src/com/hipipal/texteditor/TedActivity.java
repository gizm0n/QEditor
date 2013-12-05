package com.hipipal.texteditor;

import static fr.xgouchet.androidlib.data.FileUtils.deleteItem;
import static fr.xgouchet.androidlib.data.FileUtils.getCanonizePath;
import static fr.xgouchet.androidlib.data.FileUtils.renameItem;
import static fr.xgouchet.androidlib.ui.Toaster.showToast;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.hipipal.texteditor.common.Constants;
import com.hipipal.texteditor.common.RecentFiles;
import com.hipipal.texteditor.common.Settings;
import com.hipipal.texteditor.common.TedChangelog;
import com.hipipal.texteditor.common.TextFileUtils;
import com.hipipal.texteditor.ui.view.AdvancedEditText;
import com.hipipal.texteditor.undo.TextChangeWatcher;
import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.base._WBase;
import com.zuowuxuxi.util.NAction;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;
import com.hipipal.texteditor.BuildConfig;

import greendroid.widget.ActionBarItem;
import greendroid.widget.QuickActionWidget;
//import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;

public class TedActivity extends _ABaseAct implements Constants, TextWatcher,
		OnClickListener {
	public static final String TAG = "TED";
    protected QuickActionWidget mBarM;
    private int exitCount = 0;

    //AdvancedEditText mLogField;
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onCreate");

		//setContentView(R.layout.layout_editor);
        setActionBarContentView(R.layout.layout_editor);
        String code = NAction.getCode(this);
        setTitle(getString(R.string.app_name));

        if (code.equals("texteditor")) {
	        ImageButton homeBtn = (ImageButton)findViewById(R.id.gd_action_bar_home_item);
	        homeBtn.setImageResource(R.drawable.icon_nb);
	        
	        
			checkUpdate(CONF.BASE_PATH);
        }

        initWidgetTabItem(0);
		//prepareQuickActionBarM(0);

		Settings.updateFromPreferences(getSharedPreferences(PREFERENCES_NAME,
				MODE_PRIVATE));

		//
		mReadIntent = true;

		// editor
		mEditor = (AdvancedEditText) findViewById(R.id.editor);
		// log output
		//mLogField = (AdvancedEditText) findViewById(R.id.output_log);
		//mLogField.setVisibility(View.GONE);
		
		mEditor.addTextChangedListener(this);
		//mEditor.updateFromSettings();
		mWatcher = new TextChangeWatcher();
		mWarnedShouldQuit = false;
		mDoNotBackup = false;

		// search
		mSearchLayout = findViewById(R.id.searchLayout);
		mSearchInput = (EditText) findViewById(R.id.textSearch);
		findViewById(R.id.buttonSearchClose).setOnClickListener(this);
		findViewById(R.id.buttonSearchNext).setOnClickListener(this);
		findViewById(R.id.buttonSearchPrev).setOnClickListener(this);
		
		
        MyApp.getInstance().addActivity(this, CONF.BASE_PATH,""); 
        if (code.equals("texteditor")) {
	        MNApp mnApp = (MNApp) this.getApplication();
	        mnApp.trackPageView("/"+NAction.getCode(getApplicationContext())+"/dashboard");
        }

		
	}

    @Override
    public int createLayout() {
        return R.layout.gd_content_normal;
    }
    
	/**
	 * @see android.app.Activity#onStart()
	 */
	protected void onStart() {
		super.onStart();

		TedChangelog changeLog;
		SharedPreferences prefs;

		changeLog = new TedChangelog();
		prefs = getSharedPreferences(Constants.PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		/*if (changeLog.isFirstLaunch(this, prefs)) {
			Builder builder = new Builder(this);
			String message = getString(changeLog.getTitleResource(this))
					+ "\n\n" + getString(changeLog.getChangeLogResource(this));
			builder.setTitle(R.string.ui_whats_new);
			builder.setMessage(message);
			builder.setCancelable(true);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder.create().show();
		}*/

		changeLog.saveCurrentVersion(this, prefs);
	}

	/**
	 * @see android.app.Activity#onRestart()
	 */
	protected void onRestart() {
		super.onRestart();
		mReadIntent = false;
	}

	/**
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("TED", "onRestoreInstanceState");
		Log.v("TED", mEditor.getText().toString());
	}

	
	/**
	 * @see android.app.Activity#onResume()
	 */
	protected void onResume() {
		super.onResume();
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onResume");

		if (mReadIntent) {
			readIntent();
		}

		mReadIntent = false;

		updateTitle();
		if (mCurrentFilePath!=null && mCurrentFilePath.endsWith(".py")) {
			mEditor.updateFromSettings("py");
		} else {
			mEditor.updateFromSettings("");

		}
		
		ImageButton pBtn = (ImageButton)findViewById(R.id.play_btn);
		pBtn.setVisibility(View.VISIBLE);

		if (mCurrentFilePath!=null && (mCurrentFilePath.endsWith(".py") || mCurrentFilePath.endsWith(".html") || mCurrentFilePath.endsWith(".htm"))) {
			pBtn.setImageResource(mCurrentFilePath.endsWith(".py")?R.drawable.ic_go:R.drawable.ic_from_website);
			pBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onPlay(v);
				}
				
			});

		} else {
			//pBtn.setImageResource(R.drawable.transparent);
			pBtn.setVisibility(View.GONE);
			/*String content = mEditor.getText().toString().trim();
			if (content.length()==0) {
				pBtn.setImageResource(R.drawable.ic_local);
				pBtn.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						openFile();

					}
					
				});
			}*/
		}
	}

	/**
	 * @see android.app.Activity#onPause()
	 */
	protected void onPause() {
		super.onPause();
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onPause");

		if (Settings.FORCE_AUTO_SAVE && mDirty && (!mReadOnly)) {
			if ((mCurrentFilePath == null) || (mCurrentFilePath.length() == 0))
				doAutoSaveFile(true);
			else if (Settings.AUTO_SAVE_OVERWRITE)
				doSaveFile(mCurrentFilePath, true);
		}
	}

	/**
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 * 
	 */
	@TargetApi(11)
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Bundle extras;
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onActivityResult");
		mReadIntent = false;

		if (resultCode == RESULT_CANCELED) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Result canceled");
			return;
		}

		if ((resultCode != RESULT_OK) || (data == null)) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "Result error or null data! / " + resultCode);
			return;
		}

		extras = data.getExtras();
		if (extras == null) {
			if (BuildConfig.DEBUG)
				Log.e(TAG, "No extra data ! ");
			return;
		}

		switch (requestCode) {
		case REQUEST_SAVE_AS:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Save as : " + extras.getString("path"));
			doSaveFile(extras.getString("path"), true);
			break;
		case REQUEST_OPEN:
		case REQUEST_RECENT:
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Open : " + extras.getString("path"));
			doOpenFile(new File(extras.getString("path")), false);
			break;
		}
	}

	/**
	 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
	 */
	/*public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (BuildConfig.DEBUG)
			Log.d(TAG, "onConfigurationChanged");
	}*/

	/**
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	/*
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		return true;
	}*/
	
	
    /*protected void prepareQuickActionBarM(int flag) {
        mBarM = new QuickActionBar(this);
        mBarM.addQuickAction(new MyQuickAction(this, R.drawable.ic_new_a, R.string.info_new));
        mBarM.addQuickAction(new MyQuickAction(this, R.drawable.ic_go, R.string.info_go));
        mBarM.addQuickAction(new MyQuickAction(this, R.drawable.ic_search2, R.string.info_help));

        mBarM.setOnQuickActionClickListener(mActionListener);
    }

    protected OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        public void onQuickActionClicked(QuickActionWidget widget, int position) {
	        	switch (position) {	
		        	case 0:
		        		newContent();
		        		break;
		        	case 1:
			    		onPlay(null);
		        		//openFile();
		        		break;
		        	case 2:
		        		String link = NAction.getExtP(getApplicationContext(), "conf_manul_link");
		        		if (link.equals("")) {
		        			link = CONF.MANUAL_LINK;
		        		}
		        		
		        		startActivity(NAction.openRemoteLink(getApplicationContext(), link));
		        		break;
	                default:
            }
        }
    };*/
    
	/**
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	/*
	@TargetApi(11)
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		menu.clear();

		/*addMenuItem(menu, MENU_ID_NEW, R.string.menu_new,
				R.drawable.ic_menu_file_new);
		addMenuItem(menu, MENU_ID_OPEN, R.string.menu_open,
				R.drawable.ic_menu_file_open);

		if (!mReadOnly)
			addMenuItem(menu, MENU_ID_SAVE, R.string.menu_save,
					R.drawable.ic_menu_save);


*/
/*

		addMenuItem(menu, MENU_ID_SEARCH, R.string.menu_search,
				R.drawable.ic_menu_search);
		
		if ((!mReadOnly) && Settings.UNDO)
			addMenuItem(menu, MENU_ID_UNDO, R.string.menu_undo,
					R.drawable.ic_menu_undo);

		addMenuItem(menu, MENU_ID_SAVE_AS, R.string.menu_save_as, R.drawable.ic_menu_save);
		

		if (RecentFiles.getRecentFiles().size() > 0)
			addMenuItem(menu, MENU_ID_OPEN_RECENT, R.string.menu_open_recent,
					R.drawable.ic_menu_recent);

		/*addMenuItem(menu, MENU_ID_SETTINGS, R.string.menu_settings, 0);

		addMenuItem(menu, MENU_ID_ABOUT, R.string.menu_about, 0);

		if (Settings.BACK_BTN_AS_UNDO && Settings.UNDO)
			addMenuItem(menu, MENU_ID_QUIT, R.string.menu_quit, 0);

		if ((!mReadOnly) && Settings.UNDO)
			showMenuItemAsAction(menu.findItem(MENU_ID_UNDO),
					R.drawable.ic_menu_undo);
		showMenuItemAsAction(menu.findItem(MENU_ID_SEARCH),
				R.drawable.ic_menu_search);*/

		/*return true;
	}*/

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		mWarnedShouldQuit = false;
		switch (item.getItemId()) {
		case MENU_ID_NEW:
			newContent();
			return true;
		case MENU_ID_SAVE:
			saveContent();
			break;
		case MENU_ID_SAVE_AS:
			saveContentAs();
			break;
		case MENU_ID_OPEN:
			openFile();
			break;
		case MENU_ID_OPEN_RECENT:
			openRecentFile();
			break;
		case MENU_ID_SEARCH:
			search();
			break;
		case MENU_ID_SETTINGS:
			settingsActivity();
			return true;
		case MENU_ID_ABOUT:
			aboutActivity();
			return true;
		case MENU_ID_QUIT:
			quit();
			return true;
		case MENU_ID_UNDO:
			if (!undo()) {
				Toast.makeText(getApplicationContext(), R.string.toast_warn_no_undo, Toast.LENGTH_SHORT).show();
				//Crouton.showText(this, R.string.toast_warn_no_undo, Style.INFO);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onSetting(View v) {
		Intent intent = new Intent(this, TedSettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence,
	 *      int, int, int)
	 */
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		if (Settings.UNDO && (!mInUndo) && (mWatcher != null))
			mWatcher.beforeChange(s, start, count, after);
	}

	/**
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int,
	 *      int, int)
	 */
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (mInUndo)
			return;

		if (Settings.UNDO && (!mInUndo) && (mWatcher != null))
			mWatcher.afterChange(s, start, before, count);

	}

	/**
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	public void afterTextChanged(Editable s) {
		if (!mDirty) {
			mDirty = true;
			updateTitle();
		}
	}

	/**
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mSearchLayout.getVisibility() != View.GONE)
				search();
			else if (Settings.UNDO && Settings.BACK_BTN_AS_UNDO) {
				if (!undo())
					warnOrQuit();
			} else {
				quit();
			}
			return true;
		case KeyEvent.KEYCODE_SEARCH:
			search();
			mWarnedShouldQuit = false;
			return true;
		}
		mWarnedShouldQuit = false;
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * @see OnClickListener#onClick(View)
	 */
	public void onClick(View v) {
		mWarnedShouldQuit = false;
		if (v.getId() == R.id.buttonSearchClose) {
			search();
		} else if (v.getId() == R.id.buttonSearchNext) {
			searchNext();
		} else if (v.getId() == R.id.buttonSearchPrev) {
			searchPrevious();
		}
	}

	/**
	 * Read the intent used to start this activity (open the text file) as well
	 * as the non configuration instance if activity is started after a screen
	 * rotate
	 */
	protected void readIntent() {
		Intent intent;
		String action;
		File file;

		intent = getIntent();
		if (intent == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "No intent found, use default instead");
			doDefaultAction();
			return;
		}

		action = intent.getAction();
		if (action == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Intent w/o action, default action");
			doDefaultAction();
		} else if ((action.equals(Intent.ACTION_VIEW))
				|| (action.equals(Intent.ACTION_EDIT))) {
			try {
				file = new File(new URI(intent.getData().toString()));
				doOpenFile(file, false);
			} catch (URISyntaxException e) {
				Crouton.showText(this, R.string.toast_intent_invalid_uri,
						Style.ALERT);
			} catch (IllegalArgumentException e) {
				Crouton.showText(this, R.string.toast_intent_illegal,
						Style.ALERT);
			}
		} else if (action.equals(ACTION_WIDGET_OPEN)) {
			try {
				file = new File(new URI(intent.getData().toString()));
				doOpenFile(file,
						intent.getBooleanExtra(EXTRA_FORCE_READ_ONLY, false));
			} catch (URISyntaxException e) {
				Crouton.showText(this, R.string.toast_intent_invalid_uri,
						Style.ALERT);
			} catch (IllegalArgumentException e) {
				Crouton.showText(this, R.string.toast_intent_illegal,
						Style.ALERT);
			}
		} else {
			doDefaultAction();
		}
	}

	/**
	 * Run the default startup action
	 */
	protected void doDefaultAction() {
		File file;
		boolean loaded;
		loaded = false;

		if (doOpenBackup())
			loaded = true;

		if ((!loaded) && Settings.USE_HOME_PAGE) {
			file = new File(Settings.HOME_PAGE_PATH);
			if ((file == null) || (!file.exists())) {
				Crouton.showText(this, R.string.toast_open_home_page_error,
						Style.ALERT);
			} else if (!file.canRead()) {
				Crouton.showText(this, R.string.toast_home_page_cant_read,
						Style.ALERT);
			} else {
				loaded = doOpenFile(file, false);
			}
		}

		if (!loaded)
			doClearContents();
	}

	/**
	 * Clears the content of the editor. Assumes that user was prompted and
	 * previous data was saved
	 */
	protected void doClearContents() {
		mWatcher = null;
		mInUndo = true;
		mEditor.setText("");
		mCurrentFilePath = null;
		mCurrentFileName = null;
		Settings.END_OF_LINE = Settings.DEFAULT_END_OF_LINE;
		mDirty = false;
		mReadOnly = false;
		mWarnedShouldQuit = false;
		mWatcher = new TextChangeWatcher();
		mInUndo = false;
		mDoNotBackup = false;

		TextFileUtils.clearInternal(getApplicationContext());
		
		ImageButton pBtn = (ImageButton)findViewById(R.id.play_btn);
		//pBtn.setImageResource(R.drawable.transparent);
		pBtn.setVisibility(View.GONE);
		updateTitle();
	}

	/**
	 * Opens the given file and replace the editors content with the file.
	 * Assumes that user was prompted and previous data was saved
	 * 
	 * @param file
	 *            the file to load
	 * @param forceReadOnly
	 *            force the file to be used as read only
	 * @return if the file was loaded successfully
	 */
	protected boolean doOpenFile(File file, boolean forceReadOnly) {
		String text;

		if (file == null)
			return false;

		if (BuildConfig.DEBUG)
			Log.i(TAG, "Openning file " + file.getName());

		try {
			text = TextFileUtils.readTextFile(file);
			if (text != null) {
				mInUndo = true;
				mEditor.setText(text);
				mWatcher = new TextChangeWatcher();
				mCurrentFilePath = getCanonizePath(file);
				mCurrentFileName = file.getName();
				RecentFiles.updateRecentList(mCurrentFilePath);
				RecentFiles.saveRecentList(getSharedPreferences(
						PREFERENCES_NAME, MODE_PRIVATE));
				mDirty = false;
				mInUndo = false;
				mDoNotBackup = false;
				if (file.canWrite() && (!forceReadOnly)) {
					mReadOnly = false;
					mEditor.setEnabled(true);
				} else {
					mReadOnly = true;
					mEditor.setEnabled(false);
				}

				updateTitle();

				return true;
			} else {
				Toast.makeText(this, R.string.toast_open_error, Toast.LENGTH_SHORT).show();
				//Crouton.showText(this, R.string.toast_open_error, Style.ALERT);
			}
		} catch (OutOfMemoryError e) {
			Toast.makeText(this, R.string.toast_memory_open, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_memory_open, Style.ALERT);
		}

		return false;
	}

	/**
	 * Open the last backup file
	 * 
	 * @return if a backup file was loaded
	 */
	protected boolean doOpenBackup() {

		String text;

		try {
			text = TextFileUtils.readInternal(this);
			if (!TextUtils.isEmpty(text)) {
				mInUndo = true;
				mEditor.setText(text);
				mWatcher = new TextChangeWatcher();
				mCurrentFilePath = null;
				mCurrentFileName = null;
				mDirty = false;
				mInUndo = false;
				mDoNotBackup = false;
				mReadOnly = false;
				mEditor.setEnabled(true);

				updateTitle();

				return true;
			} else {
				return false;
			}
		} catch (OutOfMemoryError e) {
			Crouton.showText(this, R.string.toast_memory_open, Style.ALERT);
		}

		return true;
	}

	/**
	 * Saves the text editor's content into a file at the given path. If an
	 * after save {@link Runnable} exists, run it
	 * 
	 * @param path
	 *            the path to the file (must be a valid path and not null)
	 */
	protected void doSaveFile(String path, boolean show) {
		String content;

		if (path == null) {
			Crouton.showText(this, R.string.toast_save_null, Style.ALERT);
			return;
		}

		content = mEditor.getText().toString();

		if (!TextFileUtils.writeTextFile(path + ".tmp", content)) {
			Toast.makeText(getApplicationContext(), R.string.toast_save_temp, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_save_temp, Style.ALERT);
			return;
		}

		if (!deleteItem(path)) {
			Toast.makeText(getApplicationContext(), R.string.toast_save_delete, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_save_delete, Style.ALERT);
			return;
		}

		if (!renameItem(path + ".tmp", path)) {
			Toast.makeText(getApplicationContext(), R.string.toast_save_rename, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_save_rename, Style.ALERT);
			return;
		}

		mCurrentFilePath = getCanonizePath(new File(path));
		mCurrentFileName = (new File(path)).getName();
		RecentFiles.updateRecentList(path);
		RecentFiles.saveRecentList(getSharedPreferences(PREFERENCES_NAME,
				MODE_PRIVATE));
		mReadOnly = false;
		mDirty = false;
		updateTitle();
		//Crouton.showText(this, R.string.toast_save_success, Style.CONFIRM);
		if (show) Toast.makeText(getApplicationContext(), R.string.toast_save_success, Toast.LENGTH_SHORT).show();

		runAfterSave();
	}

	@SuppressWarnings("deprecation")
	protected void doAutoSaveFile(boolean show) {
		if (mDoNotBackup) {
			doClearContents();
		}

		String text = mEditor.getText().toString();
		if (text.length() == 0)
			return;

		if (TextFileUtils.writeInternal(this, text)) {
			if (show) showToast(this, R.string.toast_file_saved_auto, false);
		}
	}

	/**
	 * Undo the last change
	 * 
	 * @return if an undo was don
	 */
	protected boolean undo() {
		boolean didUndo = false;
		mInUndo = true;
		int caret;
		caret = mWatcher.undo(mEditor.getText());
		if (caret >= 0) {
			mEditor.setSelection(caret, caret);
			didUndo = true;
		}
		mInUndo = false;

		return didUndo;
	}

	/**
	 * Prompt the user to save the current file before doing something else
	 */
	protected void promptSaveDirty() {
		Builder builder;

		if (!mDirty) {
			runAfterSave();
			return;
		}

		builder = new Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setMessage(R.string.ui_save_text);

		builder.setPositiveButton(R.string.ui_save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						saveContent();
						mDoNotBackup = true;
					}
				});
		builder.setNegativeButton(R.string.ui_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		builder.setNeutralButton(R.string.ui_no_save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						runAfterSave();
						mDoNotBackup = true;
					}
				});

		builder.create().show();

	}

	/**
	 * 
	 */
	protected void newContent() {
		mAfterSave = new Runnable() {
			public void run() {
				doClearContents();
			}
		};

		promptSaveDirty();
	}

	/**
	 * Runs the after save to complete
	 */
	protected void runAfterSave() {
		if (mAfterSave == null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "No After shave, ignoring...");
			return;
		}

		mAfterSave.run();

		mAfterSave = null;
	}

	/**
	 * Starts an activity to choose a file to open
	 */
	protected void openFile() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "openFile");

		mAfterSave = new Runnable() {
			public void run() {
				Intent open = new Intent();
				open.setClass(getApplicationContext(), TedLocalActivity.class);
				// open = new Intent(ACTION_OPEN);
				open.putExtra(EXTRA_REQUEST_CODE, REQUEST_OPEN);
				try {
					startActivityForResult(open, REQUEST_OPEN);
				} catch (ActivityNotFoundException e) {
					Crouton.showText(TedActivity.this,
							R.string.toast_activity_open, Style.ALERT);
				}
			}
		};

		promptSaveDirty();
	}
	
	public void onBrowser(View v) {
		Intent intent = new Intent(this, MTubebook.class);
		startActivity(intent);
	}

	/**
	 * Open the recent files activity to open
	 */
	protected void openRecentFile() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "openRecentFile");

		if (RecentFiles.getRecentFiles().size() == 0) {
			Toast.makeText(getApplicationContext(), R.string.toast_no_recent_files, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_no_recent_files, Style.ALERT);
			return;
		}
		
		mAfterSave = new Runnable() {
			public void run() {
				Intent open;

				open = new Intent();
				open.setClass(TedActivity.this, TedLocalActivity.class);
				open.putExtra(EXTRA_REQUEST_CODE, REQUEST_RECENT);

				try {
					startActivityForResult(open, REQUEST_RECENT);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(getApplicationContext(), R.string.toast_activity_open_recent, Toast.LENGTH_SHORT).show();
					//Crouton.showText(TedActivity.this, R.string.toast_activity_open_recent, Style.ALERT);
				}
			}
		};
		 
		promptSaveDirty();
	}

	/**
	 * Warns the user that the next back press will qui the application, or quit
	 * if the warning has already been shown
	 */
	protected void warnOrQuit() {
		if (mWarnedShouldQuit) {
			quit();
		} else {
			Crouton.showText(this, R.string.toast_warn_no_undo_will_quit,
					Style.INFO);
			mWarnedShouldQuit = true;
		}
	}

	/**
	 * Quit the app (user pressed back)
	 */
	protected void quit() {
		final String code = NAction.getCode(this);
		
		mAfterSave = new Runnable() {
			public void run() {
				if (code.equals("texteditor")) {
					
				} else {
					finish();
				}
			}
		};

		promptSaveDirty();
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (item.getItemId()) {
	    	case 10:
				saveContentAs();
	    		break;
	    	case 20:
				openFile();
	    		break;
	    		
	    	case 30:
	    		//mBarM.show(item.getItemView());
				newContent();
	    		break;
	    	case 40:
	    		onGSetting(null);
	    		break;
	    	default:

		}
		return 	super.onHandleActionBarItemClick(item, position);
	}
	
	public boolean onKeyDown(int keyCoder,KeyEvent event){
		String code =NAction.getCode(this);
    	if (keyCoder == KeyEvent.KEYCODE_BACK) {
	
			if (code.equals("texteditor")) {
				if (exitCount<1) {
		    		endScreen();
		    		exitCount++;
				} else {
					finish();
				}
			}
    	}

		return super.onKeyDown(keyCoder, event);
	}
	
	@SuppressWarnings("deprecation")
	public void endScreen() {
		WBase.setTxtDialogParam2(0, R.string.confirm_exit,  getString(R.string.feed_back), getString(R.string.follow_community), "",getString(R.string.feedback_btn), getString(R.string.follow_community_btn),"",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				},null);
		showDialog(_WBase.DIALOG_BTN_ENTRY1+dialogIndex);
		dialogIndex++;
	}
	
	public void onExitPrompt1(View v) {

		Intent intent = NAction.openRemoteLink(getApplicationContext(), CONF.COMMUNITY_LINK);
		startActivity(intent);
	}
	
	public void onExitPrompt2(View v) {
		String url = NAction.getExtP(this, "conf_promp3_link");
		if (url.equals("")) {
			url = CONF.NEWS_LINK;
		}
		Intent intent = NAction.openRemoteLink(getApplicationContext(), url);
		startActivity(intent);
	}


	/**
	 * General save command : check if a path exist for the current content,
	 * then save it , else invoke the {@link TedActivity#saveContentAs()} method
	 */
	protected void saveContent() {
		if ((mCurrentFilePath == null) || (mCurrentFilePath.length() == 0)) {
			saveContentAs();
		} else {
			doSaveFile(mCurrentFilePath, true);
		}
	}

	/**
	 * General Save as command : prompt the user for a location and file name,
	 * then save the editor'd content
	 */
	protected void saveContentAs() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "saveContentAs");
		Intent saveAs;
		saveAs = new Intent();
		saveAs.setClass(this, TedLocalActivity.class);
		saveAs.putExtra(EXTRA_REQUEST_CODE, REQUEST_SAVE_AS);

		try {
			startActivityForResult(saveAs, REQUEST_SAVE_AS);
		} catch (ActivityNotFoundException e) {
			Crouton.showText(this, R.string.toast_activity_save_as, Style.ALERT);
		}
	}

	/**
	 * Opens / close the search interface
	 */
	protected void search() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "search");
		LinearLayout rb = (LinearLayout)findViewById(R.id.return_bar_box);
		switch (mSearchLayout.getVisibility()) {
		case View.GONE:
			rb.setVisibility(View.GONE);

			mSearchLayout.setVisibility(View.VISIBLE);
			break;
		case View.VISIBLE:
		default:
			rb.setVisibility(View.VISIBLE);
			mSearchLayout.setVisibility(View.GONE);
			break;
		}
	}

	/**
	 * Uses the user input to search a file
	 */
	@SuppressLint("DefaultLocale")
	protected void searchNext() {
		String search, text;
		int selection, next;

		search = mSearchInput.getText().toString();
		text = mEditor.getText().toString();
		selection = mEditor.getSelectionEnd();

		if (search.length() == 0) {
			Toast.makeText(getApplicationContext(), R.string.toast_search_no_input, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_search_no_input, Style.INFO);
			return;
		}

		if (!Settings.SEARCHMATCHCASE) {
			search = search.toLowerCase();
			text = text.toLowerCase();
		}

		next = text.indexOf(search, selection);

		if (next > -1) {
			mEditor.setSelection(next, next + search.length());
			if (!mEditor.isFocused())
				mEditor.requestFocus();
		} else {
			if (Settings.SEARCHWRAP) {
				next = text.indexOf(search);
				if (next > -1) {
					mEditor.setSelection(next, next + search.length());
					if (!mEditor.isFocused())
						mEditor.requestFocus();
				} else {
					Toast.makeText(getApplicationContext(), R.string.toast_search_not_found, Toast.LENGTH_SHORT).show();
					//Crouton.showText(this, R.string.toast_search_not_found,Style.INFO);
				}
			} else {
				Toast.makeText(getApplicationContext(), R.string.toast_search_eof, Toast.LENGTH_SHORT).show();

				//Crouton.showText(this, R.string.toast_search_eof, Style.INFO);
			}
		}
	}

	/**
	 * Uses the user input to search a file
	 */
	@SuppressLint("DefaultLocale")
	protected void searchPrevious() {
		String search, text;
		int selection, next;

		search = mSearchInput.getText().toString();
		text = mEditor.getText().toString();
		selection = mEditor.getSelectionStart() - 1;

		if (search.length() == 0) {
			Crouton.showText(this, R.string.toast_search_no_input, Style.INFO);
			return;
		}

		if (!Settings.SEARCHMATCHCASE) {
			search = search.toLowerCase();
			text = text.toLowerCase();
		}

		next = text.lastIndexOf(search, selection);

		if (next > -1) {
			mEditor.setSelection(next, next + search.length());
			if (!mEditor.isFocused())
				mEditor.requestFocus();
		} else {
			if (Settings.SEARCHWRAP) {
				next = text.lastIndexOf(search);
				if (next > -1) {
					mEditor.setSelection(next, next + search.length());
					if (!mEditor.isFocused())
						mEditor.requestFocus();
				} else {
					Toast.makeText(getApplicationContext(), R.string.toast_search_not_found, Toast.LENGTH_SHORT).show();

					//Crouton.showText(this, R.string.toast_search_not_found,Style.INFO);
				}
			} else {
				Toast.makeText(getApplicationContext(), R.string.toast_search_eof, Toast.LENGTH_SHORT).show();

				//Crouton.showText(this, R.string.toast_search_eof, Style.INFO);
			}
		}
	}
	
	public void onLeft(View v) {
		int index = mEditor.getSelectionStart();
		Editable editable = mEditor.getText();
		if (index>=4) {
			if (editable.subSequence(index -4, index).toString().equals("    ")) {
				editable.delete(index-4, index);
			}
		}
	}
	public void onRight(View v) {
		int index = mEditor.getSelectionStart();
		Editable editable = mEditor.getText();
		editable.insert(index, "    ");
	}
	
	public void onBack(View v) {
		if (!undo()) {
			Toast.makeText(getApplicationContext(), R.string.toast_warn_no_undo, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_warn_no_undo, Style.INFO);
		}
	}
	public void onPlay(View v) {
		String content = mEditor.getText().toString().trim();
		if (content.length() == 0) {
			Toast.makeText(getApplicationContext(), R.string.cannot_empty, Toast.LENGTH_SHORT).show();
		} else {
			if (content.startsWith("<")) {
				//doAutoSaveFile(false);
				doSaveFile(mCurrentFilePath, false);

				Intent intent = new Intent(getApplicationContext(), MTubebook.class);
				Uri data = Uri.fromFile(new File(mCurrentFilePath));
				intent.setData(data);
				startActivity(intent);

	
			} else {
				callPyApi("qedit",mCurrentFilePath,content);
			}
		}
	}
	public void onSearch(View v) {
		search();
	}
	
	public void onSave(View v) {
		saveContent();
	}
	public void onSaveAs(View v) {
		saveContentAs();

	}
	public void onHistory(View v) {
		openRecentFile();

	}

	/**
	 * Opens the about activity
	 */
	protected void aboutActivity() {
		Intent about = new Intent();
		about.setClass(this, TedAboutActivity.class);
		try {
			startActivity(about);
		} catch (ActivityNotFoundException e) {
			Crouton.showText(this, R.string.toast_activity_about, Style.ALERT);
		}
	}

	/**
	 * Opens the settings activity
	 */
	protected void settingsActivity() {

		mAfterSave = new Runnable() {
			public void run() {
				Intent settings = new Intent();
				settings.setClass(TedActivity.this, TedSettingsActivity.class);
				try {
					startActivity(settings);
				} catch (ActivityNotFoundException e) {
					Crouton.showText(TedActivity.this,
							R.string.toast_activity_settings, Style.ALERT);
				}
			}
		};

		promptSaveDirty();
	}

	/**
	 * Update the window title
	 */
	@TargetApi(11)
	protected void updateTitle() {
		String title;
		String name;

		name = "?";
		if ((mCurrentFileName != null) && (mCurrentFileName.length() > 0))
			name = mCurrentFileName;

		if (mReadOnly)
			title = getString(R.string.title_editor_readonly, name);
		else if (mDirty)
			title = getString(R.string.title_editor_dirty, name);
		else
			title = getString(R.string.title_editor, name);

		setTitle(title);

		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
			invalidateOptionsMenu();
	}

	/** the text editor */
	protected AdvancedEditText mEditor;
	/** the path of the file currently opened */
	protected String mCurrentFilePath;
	/** the name of the file currently opened */
	protected String mCurrentFileName;
	/** the runable to run after a save */
	protected Runnable mAfterSave; // Mennen ? Axe ?

	/** is dirty ? */
	protected boolean mDirty;
	/** is read only */
	protected boolean mReadOnly;

	/** the search layout root */
	protected View mSearchLayout;
	/** the search input */
	protected EditText mSearchInput;

	/** Undo watcher */
	protected TextChangeWatcher mWatcher;
	protected boolean mInUndo;
	protected boolean mWarnedShouldQuit;
	protected boolean mDoNotBackup;

	/** are we in a post activity result ? */
	protected boolean mReadIntent;

}