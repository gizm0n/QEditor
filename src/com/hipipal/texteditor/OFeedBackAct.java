package com.hipipal.texteditor;

import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.NormalActionBarItem;

import org.json.JSONException;
import org.json.JSONObject;

import com.zuowuxuxi.asihttp.JsonHttpResponseHandler;
import com.zuowuxuxi.asihttp.RequestParams;
import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NRequest;
import com.zuowuxuxi.util.NUtil;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OFeedBackAct extends _ABaseAct {
	ProgressDialog waitingWindow;

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.feedback);

        setActionBarContentView(R.layout.o_feedback);
        
        final TextView aboutText = (TextView) findViewById(R.id.about);
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());
        
        //initWidgetTabItem(4);
        addActionBarItem(getGDActionBar()
        		.newActionBarItem(NormalActionBarItem.class)
        		.setDrawable(new ActionBarDrawable(this, R.drawable.ic_about)), 0);

        
        final EditText searchInput = (EditText)findViewById(R.id.feedback_content);
    	searchInput.setOnKeyListener(new OnKeyListener() {
    		@Override
    		public boolean onKey(View v, int keyCode, KeyEvent event) {
    		    String barcode = searchInput.getText().toString();
    		    if (keyCode == KeyEvent.KEYCODE_ENTER && barcode.length() > 0) {
    		    	onNext(null);
    		        return true;
    		    }
    		    return false;
    		}
    	});
    	
        MNApp mnApp = (MNApp) this.getApplication();
        mnApp.trackPageView("/"+NAction.getCode(getApplicationContext())+"/ofeedback");
        MyApp.getInstance().addActivity(this); 


    }
    
    /*
     * USER METHODS
     */
    public void onEmailClick(View v) {
        final EditText email = (EditText)findViewById(R.id.editText_email);
        email.setHint("");
    }
    public void onNext(View v) {
    	RequestParams param = new RequestParams();
    	param.put("uid", NAction.getUID(getApplicationContext()));
    	param.put("token", NAction.getToken(getApplicationContext()));
    	
        final EditText email = (EditText)findViewById(R.id.editText_email);
        final EditText feedback = (EditText)findViewById(R.id.feedback_content);
        String content = feedback.getText().toString();
        String emailV = email.getText().toString();
        
        if (!NUtil.isEmail(emailV)) {
        	Toast.makeText(getApplicationContext(), R.string.feed_back_email_notvalid, Toast.LENGTH_SHORT).show();
        	return;
        }
        if (content.equals("")) {
        	Toast.makeText(getApplicationContext(), R.string.feed_back_content_notvalid, Toast.LENGTH_SHORT).show();
        	return;
        }

    	param.put("email", emailV);   
    	param.put("content", content);   
    	param.put("model", "ht_feedback");   
    	openWaitWindow();
        NRequest.post(this, "appid=manager&modeid=m_feedback_add"+NAction.getUserUrl(getApplicationContext()), param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject result) {
            	closeWaitWindow();
				JSONObject act_response;
				try {
					act_response = result.getJSONObject("ACT_RESPONSE");
					String act_stat = act_response.getString("stat");
					
					if (act_stat.equals("ok")) {
		    			Toast.makeText(getApplicationContext(), R.string.feedback_ok, Toast.LENGTH_SHORT).show(); 
		    			//
		    			feedback.setText("");
		    			finish();
		    			/*
	                    Intent intent0 = new Intent(OFeedBackAct.this, MHomeAct.class);
	                    startActivity(intent0);
		    			*/
					} else {
						String info = act_response.getString("info");
		    			Toast.makeText(getApplicationContext(), getString(R.string.form_failed)+info, Toast.LENGTH_SHORT).show(); 

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

            }
            @Override
			public void onFailure(Throwable error) {
            	closeWaitWindow();
    			Toast.makeText(getApplicationContext(), getString(R.string.form_exception)+error.getMessage(), Toast.LENGTH_SHORT).show(); 
            }
        });
    	
    }

}
