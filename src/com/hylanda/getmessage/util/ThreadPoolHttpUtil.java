package com.hylanda.getmessage.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class ThreadPoolHttpUtil {

	 public static int POOL_SIZE = 3;  
     
	    private static ExecutorService sExecutorService;  
	      
	    private static int CONNECTE_TIME_OUT = 5 * 1000;  
	      
	    
	    static {  
	        sExecutorService = Executors.newFixedThreadPool(POOL_SIZE);  
	    }
	    public interface HttpCallback <T> {  
	        public  void onSuccess(T response);  
	        public  void onError(T error);  
	    }  
	    
	    public static void doPost(final String urlPath, final Map<String, String> map,final String packageName,final HttpCallback<String> callback)
	    {
	    	sExecutorService.submit(new Runnable() {
				
				@Override
				public void run() {
					JSONObject jsonObject = new JSONObject();
			    	try {
			    		if (map.containsKey("title")) {
			    			jsonObject.put("title", map.get("title"));
						}
						if (map.containsKey("text")) {
							jsonObject.put("text", map.get("text"));
						}
						if (map.containsKey("time")) {
							jsonObject.put("time", map.get("time"));
						}
						if (map.containsKey("ip")) {
							jsonObject.put("ip", map.get("ip"));
						}
				    	jsonObject.put("mediaName", packageName);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        byte[] json = jsonObject.toString().getBytes();
			        BufferedReader bufferedReader = null;
			        HttpURLConnection httpURLConnection = null;
			        StringBuffer result = new StringBuffer();
			        try {            
			            
			            URL url = new URL(urlPath);  
			             
			            httpURLConnection = (HttpURLConnection)url.openConnection();
			            httpURLConnection.setConnectTimeout(CONNECTE_TIME_OUT);     //�������ӳ�ʱʱ��
			            httpURLConnection.setDoInput(true);                  //�����������Ա�ӷ�������ȡ����
			            httpURLConnection.setDoOutput(true);                 //����������Ա���������ύ����
			            httpURLConnection.setRequestMethod("POST");     //������Post��ʽ�ύ����
			            httpURLConnection.setUseCaches(false);               //ʹ��Post��ʽ����ʹ�û���
			            //������������������ı�����
			            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			            //����������ĳ���
			            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(json.length));
			            //�����������������д������
			            OutputStream outputStream = httpURLConnection.getOutputStream();
			            outputStream.write(json);
			            
			            int response = httpURLConnection.getResponseCode();            //��÷���������Ӧ��
			            if(response == HttpURLConnection.HTTP_OK) {
			            	bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(),"UTF-8"));  
	                        String line = null;  
	                        while ((line = bufferedReader.readLine()) != null) {  
	                        	result.append(line);  
	                        } 
			                callback.onSuccess(result.toString());            
			            }
			        } catch (IOException e) {
			            callback.onError(e.getMessage());
			        }finally{
			        	httpURLConnection.disconnect();
			        	if (bufferedReader != null) {  
	                        try {  
	                            bufferedReader.close();  
	                        } catch (IOException e) {  
	                            e.printStackTrace();  
	                        }  
	                    }  
			        }
					
				}
			});
	    }
	    
	    public static void doPostForm(final String urlPath, final String json,final HttpCallback<String> callback)
	    {
	    	sExecutorService.submit(new Runnable() {
				
				@Override
				public void run() {
					
			       String _json = json;
			    	
			    	//String data = "sig=123"+"&data="+jsonObject.toString()+"&t="+new Date();
			    	
			    	 byte[] json = getRequestData(_json,"UTF-8").toString().getBytes();
			    	
			        URL postUrl = null;
			        HttpURLConnection connection = null;
			        
					try {
						postUrl = new URL(urlPath);
						connection = (HttpURLConnection) postUrl.openConnection();
						connection.setDoOutput(true);
				        // Read from the connection. Default is true.
				        connection.setDoInput(true);
				        connection.setRequestMethod("POST");
				        connection.setUseCaches(false);
				        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				        connection.setRequestProperty("Content-Length", String.valueOf(json.length));
				        connection.connect();
				        
				        DataOutputStream out = null;
				        out = new DataOutputStream(connection
						        .getOutputStream());
				        //String content = "sig=" + URLEncoder.encode("ABCDEFG", "utf-8");
				        out.write(json);
				        //out.writeBytes(content);
				        out.flush();
				        out.close();
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        String line;
			        
			        try {
						while ((line = reader.readLine()) != null){
						    System.out.println(line);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			      
			        try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        connection.disconnect();
			    	
			    	
			    	
				}
			});
	    }
	    
	    public static StringBuffer getRequestData(String json, String encode) {
	    	        StringBuffer stringBuffer = new StringBuffer();        //�洢��װ�õ���������Ϣ
	    	          try {
	    	        	  String sig = MD5Utils.getMd5("hylanda");
	    	        	  stringBuffer.append("sig").append("=").append(URLEncoder.encode(sig,"UTF-8")).
	    	        	  append("&data").append("=").append(URLEncoder.encode(json,"UTF-8")).
	    	        	  append("&t=").append(URLEncoder.encode(String.valueOf(System.currentTimeMillis()),"UTF-8"));
	    	          } catch (Exception e) {
	    	             e.printStackTrace();
	    	         }
	    	         return stringBuffer;
	    	     }
}
