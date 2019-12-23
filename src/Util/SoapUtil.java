package Util;


import com.mifmif.common.regex.Generex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

import static org.apache.jena.dboe.trans.bplustree.BlockTracker.logger;

public class SoapUtil {
    /*
    * 根据minOccurs maxOccurs mod 生成occur
    * */
    public static int getOccurs(int minOccurs,int maxOccurs,int mod){
        //mod == 0 全设为1
        //mod == 1 随机值
        //mod == n maxOccurs 为unbounded时设为有限值n
        int occurs = 0;
        int n = mod;
        switch (mod){
            case 1:
                if (minOccurs == maxOccurs){
                    occurs = minOccurs;
                }
                else{
                    occurs = new Random().nextInt(maxOccurs-minOccurs)+minOccurs;
                }
                break;
            case 0:
                occurs = 1;
                break;
            default:
                occurs = mod;
                break;
        }
        return occurs;
    }

    /*
    * 在不需要命名空间时去除以便解析
    * */
    public static String trimNamespace(String s) {
        int indexOfColon = s.indexOf(":")+1;
        if(indexOfColon >0) {
            s = s.substring(indexOfColon);
        }
        return s;
    }

    /*
    * 获取协议名称
    * */
    public static String searchNamepace(Element node){
        List<Element> elementIterator = node.elements();
        for (Element e:elementIterator){
            if(e.getNamespacePrefix().equals("soap")){
                return "soap";
            }
            else if(e.getNamespacePrefix().equals("soap12")){
                return "soap12";
            }else if (e.getNamespacePrefix().equals("http")){
                return "http";
            }else{
                return searchNamepace(e);
            }
        }
        //默认返回soap
        return "soap";
    }



/*
* 发出soap请求并获取soap响应
* */
    public static String pushSoap(String soapXML,String urlname) throws IOException {
//第一步：创建服务地址
        URL url = new URL(urlname);
        //第二步：打开一个通向服务地址的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //第三步：设置参数
        //3.1发送方式设置：POST必须大写
        connection.setRequestMethod("POST");
        //3.2设置数据格式：content-type
        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");
        //3.3设置输入输出，因为默认新创建的connection没有读写权限，
        connection.setDoInput(true);
        connection.setDoOutput(true);

        //第四步：组织SOAP数据，发送请求

        //将信息以流的方式发送出去
        OutputStream os = connection.getOutputStream();
        os.write(soapXML.getBytes());
        //第五步：接收服务端响应，打印
        int responseCode = connection.getResponseCode();
        if(200 == responseCode){//表示服务端响应成功
            //获取当前连接请求返回的数据流
            logger.info("服务器响应成功！");
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String temp = null;
            while(null != (temp = br.readLine())){
                sb.append(temp);
            }
            //打印结果\

            is.close();
            isr.close();
            br.close();
            return StringEscapeUtils.unescapeXml( sb.toString());

        }
        else {
            logger.info("请求失败！");
        }
        os.close();
        return null;
    }
    public static String generateRegex (String pattern){
        Generex generex = new Generex(pattern);
        String randomStr = generex.random();
        // Generate random String
        return  randomStr;// a random value from the previous String list
//        // generate the second String in lexicographical order that match the given Regex.
//        String secondString = generex.getMatchedString(2);
//        System.out.println(secondString);// it print '0b'
//
//        // Generate all String that matches the given Regex.
//        List<String> matchedStrs = generex.getAllMatchedStrings();
//
//        // Using Generex iterator
//        Iterator iterator = generex.iterator();
//        while (iterator.hasNext()) {
//            System.out.print(iterator.next() + " ");
//        }
    }

}