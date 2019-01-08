package com.hylanda.getmessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.hylanda.getmessage.util.Constant;
import com.hylanda.getmessage.util.SystemUtil;
import com.hylanda.getmessage.util.ThreadPoolHttpUtil;
import com.hylanda.getmessage.util.ThreadPoolHttpUtil.HttpCallback;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

@SuppressLint("NewApi")
public class NewsNotificationListenerService extends
		NotificationListenerService {

	private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
	int i = 0 ;
	@Override
	public void onCreate() {
		
		Log.e("�����������", "success");
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// �ж�wifi�Ƿ���
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		Log.e("int ip ", "="+ipAddress);
		Constant.ip = SystemUtil.intToIp(ipAddress);
		
		if (!isEnabled()) {
			Log.e("����û������", "������ҳ��");
			Intent intent = new Intent(
					"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
			startActivity(intent);
			toggleNotificationListenerService();
		} else {
			// toggleNotificationListenerService(MainActivity.this);
			//toggleNotificationListenerService();
			/*if (i==0) {
				toggleNotificationListenerService();
				i++;
			}*/
			Log.e("�����Ѿ�����", "success");
		}
		super.onCreate();
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		// ͨ������ķ�����ȡintent,��ȡ����,��С���ֻ��л��޸�sbn.getPackageName
		PendingIntent pendingIntent = sbn.getNotification().contentIntent;
		String packageName = getPackageName(pendingIntent);

		Log.e("����", packageName);
		Notification notification = sbn.getNotification();
		if (notification == null) {
			return;
		}
		String title = "";
		String content = "";
		// PendingIntent pendingIntent = null;
		// api>18 ʹ��extras��ȡ֪ͨ����ϸ��Ϣ
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Bundle extras = notification.extras;
			if (extras != null) {
				title = extras.getString(Notification.EXTRA_TITLE, "");
				content = extras.getString(Notification.EXTRA_TEXT, "");
				Log.e("title", title);
				Log.e("content", content);
			}
		} else {
			// �� API = 18 ʱ�����÷����ȡ�����ֶ�
			List<String> textList = getText(notification);
			if (textList != null && textList.size() > 0) {
				for (String text : textList) {
					if (!TextUtils.isEmpty(text)) {
						Log.e("18 text", text);
						pendingIntent = notification.contentIntent;
						break;
					}
				}
			}
		}
		sendData(title, content, packageName);
		super.onNotificationPosted(sbn);
	}

	private String getPackageName(PendingIntent pendingIntent) {

		String result = "";
		Class<? extends PendingIntent> pdi = pendingIntent.getClass();

		Field[] fs = pdi.getDeclaredFields();

		StringBuffer sb = new StringBuffer();
		sb.append(Modifier.toString(pdi.getModifiers()) + " class "
				+ pdi.getSimpleName() + "{\n");
		for (Field field : fs) {
			sb.append("\t");// �ո�
			sb.append(Modifier.toString(field.getModifiers()) + " ");// ������Ե����η�������public��static�ȵ�
			sb.append(field.getType().getSimpleName() + " ");// ���Ե����͵�����
			sb.append(field.getName() + ";\n");// ���Ե�����+�س�
		}
		sb.append("}");

		Method[] methods = pdi.getDeclaredMethods();

		for (Method method : methods) {
			Log.e("" + method.getName(),
					"returntype = " + method.getReturnType());
		}
		// String pdipgn = pendingIntent.getTargetPackage();

		try {
			Method method = pdi.getDeclaredMethod("getIntent");

			method.setAccessible(true);
			Intent intent = (Intent) method.invoke(pendingIntent);
			ComponentName componentName = intent.getComponent();
			result = componentName.getPackageName();

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		super.onNotificationRemoved(sbn);
	}

	@Override
	public void onListenerConnected() {
		super.onListenerConnected();
	}

	public List<String> getText(Notification notification) {
		if (null == notification) {
			return null;
		}
		RemoteViews views = notification.bigContentView;
		if (views == null) {
			views = notification.contentView;
		}
		if (views == null) {
			return null;
		}
		// Use reflection to examine the m_actions member of the given
		// RemoteViews object.
		// It's not pretty, but it works.
		List<String> text = new ArrayList<>();
		try {
			Field field = views.getClass().getDeclaredField("mActions");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field
					.get(views);
			// Find the setText() and setTime() reflection actions
			for (Parcelable p : actions) {
				Parcel parcel = Parcel.obtain();
				p.writeToParcel(parcel, 0);
				parcel.setDataPosition(0);
				// The tag tells which type of action it is (2 is
				// ReflectionAction, from the source)
				int tag = parcel.readInt();
				if (tag != 2)
					continue;
				// View ID
				parcel.readInt();
				String methodName = parcel.readString();
				if (null == methodName) {
					continue;
				} else if (methodName.equals("setText")) {
					// Parameter type (10 = Character Sequence)
					parcel.readInt();
					// Store the actual string
					String t = TextUtils.CHAR_SEQUENCE_CREATOR
							.createFromParcel(parcel).toString().trim();
					text.add(t);
				}
				parcel.recycle();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * ����http����
	 * 
	 * @param map
	 * @param packName
	 */
	private void sendData(String title, String text, String packName) {
		try {
			// String urlPath =
			// "http://192.168.12.188:8080/NewsTest/NewsUploadServlet";
			// String urlPath =
			// "http://192.168.11.138:8080/NewsTest/NewsUploadServlet";
			String urlPath = "http://wly.hylanda.com/server/api/datareceiver.php";
			String urlPathXinjiang = "http://xinjiang.cmd.hylanda.com/server/api/datareceiver.php";
			
			Date date = new Date();
			SimpleDateFormat format1 = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String tempTime = format1.format(date);
			if (Constant.ip == null||Constant.ip.equals("0.0.0.0")) {
				getIp();
			}
			if (isNull(text) && isNull(title)) {
				return;
			}
			JSONObject jsonObject = new JSONObject();
			try {

				jsonObject.put("title", title);

				jsonObject.put("text", text);
				jsonObject.put("time", tempTime);
				jsonObject.put("ip", Constant.ip);

				jsonObject.put("mediaName", packName);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (!isContainsText(title) && !isContainsText(text)) {
				ThreadPoolHttpUtil.doPostForm(urlPathXinjiang, jsonObject.toString(),
						new HttpCallback<String>() {

							@Override
							public void onSuccess(String response) {
								System.out.println("success=" + response);
							}

							@Override
							public void onError(String error) {
								System.out.println("error = " + error);
							}
						});
			}
			if (!isContainsText(title) && !isContainsText(text)) {
				ThreadPoolHttpUtil.doPostForm(urlPath, jsonObject.toString(),
						new HttpCallback<String>() {

							@Override
							public void onSuccess(String response) {
								System.out.println("success=" + response);
							}

							@Override
							public void onError(String error) {
								System.out.println("error = " + error);
							}
						});
				
			}

		} catch (Exception e) {
		}
	}

	private void getIp() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// �ж�wifi�Ƿ���
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		Constant.ip = SystemUtil.intToIp(ipAddress);
	}

	private boolean isNull(String text) {

		if (text == null) {
			return true;
		}
		if (text.length() == 0) {
			return true;
		}
		if (text.trim().length() == 0) {
			return true;
		}
		if (text.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * �ж�text���Ƿ����ĳЩ�ؼ���
	 * 
	 * @param text
	 * @return
	 */
	private boolean isContainsText(String text) {
		boolean flag = false;

		String loading = "��������";
		if (text.indexOf(loading) != -1) {
			flag = true;
		}
		if (text.indexOf("��ң�г�") != -1) {
			flag = true;
		}
		if (text.indexOf("��������") != -1) {
			flag = true;
		}
		if (text.indexOf("���ڳ��") != -1) {
			flag = true;
		}
		if (text.indexOf("�ֻ�����ѳ���") != -1) {
			flag = true;
		}
		if (text.indexOf("�㶹��") != -1) {
			flag = true;
		}
		if (text.indexOf("USB����") != -1) {
			flag = true;
		}
		if (text.indexOf("�����°汾") != -1) {
			flag = true;
		}
		if (text.indexOf("SD��") != -1) {
			flag = true;
		}
		if (text.indexOf("���ڰ�װ") != -1) {
			flag = true;
		}
		if (text.indexOf("��ɰ�װ") != -1) {
			flag = true;
		}
		if (text.indexOf("��������") != -1) {
			flag = true;
		}
		if (text.indexOf("Ӧ������") != -1) {
			flag = true;
		}
		if (text.indexOf("�Զ��ػ�") != -1) {
			flag = true;
		}

		return flag;
	}

	private boolean isEnabled() {
		String pkgName = getPackageName();
		final String flat = Settings.Secure.getString(getContentResolver(),
				ENABLED_NOTIFICATION_LISTENERS);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName
						.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void toggleNotificationListenerService() {
		PackageManager pm = getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(this,
				com.hylanda.getmessage.NewsNotificationListenerService.class),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);

		pm.setComponentEnabledSetting(new ComponentName(this,
				com.hylanda.getmessage.NewsNotificationListenerService.class),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);

	}
}
