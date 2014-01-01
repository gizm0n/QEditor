package com.hipipal.texteditor;

import java.io.File;
import java.net.InetAddress;
import java.text.MessageFormat;
import org.swiftp.Defaults;

import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.base._WBase;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NUtil;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MFTPSettingAct extends _ABaseAct {
	private static final String TAG = "MSettingAct";
    //private static final int SCRIPT_EXEC_CODE = 1235;  

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.m_ftp_setting);
        setTitle(R.string.swiftp_name);
        //ArrayList<String> av = new ArrayList();
        //String k = av.get(1);
        
        initWidgetTabItem(4);

        //NAction.recordUserLog(getApplicationContext(), "setting", "");

        /*TextView host = (TextView)findViewById(R.id.ftp_host_value);
        host.setText(NAction.getProxyHost(getApplicationContext()));
        
        TextView port = (TextView)findViewById(R.id.ftp_port_value);
        port.setText(NAction.getProxyPort(getApplicationContext()));

        TextView username = (TextView)findViewById(R.id.ftp_username_value);
        username.setText(NAction.getProxyUsername(getApplicationContext()));

        TextView pwd = (TextView)findViewById(R.id.ftp_pwd_value);
        pwd.setText(NAction.getProxyPwd(getApplicationContext()));*/
        
        String externalStorage = new File(Environment.getExternalStorageDirectory(), CONF.BASE_PATH).getAbsolutePath();
        String frv = MessageFormat.format(getString(R.string.ftp_root), externalStorage);
        TextView fr = (TextView)findViewById(R.id.ftp_root_value);
        fr.setText(frv);
        
        displayAccount();
        //
        Button ftpOpBtn = (Button)findViewById(R.id.ftp_op_btn);
        if (org.swiftp.FTPServerService.isRunning()) {
        	ftpOpBtn.setText(getString(R.string.ftp_stop));
        	
        } else {
        	ftpOpBtn.setText(getString(R.string.ftp_start));

        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(org.swiftp.FTPServerService.ACTION_STARTED);
        filter.addAction(org.swiftp.FTPServerService.ACTION_STOPPED);
        filter.addAction(org.swiftp.FTPServerService.ACTION_FAILEDTOSTART);
        registerReceiver(ftpServerReceiver, filter);
        
        MNApp mnApp = (MNApp) this.getApplication();
        mnApp.trackPageView("/"+NAction.getCode(getApplicationContext())+"/mftpsetting");

        MyApp.getInstance().addActivity(this); 
    }
    
    @Override
	public void onDestroy() {
        Log.v(TAG, "onPause");
        super.onDestroy();

        Log.v(TAG, "Unregistering the FTPServer actions");
        unregisterReceiver(ftpServerReceiver);
    }
    
    public void onFTPOp(View v) {
    	if (org.swiftp.FTPServerService.isRunning()) {
    		stopServer();
    	} else {
    		startServer();
    	}
    }
    
    private void startServer() {
        Context context = getApplicationContext();
        NAction.setFtpRoot(context, Environment.getExternalStorageDirectory()+"/"+CONF.BASE_PATH);
        Intent serverService = new Intent(context, FTPServerService.class);
        if (!org.swiftp.FTPServerService.isRunning()) {
            startService(serverService);
        }
    }

    private void stopServer() {
        Context context = getApplicationContext();
        Intent serverService = new Intent(context, FTPServerService.class);
        stopService(serverService);
    }
    
    public void displayAccount() {
    	String ftpPort = NAction.getFtpPort(getApplicationContext());
    	if (ftpPort.equals("")) {
    		ftpPort = String.valueOf(Defaults.getPortNumber());
    	}
    	String ftpUsername = NAction.getFtpUsername(getApplicationContext());
    	if (ftpUsername.equals("")) {
    		ftpUsername = NAction.getCode(getApplicationContext());
    	}
    	String ftpPwd = NAction.getFtpPwd(getApplicationContext());
    	if (ftpPwd.equals("")) {
    		ftpPwd = NAction.getCode(getApplicationContext());
    	}
    	
    	TextView ftpPortValue = (TextView)findViewById(R.id.ftp_port_value);
    	ftpPortValue.setText(ftpPort);
    	
    	TextView ftpAccountValue = (TextView)findViewById(R.id.plugin_ftp_account_value);
    	ftpAccountValue.setText(ftpUsername+" / "+ftpPwd);
    }
   
    @SuppressWarnings("deprecation")
	public void onFtpAccountSetting(View v) {
    	String ftpUsername = NAction.getFtpUsername(getApplicationContext());
    	if (ftpUsername.equals("")) {
    		ftpUsername = NAction.getCode(getApplicationContext());
    	}
    	String ftpPwd = NAction.getFtpPwd(getApplicationContext());
    	if (ftpPwd.equals("")) {
    		ftpPwd = NAction.getCode(getApplicationContext());
    	}
    	
		WBase.setTxtDialogParam2(0, R.string.ftp_account_setting, getString(R.string.ftp_username), getString(R.string.ftp_pwd), ftpUsername, ftpPwd,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t1 = (EditText) ad.findViewById(R.id.editText_prompt1);
				        EditText t2 = (EditText) ad.findViewById(R.id.editText_prompt2);
				        
				        String username = t1.getText().toString();
				        String pwd = t2.getText().toString();
				        if (username!=null && pwd!=null && !username.equals("") && !pwd.equals("")) {
				        	NAction.setFtpUsername(getApplicationContext(), username);
				        	NAction.setFtpPwd(getApplicationContext(), pwd);

				        } else {
				        	Toast.makeText(getApplicationContext(), R.string.err_need_input, Toast.LENGTH_SHORT).show();

				        }
			        
				        displayAccount();

					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY2+dialogIndex);
		dialogIndex++;
    }
 
	@SuppressWarnings("deprecation")
	public void onFtpPortSetting(View v) {
		final TextView port = (TextView)findViewById(R.id.ftp_port_value);
		String portVal = port.getText().toString();
		WBase.setTxtDialogParam(0, R.string.ftp_port, portVal,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        AlertDialog ad = (AlertDialog) dialog;  
				        EditText t = (EditText) ad.findViewById(R.id.editText_prompt);
				        String content = t.getText().toString();
				        
				        if (content!=null && !content.equals("")) {
					        if (NUtil.isInt(content)) {
					        	NAction.setFtpPort(getApplicationContext(), content);
					        	displayAccount();
					        } else {
					        	Toast.makeText(getApplicationContext(), R.string.err_need_int, Toast.LENGTH_SHORT).show();
					        }
				        } else {
					        NAction.setProxyPort(getApplicationContext(), "");
							port.setText("");
				        }
					}
				},null);
		showDialog(_WBase.DIALOG_TEXT_ENTRY+dialogIndex);
		dialogIndex++;
	}
	
    BroadcastReceiver ftpServerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "FTPServerService action received: " + intent.getAction());
            TextView running_state = (TextView) findViewById(R.id.ftp_service_value);
            Button ftpOpBtn = (Button)findViewById(R.id.ftp_op_btn);

            if (intent.getAction().equals(org.swiftp.FTPServerService.ACTION_STARTED)) {
                // Fill in the FTP server address
                InetAddress address = org.swiftp.FTPServerService.getWifiIp();
                if (address == null) {
                    Log.v(TAG, "Unable to retreive wifi ip address");
                    running_state.setText(getString(R.string.cant_get_url));
                    return;
                }
                String iptext = "ftp://" + address.getHostAddress() + ":"
                        + org.swiftp.FTPServerService.getPort() + "/";
                Resources resources = getResources();
                String summary = resources.getString(R.string.running_summary_started, iptext);
                running_state.setText(summary);

                ftpOpBtn.setText(getString(R.string.ftp_stop));

            } else if (intent.getAction().equals(org.swiftp.FTPServerService.ACTION_STOPPED)) {
                running_state.setText("");
                running_state.setText(R.string.running_summary_stopped);

                ftpOpBtn.setText(getString(R.string.ftp_start));

            } else if (intent.getAction().equals(org.swiftp.FTPServerService.ACTION_FAILEDTOSTART)) {
                running_state.setText(R.string.running_summary_failed);
            	ftpOpBtn.setText(getString(R.string.ftp_start));
            }
        }
    };
}
