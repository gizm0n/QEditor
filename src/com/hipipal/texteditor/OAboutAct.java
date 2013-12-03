package com.hipipal.texteditor;

import java.text.MessageFormat;

import com.zuowuxuxi.base.MyApp;
import com.zuowuxuxi.util.NAction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class OAboutAct extends _ABaseAct {
	//private static final String TAG = "OAboutAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarContentView(R.layout.o_about);
        setTitle(R.string.m_title_aboutus);
        
        initWidgetTabItem(3);
        
		//NAction.recordUserLog(getApplicationContext(), "about", "");

		//initAD();
        
        String[] appConf = NAction.getAppConf(getApplicationContext());
        String about = appConf[0];
        String link = appConf[1];
        String feed = appConf[2];
        String feedUrl = appConf[3];

        //String selfcheck = appConf[4];
        //String selfCheckTitle = appConf[5];
        
        TextView aboutT = (TextView)findViewById(R.id.about);
        TextView marketLink = (TextView)findViewById(R.id.market_link);
        TextView feedT = (TextView)findViewById(R.id.feed_content);
        TextView feedLink = (TextView)findViewById(R.id.feed_link);
        //TextView updateT = (TextView)findViewById(R.id.update_content);

        //Button selfCheckBtn = (Button)findViewById(R.id.selfcheck_btn);
        
        if (!about.equals("")) {
        	aboutT.setText(about);
        } else {
        	aboutT.setText(getString(R.string.about_content));
        }
        if (!link.equals("")) {
        	marketLink.setText(link);
        	marketLink.setVisibility(View.VISIBLE);
        }
        
        if (!feed.equals("")) {
        	feedT.setText(feed);
        	feedT.setVisibility(View.VISIBLE);
        }
        if (!feedUrl.equals("") && !feed.equals("")) {
        	feedLink.setText(feedUrl);
        	feedLink.setVisibility(View.VISIBLE);
        }
        
        MNApp mnApp = (MNApp) this.getApplication();
        mnApp.trackPageView("/"+NAction.getCode(getApplicationContext())+"/oabout");

        MyApp.getInstance().addActivity(this); 
    }
	
	public void checkUpdate(View v) {

		//if (NUtil.netCheckin(getApplicationContext())) {
			String[] conf = NAction.getAppConf(getApplicationContext());
			if (conf[6].equals("")) {
				checkUpdate(getApplicationContext(), false);
				
			} else if (conf[6].equals("feedback")) {
				Intent intent = new Intent(OAboutAct.this, OFeedBackAct.class);
				startActivity(intent);
				
			} else {
				NAction.recordAdLog(getApplicationContext(), "feedback", "");
				Intent intent = NAction.openRemoteLink(this, conf[6]);
				this.startActivity(intent);	
			}
		/*} else {
			Toast.makeText(getApplicationContext(), R.string.net_error, Toast.LENGTH_SHORT).show();
		}*/
	}
	
    public void onShare(View v) {
		NAction.recordUseLog(getApplicationContext(), "ishare", "");

        String[] appConf = NAction.getAppConf(getApplicationContext());
        String about = appConf[0];
        //String link = appConf[1];
        //String feed = appConf[2];
        String feedUrl = appConf[3];
        
        if (feedUrl.equals("")) {
        	feedUrl = getString(R.string.app_url);
        }
		String shareContent = MessageFormat.format(getString(R.string.share_info), feedUrl);

        if (!about.equals("")) {
        	shareContent = about+" "+feedUrl;
        }  else {
        	shareContent = MessageFormat.format(getString(R.string.share_info), feedUrl);
        }
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, shareContent);

		startActivity(Intent.createChooser(share, getString(R.string.share)));
    }

}
