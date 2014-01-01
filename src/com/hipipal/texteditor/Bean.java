package com.hipipal.texteditor;

import org.json.JSONException;
import org.json.JSONObject;

import com.zuowuxuxi.util.NAction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Bean{
	protected String title;
    protected Context context;
    //private final String TAG = "BEAN";
 
    public Bean(Context context) {
    	this.context = context;
    }
    public void setTitle(String title){
        this.title = title;
    }
    
    public void a8setmedia(String url) {
        NAction.setMediCenter(context, url);
    }
    
    /*public void a8viewChanel(String url, String nav, String act, String desc, String icon) {
    	Intent intent = new Intent(context, MSearchAct.class);
		intent.putExtra(CONF.EXTRA_CONTENT_URL1, url);
		intent.putExtra(CONF.EXTRA_CONTENT_URL2, nav);
		intent.putExtra(CONF.EXTRA_CONTENT_URL3, "0");
		intent.putExtra(CONF.EXTRA_CONTENT_URL4, act);
		intent.putExtra(CONF.EXTRA_CONTENT_URL5, desc);
		intent.putExtra(CONF.EXTRA_CONTENT_URL6, icon);

		context.startActivity(intent);
    }*/
    
    public void tbdownload(String title, String url, String cat) {
    	
		Intent intent1 = new Intent(".MTubebook");
		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1, "download");

		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2, url);
		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL3, cat);
		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL4, title);

    	context.sendBroadcast(intent1);
    }
    public void a8addChanel(String title, String link, String desc, String act, String theme) {
    	JSONObject obj = new JSONObject();
		try {
			obj.put("title", title);
    		obj.put("icon", "default");
    		//"grey3"
    		obj.put("theme", theme);
    		//"milib:pyplugin"
    		obj.put("act", act);
    		obj.put("link", link);
    		obj.put("desc", desc);
    		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//CatMan.setChanel(context, obj, true, "");
    }
    public void a8removeChanel(String link) {
		//CatMan.setChanel(context, null, false, link);
    }
    
    public void a8getTubookplugin(String link) {
		Intent intent = new Intent();
		intent.setClassName(context.getPackageName(), "com.hipipal."+NAction.getCode(context)+".MPyLibAct");
		intent.setAction("android.intent.action.VIEW");
		Uri uri = Uri.parse(link);
		intent.setDataAndType(uri , "text/*");
		context.startActivity(intent);
    }
    
    public void qpylibinstall(String cat, String link, String target) {
    	
		/*Intent intent1 = new Intent(".MTubebook");
		intent1.putExtra(CONF.EXTRA_CONTENT_URL1, "installqpylib");

		intent1.putExtra(CONF.EXTRA_CONTENT_URL2, cat);
		intent1.putExtra(CONF.EXTRA_CONTENT_URL3, link);
		intent1.putExtra(CONF.EXTRA_CONTENT_URL4, target);

    	context.sendBroadcast(intent1);*/
    	
    	Intent intent = new Intent();
    	if (cat.equals("user") || cat.equals("script")) {
    		intent.setClassName(context.getPackageName(), context.getPackageName()+".UProfileAct");
    		intent.putExtra("from", cat);

    	} else {
    		intent.setClassName(context.getPackageName(), context.getPackageName()+".MPyLibAct");
    	}
    	intent.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL0, "install");
    	intent.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1, cat);
    	intent.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2, link);
    	intent.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL3, target);

    	context.startActivity(intent);
    	
    }
    
    public void a8playVideoFromGW(String link) {
		Intent intent1 = new Intent(".MTubebook");
		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1, "playgw");

		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2, link);

    	context.sendBroadcast(intent1);
	    	
    }
    
    public void a8playVideo(String link) {
    	//String process = NUtil.getCpuProcessFromByInfo();
    	//String features = NUtil.getCpuFeaturesFromByInfo();
		Intent intent1 = new Intent(".MTubebook");
		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL1, "play");

		intent1.putExtra(com.zuowuxuxi.config.CONF.EXTRA_CONTENT_URL2, link);

    	context.sendBroadcast(intent1);
	    	
    }

 
    public String getTitle(){
        return this.title;
    }
}
