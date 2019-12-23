package paramCollection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
* 收集WSDL中的参数信息，保存到XML文件
* @ZHQ
* @version 0.1
*/
public class CollectParam {
	public static Collection<String> unusefulWords = new ArrayList<String>();
	
	public static void init() {
		unusefulWords.add("get");
		unusefulWords.add("list");
		unusefulWords.add("string");
		unusefulWords.add("int");
	}
	
	/**
	 * 主函数入口
	 * @param args
	 */
    public static void main (String[] args) throws IOException {
    	init();//初始化一些数据
    	
    	//1. 解析WSDL文档，获取其中无需输入参数的operation（大概率为获取参数的）
    	String path = "/Users/harodfinvh/Desktop/y1/docs/webservice/WSDL_Documents";
    	File file = new File(path);
    	File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
    	for(File f:fs){					//遍历File[]数组
			if(!f.isDirectory()) {		//若非目录(即文件)，则打印
				System.out.println(f);
			    JSONObject serviceInfo = parseXML(f);
			    //2.分别对每个operation生成Soap请求需要的XML，并发送Soap请求获取可输入参数，最后进行保存
			    try{
			    	saveParamXML(serviceInfo);
			    } catch(Exception e){
			    	e.printStackTrace();
			    }
			}
		}
//    	JSONObject serviceInfo = parseXML("E:\\WSDL_Documents\\10.wsdl");
//    	System.out.println(serviceInfo.toString());
    	
//    	//2.分别对每个operation生成Soap请求需要的XML，并发送Soap请求获取可输入参数，最后进行保存
//    	saveParamXML(serviceInfo);
    
    }
    
    public static void saveParamXML(JSONObject serviceInfo) throws IOException {
    	if(serviceInfo.has("operations") & serviceInfo.has("servicePort")) {
    		String targetNamespace = serviceInfo.getString("targetNamespace");
    		JSONArray operations = serviceInfo.getJSONArray("operations");
    		String urlName = serviceInfo.getJSONObject("servicePort").getString("address");
    		for(Object ja: operations) {
    			String operationName = ja.toString();
    			String soapXML = generateXML(targetNamespace, operationName);
    			//发送Soap请求，获取参数的XML文件
    			String resXML = pushSoap(soapXML, urlName);
    			try {
					Document document = DocumentHelper.parseText(resXML);
					//格式化为缩进格式
	    			OutputFormat format = OutputFormat.createPrettyPrint();
	    			//设置编码格式
	    			format.setEncoding("UTF-8");
	    			try {
	    				//保存服务器返回的XML文件
	    				XMLWriter writer = new XMLWriter(new FileWriter("E:\\paramXML\\"+ operationName +".xml"),format);
	    				//写入数据
	    				writer.write(document);
	    				writer.close();
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
				} catch (DocumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    		}
    	}else {
    		//do nothing
    	}
    }
    
    /**
     * 发送Soap请求，获取该服务的可输入参数
     */
    public static String pushSoap(String soapXML,String urlname) throws IOException{
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
            System.out.println("服务器响应成功！");
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            StringBuilder sb = new StringBuilder();
            String temp = null;
            while(null != (temp = br.readLine())){
                sb.append(temp);
            }
            //打印结果
            System.out.println(sb.toString());
            is.close();
            isr.close();
            br.close();
            return sb.toString();
        }
        else {
            System.out.print("请求失败！");
        }
        os.close();
        return null;
    }
    
    /**
     * 解析WSDL，获取其中能够返回可输入参数集合的operation及servicePort
     * @param file
     * @return 一个JSONObject对象，包含operations（可能不存在）及servicePort等信息。
     */
    private static JSONObject parseXML(File file){
    	JSONObject jsonObj = new JSONObject();
    	ArrayList<String> operations  = new ArrayList<String>();
        try {
            // 创建dom4j解析器
            SAXReader reader = new SAXReader();
            // 获取Document节点
            Document document = reader.read(file);
            Element root = document.getRootElement();
            String targetNamespace = root.attributeValue("targetNamespace");
            jsonObj.put("targetNamespace", targetNamespace);
            //used for soap
            List<Element> elements= root.elements();
            for(Element node: elements) {
            	String nodeName = node.getName();
            	//从types中获取可以得到输入参数的operationName
            	if(nodeName.equals("types")){
                    Element schema = node.element("schema");
                    List<Element> eles = schema.elements();  //schema下的多个element，可能是element-->complex，也可能是simpleType
                    for(Element e: eles) {  //遍历types下面的所有element
                    	if(e.getName()=="element") {
                    		if(e.attributeValue("name").contains("Response")) {  //过滤Response operation
                    			continue;
                    		}
                    		if(e.element("complexType") != null) {
                                Element complexType = e.element("complexType");
                                //判断有没有sequence
                                if(complexType.element("sequence") != null) {
                                    //do nothing
                                }else {
                                    //如果没有sequence，说明这个operation大概率是一个获取参数的operation，把它记录下来
                                	String operationName = e.attributeValue("name");//获取operation name
                                	operations.add(operationName);
                                }
                            }
                    	}
                    }
                    if(operations.size()>0) {
                    	jsonObj.put("operations", operations);
                    }
                }
            	//从service中获取服务的访问地址
            	else if (nodeName.equals("service")){
            		JSONObject servicePort = new JSONObject();
                    List<Element> elems = node.elements();
                    for (Element e:elems){
                        if (e.getName().equals("port")) {
                        	//理论上只需要soap的address，其它暂时忽略
                        	if(e.element("address").getNamespacePrefix().equals("soap")) {
                                servicePort.put("name",e.attributeValue("name"));
                                servicePort.put("address",e.element("address").attributeValue("location"));
                                servicePort.put("namespace",e.element("address").getNamespacePrefix());
                                jsonObj.put("servicePort", servicePort);
                        	}
                        }
                    }
                }
            }
            return jsonObj;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }
    
    
    /**
     * 生成Soap请求需要的XML
     */
    public static String generateXML(String targetNamespace, String operationName){
        Document document = DocumentHelper.createDocument();

        Element root = document.addElement("soapenv:Envelope");
        root.addNamespace("soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        root.addNamespace("q0",targetNamespace);
        root.addNamespace("xsd","http://www.w3.org/2001/XMLSchema");
        root.addNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Element Body = root.addElement("soapenv:Body");
        Element Operation = Body.addElement("q0:"+operationName);
        String soapXML = document.asXML();
        return soapXML;
    }
    
}
