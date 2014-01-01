package com.hipipal.texteditor;

import java.io.File;
import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.base._WBase;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MSettingAct extends _ABaseAct {
	private static final String TAG = "MSettingAct";
    private static final int SCRIPT_EXEC_CODE = 1235;  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.m_setting);
        setTitle(R.string.m_title_3);
        //ArrayList<String> av = new ArrayList();
        //String k = av.get(1);
        
        initWidgetTabItem(4);
        
        // alpha
        if (NAction.checkPluginNoAdEnable(getApplicationContext())) {
        	RelativeLayout tb = (RelativeLayout)findViewById(R.id.plugin_setting_box);
        	tb.setVisibility(View.VISIBLE);
        }
        // extend plugin
        if (NAction.checkIfScriptExtend(getApplicationContext())) {
        	RelativeLayout sb = (RelativeLayout)findViewById(R.id.plugin_script_box);
        	sb.setVisibility(View.VISIBLE);
        }

        
        if (NAction.getExtP(getApplicationContext(), "conf_is_pro").equals("1")) {
	        RelativeLayout fb = (RelativeLayout)findViewById(R.id.plugin_ftp_box);
	        fb.setVisibility(View.VISIBLE);
        }

        
        RelativeLayout rb = (RelativeLayout)findViewById(R.id.proxy_box);
        rb.setVisibility(View.GONE);
        
        //if (NAction.getExtP(getApplicationContext(), "conf_is_pro").equals("1")) {
        RelativeLayout fb = (RelativeLayout)findViewById(R.id.plugin_ftp_box);
        fb.setVisibility(View.VISIBLE);
        //}

        //RelativeLayout pb = (RelativeLayout)findViewById(R.id.plugin_defaultroot_box);
        //pb.setVisibility(View.VISIBLE);

        if (NAction.getExtP(this, "conf_is_pro").equals("0")) {
	        String notifyMsg = NAction.getExtP(getApplicationContext(), "conf_pro_msg");

            RelativeLayout ab = (RelativeLayout)findViewById(R.id.plugin_adfree_box);
            TextView at = (TextView)findViewById(R.id.plugin_adfree);
            if (!notifyMsg.equals("")) {
            	at.setText(notifyMsg);
            }
            
            String adpkg = NAction.getExtP(getApplicationContext(), "conf_no_ad_pkg");
    		if  (!NUtil.checkAppInstalledByName(getApplicationContext(), adpkg)) {
                ab.setVisibility(View.VISIBLE);
    		}
        }
	    
        //RelativeLayout pb = (RelativeLayout)findViewById(R.id.pylib_box);
        //pb.setVisibility(View.VISIBLE);


        //TextView ftpVal = (TextView)findViewById(R.id.plugin_ftp_value);
        //ftpVal.setText();
        
        
		//NAction.recordUserLog(getApplicationContext(), "setting", "");

        /*TextView host = (TextView)findViewById(R.id.proxy_host_value);
        host.setText(NAction.getProxyHost(getApplicationContext()));
        
        TextView port = (TextView)findViewById(R.id.proxy_port_value);
        port.setText(NAction.getProxyPort(getApplicationContext()));

        TextView username = (TextView)findViewById(R.id.proxy_username_value);
        username.setText(NAction.getProxyUsername(getApplicationContext()));

        TextView pwd = (TextView)findViewById(R.id.proxy_pwd_value);
        pwd.setText(NAction.getProxyPwd(getApplicationContext()));*/
        
        displayDefaultRoot();
        displayProxy();
        
        MNApp mnApp = (MNApp) this.getApplication();
        mnApp.trackPageView("/"+NAction.getCode(getApplicationContext())+"/msetting");

        MyApp.getInstance().addActivity(this); 
    }
    

    public void onADFree(View v) {
        String adfreeUrl = NAction.getExtP(getApplicationContext(), "conf_no_ad_pkg_url");
        try {
			Intent intent = NAction.openRemoteLink(this, adfreeUrl);
			startActivity(intent);
        } catch (Exception e) {
    		Intent intent = NAction.openRemoteLink(this, "http://play.tubebook.net/adfree-tubeboook-app.html");
    		startActivity(intent);
        }
    }


    public void onPyLib(View v) {
    	String extPlgPlusName = com.zuowuxuxi.config.CONF.EXT_PLG_PLUS;

		String localPlugin = this.getPackageName();
		Intent intent = new Intent();
		intent.setClassName(localPlugin, extPlgPlusName+".MPyLibAct");

		startActivity(intent);
    }

    public void onFtpSetting(View v) {
    	Intent intent = new Intent(this, MFTPSettingAct.class);
    	startActivity(intent);
    }
    
    
    public void displayDefaultRoot() {
    	//String proxyHost = NAction.getProxyHost(getApplicationContext());
    	//String proxyPort = NAction.getProxyPort(getApplicationContext());
    	String root = NAction.getDefaultRoot(getApplicationContext());
	    String code = NAction.getCode(getApplicationContext());

    	if (root.equals("")) {
    	    if (code.startsWith("mn")) {
    	    	root = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
    	    } else {
    	    	root = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/"+CONF.BASE_PATH+"/";
    	    }
    	}
    	TextView rootValue = (TextView)findViewById(R.id.plugin_defaultroot_value);
    	rootValue.setText(root);
    }

    public void displayProxy() {
    	String proxyHost = NAction.getProxyHost(getApplicationContext());
    	String proxyPort = NAction.getProxyPort(getApplicationContext());
    	TextView proxyValue = (TextView)findViewById(R.id.proxy_value);
    	proxyValue.setText(proxyHost+":"+proxyPort);
    }
    
    @SuppressWarnings("deprecation")
	public void onSetProxy(View v) {
    	String proxyHost = NAction.getProxyHost(getApplicationContext());
    	String proxyPort = NAction.getProxyPort(getApplicationContext());
    	
		WBase.setTxtDialogParam2(0, R.string.proxy_setting, getString(R.string.proxy_host), getString(R.string.proxy_port), proxyHost, proxyPort,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t1 = (EditText) ad.findViewById(R.id.editText_prompt1);
				        EditText t2 = (EditText) ad.findViewById(R.id.editText_prompt2);
				        String host = t1.getText().toString();
				        String port = t2.getText().toString();
				        boolean alert = false;
				        if (host!=null && !host.equals("")) {
				        	if (NUtil.isIP(host)) {
						        NAction.setProxyHost(getApplicationContext(), host);
								t1.setText(host);
								
				        	} else {
				        		alert = true;
					        	Toast.makeText(getApplicationContext(), R.string.err_ip_format, Toast.LENGTH_SHORT).show();

				        	}
				        } else {
					        NAction.setProxyHost(getApplicationContext(), "");
							t1.setText("");
				        }
				        if (!alert) {
					        if (port!=null && !port.equals("")) {
						        if (NUtil.isInt(port)) {
							        NAction.setProxyPort(getApplicationContext(), port);
									t2.setText(port);
						        } else {
						        	Toast.makeText(getApplicationContext(), R.string.err_need_int, Toast.LENGTH_SHORT).show();
						        }
					        } else {
						        NAction.setProxyPort(getApplicationContext(), "");
								t2.setText("");
					        }
				        }
				        
				        displayProxy();

					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY2+1);
    }

 
    @SuppressWarnings("deprecation")
	public void onDefaultRootSetting(View v) {
		final TextView rootText = (TextView)findViewById(R.id.plugin_defaultroot_value);
		String rootVal = rootText.getText().toString();
		WBase.setTxtDialogParam(0, R.string.plugin_defaultroot, rootVal,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        
				        boolean failed = true;
				        String err = getString(R.string.root_need);
				        if (content!=null && !content.equals("")) {
				        	
				        	File r = new File(content);
				        	if (r.exists()) {
				        		if (r.isDirectory()) {
				        			failed = false;
				        		} else {
				        			err = getString(R.string.root_notdir);
				        		}
				        	} else {
			        			err = getString(R.string.root_noexist);

				        	}
				        	
				        } 
				        
				        if (failed) {
				        	Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();
				        	onDefaultRootSetting(null);
				        } else {
				        	NAction.setDefaultRoot(getApplicationContext(), content);
				        	displayDefaultRoot();
				        	Toast.makeText(getApplicationContext(), R.string.set_root_ok, Toast.LENGTH_SHORT).show();
				        }
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+dialogIndex);
		dialogIndex++;
	}
    
    @SuppressWarnings("deprecation")
	public void onMediaCenterSetting(View v) {
		final TextView media = (TextView)findViewById(R.id.plugin_mediacenter_value);
		String mediaVal = media.getText().toString();
		WBase.setTxtDialogParam(R.drawable.ic_setting, R.string.plugin_mediacenter, mediaVal,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        NAction.setMediCenter(getApplicationContext(), content);
				        media.setText(content);
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+dialogIndex);
		dialogIndex++;
    }
    
	public void setProxyPort(View v) {
		/*final TextView port = (TextView)findViewById(R.id.proxy_port_value);
		String portVal = port.getText().toString();
		WBase.setTxtDialogParam(0, R.string.proxy_port, portVal,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        if (content!=null && !content.equals("")) {
					        if (NUtil.isInt(content)) {
						        NAction.setProxyPort(getApplicationContext(), content);
								port.setText(content);
					        } else {
					        	Toast.makeText(getApplicationContext(), R.string.err_need_int, Toast.LENGTH_SHORT).show();
					        }
				        } else {
					        NAction.setProxyPort(getApplicationContext(), "");
							port.setText("");
				        }
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+2);*/
	}

	/*public void setProxyUsername(View v) {
		final TextView username = (TextView)findViewById(R.id.proxy_username_value);
		String usernameVal = username.getText().toString();
		WBase.setTxtDialogParam(R.drawable.ic_setting, R.string.proxy_port, usernameVal,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        NAction.setProxyUsername(getApplicationContext(), content);
						username.setText(content);
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+3);
	}
	
	public void setProxyPwd(View v) {
		final TextView pwd = (TextView)findViewById(R.id.proxy_pwd_value);
		String pwdVal = pwd.getText().toString();
		WBase.setTxtDialogParam(R.drawable.ic_setting, R.string.proxy_port, pwdVal,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        NAction.setProxyPwd(getApplicationContext(), content);
						pwd.setText(content);
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+4);
	}*/
	
	public void enableScript(View v) {
		Intent intent = new Intent();
		intent.setClassName("com.hipipal.mi", "com.hipipal.mi.PyScriptExtendAct");
		intent.setAction("com.hipipal.mi.action.PyScriptExtendAct");
		
		Bundle mBundle = new Bundle(); 
		mBundle.putString("app","mn");
		mBundle.putString("act", "main");
		intent.putExtras(mBundle);
		
		//Intent intent = new Intent(MSettingAct.this, PyScriptExtendAct.class);

		//Intent intent = new Intent(MSettingAct.this, SL4AScriptActivity.class);
		startActivityForResult(intent, SCRIPT_EXEC_CODE);
	}
	
	public void onRate(View v){
		//Log.d(TAG, "exit1");
		//String rateUrl = NAction.getInstallLink(getApplicationContext());
		String rateUrl = NAction.getExtP(this, "conf_rate_url");
		if (rateUrl.equals("")) {
			rateUrl = "http://play.tubebook.net/";
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(rateUrl));
		startActivity(i);
	}
	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if (requestCode == SCRIPT_EXEC_CODE) {
			Log.d(TAG, "script exec:");
		}
        super.onActivityResult(requestCode, resultCode, data);  

	}

}
