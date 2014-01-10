package com.hipipal.texteditor;


import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Stack;

import com.hipipal.texteditor.common.Constants;
import com.hipipal.texteditor.common.RecentFiles;
import com.zuowuxuxi.util.FileHelper;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NUtil;
import com.zuowuxuxi.base._WBase;

import greendroid.widget.ItemAdapter;
import greendroid.widget.QuickActionBar;
import greendroid.widget.QuickActionWidget;
import greendroid.widget.QuickActionWidget.OnQuickActionClickListener;
import greendroid.widget.item.DrawableItem;
import greendroid.widget.item.TextItem;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TedLocalActivity extends _ABaseAct implements Constants {
	private static final String TAG = "local";

	private Stack<String> curArtistDir;
	private QuickActionWidget mBarT;

	private TextItem curTextItem;
	private ItemAdapter adapter;
	private int curPosition = 0;
	private int request;

	private Stack<String> prevDir;
	
    @SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			request = extras.getInt(EXTRA_REQUEST_CODE);
		} else {
			request = -1;
		}
		
		prevDir = new Stack();
        setActionBarContentView(R.layout.m_ted_local);
        setTitle(R.string.app_name);
        
        //initWidgetTabItem(7);
		//initAD(TAG);
        
    	ListView listView = (ListView)findViewById(android.R.id.list);
    	//listView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.v_local_bar, null));
    	
    	listView.setDivider(new ColorDrawable(getResources().getColor(R.color.cgrey6)));
    	listView.setDividerHeight(1);
    	listView.setCacheColorHint(0);
        
        adapter = new ItemAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View view, int position, long id) {
			    	final TextItem textItem = (TextItem) l.getAdapter().getItem(position);
			    	onListItemClick(view, textItem, position);
			}
        });
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> l, View view, int position, long id) {
		    	final TextItem textItem = (TextItem) l.getAdapter().getItem(position);
		    	curTextItem = textItem;
		    	
		    	prepareQuickActionBarT();
		    	mBarT.show(view);
				return false;
			}
        	
        });
    	String root = NAction.getDefaultRoot(getApplicationContext());
    	if (root.equals("")) {
    		root = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/com.hipipal.qpyplus";
    	}
    	
        curArtistDir = new Stack<String>();
        String[] xx = root.split("/");
        curArtistDir.push("/");
        if (xx.length>1) {
	        for (int i=0;i<xx.length;i++) {
	        	Log.d(TAG, "seq:"+xx[i]);
	        	if (!xx[i].equals("")) {
	        		String yy;
	        		if (curArtistDir.peek().endsWith("/")) {
		        		yy = curArtistDir.peek()+xx[i];

	        		} else {
		        		yy = curArtistDir.peek()+"/"+xx[i];
	        		}
	        		curArtistDir.push(yy);

	        	}
	        }
        }
        
    	if (!NUtil.isExternalStorageExists()) {
    		WBase.setTxtDialogParam(R.drawable.alert_dialog_icon, R.string.not_sd, new  android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
    		});
    		showDialog(_WBase.DIALOG_NOTIFY_MESSAGE+dialogIndex);
    		dialogIndex++;

    	} else {
    		TextItem progressItem = new TextItem(getString(R.string.loading));
            adapter.add(progressItem);
            adapter.notifyDataSetChanged();
            //new CacheAvaiDirs().execute();

    	}

    	LinearLayout rb = (LinearLayout)findViewById(R.id.return_bar_box);
    	LinearLayout sb = (LinearLayout)findViewById(R.id.setting_box);
    	
		switch (request) {
		case REQUEST_RECENT:
			ImageButton ln = (ImageButton)findViewById(R.id.left_nav);
			ln.setImageResource(R.drawable.transparent);
			rb.setVisibility(View.VISIBLE);
			setTitle(R.string.title_open_recent);
			break;
		case REQUEST_OPEN:
			rb.setVisibility(View.VISIBLE);
			setTitle(R.string.title_open);
			//Toast.makeText(this, R.string.toast_open_select, Toast.LENGTH_SHORT).show();
			//Crouton.showText(this, R.string.toast_open_select, Style.INFO);
			break;
		case REQUEST_SAVE_AS:
			sb.setVisibility(View.VISIBLE);
			setTitle(R.string.title_save_as);
			break;
		case REQUEST_HOME_PAGE:
			setTitle(R.string.toast_home_page_select);
			Toast.makeText(this, R.string.toast_home_page_select, Toast.LENGTH_SHORT).show();

			//Crouton.showText(this, R.string.toast_home_page_select, Style.INFO);
			break;
		}
        myloadContent("", -1);
        
    }
    
    /*@TargetApi(3)
	@SuppressWarnings("rawtypes")
	private class CacheAvaiDirs extends AsyncTask {
		@Override
		protected Object doInBackground(Object... arg0) {
            MyApp.getInstance().getAvaiDirs("", true,true);
    		Log.d(TAG, "doInBackground end");
    		updatePositionHandler.sendEmptyMessage(0);
            return null;
		}
    }
	public Handler updatePositionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
    		myloadContent("", -1);
		}
	};*/
    
    @Override
    public void onResume() {
    	myloadContent("", -1);
    	disNotify(TAG);

    	super.onResume();
    }
    
	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}
    
    public void onNotify(View v) {
    }
    
    /*@Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
    	if (keycode == KeyEvent.KEYCODE_BACK) {
	    	boolean ret = onBack();
	    	if (ret) return ret;
    	}
    	
    	return super.onKeyDown(keycode, event);
    }*/
    
    public boolean onTop() {
    	if (curArtistDir.size() == 1) {
    		finish();

    		return false;
    		
    	} else {
    		
    		String xx = curArtistDir.pop();
    		/*if (xx.lastIndexOf("/")+1 < xx.length()) {
    			xx = xx.substring(xx.lastIndexOf("/")+1);
    			prevDir.push(xx);
    		}*/
    		prevDir.push(xx);
    		
    		//Log.d(TAG, "prevDir:"+prevDir);
    		myloadContent("", curPosition);
    		return true;
    	}
    }
    
    
    private void prepareQuickActionBarT() {
        mBarT = new QuickActionBar(this);
        mBarT.addQuickAction(new MyQuickAction(this, R.drawable.ic_delete, R.string.info_delete));
        mBarT.addQuickAction(new MyQuickAction(this, R.drawable.ic_edit_b, R.string.info_rename));
        mBarT.addQuickAction(new MyQuickAction(this, R.drawable.ic_menu_share, R.string.share));
        
        mBarT.setOnQuickActionClickListener(mActionListener);
    }
    private OnQuickActionClickListener mActionListener = new OnQuickActionClickListener() {
        @Override
		public void onQuickActionClicked(QuickActionWidget widget, int position) {
        	switch (position) {
	        	case 0:

    				deleteCurItem();
    				//Toast.makeText(getApplicationContext(), R.string.not_implement, Toast.LENGTH_SHORT).show();
                    break;
        		case 1:
        			renameItem(curTextItem);
        	    	//infoOpen(curTextItem, 0);
        			break;
        		case 2:
        			shareFile();
        			break;
        		case 3:
                    break;
                default:
        	}
        }
    };
    
    
	public String getCurrentDir() {
		return curArtistDir.peek();
	}
	
    @SuppressLint("DefaultLocale")
	public void myloadContent(String dirname, int position) {    
    	
    	if (request == REQUEST_RECENT) {
	    	adapter.clear();
	    	adapter.add(new TextItem(getString(R.string.info_recent)));
	    	ArrayList<String> mList = RecentFiles.getRecentFiles();
	    	for (int i=0;i<mList.size();i++) {
	    		String item = mList.get(i);
	    		
	    		DrawableItem sItem = new DrawableItem(item,R.drawable.file_text);
				sItem.setTag(0, "");
				sItem.setTag(1, item);
				adapter.add(sItem);
	    	}
	    	adapter.notifyDataSetChanged();

    	} else {
			//CacheLog cDL = new CacheLog(getApplicationContext());
	
	    	//Map<String, Integer> movieDirs = MyApp.getInstance().getAvaiDirs(dirname, false, false);
	
	    	if (dirname!=null && !dirname.equals("")) {
	    		curArtistDir.push(curArtistDir.peek()+"/"+dirname);
	    	}
	    	
	    	String curDir = getCurrentDir();
	    	
	    	File d = new File(curDir);
	    	if (d.exists()) {
		    	//Log.d(TAG, "curDi:r"+curDir);
		    	
		    	String filename,fullfn;
		    	DrawableItem sItem;
		
		    	//int filesCount;
		    	
		    	adapter.clear();
		    	adapter.add(new TextItem(MessageFormat.format(getString(R.string.current_dir), curDir)));
		    	//reduceFiles(curDir);
		    	
		    	try {
		
		        	File[] files = FileHelper.getABSPath(curDir).listFiles();
		        	if (files!=null) {
			        	//Arrays.sort(files, sortType);
		        		//int index = 0;
						for (final File file : files) {
							//index ++;
							fullfn = file.getAbsolutePath().toString();
							
							filename = file.getName();
							if (file.isDirectory()) {
								sItem = new DrawableItem(filename,R.drawable.path_none);
		
								sItem.setTag(0, filename);
								sItem.setTag(1, fullfn);
								adapter.add(sItem);
								
							} else {
								String ext = FileHelper.getExt(filename.toLowerCase(), "");
								if (!ext.equals("") && CONF.MUEXT.contains("#"+ext+"#")) {
									//long size = file.length();								
									//String timeStr = DateTimeHelper.converTime(file.lastModified()/1000, getResources().getStringArray(R.array.time_labels));
									//final Uri uFile = Uri.fromFile(file);
									
									//
									//String mCover[] = cDL.get(file.getName(), CONF.CACHE_TYPE_COVER);
									//String[] syncStat = cDL.get(file.getName(), CONF.CACHE_TYPE_SYNC_DROPBOX);
									/*int statImg = 0;
									
									if (syncStat[0].equals("")) {
										statImg = 0;
									} else if (syncStat[0].equals("0")) {
										statImg = R.drawable.ic_upload;
										
									} else if (syncStat[0].equals("1")) {
										statImg = R.drawable.bsync_dropbox_d;	// sucess
										
									} else {
										statImg = R.drawable.ic_warning;	// error
									}*/
			
									sItem = new DrawableItem(filename,R.drawable.file_text);
									sItem.setTag(0, "");
									sItem.setTag(1, fullfn);
									adapter.add(sItem);
		
								} else {
									sItem = new DrawableItem(filename,R.drawable.file_locked);
									sItem.setTag(0, "");
									sItem.setTag(1, fullfn);
									adapter.add(sItem);
		
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		    	adapter.notifyDataSetChanged();
	    	} else {
	    		Toast.makeText(getApplicationContext(), R.string.file_not_exits, Toast.LENGTH_SHORT).show();
	    	}
	    	
	    	if (position!=-1) {
	        	ListView listView = (ListView)findViewById(android.R.id.list);    	
	    		listView.setSelection(position);
	    	}
    	}
    }
    
    @SuppressWarnings("deprecation")
	public void renameItem(final TextItem textItem) {
    	//Object o1 = textItem.getTag(1);
		final String fullname = textItem.getTag(1).toString();
		final File oldf = new File(fullname);

		WBase.setTxtDialogParam(R.drawable.ic_setting, R.string.info_rename, oldf.getName(),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String filename = t.getText().toString().trim();
				        File newf = new File(oldf.getParent()+"/"+filename);
				        if (newf.exists()) {
				        	Toast.makeText(getApplicationContext(), R.string.file_exists, Toast.LENGTH_SHORT).show();
				        	renameItem(textItem);
				        } else {
				        	oldf.renameTo(newf);
					        myloadContent("", curPosition);

				        }

					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+dialogIndex);
		dialogIndex++;
    }
    
    @SuppressWarnings("deprecation")
	public void deleteCurItem() {
    	Object o1 = curTextItem.getTag(1);
    	final String filename = o1.toString();
		WBase.setTxtDialogParam(R.drawable.alert_dialog_icon, R.string.confirm_delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		    	File file = new File(filename);
		    	if (file.isFile()) {
		    		file.delete();
		    	} else {
		    		FileHelper.clearDir(filename, 0, true);
		    	}
		    	adapter.remove(curTextItem);
		    	adapter.notifyDataSetChanged();
			}
			});
			showDialog(_WBase.DIALOG_YES_NO_MESSAGE+dialogIndex);
			dialogIndex++;
    }
    /**
     * Share the selected file
     */
    public void shareFile() {
    	Object o1 = curTextItem.getTag(1);
    	String filename = o1.toString();
    	File file = new File(filename);
    	if (file.isFile()) {
    		//Bug: Not filtering for share file intent 
    		Intent sendIntent = new Intent(Intent.ACTION_SEND);
    		sendIntent.setType("text/plain");
        	sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        	startActivity(Intent.createChooser(sendIntent, "Share File"));
    	} else {
    		Toast.makeText(getApplicationContext(), "Not a file", Toast.LENGTH_SHORT).show();
    	}
    }
    
	protected boolean setOpenResult(File file) {
		Log.d(TAG, "setOpenResult");
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
    
	public void infoOpen(TextItem textItem, int position) {
    	Object o0 = textItem.getTag(0);
    	if (o0!=null) {
	    	String filename = o0.toString();
    		curTextItem = textItem;

	    	if (filename.equals("")) {
    			String fullname = textItem.getTag(1).toString();
    			if (fullname.equals("")) {
    				Toast.makeText(getApplicationContext(), R.string.cannot_edit, Toast.LENGTH_SHORT).show();
    			
    			} else {
		    		if (request == REQUEST_OPEN || request == REQUEST_RECENT) {
		    			Log.d(TAG, "fullname:"+fullname);

		    			if (setOpenResult(new File(fullname)))
		    				finish();	
		    		}
		    		
		    		if (request == REQUEST_SAVE_AS) {
		    			EditText fname = (EditText)findViewById(R.id.search_input);
		    			File f = new File(fullname);
		    			fname.setText(f.getName());
		    		}
    			}
	    		// TODO
	    	} else {
	    		curPosition = position;
	    		prevDir.push("..");
	    		myloadContent(filename, 0);
	    	}
    	}
	}
	
    protected void onListItemClick(View v, TextItem textItem, int position) {
    	infoOpen(textItem, position);
    }
    
    public void onInputClicked(View v) {
    	
    }

    public void onForward(View v) {
    	if (prevDir.size()==0) {
    		Toast.makeText(this, R.string.cannot_foward, Toast.LENGTH_SHORT).show();
    		
    	} else {
    		String xx = prevDir.pop();
    		if (xx.equals("..")) {
    			curArtistDir.pop();
    			xx = "";
    		} else {
	    		if (xx.lastIndexOf("/")+1 < xx.length()) {
	    			xx = xx.substring(xx.lastIndexOf("/")+1);
	    		} else {
	    			xx = "";
	    		}
    		}
    		myloadContent(xx, 0);
    		
    	}
    }
    
    @Override
	public void onBack(View v) {
    	finish();
    }
    
    @SuppressWarnings("deprecation")
	public void doNewDir(View v) {
    	
		//final TextView media = (TextView)findViewById(R.id.plugin_mediacenter_value);
		//String mediaVal = media.getText().toString();
		WBase.setTxtDialogParam(R.drawable.ic_setting, R.string.dir_new, "sampleproject",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        
				        File dirN = new File(curArtistDir.peek(),content);
				        if (dirN.exists()) {
				        	Toast.makeText(getApplicationContext(), R.string.dir_exists, Toast.LENGTH_SHORT).show();
				        } else {
				        	dirN.mkdir();
				            myloadContent("", curPosition);

				        }
				        //
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+dialogIndex);
		dialogIndex++;
    }
    
    @SuppressWarnings("deprecation")
	public void doSave(View v) {
		EditText fname = (EditText)findViewById(R.id.search_input);
		String fn = fname.getText().toString();
		if (fn.length() == 0) {
			Toast.makeText(getApplicationContext(), R.string.toast_filename_empty, Toast.LENGTH_SHORT).show();
		} else {
			String filename = curArtistDir.peek()+"/"+fn;
			final File f = new File(filename);
			if (f.exists()) {
			WBase.setTxtDialogParam(R.drawable.alert_dialog_icon, R.string.confirm_save, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setSaveResult(f.getAbsolutePath().toString());
				}
				});
				showDialog(_WBase.DIALOG_YES_NO_MESSAGE+dialogIndex);
				dialogIndex++;
			} else {
				setSaveResult(f.getAbsolutePath().toString());
			}
		}
    }
    
	protected boolean setSaveResult(String filepath) {
		Intent result;

		File f = new File(filepath);
		if (f.getParentFile().canWrite()) {
			result = new Intent();
			result.putExtra("path", filepath);
	
			setResult(RESULT_OK, result);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), R.string.toast_folder_cant_write, Toast.LENGTH_SHORT).show();
		}
		return true;
	}
    
    public void onUp(View v) {
    	onTop();
    }
}
