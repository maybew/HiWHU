package com.hiwhu.tool;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfigurationUtil {

    public static final String URL_INDEX = "http://my.whu.edu.cn";                                                       //����½��url
    public static final String URL_LOGIN = "http://my.whu.edu.cn/userPasswordValidate.portal";           //��¼��url
    public static final String URL_STUINDEX = "http://jw.whu.edu.cn/IDStarLogin";      //ѧ������ϵͳ��url
    public static final String URL_STU = "http://jw.whu.edu.cn";      //ѧ������ϵͳ��url
    public static final String URL_SCORE = "http://jw.whu.edu.cn/stu/query_score.jsp";                        //��ʾ������url
    public static final String URL_LESSON = "http://jw.whu.edu.cn/stu/query_stu_lesson.jsp";          //��ʾ�γ̵�url
    public static final String URL_SEARCHLESSON = "http://jw.whu.edu.cn/stu/choose_pubLesson_list_beta.jsp?actionType=choose";//ѡ�޿γ�url
    public static final String URL_UPLOADLESSON = "http://jw.whu.edu.cn/servlet/ProcessApply?applyType=pub&studentNum=";          //�ύѡ�޿γ̵�url
    public static final String URL_CANCELLESSON = "http://jw.whu.edu.cn/servlet/DelChoosed?lessonNum=";          //���ε�url
    public static final String URL_LECTURE = "http://www.whulecture.net";          //У԰������url
    public static final String URL_NEWS = "http://su.whu.edu.cn";          //У԰��̬��url
    public static final String URL_EMPLOYMENT = "http://www.xsjy.whu.edu.cn/jiuyexinxi/"; //У԰��Ƹ��url
    public static final String URL_LIB = "http://metalib.lib.whu.edu.cn:80/pds";//ͼ��ݵ�½
    public static final String URL_BALANCE = "http://my.whu.edu.cn/pnull.portal?.pen=pe169&.ia=false&action=informationCenterAjax&.pmn=view&.f=f1298";//У԰�����
    public static final int QUERYSCORE = 1;
    public static final int MYLESSON = 3;
    
    public static String cookie;				//cookie
    public static final String COOKIE_TIME= "cookie_time";
    
    public static final String PUBCLASS_DATE= "pubclass_date";
    
    public static final String USER_DATA= "user_data";
    public static final String IFSIGNED= "ifSigned";
    public static final String USER_NAME= "user_name";
    public static final String USER_PWD= "user_pwd";
    public static final String REAL_NAME= "real_name";
    public static final String USER_COOKIE= "cookie";
    public static final String USER_SCORE= "user_score";
    public static final String USER_LESSON= "user_lesson";
    public static final String USER_PUBCLASS= "user_pubclass";
    public static final String USER_LIB_PWD= "user_lib_pwd";
    public static final String FLOW_1_DAY_UPLOAD = "flow_1_day_upload";
    public static final String FLOW_1_DAY_RECEIVE= "flow_1_day_receive";
    public static final String FLOW_2_DAY_UPLOAD = "flow_2_day_upload";
    public static final String FLOW_2_DAY_RECEIVE = "flow_2_day_receive";
    public static final String FLOW_1_MONTH_UPLOAD = "flow_1_month_upload";
    public static final String FLOW_1_MONTH_RECEIVE = "flow_1_month_receive";
    public static final String FLOW_2_MONTH_UPLOAD = "flow_2_month_upload";
    public static final String FLOW_2_MONTH_RECEIVE = "flow_2_month_receive";
    public static final String FLOW_1_TIME = "flow_1_time";
    public static final String FLOW_2_TIME = "flow_2_time";
    public static final String IFCONTROLED = "ifControled";
    
    public static ArrayList<HashMap<String, Object>> chooseList = new ArrayList<HashMap<String, Object>>();//ѡ�ε�
    public static double totalPoint = 0;//��ѡ�γ���ѧ��
    public static boolean ifchoose = false;//�Ƿ����ѡ��
    public static boolean ifdelete = false;//�Ƿ���Գ���
    public static String deletenum = "";//���ο�ͷ��
}
