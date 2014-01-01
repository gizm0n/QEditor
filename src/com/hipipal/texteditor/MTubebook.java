package com.hipipal.texteditor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zuowuxuxi.util.FileHelper;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NRequest;
import com.zuowuxuxi.util.NUtil;
import com.zuowuxuxi.util.VeDate;
import com.zuowuxuxi.widget.RightDrawableOnTouchListener;
import com.zuowuxuxi.asihttp.AsyncHttpResponseHandler;
import com.zuowuxuxi.base._WBase;

import greendroid.widget.ItemAdapter;
import greendroid.widget.item.ProgressItem;
import greendroid.widget.item.TextItem;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MTubebook extends _ABaseAct implements OnTouchListener, Handler.Callback  {
	private static final String TAG = "search";
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1235;  
    private final Handler handler = new Handler(this);
    private static final int CLICK_ON_WEBVIEW = 1;
    private static final int CLICK_ON_URL = 2;
    
    protected int limit = 30;
    protected int page = 1;
    protected boolean myload = true;
    protected int exitCount = 0;

    TextItem curTextItem = null;

    protected ProgressItem progressItem = new ProgressItem("", true);

    
	private ItemAdapter adapter;
	
    @SuppressWarnings("deprecation")
	@TargetApi(3)
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.m_tubebook);
        setTitle(R.string.info_browser);
        //initWidgetTabItem(10);

        if (NAction.getCode(this).startsWith("ysearch")) {
	        ImageButton homeBtn = (ImageButton)findViewById(R.id.gd_action_bar_home_item);
	        homeBtn.setImageResource(R.drawable.icon_nb);
        }
        
    	ListView listView = (ListView)findViewById(android.R.id.list);
    	listView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.v_tubebook_wrap, null));
    	listView.setDivider(new ColorDrawable(getResources().getColor(R.color.cgrey6)));
    	listView.setDividerHeight(1);
    	listView.setCacheColorHint(0);
        adapter = new ItemAdapter(this);
        listView.setAdapter(adapter);
        
        //LinearLayout sb = (LinearLayout)findViewById(R.id.setting_box);
        //sb.setVisibility(View.GONE);
        
    	//adapter.add(new TextItem(getString(R.string.play_from_website)));

        //listView.setVisibility(View.GONE);

		LinearLayout bBar = (LinearLayout)findViewById(R.id.return_bar_box);

		String act = getIntent().getStringExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1);
    	EditText termT = (EditText)findViewById(R.id.url_input);
    	if (termT!=null) {
    		termT.setOnTouchListener(new RightDrawableOnTouchListener(termT) {
    	        @Override
    	        public boolean onDrawableTouch(final MotionEvent event) {
    	             doClear(null);
    	             return true;
    	        }
    	    });
    	}
    	
		if (act!=null && act.equals("search")) {
			bBar.setVisibility(View.GONE);
			//initAD(TAG);
		
			String term = getIntent().getStringExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2);
	    	termT.setText(term);
	    	doSearch(null);
	    	
			EditText t = (EditText)findViewById(R.id.url_input);
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
			//t.setText(null);
			imm.hideSoftInputFromWindow(t.getWindowToken(), 0);
			t.clearFocus();
		}  else {
			bBar.setVisibility(View.VISIBLE);
		}
		    	
    	// check network
		if (NAction.getCode(this).startsWith("ysearch")) {
	    	if (!NUtil.isExternalStorageExists()) {
	    		WBase.setTxtDialogParam(R.drawable.alert_dialog_icon, R.string.not_sd, new  DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
	    		});
	    		showDialog(_WBase.DIALOG_NOTIFY_MESSAGE+dialogIndex);
	    		dialogIndex++;
	    	}
	    	
	    	if (!NUtil.netCheckin(getApplicationContext())) {
	    		//Toast.makeText(getApplicationContext(), R.string.need_network, Toast.LENGTH_LONG).show();
	    		Toast.makeText(getApplicationContext(), R.string.need_network, Toast.LENGTH_SHORT).show();

	    	} else {
	    		int now = VeDate.getStringDateHourAsInt();
	    		int lastCheck = NAction.getUpdateCheckTime(this);
	    		
	    		if (!notifyErr(getApplicationContext())) {
	    		
	    			int q = NAction.getUpdateQ(getApplicationContext());
	    			if (q==0) {
	    				q = com.zuowuxuxi.config.CONF.UPDATEQ;
	    			}
	    			if ((now-lastCheck)>=q) {	// 每q小时检查一次更新/清空一下不必要的cache
	    				checkUpdate(getApplicationContext(), true);
	    				// 清空图片目录的缓存
	    	    		String cacheDir = Environment.getExternalStorageDirectory()+"/"+CONF.BASE_PATH+"/"+com.zuowuxuxi.config.CONF.DCACHE+"/";
	    				FileHelper.clearDir(cacheDir, 0, false);
	    				
	    				// 清理DB缓存
	    				
	    			}
	    		}
	    	}
		}    	

		// display input
    	View tl = findViewById(R.id.topline);
    	tl.setVisibility(View.GONE);
    	
    	//EditText termT = (EditText)findViewById(R.id.url_input);
    	//termT.setSelected(true);
    	//termT.requestFocus();
		
    	// webview

    	startWV();
    	
    	String sAct = getIntent().getStringExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1);
    	String term = getIntent().getStringExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2);
		final EditText searchInput = (EditText)findViewById(R.id.url_input);

		if (searchInput!=null) {
	    	if (sAct!=null && sAct.equals("search")) {
				searchInput.setText(term);
				doSearch(null);
	    	} 
	    	searchInput.setOnKeyListener(new OnKeyListener() {
	    		@Override
	    		public boolean onKey(View v, int keyCode, KeyEvent event) {
	    		    String barcode = searchInput.getText().toString();
	    		    if (keyCode == KeyEvent.KEYCODE_ENTER && barcode.length() > 0) {
	    		    	doSearch(null);
	    		        return true;
	    		    }
	    		    return false;
	    		}
	    	});
		}
    	
		//disNotify(TAG);
        IntentFilter filter = new IntentFilter(".MTubebook");

		registerReceiver(playOrDownloadReceiver, filter);
    }
    
    @Override
	public void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(playOrDownloadReceiver);
    }

    protected void playFromGW(String videoUrl) {
    	if (NUtil.netCheckin(getApplicationContext())) {
    		openWaitWindow();
			String str2;
			try {
				str2 = URLEncoder.encode(videoUrl, "UTF-8");

	    		final String searchUrl = CONF.VIDEO_GW_SEARCH_URL+str2;
	        	NRequest.get2(getApplicationContext(), searchUrl, null, new AsyncHttpResponseHandler() {
	                @Override
					@SuppressWarnings("deprecation")
					public void onSuccess(String response) {
	                	closeWaitWindow();

	                	//Log.d(TAG, "get video:"+searchUrl+"\n"+response);
	                	JSONObject json;
						try {
							json = new JSONObject(response);
		                	final JSONArray urls = json.getJSONArray("url");
		                	//String videoTitle = null;
		                	JSONArray titles;
		                	try {
		                		titles = json.getJSONArray("titles");
		                	} catch (Exception e) {
		                		titles = null;
		                	}
		                	try {
		                		//videoTitle = json.getString("title");
		                		//byte [] b = videoTitle.getBytes("UTF-8");
		                		//videoTitle = new String(b, "UTF-8");
		                		//Log.d(TAG, "videoTitle:"+videoTitle);
		                	} catch (Exception e) {
		                	}
		                	//final String title = videoTitle;
		                	if (urls!=null) {
		                		String list[] = new String[urls.length()];
		                		for (int i=0;i<list.length;i++) {
		                			if (titles!=null && titles.length()>i) {
		                				list[i] = titles.getString(i);
		                			} else {
		                				list[i] = MessageFormat.format(getString(R.string.video_src), i+1);
		                			}
		                		}
		                		WBase.setSingleListDialogParam(0, R.string.play_video, list,
		                			new DialogInterface.OnClickListener() {
			        					@Override
			        					public void onClick(DialogInterface dialog, int which) {
			        						dialog.dismiss();
			        						String videoUrl;
											try {
												videoUrl = (String)urls.get(which);
												playFromRemote(videoUrl);
											} catch (JSONException e) {
												// TODO Auto-generated catch block
												Toast.makeText(getApplicationContext(), R.string.exception_try_again, Toast.LENGTH_SHORT).show();
												e.printStackTrace();
											}
			        					}
		        				});
		                        showDialog(_WBase.DIALOG_SINGLE_LIST+dialogIndex);
		                        dialogIndex++;
		                	} else {
			                	Toast.makeText(getApplicationContext(), R.string.no_videos_found, Toast.LENGTH_LONG).show();
		                	}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
		                	Toast.makeText(getApplicationContext(), R.string.no_videos_found, Toast.LENGTH_LONG).show();
						}
	                	
	                }
	                @Override
	                public void onFailure(Throwable error) {
	                	closeWaitWindow();
	                	Toast.makeText(getApplicationContext(), R.string.no_videos_found, Toast.LENGTH_LONG).show();
	                }

	        	});
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	} else {
    		Toast.makeText(getApplicationContext(), R.string.need_network, Toast.LENGTH_SHORT).show();
    	}
    }
    protected final BroadcastReceiver playOrDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "downloadReceiver");
			String act = intent.getExtras().getString(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1);
			String url = intent.getExtras().getString(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2);

			if (act.equals("play")) {
				playFromRemote(url);
				
			} else if (act.equals("playgw")) {
				playFromGW(url);
				
			} else if (act.equals("installqpylib")) {
				
			} else {
				String title = intent.getExtras().getString(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL3);
				String cat = intent.getExtras().getString(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL4);
	
		    	downloadReceiver(title, url, cat);
			}
		}
    };

    @SuppressWarnings("deprecation")
	public void downloadReceiver(final String title, final String addr, final String cat){
    	Log.d(TAG, "downloadReceiver:"+addr+"-"+cat+"-"+title);
        String root = NAction.getDefaultRoot(getApplicationContext());
		File targetFile;

		String xx = "."+FileHelper.getExt(FileHelper.getFileName(NUtil.getPathFromUrl(addr)), "mp4");
		try {
			if (root.equals("")) {
				targetFile = new File(FileHelper.getBasePath(CONF.BASE_PATH, com.zuowuxuxi.config.CONF.DFROM_LOCAL), cat+"/"+title+xx);
	
			} else {
				targetFile = new File(FileHelper.getABSPath(root+"/"+cat+"/"), title+xx);
			}
			
			if (targetFile.exists()) {
	    		WBase.setTxtDialogParam(R.drawable.alert_dialog_icon, R.string.confirm_title, MessageFormat.format(getString(R.string.confirm_download_target_exitst), cat, title), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {	
						// TODO: continue download
						//tbdownload(title, addr, cat);
					}
	    		}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
	    			
	    		});
	    		showDialog(_WBase.DIALOG_YES_NO_LONG_MESSAGE+dialogIndex);
	    		dialogIndex++;

			} else {
				File downloadFile;
				downloadFile = new File(FileHelper.getBasePath(CONF.BASE_PATH, "tmp"), cat+"_"+title+xx);
				if (downloadFile.exists()) {
					
		    		WBase.setTxtDialogParam(R.drawable.alert_dialog_icon, R.string.confirm_title, MessageFormat.format(getString(R.string.confirm_download),cat, title), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							//tbdownload(title, addr, cat);
						}
		    		}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
		    		});
		    		showDialog(_WBase.DIALOG_YES_NO_LONG_MESSAGE+dialogIndex);
		    		dialogIndex++;
				} else {
					//tbdownload(title, addr, cat);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //download(dTitle, dLink, dArtist, dAlbum, dPlay, dOrgLink, dQuality, dCompletedSize, dIsNew, dExt);
    }
    
    @Override
    public int createLayout() {
        return R.layout.gd_content_normal;
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
    public void startWV() {
    	initWebView();//执行初始化函数
        MyBean bean = new MyBean(this);
        bean.setTitle("MILIB");
        wv.addJavascriptInterface(bean, "milib");
        wv.setOnTouchListener(this);
        wv.requestFocus();

    	wv.setDownloadListener(new DownloadListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onDownloadStart(final String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				//String dir = FileHelper.getTypeByMimeType(mimetype);
				String filename = FileHelper.getFileNameFromUrl(url);

				//Toast.makeText(getApplicationContext(), "Download(mimetype:"+mimetype+")(contentDisposition:"+contentDisposition+")(contentLength:"+contentLength+")(dir:"+dir+")(file:"+filename+"):"+url, Toast.LENGTH_LONG).show();
				//Log.d(TAG, "Download(mimetype:"+mimetype+")(contentDisposition:"+contentDisposition+")(contentLength:"+contentLength+")(dir:"+dir+")(file:"+filename+"):"+url);
			
    	    	EditText termT = (EditText)findViewById(R.id.url_input);    	

    			WebBackForwardList mWebBackForwardList = wv.copyBackForwardList();
    			try {
	    			String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()).getUrl();
	
	    	    	termT.setText(historyUrl);
    			} catch (Exception e) {
    			}
    	    	
    	    	// Download confirm
    	        /*String root = NAction.getDefaultRoot(getApplicationContext());
    	        String rootDir;
    			try {
    				if (root.equals("")) {
    					rootDir = new File(FileHelper.getBasePath(CONF.BASE_PATH, CONF.DFROM_LOCAL),"").getAbsolutePath();
    				} else {
    					rootDir = root;
    				}

    				String ext = "."+FileHelper.getExt(FileHelper.getFileName(NUtil.getPathFromUrl(url)), "dat");
					*/
	    	    	String savePath = filename;
	    			WBase.setTxtDialogParam(0, R.string.download_as, savePath,
	    					new DialogInterface.OnClickListener() {
	    						@Override
	    						public void onClick(DialogInterface dialog, int which) {
	    					        AlertDialog ad = (AlertDialog) dialog;  
	    					        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
	    					        String content = t.getText().toString();
	    					        
	    					        String ext = FileHelper.getExt(content, "dat");
	    					        String title = content.substring(0, content.lastIndexOf("."+ext));
	    					        downloadReceiver(title, url,ext);
	    						}
	    					},null);
	    			showDialog(_WBase.DIALOG_TEXT_ENTRY+dialogIndex);
	    			dialogIndex++;
    			/*} catch (Exception e) {
    				
    			}*/


			}
    	});

    	//if (NUtil.netCheckin(getApplicationContext())) {
			//NAction.userProxy(getApplicationContext());
    		//String lang = NUtil.getLang();
    		//Log.d(TAG, "lang:"+lang);
    		String mediaUrl = NAction.getMediaCenter(getApplicationContext());
            Intent i = getIntent();
            if (i.getData() != null) {
            	mediaUrl = i.getDataString();
            } else {
            	mediaUrl = "file:///android_asset/mbox/md3.html";
        		String lang = NUtil.getLang();

    			if (lang.equals("zh")) {
    				mediaUrl = "file:///android_asset/mbox/md3_zh.html";
    			}
            }
            
    		/*if (mediaUrl.equals("")) {
    			mediaUrl = CONF.MEDIA_LINK;
    		}*/
			/*String html5file = "http://play.qpython.com/mna8-video.php";

			if (lang.equals("zh")) {
				html5file = "http://play.qpython.com/mna8-video-zh.php";
			}*/
			
	    	EditText termT = (EditText)findViewById(R.id.url_input);    	
	    	termT.setText(mediaUrl);
			loadurl(wv, mediaUrl);
    		
    	/*} else {
    		
			String html5file = "file:///android_asset/mbox/md3.html";
    		String lang = NUtil.getLang();

			if (lang.equals("zh")) {
				html5file = "file:///android_asset/mbox/md3_zh.html";
			}
            Intent i = getIntent();
            if (i.getData() != null) {
            	html5file = i.getDataString();
            	Toast.makeText(getApplicationContext(), R.string.need_network, Toast.LENGTH_SHORT).show();
            } 
            
	    	EditText termT = (EditText)findViewById(R.id.url_input);    	
	    	termT.setText(html5file);

			loadurl(wv, html5file);
    	}*/
    }
    


	@SuppressWarnings("deprecation")
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			//其他Intent返回的结果
			final EditText searchInput = (EditText)findViewById(R.id.url_input);

			if (data!=null) {
	            final ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  
	            if (results!=null) {
	            	
	        		WBase.setSingleChoiceDialogParam(R.drawable.alert_dialog_icon, R.string.mp3_opt, results,
	        				new DialogInterface.OnClickListener() {
	        					@Override
	        					public void onClick(DialogInterface dialog, int which) {
	        						searchInput.setText(results.get(which));
	        						
	        						doSearch(null);
	        						
	        						dialog.dismiss();
	        					}
	        				},new DialogInterface.OnClickListener() {
	        					@Override
	        					public void onClick(DialogInterface dialog, int which) {
	        						searchInput.setText(results.get(0));
	        						doSearch(null);

	        						dialog.dismiss();
	        					}
	        				},null);
	        		
	                showDialog(_WBase.DIALOG_SINGLE_CHOICE+dialogIndex);
	                dialogIndex = dialogIndex+1;
	                
	            } else {
	            	Toast.makeText(MTubebook.this, R.string.search_no_data, Toast.LENGTH_LONG).show(); 
	            }
			}
		}
        super.onActivityResult(requestCode, resultCode, data);  
    }      

    public void doSearch(View v) {
    	EditText termT = (EditText)findViewById(R.id.url_input);    	
    	String url;
    	if (termT == null || termT.getText().toString().equals("")) {
    		Toast.makeText(getApplicationContext(), R.string.err_not_input, Toast.LENGTH_SHORT).show();
    	} else {
    		url = termT.getText().toString();
    		if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file://") && !url.startsWith("content://")) {
    			url = "http://"+url;
    			termT.setText(url);
    		}
    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
    		//t.setText(null);
    		imm.hideSoftInputFromWindow(termT.getWindowToken(), 0);
    		termT.clearFocus();
    		
			loadurl(wv, url);
    	}
    }
    
    public void doClear(View v) {
    	EditText termT = (EditText)findViewById(R.id.url_input);
    	termT.setText("");
    }
    public void onInputClicked(View v) {
    	EditText termT = (EditText)findViewById(R.id.url_input);
    	termT.setHint("");
    }

	class MyBean extends Bean {

		public MyBean(Context context) {
			super(context);
		}
		
	    public void search(String term) {
			Message msg = new Message();
			msg.obj = term;
			searchHandler.sendMessage(msg);

	    	//Toast.makeText(context, "searched:"+term, Toast.LENGTH_SHORT).show();
	    }
	}
	@SuppressLint("HandlerLeak")
	private Handler searchHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String term = (String)msg.obj;
			
			final EditText searchInput = (EditText)findViewById(R.id.url_input);
			searchInput.setText(term);
			doSearch(null);
		}
	};
	
	public void onPlay(View v) {
	}
	
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
    	if (keycode == KeyEvent.KEYCODE_BACK) {
    		if (wv.canGoBack()) {
    			WebBackForwardList mWebBackForwardList = wv.copyBackForwardList();
    			String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()-1).getUrl();

    	    	EditText termT = (EditText)findViewById(R.id.url_input);    	
    	    	termT.setText(historyUrl);

    			wv.goBack();
    			return false;
    		} else {
        		
    			finish();

        		
    		}
    		
    	}
    	return super.onKeyDown(keycode, event);
    }


	public void onSearch(View v) {
		doSearch(null);
	}

	
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.wv && event.getAction() == MotionEvent.ACTION_DOWN){
            handler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 500);
        }
        return false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == CLICK_ON_URL){
            handler.removeMessages(CLICK_ON_WEBVIEW);
            return true;
        }
        if (msg.what == CLICK_ON_WEBVIEW){
        	wv.requestFocus();
            //Toast.makeText(this, "WebView clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


	
}
