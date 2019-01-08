package com.hylanda.getmessage;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hylanda.getmessage.util.Constant;
import com.hylanda.getmessage.util.SystemUtil;

public class MainActivity extends Activity {

	private Button open;
	private TextView ip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		openNotificationListenSettings();
		open = (Button) findViewById(R.id.open);
		ip = (TextView) findViewById(R.id.ip);
		/*WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ipString = SystemUtil.intToIp(ipAddress);*/
		ip.setText(Constant.ip);
		
		open.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleNotificationListenerService();
				Toast.makeText(MainActivity.this, "启动完成", Toast.LENGTH_LONG).show();
			}
		});
		
	}

	/*public boolean isNotificationListenerEnabled(Context context) {
		Set<String> packageNames = NotificationManagerCompat
				.getEnabledListenerPackages(this);
		if (packageNames.contains(context.getPackageName())) {
			return true;
		}
		return false;
	}*/
	public void openNotificationListenSettings() {
	    try {
	        Intent intent;
	        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
	            intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
	        } else {
	            intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
	        }
	        startActivity(intent);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void toggleNotificationListenerService() {
	    PackageManager pm = getPackageManager();
	    pm.setComponentEnabledSetting(new ComponentName(this, NewsNotificationListenerService.class),
	            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
	    pm.setComponentEnabledSetting(new ComponentName(this, NewsNotificationListenerService.class),
	            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	    
	}
}
