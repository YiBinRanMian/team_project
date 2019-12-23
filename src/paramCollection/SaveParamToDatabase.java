package paramCollection;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;

public class SaveParamToDatabase {
    public static void main(String[] args) {
    	String path = "/Users/harodfinvh/Desktop/y1/docs/webservice/test";
    	File file = new File(path);
    	File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
    	for(File f:fs){					//遍历File[]数组
			if(!f.isDirectory()) {		//若非目录(即文件)，则读取
				System.out.println(f);
			    try{
			    	parseXML(f);
			    } catch(Exception e){
			    	e.printStackTrace();
			    }
			}
		}
//    	parseXML("E:\\paramXML\\GetRegionProvince.xml");
//    	System.out.println("写入完成！");
    }
    
    public static void parseXML(File file){
    	
        try {
            // 创建dom4j解析器
            SAXReader reader = new SAXReader();
            // 获取Document节点
            Document document = reader.read(file);
            Element root = document.getRootElement();
            if(root.element("Body")!=null) {
            	Element Body = root.element("Body");
            	List<Element> elements= Body.elements();
            	for(Element node: elements) {
            		if(node.getName().contains("Response")) {    //<getRegionCountryResponse xmlns="http://WebXml.com.cn/">
            			Element result= (Element) node.elements().get(0);       //<getRegionCountryResult>
            			//result元素下面的子元素应该为输入参数
        				//将参数存入数据库
            			List<Element> res = result.elements();
            			for(Element params: res) {
            				String SQLtag = null;
            		    	String SQLvalue = null;
            		    	String SQLcode = null;
            				if(params.elements().size()>1) {  //如果有多个参数
            					SQLtag = params.getName();
        						System.out.print(SQLtag+" ");
            					List<Element> param = params.elements();
            					for(Element p:param) {
            						if(p.getName().toLowerCase().contains("code") || p.getName().toLowerCase().contains("id")) {
            							String code = p.getText();
            							System.out.print(code+" ");
            							SQLcode = code;
            						}
            						else if(p.getName().toLowerCase().contains("name")) {
            							String value = p.getText();
            							SQLvalue = value;
            							System.out.println(value);
            						}
            						else {
            							if(p.getName()!="") {
            								String value = p.getText();
                							System.out.println(value);
                							SQLvalue = value;
            							}
            							else {
            								//TODO
            							}
            						}
            					}
            				}
            				else {  //如果是单个参数
            					SQLtag=result.getName().replace("Result", "");
            					String paramString = params.getText();
            					//有的将两个参数合起来，中间用逗号分隔，如: <string>突尼斯,3321</string>
            					if(paramString.contains(",")||paramString.contains("@")) {
            						String[] param = paramString.split(",|@");
            					    //TODO:此处对code和value的区分不完善且默认只包含两个参数
            						if(param[0].matches("^[0-9]*$")) { //只包含数字的情况大多为code
        								SQLcode=param[0];
        								SQLvalue=param[1];
        							}
            						else {
        								SQLcode=param[1];
        								SQLvalue=param[0];
            						}
            					}
            					else {
            						//只有一个参数且没有分隔符
            						SQLvalue=paramString;
            						SQLcode="";
            					}
//        						System.out.print(SQLtag+" "+SQLvalue+" "+SQLcode);
            				}
            				
            				//将参数写入数据库
            				if(SQLtag!=null & SQLvalue!=null) {
            					writeSQL(SQLtag,SQLvalue,SQLcode);
            				}
            			}
            		}
                }
            }
            
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * 构造SQL语句并写入数据库
     */
    public static void writeSQL(String tag, String value, String code) {
		//要判断数据库中是否已存在这样的一条类似数据
    	String Verification = "select tag,value from parameter_semantic where tag='" +tag+ "' and value='" + value + "'";
    	DBHelper dbHelper = new DBHelper();
    	int count = dbHelper.getResultNum(Verification);
    	if(count>0) {
    		//do nothing
    		System.out.println("已存在相似数据！");
    	}
    	else {//如果不存在，则写入
    		String sqlString = "INSERT INTO parameter_semantic(tag,value,code) values('"+tag+"','"+value+"','"+code+"')";
    		dbHelper.execete(sqlString);
    	}
	}
}
