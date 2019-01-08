package com.hylanda.getmessage.util;

public class SystemUtil {

	/**
	 * ��ȡ��Ǯ�ֻ�ϵͳ�汾��
	 * @return
	 */
	public static String getSystemVersion()
	{
		return android.os.Build.VERSION.RELEASE;
	}
	/**
	 * int תip
	 * @param i
	 * @return
	 */
	public static String intToIp(int i) {       
        
        return (i & 0xFF ) + "." +       
      ((i >> 8 ) & 0xFF) + "." +       
      ((i >> 16 ) & 0xFF) + "." +       
      ( i >> 24 & 0xFF) ;  
   }   
}
