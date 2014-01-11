package com.hiwhu.tool;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hiwhu.tool.ConfigurationUtil;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;

public class HttpHelper {
	

	Bitmap yzmImg = null;
	SQLiteDatabase db;
	String realNameUrl;
	String real_name = "";
	int num = 0;

	String tempRaw;//仅供测试使用
	
	public String getName(){
		return real_name;
	}
	
    public String getCookie(String url){
        String cookieVal = null;
        String sessionId = "";
    	String key=null;
    	try{
		URL imgUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
		conn.setDoInput(true);
		conn.connect();
		
        for  ( int  i =  1 ; (key = conn.getHeaderFieldKey(i)) !=  null ; i++ ) {
            if  (key.equalsIgnoreCase( "set-cookie" )) {  
                cookieVal = conn.getHeaderField(i);  
                //System.out.println("raw  "+cookieVal);
                cookieVal = cookieVal.substring(0 , cookieVal.indexOf( ";" ));  
                sessionId = sessionId+cookieVal+";" ;  
            }  
        }
        
        //System.out.println("done   "+sessionId);
        return sessionId;
    	} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    
	public Bitmap getYzmImg(String url, String sessionId){
		try {
			URL imgUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
			conn.setDoInput(true);
            conn.setRequestProperty("Cookie" , sessionId);  
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1)");
            conn.setUseCaches(false);
			conn.connect();
			InputStream in = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(in);
			yzmImg = BitmapFactory.decodeStream(bis);
			bis.close();
			in.close();
			
/*            for  ( int  i =  1 ; (key = conn.getHeaderFieldKey(i)) !=  null ; i++ ) {
                if  (key.equalsIgnoreCase( "set-cookie" )) {  
                    cookieVal = conn.getHeaderField(i);  
                    cookieVal = cookieVal.substring(0 , cookieVal.indexOf( ";" ));  
                    sessionId = sessionId+cookieVal+";" ;  
                }  
            }*/  
			
			return yzmImg;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
/*	public String getRealName(int maxLength, String charSet, String sessionId){
		String name = null;
		StringBuffer buffer = new StringBuffer();
		try {
			URL imgUrl = new URL(realNameUrl);
			HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
			conn.setDoInput(true);
            conn.setRequestProperty("Cookie" , sessionId);  
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setUseCaches(false);
			conn.connect();

		    // 读取内容
		    BufferedReader rd = new BufferedReader(new InputStreamReader(
		            conn.getInputStream(), charSet));
		    int ch;
		    for (int length = 0; (ch = rd.read()) > -1
		            && (maxLength <= 0 || length < maxLength); length++){
		        buffer.append((char) ch);
		    }

		    String s = buffer.toString();
		    
		    rd.close();
		    conn.disconnect();
            for  ( int  i =  1 ; (key = conn.getHeaderFieldKey(i)) !=  null ; i++ ) {
                if  (key.equalsIgnoreCase( "set-cookie" )) {  
                    cookieVal = conn.getHeaderField(i);  
                    cookieVal = cookieVal.substring(0 , cookieVal.indexOf( ";" ));  
                    sessionId = sessionId+cookieVal+";" ;  
                }  
            }  
			
			return name;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}*/
	
	
    // 这种方法是JAVA自带的URL来抓取网站内容
	
	String sno = null;
	String pwd = null;
	String yzm = null;
	String type = null;
	String keyword = null;
	
	public void setSearch(String type, String keyword){
		this.type = type;
		this.keyword = keyword;
	}
	
	public void setInformation(String sno, String pwd){
		this.sno = sno;
		this.pwd = pwd;
		this.yzm = yzm;
	}
	
	public String getContent(String url, String charSet) {
		StringBuffer buffer = new StringBuffer();
		try {
			HttpURLConnection hConnect;
			URL newUrl = new URL(url);

			hConnect = (HttpURLConnection) newUrl.openConnection();
			hConnect.setDoInput(true);
			// HttpURLConnection.setFollowRedirects(true);
			// hConnect.setInstanceFollowRedirects(true);
			hConnect.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			hConnect.setUseCaches(false);
			hConnect.connect();

			// 读取内容
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					hConnect.getInputStream(), charSet));
			int ch;
			for (; (ch = rd.read()) > -1;) {
				buffer.append((char) ch);
			}

			return buffer.toString();
			// System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void getStuIndex(String url, String strPostRequest, int maxLength, String charSet, String sessionId, Context mContext, Handler handler, SharedPreferences userData){
		StringBuffer buffer = new StringBuffer();
    	try{
        HttpURLConnection hConnect;
        URL newUrl = new URL(url);
    		
        hConnect = (HttpURLConnection) newUrl
                .openConnection();
        hConnect.setDoInput(true);
           // HttpURLConnection.setFollowRedirects(true);
           // hConnect.setInstanceFollowRedirects(true);
         hConnect.setRequestProperty("Cookie" , sessionId);  
         hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
         hConnect.setUseCaches(false);
         hConnect.connect();
        
        
    // 读取内容
    BufferedReader rd = new BufferedReader(new InputStreamReader(
            hConnect.getInputStream(), charSet));
    int ch;
    for (int length = 0; (ch = rd.read()) > -1
            && (maxLength <= 0 || length < maxLength); length++){
        buffer.append((char) ch);
    }
    
    String s = buffer.toString();
    //System.out.println(s);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	
	public void getSearchLesson(String strUrl, String strPostRequest, int maxLength, String charSet, String sessionId, Context mContext, Handler handler, SharedPreferences userData){
        // 读取结果网页
        StringBuffer buffer = new StringBuffer();

        try {
            URL newUrl = new URL(strUrl);
            HttpURLConnection hConnect = (HttpURLConnection) newUrl
                    .openConnection();
            // POST方式的额外数据
                hConnect.setDoOutput(true);
                hConnect.setDoInput(true);
                hConnect.setRequestMethod("POST");
               // HttpURLConnection.setFollowRedirects(true);
               // hConnect.setInstanceFollowRedirects(true);
                hConnect.setRequestProperty("Cookie" , sessionId);  
                hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hConnect.setUseCaches(false);
                hConnect.connect();
                //OutputStreamWriter out = new OutputStreamWriter(hConnect.getOutputStream(), charSet);
                DataOutputStream out = new DataOutputStream(hConnect.getOutputStream());  //声明数据写入流
                
                //String content = URLEncoder.encode("who=student&id=" + sno + "&pwd=" + pwd + "&yzm=" + yzm + "&submit=%C8%B7+%B6%A8", "gb2312");
                String content = "keyword=" + URLEncoder.encode(keyword, "gb2312") + "&ttype=" + URLEncoder.encode(type, "gb2312") + "&submit=%BC%EC+%CB%F7";
                out.writeBytes(content);
                //out.write("who=student&id=" + sno + "&pwd=" + pwd + "&yzm=" + yzm + "&submit=%C8%B7+%B6%A8");
                out.flush();
                out.close();
                //newUrl = new URL("http://202.114.74.198/stu/query_score.jsp");
                //newUrl = new URL(hConnect.getHeaderField("Location"));
                // 读取内容
                /*BufferedReader rd = new BufferedReader(new InputStreamReader(
                        hConnect.getInputStream(), charSet));
                int ch;
                for (int length = 0; (ch = rd.read()) > -1
                        && (maxLength <= 0 || length < maxLength); length++){
                    buffer.append((char) ch);
                }

                String s = buffer.toString();
                s = s.replaceAll("a href=\"choose_pubLesson_list.jsp.actionType=choose&pageNum=", "");
                s = s.replaceAll(" class=\"navegate\"", "");
                Pattern p = Pattern.compile("[0-9]{1,2}\">下一页");
                Matcher m = p.matcher(s);
                if(m.find()){
                	String id = m.group().split("\"")[0];
                	getSearchLessonChild(id, 500200, "gb2312", sessionId, mContext);
                }
                //System.out.println(s);
                hConnect.disconnect();
                getSearchLessonPart1(s, mContext);*/
                getSearchLessonChild("1", 500200, "gb2312", sessionId, mContext, handler, userData);
                hConnect.disconnect();
                
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	public void getSearchLessonChild(String id, int maxLength, String charSet, String sessionId, Context mContext, Handler handler, SharedPreferences userData){
		StringBuffer buffer = new StringBuffer();
		try {
            URL newUrl = new URL(ConfigurationUtil.URL_SEARCHLESSON+"&pageNum="+id);
            HttpURLConnection hConnect = (HttpURLConnection) newUrl
                    .openConnection();
            // POST方式的额外数据
                hConnect.setDoInput(true);
               // HttpURLConnection.setFollowRedirects(true);
               // hConnect.setInstanceFollowRedirects(true);
                hConnect.setRequestProperty("Cookie" , sessionId);  
                hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hConnect.setUseCaches(false);
                hConnect.connect();
                // 读取内容
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        hConnect.getInputStream(), charSet));
                int ch;
                for (int length = 0; (ch = rd.read()) > -1
                        && (maxLength <= 0 || length < maxLength); length++){
                    buffer.append((char) ch);
                }
                
                if(id.equals("1")){
                //判断是否进行更新
                String ss = buffer.toString();
                Pattern p = Pattern.compile("当前所选的课是.*学期");
                Matcher m = p.matcher(ss);
                if(m.find()){
                	//System.out.println(m.group());
                	//String lesson = userData.getString(ConfigurationUtil.USER_LESSON, "");
                	ss = m.group();
                	//System.out.println(ss);
                	//if(s.equals(lesson)) return;
                	//else userData.edit().putString(ConfigurationUtil.USER_LESSON, s).commit();
                }
                
                p = Pattern.compile("共有[0-9]*条记录");
                m = p.matcher(buffer.toString());
                if(m.find()){
                	//System.out.println(m.group());
                	String lesson = userData.getString(ConfigurationUtil.USER_PUBCLASS, "");
                	//System.out.println(lesson);
                	ss += m.group();
                	//System.out.println(ss);
                	String temp = buffer.toString();
                	int first = temp.indexOf("<TR align=\"center\" class=\"TR_BODY\">");
                	temp = temp.substring(first+20);
                	first = temp.indexOf("<TR align=\"center\" class=\"TR_BODY\">");
                	temp = temp.substring(first+20);
                	first = temp.indexOf("<TR align=\"center\" class=\"TR_BODY\">");
                	temp = temp.substring(first, first+400);//第一个条目不可选
                	//System.out.println(temp);
                	if(temp.contains("不可选"))ConfigurationUtil.ifchoose = false;
                	else if(temp.contains("选课")){ConfigurationUtil.ifchoose = true;System.out.println("选课");}
                	
    				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    				Date date = new Date();
    				String pubclassdate = userData.getString(ConfigurationUtil.PUBCLASS_DATE, "");
                	if(ss.equals(lesson) && temp.contains("不可选")) return;
                	else if(ss.equals(lesson) && format.format(date).equals(pubclassdate)) return;
                	else {
                    	MySqliteHelper my = new MySqliteHelper(mContext, "mydb", null, 1);
                    	db = my.getWritableDatabase();
                    	db.execSQL("DROP TABLE IF EXISTS "+MySqliteHelper.TB_SEARCHLESSON);
                    	my.onCreate(db);
                    	db.close();
                		userData.edit().putString(ConfigurationUtil.USER_PUBCLASS, ss).commit();
                	}
                }
                }
                Message msg = new Message();
                msg.what = 7;
                msg.arg1 = Integer.parseInt(id);
                msg.arg2 = 2;
                handler.sendMessage(msg);
                
                String s = buffer.toString();
                s = s.replaceAll("a href=\"choose_pubLesson_list.jsp.actionType=choose&pageNum=", "");
                s = s.replaceAll(" class=\"navegate\"", "");
                Pattern p = Pattern.compile("[0-9]{1,2}\">下一页");
                Matcher m = p.matcher(s);
                if(m.find()){
                	String ID = m.group().split("\"")[0];
                	System.out.println(ID);
                	getSearchLessonChild(ID, 500200, "gb2312", sessionId, mContext, handler, userData);
                }
                //System.out.println(s);
                Message msg1 = new Message();
                msg1.what = 7;
                msg1.arg1 = 0;
                msg1.arg2 = 2;
                handler.sendMessage(msg1);
                hConnect.disconnect();
                getSearchLessonPart1(s, mContext);
                
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
    public int getPageContent(String strUrl, String strPostRequest, int maxLength, String charSet, String sessionId) {
        // 读取结果网页
/*        System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
        System.setProperty("sun.net.client.defaultReadTimeout", "5000");*/
        try {
            URL newUrl = new URL(strUrl);
            HttpURLConnection hConnect = (HttpURLConnection) newUrl
                    .openConnection();
            // POST方式的额外数据
                hConnect.setDoOutput(true);
                hConnect.setDoInput(true);
                hConnect.setRequestMethod("POST");
               // HttpURLConnection.setFollowRedirects(true);
               // hConnect.setInstanceFollowRedirects(true);
                hConnect.setRequestProperty("Cookie" , sessionId);  
                hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hConnect.setUseCaches(false);
                hConnect.connect();

                String key = null;
                String cookieVal = "";
                
                
                //OutputStreamWriter out = new OutputStreamWriter(hConnect.getOutputStream(), charSet);
                DataOutputStream out = new DataOutputStream(hConnect.getOutputStream());  //声明数据写入流
                
                //String content = URLEncoder.encode("who=student&id=" + sno + "&pwd=" + pwd + "&yzm=" + yzm + "&submit=%C8%B7+%B6%A8", "gb2312");
                String content = "Login.Token1=" + URLEncoder.encode(sno, "gb2312") + "&Login.Token2=" + URLEncoder.encode(pwd, "gb2312") + "&goto=http%3A%2F%2Fmy.whu.edu.cn%2FloginSuccess.portal&gotoOnFail=http%3A%2F%2Fmy.whu.edu.cn%2FloginFailure.portal";
                
                //Login.Token1=2009302580142&Login.Token2=180514&goto=http%3A%2F%2Fmy.whu.edu.cn%2FloginSuccess.portal&gotoOnFail=http%3A%2F%2Fmy.whu.edu.cn%2FloginFailure.portal
                
                out.writeBytes(content);
                //out.write("who=student&id=" + sno + "&pwd=" + pwd + "&yzm=" + yzm + "&submit=%C8%B7+%B6%A8");
                out.flush();
                out.close();
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        hConnect.getInputStream(), "UTF-8"));
                
                StringBuffer buffer = new StringBuffer();
                int ch;
                for (int length = 0; (ch = rd.read()) > -1
                        && (maxLength <= 0 || length < maxLength); length++){
                    buffer.append((char) ch);
                }
                System.out.println(buffer.toString());

                
                for  ( int  i =  1 ; (key = hConnect.getHeaderFieldKey(i)) !=  null ; i++ ) {
                    if  (key.equalsIgnoreCase( "set-cookie" )) {  
                        cookieVal = hConnect.getHeaderField(i);  
                        cookieVal = cookieVal.substring(0 , cookieVal.indexOf( ";" ));  
                        sessionId = sessionId+cookieVal+";" ;  
                    }  
                }
                
                int num = sessionId.indexOf(";");
                sessionId = sessionId.substring(num+1);
                ConfigurationUtil.cookie = sessionId;
                //newUrl = new URL("http://202.114.74.198/stu/query_score.jsp");
                hConnect.disconnect();
                //String temp = newUrl.toString();
                
/*                if(temp.equals(ConfigurationUtil.URL_ERRPWD)){//用户名或密码错误
                	return 3;
                }else if(temp.equals(ConfigurationUtil.URL_ERRYZM)){//验证码错误
                	return 4;
                }else if(temp.equals(ConfigurationUtil.URL_STUINDEX)){//2010届以前学生登录成功界面
                	realNameUrl = temp;
                	return 1;
                }else if(temp.equals(ConfigurationUtil.URL_STUNEWINDEX)){//2010届以后的
                	realNameUrl = temp;
                	return 5;
                }else*/
                if(buffer.toString().contains("Successed")) return 1;//登陆成功
                else if(buffer.toString().contains("Failure")) return 2;//登陆失败，用户名密码错误
                else return 3;//网络错误

        } catch (Exception e) {
            // return "错误:读取网页失败！";
            //
        	return 3;//网络错误
             
        }
    }
    
    public void cancelLesson(String strUrl, String strPostRequest, int maxLength, String charSet, String sessionId, Context mContext){
        try {
            URL newUrl = new URL(strUrl);
            HttpURLConnection hConnect = (HttpURLConnection) newUrl
                    .openConnection();

                hConnect.setDoInput(true);

                hConnect.setRequestProperty("Cookie" , sessionId);  
                hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hConnect.setUseCaches(false);
                hConnect.connect();
                
                StringBuffer buffer = new StringBuffer();
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        hConnect.getInputStream(), charSet));
                int ch;
                for (int length = 0; (ch = rd.read()) > -1
                        && (maxLength <= 0 || length < maxLength); length++){
                    buffer.append((char) ch);
                }
                //newUrl = new URL(hConnect.getHeaderField("Location"));
                newUrl = new URL(hConnect.getHeaderField("Location"));
                hConnect.disconnect();
                //String temp = newUrl.toString();

        } catch (Exception e) {
             e.printStackTrace();
        }
    }
    
    public void uploadLesson(String strUrl, String strPostRequest, int maxLength, String charSet, String sessionId, Context mContext){
        try {
        	
        	System.out.println(strUrl);
            URL newUrl = new URL(strUrl);
            HttpURLConnection hConnect = (HttpURLConnection) newUrl
                    .openConnection();
            // POST方式的额外数据
                hConnect.setDoOutput(true);
                hConnect.setDoInput(true);
                hConnect.setRequestMethod("POST");
               // HttpURLConnection.setFollowRedirects(true);
               // hConnect.setInstanceFollowRedirects(true);
                hConnect.setRequestProperty("Cookie" , sessionId);  
                hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hConnect.setUseCaches(false);
                hConnect.connect();
                
                //OutputStreamWriter out = new OutputStreamWriter(hConnect.getOutputStream(), charSet);
                DataOutputStream out = new DataOutputStream(hConnect.getOutputStream());  //声明数据写入流
                
                //String content = URLEncoder.encode("who=student&id=" + sno + "&pwd=" + pwd + "&yzm=" + yzm + "&submit=%C8%B7+%B6%A8", "gb2312");
                String content = "";
                MySqliteHelper my;
                Cursor c = null;
                HashMap<String, Object> map = null;
                String classnum = "";
                for(int i = 0; i < ConfigurationUtil.chooseList.size(); i++){
                	//查找课头号
		        	my = new MySqliteHelper(mContext, "mydb", null, 1);		
		        	db = my.getReadableDatabase();
		        	//查询字符串时务必加入单引号
		        	map = ConfigurationUtil.chooseList.get(i);
		        	
		    		c=db.query(MySqliteHelper.TB_SEARCHLESSON, null, MySqliteHelper.LESSON_NAME+"="+"'"+(String)map.get("LessonName")+"'"
		    																		+" and "+MySqliteHelper.LESSON_WEEK+"="+"'"+(String)map.get("LessonWeek")+"'"
		    																		+" and "+MySqliteHelper.LESSON_TEACHER+"="+"'"+(String)map.get("LessonTeacher")+"'"
		    																		+" and "+MySqliteHelper.LESSON_LASTING+"="+"'"+(String)map.get("LessonLast")+"'", 
		    																		null, null, null, null);
		    		if(c != null){
		    			c.moveToFirst();
		    			classnum = c.getString(c.getColumnIndexOrThrow(MySqliteHelper.LESSON_NUM));
		    			if(i == 0)
		    				content += "apply="+URLEncoder.encode(classnum, "gb2312");
		    			else
		    				content += "&apply="+URLEncoder.encode(classnum, "gb2312");
		    		}
                }
                System.out.println(content);
                c.close();
                db.close();
                out.writeBytes(content);
                //out.write("who=student&id=" + sno + "&pwd=" + pwd + "&yzm=" + yzm + "&submit=%C8%B7+%B6%A8");
                out.flush();
                out.close();
                
                //newUrl = new URL("http://202.114.74.198/stu/query_score.jsp");
                newUrl = new URL(hConnect.getHeaderField("Location"));
                hConnect.disconnect();
        	
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
    
    //测试使用
/*	private void tempRaw(String temp) {
		// TODO Auto-generated method stub
		this.tempRaw = temp;
		System.out.println(temp);
	}*/
    
    public void getLessonStart(String url, String sessionId, String charSet, int maxLength, Context mContext, SharedPreferences userData, int choice){
    	StringBuffer buffer = new StringBuffer();
    	try{
        HttpURLConnection hConnect;
        URL newUrl = new URL(url);
    		
        hConnect = (HttpURLConnection) newUrl
                .openConnection();
        hConnect.setDoInput(true);
           // HttpURLConnection.setFollowRedirects(true);
           // hConnect.setInstanceFollowRedirects(true);
         hConnect.setRequestProperty("Cookie" , sessionId);  
         hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
         hConnect.setUseCaches(false);
         hConnect.connect();
        
        
    // 读取内容
    BufferedReader rd = new BufferedReader(new InputStreamReader(
            hConnect.getInputStream(), charSet));
    int ch;
    for (int length = 0; (ch = rd.read()) > -1
            && (maxLength <= 0 || length < maxLength); length++){
        buffer.append((char) ch);
    }

    String s = buffer.toString();
    //System.out.println(s);
    
    
    Pattern p = Pattern.compile("姓名:[^&]{1,10}");
    Matcher m = p.matcher(s);
    if(m.find()){
    	real_name = m.group().split(":")[1];
    }
    //判断是否进行更新
    p = Pattern.compile("当前是.*学期");
    m = p.matcher(s);
    if(m.find()){
    	//System.out.println(m.group());
    	//String lesson = userData.getString(ConfigurationUtil.USER_LESSON, "");
    	s = m.group();
    	//if(s.equals(lesson)) return;
    	//else userData.edit().putString(ConfigurationUtil.USER_LESSON, s).commit();
    }
    
    p = Pattern.compile("共有[0-9]*条记录");
    m = p.matcher(buffer.toString());
    if(m.find()){
    	//System.out.println(m.group());
    	String lesson = userData.getString(ConfigurationUtil.USER_LESSON, "");
    	s += m.group();
    	String temp = buffer.toString();
    	int first = temp.indexOf("<tr align=\"center\" class=\"TR_BODY\" >");
    	temp = temp.substring(first, first+400);//第一个条目不可选
    	//System.out.println(temp);
    	if(temp.contains("无"))ConfigurationUtil.ifdelete = false;
    	else if(temp.contains("撤销")){ConfigurationUtil.ifdelete = true;System.out.println("撤销");}
    	if(s.equals(lesson) && temp.contains("无")) return;
    	else {
        	MySqliteHelper my = new MySqliteHelper(mContext, "mydb", null, 1);
        	db = my.getWritableDatabase();
        	db.execSQL("DROP TABLE IF EXISTS "+MySqliteHelper.TB_LESSON);
        	my.onCreate(db);
        	db.close();
    		userData.edit().putString(ConfigurationUtil.USER_LESSON, s).commit();
    	}
    }
    //s.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
    //System.out.println("in");
    rd.close();
    hConnect.disconnect();
    this.getLessonPart1(buffer.toString(), mContext);
} catch (Exception e) {
    // return "错误:读取网页失败！";
	e.printStackTrace();
}
    }
    
	private void getLessonPart1(String raw, Context mContext) {
		// TODO Auto-generated method stub
		raw = raw.replaceAll("<tr align=\"center\" class=\"TR_BODY\" >", "@@@");


		raw = raw.replaceAll("</tr>", "");
		StringTokenizer stringTokenizer = new StringTokenizer(raw, "@@@");
		
		int i = 1;//计数器
		while(stringTokenizer.hasMoreTokens()){
			if(i == 1){//第一个跳过
				stringTokenizer.nextToken();
				i++;
			}else{
				String temp = stringTokenizer.nextToken();
				temp = temp.replaceAll("<td([^/])*>", "</td>");
				temp = temp.replace(" ", "");//去掉空格
				temp = temp.replace("\n", "");//去掉换行，制表符
				temp = temp.replace("\t", "");//去掉换行，制表符
				temp = temp.replace("</font>", "");
				temp = temp.replace("</a>", "");
				temp = temp.replace("<fontcolor=#0000ff>", "");
				temp = temp.replaceAll("</td>", "<>");
				temp = temp.replace(">撤销", "@撤销");
				temp = temp.replace("<ahref", "@ahref");
				temp = temp.replaceAll("@.*@", "");
				//temp = temp.replace("<ahref=\"javascript:doDel('../servlet/DelChoosed?lessonNum=", "");
				System.out.println(temp);
				StringTokenizer st = new StringTokenizer(temp, "<>");
				getLessonPart2(st, mContext);
				i++;
			}
		}
	}

	private void getLessonPart2(StringTokenizer st, Context mContext) {
		// TODO Auto-generated method stub
    	MySqliteHelper my = new MySqliteHelper(mContext, "mydb", null, 1);
    	db = my.getWritableDatabase();
    	ContentValues values=new ContentValues();
    	String[] tempSaver = new String[9];
    	String[] tempThree = new String[3];//lasting, time, location
    	String operation = "";
    	for(int kk = 0; kk < tempSaver.length; kk++)
    		tempSaver[kk] = "";
    	for(int kk = 0; kk < tempThree.length; kk++)
    		tempThree[kk] = "";
		int i = 1;//计数器
		while(st.hasMoreTokens()){
			String temp = st.nextToken();
				switch(i){
				case 6:
					operation = temp;//lesson_state
					//System.out.println(temp);
				case 8:
					tempSaver[0] = temp;//lesson_name
					break;
				case 12:
					tempSaver[1] = temp;//lesson_type
					//System.out.println(temp);
					break;
				case 16:
					tempSaver[2] = temp;//lesson_college
					break;
				case 18:
					tempSaver[3] = temp;//lesson_teacher
					break;
				case 24:
					tempSaver[4] = temp;//lesson_point
					break;
				case 26:
					tempSaver[5] = temp;//lesson_learntime
					break;
				case 28://星期一
					if(!temp.equals("&nbsp;") && temp.length()>4){
/*						System.out.println(temp);
						System.out.println(temp.length());*/
						tempSaver[6] += "1@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 30:
					if(!temp.equals("&nbsp;")&& temp.length()>4){
						tempSaver[6] += "2@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 32:
					if(!temp.equals("&nbsp;")&& temp.length()>4){
						tempSaver[6] += "3@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 34:
					if(!temp.equals("&nbsp;")&& temp.length()>4){
						tempSaver[6] += "4@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 36:
					if(!temp.equals("&nbsp;")&& temp.length()>4){
						tempSaver[6] += "5@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 38:
					if(!temp.equals("&nbsp;")&& temp.length()>4){
						tempSaver[6] += "6@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 40:
					if(!temp.equals("&nbsp;")&& temp.length()>4){
						tempSaver[6] += "7@";
						tempSaver[7] = temp;
						//拆解信息
						String[] detail = tempSaver[7].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";
					}
					break;
				case 42:
					if(!temp.equals("&nbsp;")){
						tempSaver[8] = temp;
					}
					break;
				}
				i++;
		}
		
		values.put(MySqliteHelper.LESSON_LASTING, tempThree[0]);
		values.put(MySqliteHelper.LESSON_TIME, tempThree[1]);
		values.put(MySqliteHelper.LESSON_LOCATION, tempThree[2].replace("（", "(").replace("）", ")"));
        values.put(MySqliteHelper.LESSON_NAME, tempSaver[0].replace("（", "(").replace("）", ")"));
        values.put(MySqliteHelper.LESSON_TYPE, tempSaver[1]);
        values.put(MySqliteHelper.LESSON_COLLEGE, tempSaver[2]);
        values.put(MySqliteHelper.LESSON_TEACHER, tempSaver[3]);
        values.put(MySqliteHelper.LESSON_POINT, tempSaver[4]);
        values.put(MySqliteHelper.LESSON_LEARNTIME, tempSaver[5]);
        values.put(MySqliteHelper.LESSON_WEEK, tempSaver[6]);
        if(tempSaver[8].equals(""))
        	tempSaver[8] = "无";
        values.put(MySqliteHelper.LESSON_OTHER, tempSaver[8]);
        values.put(MySqliteHelper.LESSON_STATE, operation);
        db.insert(MySqliteHelper.TB_LESSON, null, values);
        db.close();
	}
    
    public void getScoreStart(String url, String sessionId, String charSet, int maxLength, Context mContext, SharedPreferences userData, int choice){

    	StringBuffer buffer = new StringBuffer();
    	try{
        HttpURLConnection hConnect;
        URL newUrl = new URL(url);
    		
        hConnect = (HttpURLConnection) newUrl
                .openConnection();
        hConnect.setDoInput(true);
           // HttpURLConnection.setFollowRedirects(true);
           // hConnect.setInstanceFollowRedirects(true);
         hConnect.setRequestProperty("Cookie" , sessionId);  
         hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
         hConnect.setUseCaches(false);
         hConnect.connect();
        
        
    // 读取内容
    BufferedReader rd = new BufferedReader(new InputStreamReader(
            hConnect.getInputStream(), charSet));
    int ch;
    for (int length = 0; (ch = rd.read()) > -1
            && (maxLength <= 0 || length < maxLength); length++){
        buffer.append((char) ch);
    }

    String s = buffer.toString();
    //System.out.println(s);
    Pattern p = Pattern.compile("共有[0-9]*条记录");
    Matcher m = p.matcher(s);
    if(m.find()){
    	//System.out.println(m.group());
    	String score = userData.getString(ConfigurationUtil.USER_SCORE, "");
    	s = m.group();
    	if(s.equals(score) && choice == 0) return;
    	else {
        	MySqliteHelper my = new MySqliteHelper(mContext, "mydb", null, 1);
        	db = my.getWritableDatabase();
        	db.execSQL("DROP TABLE IF EXISTS "+MySqliteHelper.TB_SCORE);
        	my.onCreate(db);
        	db.close();
    		userData.edit().putString(ConfigurationUtil.USER_SCORE, s).commit();
    	}
    }
    //s.replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll("<[^>]*>", "");
    //System.out.println("ininin");
    rd.close();
    hConnect.disconnect();
    getScorePart1(buffer.toString().trim(), mContext);
} catch (Exception e) {
    // return "错误:读取网页失败！";
	e.printStackTrace();
}
    }

	private void getScorePart1(String raw, Context mContext) {
		// TODO Auto-generated method stub
		raw = raw.replaceAll("<tr align=\"center\" class=\"TR_BODY\"([^>])+>", "@@@");
		raw = raw.replaceAll("</tr>", "");
		StringTokenizer stringTokenizer = new StringTokenizer(raw, "@@@");
		int i = 1;//计数器
		while(stringTokenizer.hasMoreTokens()){
			if(i == 1){//第一个跳过
				stringTokenizer.nextToken();
				i++;
			}else{
				String temp = stringTokenizer.nextToken();
				temp = temp.replaceAll("<td([^/])*>", "</td>");
				temp = temp.replace(" ", "");//去掉空格
				temp = temp.replace("\n", "");//去掉换行，制表符
				temp = temp.replace("\t", "");//去掉换行，制表符
				//System.out.println(temp);
				StringTokenizer st = new StringTokenizer(temp, "</td>");
				getScorePart2(st, mContext);
				i++;
			}
		}
	}

	private void getScorePart2(StringTokenizer st, Context mContext) {
		// TODO Auto-generated method stub
    	MySqliteHelper my = new MySqliteHelper(mContext, "mydb", null, 1);
    	db = my.getWritableDatabase();
    	ContentValues values=new ContentValues();
		int i = 1;//计数器
		String[] tempSaver = new String[9];
    	for(int kk = 0; kk < tempSaver.length; kk++)
    		tempSaver[kk] = "";
		while(st.hasMoreTokens()){
			String temp = st.nextToken();
				switch(i){
/*				case 1:
					this.ID.add(temp);
					break;
				case 2:
					this.name.add(temp);
					break;
				case 3:
					this.classID.add(temp);
					break;*/
				case 8:
					tempSaver[0] = temp;//lesson_name
					break;
				case 10:
					tempSaver[1] = temp;//lesson_teacher
					break;
				case 12:
					tempSaver[2] = temp;//lesson_college
					break;
				case 14:
					tempSaver[3] = temp;//lesson_year
					break;
				case 16:
					tempSaver[4] = temp;//lesson_term
					break;
				case 18:
					tempSaver[5] = temp;//lesson_point
					break;
				case 20:
					tempSaver[6] = temp;//lesson_type
					break;
				case 22:
					tempSaver[7] = temp;//learn_type
					break;
				case 24:
					tempSaver[8] = temp;//lesson_score
					break;
				}
				i++;
		}
        values.put(MySqliteHelper.LESSON_NAME, tempSaver[0]);
        values.put(MySqliteHelper.LESSON_TEACHER, tempSaver[1]);
        values.put(MySqliteHelper.LESSON_COLLEGE, tempSaver[2]);
        values.put(MySqliteHelper.LESSON_YEAR, tempSaver[3]);
        values.put(MySqliteHelper.LESSON_TERM, tempSaver[4]);
        values.put(MySqliteHelper.LESSON_POINT, tempSaver[5]);
        values.put(MySqliteHelper.LESSON_TYPE, tempSaver[6]);
        values.put(MySqliteHelper.LEARN_TYPE, tempSaver[7]);
        values.put(MySqliteHelper.LESSON_SCORE, tempSaver[8]);
        db.insert(MySqliteHelper.TB_SCORE, null, values);
        db.close();
	}
	
	private void getSearchLessonPart1(String raw, Context mContext) {
		// TODO Auto-generated method stub
		raw = raw.toLowerCase();
		raw = raw.replaceAll("<tr align=\"center\" class=\"tr_body\">", "@@@");


		raw = raw.replaceAll("</tr>", "");
		StringTokenizer stringTokenizer = new StringTokenizer(raw, "@@@");
		int i = 1;//计数器
		while(stringTokenizer.hasMoreTokens()){
			if(i < 4){//第一、二、三个跳过
				stringTokenizer.nextToken();
				i++;
			}else{
				String temp = stringTokenizer.nextToken();
				String num = "";
				//System.out.println(temp);
				Pattern p = Pattern.compile("a href=\".*\"><");
				Matcher m = p.matcher(temp);
				if(m.find()){
					String temp1 = m.group();
					//System.out.println(temp1);
					num = temp1.split("'")[2];
					//System.out.println(num);
					num = num.substring(0, num.length()-1);
				}
				temp = temp.replaceAll("<td([^/])*>", "</td>");
				temp = temp.replace(" ", "");//去掉空格
				temp = temp.replace("\n", "");//去掉换行，制表符
				temp = temp.replace("\t", "");//去掉换行，制表符
				temp = temp.replace("</font>", "");
				temp = temp.replace("</span>", "");
				temp = temp.replace("</a>", "");
				temp = temp.replace("</td>", "<>");
				//System.out.println(temp);
				StringTokenizer st = new StringTokenizer(temp, "<>");
				getSearchLessonPart2(st, mContext, num);
				i++;
			}
		}
	}

	private void getSearchLessonPart2(StringTokenizer st, Context mContext, String classnum) {
		// TODO Auto-generated method stub
    	MySqliteHelper my = new MySqliteHelper(mContext, "mydb", null, 1);
    	db = my.getWritableDatabase();
    	ContentValues values=new ContentValues();
    	String[] tempSaver = new String[10];
    	String[] tempThree = new String[3];//lasting, time, location
    	for(int kk = 0; kk < tempSaver.length; kk++)
    		tempSaver[kk] = "";
    	for(int kk = 0; kk < tempThree.length; kk++)
    		tempThree[kk] = "";
		int i = 1;//计数器
		while(st.hasMoreTokens()){
			String temp = st.nextToken();
				switch(i){
				case 4:
					tempSaver[0] = temp;//lesson_name
					break;
				case 6:
					tempSaver[2] = temp;//lesson_point
					break;
				case 8:
					tempSaver[3] = temp;//lesson_remainnum
					break;
				case 10:
					tempSaver[4] = temp;//lesson_teacher
					break;
				case 12://星期一
					if(!temp.equals("&nbsp;")){
						//System.out.println(temp);
						StringTokenizer token = new StringTokenizer(temp, ";");
						String tt = "";
						while(token.hasMoreTokens()){
							tt = token.nextToken();
							if(tt.contains(":")){
								//System.out.println(tt+"==");
								tempSaver[7] +=tt.split(":")[0].substring(tt.indexOf("周"))+"@";//week
								tempThree[0] +=tt.split(":")[1]+"@";//lasting
							}else{
								if(!tt.contains("节"))continue;
								//System.out.println(tt+"asdf"+tt.length());
								tempThree[1] +=tt.split(",")[0]+"@";//time
								tempThree[2] +=tt.substring(tt.indexOf(",")+1)+"@";//location
							}
						}
/*						tempSaver[7] += "1@";
						tempSaver[8] = temp;
						//拆解信息
						String[] detail = tempSaver[8].split(";");
						tempThree[0] += detail[0]+"@";
						String[] other = detail[1].split(",");
						tempThree[1] += other[0]+"@";

						if(other.length ==3)
							tempThree[2] += other[1]+other[2]+"@";
						else
							tempThree[2] += "不定@";*/
					}
					break;
				case 14:
					if(!temp.equals("&nbsp;")){
						//System.out.println(temp);
						tempSaver[9] = temp;
					}
					break;
				}
				i++;
		}
		
		values.put(MySqliteHelper.LESSON_NUM, classnum);
		values.put(MySqliteHelper.LESSON_LASTING, tempThree[0]);
		values.put(MySqliteHelper.LESSON_TIME, tempThree[1]);
		values.put(MySqliteHelper.LESSON_LOCATION, tempThree[2]);
        values.put(MySqliteHelper.LESSON_NAME, tempSaver[0]);
        values.put(MySqliteHelper.LESSON_TEACHER, tempSaver[4]);
        values.put(MySqliteHelper.LESSON_POINT, tempSaver[2]);
        values.put(MySqliteHelper.LESSON_WEEK, tempSaver[7]);
        values.put(MySqliteHelper.LESSON_REMAINNUM, tempSaver[3]);
        if(tempSaver[9].equals(""))
        	tempSaver[9] = "无";
        values.put(MySqliteHelper.LESSON_OTHER, tempSaver[9]);
        values.put(MySqliteHelper.LESSON_ID, num++);
        db.insert(MySqliteHelper.TB_SEARCHLESSON, null, values);
        db.close();
	}
	
	public ArrayList<String[]> getLecture(String url, String charSet) {

		StringBuffer buffer = new StringBuffer();
		ArrayList<String[]> result = new ArrayList<String[]>();
		try {
			HttpURLConnection hConnect;
			URL newUrl = new URL(url);

			hConnect = (HttpURLConnection) newUrl.openConnection();
			hConnect.setDoInput(true);
			// HttpURLConnection.setFollowRedirects(true);
			// hConnect.setInstanceFollowRedirects(true);
			// hConnect.setRequestProperty("Cookie" , sessionId);
			hConnect.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			hConnect.setUseCaches(false);
			hConnect.connect();

			// 读取内容
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					hConnect.getInputStream(), charSet));
			int ch;
			for (; (ch = rd.read()) > -1;) {
				buffer.append((char) ch);
			}

			String s = buffer.toString();
			if (!s.contains("详情"))
				return null;
			// System.out.println(s);
			Pattern p = Pattern
					.compile("<a href=\"/Lecture/lecture/[0-9]*/\" title=\"\" target=\"_blank\">([^<])+");
			Matcher m = p.matcher(s);
			String[] item = new String[6];
			String temp = "";
			int times = 0;
			while (m.find()) {
				times++;
				temp = m.group();
				// System.out.println(item);

				if (times % 2 == 0) {
					item[5] = ConfigurationUtil.URL_LECTURE
							+ temp.split("\"")[1];
					temp = temp.split(">")[1].replaceAll("&nbsp;", "@")
							.replaceAll("&quot;", "\"")
							.replaceAll("&#8226;", "·");
					item[1] = temp.split("@+")[0] + " " + temp.split("@+")[1];
					item[2] = "类型/讲座";
					item[3] = "地点：" + temp.split("@+")[2];

					System.out.println(item);
					result.add(item);
					item = new String[6];
				} else
					item[0] = temp.split(">")[1].replaceAll("&nbsp;", " ")
							.replaceAll("&quot;", "\"")
							.replaceAll("&#8226;", "·");
			}

			rd.close();
			hConnect.disconnect();
			return result;
		} catch (Exception e) {
			// return "错误:读取网页失败！";
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<String[]> getNews(String url, String charSet) {

		StringBuffer buffer = new StringBuffer();
		ArrayList<String[]> result = new ArrayList<String[]>();
		try {
			HttpURLConnection hConnect;
			URL newUrl = new URL(url);

			hConnect = (HttpURLConnection) newUrl.openConnection();
			hConnect.setDoInput(true);
			// HttpURLConnection.setFollowRedirects(true);
			// hConnect.setInstanceFollowRedirects(true);
			// hConnect.setRequestProperty("Cookie" , sessionId);
			hConnect.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			hConnect.setUseCaches(false);
			hConnect.connect();

			// 读取内容
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					hConnect.getInputStream(), charSet));
			int ch;
			for (; (ch = rd.read()) > -1;) {
				buffer.append((char) ch);
			}

			String s = buffer.toString();
			if (!s.contains("首页"))
				return null;
			// System.out.println(s);
			// <a href="/a/xiaoyuandongtai/2012/0321/5598.html" class="title"
			// target="_blank">魏德圣——永远的莫那鲁道</a>
			s = s.replaceAll("<div class=\"listbox\">", "@@")
					.replaceAll("<!-- ArticleList end -->", "@@").split("@@")[1];
			s = s.replaceAll("<li>", "@@");

			// 将每个item切开，装入数组等待提取数据
			String[] itemCollection = s.split("@@");

			// 提取数据
			Pattern pTitle = Pattern
					.compile("<a href=\"/a/xiaoyuandongtai/([^\"])*\" class=\"title\" target=\"_blank\">([^/])+");
			Pattern pSub = Pattern
					.compile("<font color=\"#999999\">([^<])+</font>");
			Pattern pImg = Pattern
					.compile("<img src='(http://su.whu.edu.cn)?/uploads/allimg([^>])+>");
			Pattern pDate = Pattern.compile("\\d+-\\d+-\\d+\\s\\d+:\\d+:\\d+");

			String tempItem = null;
			Matcher m;
			String item[] = new String[6];
			String temp = "", tempLink = "";
			for (int i = 1; i < itemCollection.length; i++) {
				tempItem = itemCollection[i];
				// 提取标题与连接
				m = pTitle.matcher(tempItem);
				if (m.find()) {
					temp = m.group();
					temp = temp.replaceAll("<b>", "");
					temp = temp.replaceAll("<\b>", "");
					item [0] =  temp.split(">")[1].replaceAll("&nbsp;", " ")
							.replaceAll("&quot;", "\"")
							.replaceAll("&#8226;", "·").replaceAll("<", "");
					item[5] = ConfigurationUtil.URL_NEWS + temp.split("\"")[1];
				}
				// 提取副标题
				m = pSub.matcher(tempItem);
				if (m.find()) {
					temp = m.group();
					temp = temp.replaceAll("</font", "").replaceAll("&#8226;",
							"·");
					temp = temp.split(">")[1];
					item[3] = temp;
					//item += tempLink;
				}
				// 提取缩略图

				m = pImg.matcher(tempItem);
				if (m.find()) {
					temp = m.group();
					temp = temp.split("\'")[1];
					item[4] = ConfigurationUtil.URL_NEWS+temp.replaceAll("http://su.whu.edu.cn", "");
				}
				
				//提取日期
				m = pDate.matcher(tempItem);
				if(m.find()) {
					temp = m.group();
					item[1] = temp;
				}

				item[2] = "类型/新闻";
				result.add(item);
				item = new String[6];
				temp = "";
			}

			rd.close();
			hConnect.disconnect();
			return result;
		} catch (Exception e) {
			// return "错误:读取网页失败！";
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<String[]> getEmployment(String url, String charSet) {

		StringBuffer buffer = new StringBuffer();
		ArrayList<String[]> result = new ArrayList<String[]>();
		try {
			HttpURLConnection hConnect;
			URL newUrl = new URL(url);

			hConnect = (HttpURLConnection) newUrl.openConnection();
			hConnect.setDoInput(true);
			// HttpURLConnection.setFollowRedirects(true);
			// hConnect.setInstanceFollowRedirects(true);
			// hConnect.setRequestProperty("Cookie" , sessionId);
			hConnect.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			hConnect.setUseCaches(false);
			hConnect.connect();

			// 读取内容
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					hConnect.getInputStream(), charSet));
			int ch;
			for (; (ch = rd.read()) > -1;) {
				buffer.append((char) ch);
			}

			String s = buffer.toString();

			// System.out.println(s);
			Pattern p = Pattern.compile("页次：([^&])+&nbsp;");
			Matcher m = p.matcher(s);
			String[] item = new String[6];
			String temp = "";
			int times = 0;
			/*
			 * while(m.find()){ times++; temp = m.group();
			 * //System.out.println(item); item +=
			 * temp.split(">")[1].replaceAll("&nbsp;", " ").replaceAll("&quot;",
			 * "\"").replaceAll("&#8226;", "·")+"@@"; if(times%2 == 0){ item +=
			 * ConfigurationUtil.URL_LECTURE+temp.split("\"")[1];
			 * System.out.println(item); result.add(item); item = ""; } }
			 */

			if (m.find()) {
				temp = m.group().replaceAll("页次：", "").replaceAll("&nbsp;", "");
				System.out.println(temp);
				int pos = temp.indexOf("/");
				// System.out.println(temp.substring(0, pos));
				// System.out.println(temp.substring(pos+1));
				if (Integer.parseInt(temp.substring(0, pos)) > Integer
						.parseInt(temp.substring(pos + 1)))
					return null;
			}
			/*
			 * p = Pattern.compile("<tr bgcolor=\"#ffffff\">.*</tr>"); m =
			 * p.matcher(s);
			 */
			s = s.replaceAll("<tr bgcolor=\"#ffffff\">", "@@@");
			StringTokenizer token = new StringTokenizer(s, "@@@");
			System.out.println("in");
			token.nextToken();
			while (token.hasMoreTokens()) {
				temp = token.nextToken();
				temp = temp.replaceAll(" ", "");// 去掉空格
				temp = temp.replaceAll("\n", "");// 去掉换行，制表符
				temp = temp.replaceAll("\r", "");// 去掉换行，制表符
				temp = temp.replaceAll("\t", "");// 去掉换行，制表符
				temp = temp.replaceAll("<tdalign=\"center\">", "");
				temp = temp.replaceAll("<td>", "");
				temp = temp.replaceAll("<ahref=\"", "");
				temp = temp.replaceAll("\"title=([^>])*>", "</td>");
				temp = temp.replaceAll("</a>", "");
				temp = temp.replaceAll("&#8226;", "·");// &#8226;
				if (!temp.contains("</td>"))
					continue;
				// System.out.println(temp);
				item[0] = temp.split("</td>")[1];
				item[1] = temp.split("</td>")[2]+" "+temp.split("</td>")[3];
				item[2] = "类型/招聘信息";
				item[3] = "地点："+temp.split("</td>")[4];
				item[5] = ConfigurationUtil.URL_EMPLOYMENT
						+ temp.split("</td>")[0];
				// System.out.println(item);
				result.add(item);
				item = new String[6];
			}

			rd.close();
			hConnect.disconnect();
			return result;
		} catch (Exception e) {
			// return "错误:读取网页失败！";
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<String[]> getActivities(String url, String charSet) {

		StringBuffer buffer = new StringBuffer();
		ArrayList<String[]> result = new ArrayList<String[]>();
		try {
			HttpURLConnection hConnect;
			URL newUrl = new URL(url);

			hConnect = (HttpURLConnection) newUrl.openConnection();
			hConnect.setDoInput(true);
			
			hConnect.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded");
			hConnect.setUseCaches(false);
			hConnect.connect();

			// 读取内容
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					hConnect.getInputStream(), charSet));
			int ch;
			for (; (ch = rd.read()) > -1;) {
				buffer.append((char) ch);
			}

			String s = buffer.toString();

			s = s.replaceAll("div id=\"db-events-list\"", "@@")
					.replaceAll("<div class=\"paginator\">", "@@").split("@@")[1];
			s = s.replaceAll("<li class=\"list-entry\">", "@@");

			// 将每个item切开，装入数组等待提取数据
			String[] itemCollection = s.split("@@");

			// 提取数据
			Pattern pTitle = Pattern
					.compile("title=\".+?\">");
			Pattern pSub = Pattern
					.compile("</span>[^<]+?</li>");
			Pattern pImg = Pattern
					.compile("<img src=\".+?\"/>");
			Pattern pLink = Pattern.compile("<a href=\"http://www.douban.com/event/\\d+/\">");

			String tempItem = null;
			Matcher m;
			String item[] = new String[6];
			String temp = "";
			for (int i = 1; i < itemCollection.length; i++) {
				tempItem = itemCollection[i];
				// 提取img
				m = pImg.matcher(tempItem);
				if (m.find()) {
					temp = m.group();
					temp = temp.replaceAll("<img src=\"", "");
					temp = temp.replaceAll("\"/>", "");
					item[4] = temp;
				}
				// 提取副标题
				m = pTitle.matcher(tempItem);
				if (m.find()) {
					temp = m.group();
					temp = temp.replaceAll("title=\"", "").replaceAll("\">",
							"");
					temp = temp.replaceAll("&.+?;", "");
					item[0] = temp;
					//item += tempLink;
				}
				// 提取缩略图

				m = pSub.matcher(tempItem);
				
				int j=0;
				while(m.find()) {
					temp = m.group();
					temp = temp.replaceAll("</span>", "").replaceAll("</li>", "").trim();
					switch(j) {
						case 0:
							item[1] = temp;
							break;
						case 1:
							temp = temp.replaceAll("\\s+", " ");
							item[3] = temp;
							break;
						case 2:
							item[2] = temp;
							break;
					}
					j++;
					if(j > 2) break;
				}
				
				//提取链接
				m = pLink.matcher(tempItem);
				if(m.find()) {
					temp = m.group();
					temp = temp.replaceAll("<a href=\"", "").replaceAll("\">", "");
					item[5] = temp;
				}
				
				result.add(item);
				item = new String[6];

			}

			rd.close();
			hConnect.disconnect();
			return result;
		} catch (Exception e) {
			// return "错误:读取网页失败！";
			e.printStackTrace();
			return null;
		}
	}
	
    public String getLibInfo(String strUrl, String charSet) {
        // 读取结果网页
    	String result = "", temp = "";
        try {
            URL newUrl = new URL(strUrl);
            HttpURLConnection hConnect = (HttpURLConnection) newUrl
                    .openConnection();
            // POST方式的额外数据
                hConnect.setDoOutput(true);
                hConnect.setDoInput(true);
                hConnect.setRequestMethod("POST");
                hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                hConnect.setUseCaches(false);
                hConnect.connect();

                String key = null;
                String cookieVal = "";
                
                DataOutputStream out = new DataOutputStream(hConnect.getOutputStream());  //声明数据写入流
                
                String content = "func=login&calling_system=aleph&term1=short&selfreg=&bor_id=" + URLEncoder.encode(sno, "utf-8") + "&bor_verification=" + URLEncoder.encode(pwd, "utf-8") + "&institute=WHU&url=http%3A%2F%2Fopac.lib.whu.edu.cn%2FF%2FCTA1T9SUMXHJGEREMUT9MQU1R3AI3R3ESKF4RL6RPD8579NGNN-26175%3Ffunc%3Dbor-info";
                
                out.writeBytes(content);
                out.flush();
                out.close();
                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        hConnect.getInputStream(), "UTF-8"));
                
                StringBuffer buffer = new StringBuffer();
                int ch;
                for (; (ch = rd.read()) > -1;){
                    buffer.append((char) ch);
                }
                result = buffer.toString();
                Pattern p = Pattern.compile("<noscript><a href=\"([^\"])+");
                Matcher m = p.matcher(result);
                if(m.find()){
                	result = "http://opac.lib.whu.edu.cn:80"+m.group().split("\"")[1];
                }else{
                	System.out.println("密码错误===="+pwd);
                	return null;
                }
                
                result = this.getContent(result, "utf-8");
                
                p = Pattern.compile("var url = '([^'])+");
                m = p.matcher(result);
                if(m.find()){
                	result = m.group().split("'")[1];
                }
                result = this.getContent(result, "utf-8");
                
                p = Pattern.compile("<a href=\"([^\"])+");
                m = p.matcher(result);
                if(m.find()){
                	result = "http://opac.lib.whu.edu.cn:80"+m.group().split("\"")[1];
                }
                
                result = this.getContent(result, "utf-8");
                
                String num = "0";
                p = Pattern.compile("当前借阅数：([^\\)])+\\);\">[0-9]");
                m = p.matcher(result);
                if(m.find()){
                	result = m.group();
                	num = result.substring(result.length()-1);
                	result = result.split("'")[1];
                }
                System.out.println("当前借阅数"+num);
                if(!num.endsWith("0"))
                	result = this.getContent(result, "utf-8");
                else
                	return "0";
               
                temp += "当前借阅数"+num+"@";
                
                p = Pattern.compile("<a href=([^>])+>全部续借");
                m = p.matcher(result);
                if(m.find()){
                	temp += m.group().split("'")[1]+"@";
                }
                //<td class=td1 valign=top width="27%">
                p = Pattern.compile("<td class=td1 valign=top width=\"27%\">([^<])+");
                m = p.matcher(result);
                while(m.find()){
                	temp += m.group().split(">")[1]+"@";
                }
                hConnect.disconnect();


        } catch (Exception e) {
            // return "错误:读取网页失败！";
            //
        	return null;//网络错误
             
        }
		return temp;
    }
    
    public String getCampusCardInfo(String url, String charSet, String cookie){
    	StringBuffer buffer = new StringBuffer();
    	try{
        HttpURLConnection hConnect;
        URL newUrl = new URL(url);
    		
        hConnect = (HttpURLConnection) newUrl
                .openConnection();
        hConnect.setDoInput(true);
         hConnect.setRequestProperty("Cookie" , cookie);
         hConnect.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
         hConnect.setUseCaches(false);
         hConnect.connect();
         
         BufferedReader rd = new BufferedReader(new InputStreamReader(
                 hConnect.getInputStream(), charSet));
              int ch;
              for (; (ch = rd.read()) > -1;){
             	 buffer.append((char) ch);
              }

              String s = buffer.toString();
              
              Pattern p = Pattern.compile("账号.+元");
              Matcher m = p.matcher(s);
              if(m.find()){
            	  return m.group().replace("<span>", "").replace("<\\/span>", "");
              }
         
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return null;
    }
	
}
