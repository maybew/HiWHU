package com.hiwhu.tool;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextHelper {
	
	
	public String contentDeal(String content, int choice) {
		Pattern pTitle = Pattern.compile("(<p[^>]*>.*?</p>)|(src=\"(http://su.whu.edu.cn)?/uploads/allimg([^>])+>)");
		Matcher m;
		String result = "";
		String temp = "";
		m = pTitle.matcher(content);
		while(m.find()) {
			temp = m.group().replaceAll("\\&[a-zA-Z]{1,10};", "").replaceAll(  "<[^>]*>", "");
			temp = temp.replaceAll("[\\s]+", "").trim();
			if(temp.equals("")) continue;
			if(temp.startsWith("src")) {
				temp = temp.split("\"")[1];
				if(!temp.contains("http://su.whu.edu.cn")) temp = "http://su.whu.edu.cn"+temp;
				result += "@@p"+temp;
			} else result += "@@0"+temp;
		}
		return result;
	}
	
	public ArrayList<String[]> infoListAbbr(ArrayList<String[]> mMore) {
		ArrayList<String[]> mAbbr = new ArrayList<String []>();
		String[] temp;
		for(int i=0;i<mMore.size();i++) {
			temp = mMore.get(i).clone();
			if(temp[4] == null || temp[4].equals("")) {
				if(temp[0].length() > 34) temp[0] = temp[0].substring(0, 33)+"...";
				
			} else {
				if(temp[0].length() > 25) temp[0] = temp[0].substring(0, 24)+"...";
				if(temp[1].length() > 22) {
					if(temp[1].contains("ÿ")) {
						temp[1] = temp[1].replaceAll("ÿ", "@@").split("@@")[0];
					} else temp[1] = temp[1].substring(0, 18);
					
				}
			}
			if (temp[3].length() > 45) temp[3] = temp[3].substring(0, 36) + "...";
			mAbbr.add(temp);
		}
		return mAbbr;
	}
}
