package com.hipipal.texteditor;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.zuowuxuxi.common.CrashHandler;
import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NUtil;

import android.content.Context;
import android.content.Intent;
import greendroid.app.GDApplication;

public class MNApp extends GDApplication {

	//Map<String, Integer> movieDirs = null;
	GoogleAnalyticsTracker tracker;

	@Override  
    public void onCreate() {  
        super.onCreate();  
        CrashHandler crashHandler = CrashHandler.getInstance();  
        //注册crashHandler类  
        crashHandler.init(getApplicationContext()); 
    	tracker = GoogleAnalyticsTracker.getInstance();
    	String x = NAction.getExtP(getApplicationContext(), "ga_gap");
    	int xq = 30;
    	if (!x.equals("")) {
    		xq = Integer.valueOf(x);
    	}
    	String gtid = CONF.GOOGLE_TRACKER_ID;
    	String gtid2 = NAction.getExtP(getApplicationContext(), "ga_gtid");
    	if (!gtid2.equals("")) {
    		gtid = gtid2;
    	}
        tracker.startNewSession(gtid, xq, getApplicationContext());
    }  
    @Override
    public Class<?> getHomeActivityClass() {
        return TedActivity.class;
    }
    
    @Override
    public Intent getMainApplicationIntent() {
    	return null;

        //return new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
    }
    
    public void trackPageView(String page) {
    	if (tracker!=null) {
    		tracker.trackPageView("/"+NUtil.getVersinoCode(getApplicationContext())+"-"+page);
    	}
    }
    
    public void stopTraker() {
    	if (tracker!=null) {
    		tracker.stopSession();
    	}
    }

    
    public void logout(Context context) {
    }


}
