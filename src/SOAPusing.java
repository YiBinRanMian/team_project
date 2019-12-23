import Entity.*;
import Util.SoapUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.query.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import paramCollection.DBHelper;
import paramGeneration.GenerateParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Util.SoapUtil.generateRegex;
import static Util.SoapUtil.getOccurs;
import static Util.SoapUtil.pushSoap;
import static org.apache.jena.dboe.trans.bplustree.BlockTracker.logger;

public class SOAPusing {
    public static void main(String[] args) throws IOException, DocumentException {
//        String file = "/Users/harodfinvh/Desktop/y1/docs/webservice/creditRating/bpelContent/1.wsdl";
          String file = "/Users/harodfinvh/Desktop/y1/docs/webservice/WSDL_Documents/10.wsdl";

        logger.info("generating service object for "+file);
        Service service = WSDLAnalyser.parse_and_retrive(file);

        for (Service.port port: service.getPorts()){
            if (port.getNamespace().equals("soap")){
                String address = port.getAddress();
                String targetNamespace = service.getTargetNamespace();
                Binding binding = port.getBindingObj();
                ArrayList<Operation> operations = binding.getOperations();
                //以operation为单位
                for (Operation operation:operations){
                    String operationName = operation.getOperation();
                    //输入参数
                    ArrayList<Parameter> paramInput = operation.getParamInput();
                    //输出参数
                    ArrayList<Parameter> paramOutput = operation.getParamOutput();
//                    targetNamespace + operationName + address
                    if(paramInput.size()==0){

                        String soapXML = generatesimpleXML(targetNamespace,operationName);
                        logger.info("simple soapXML generate compete:" + soapXML);
//                        System.out.println("begin pushing soap request:");
                        String response = pushSoap(soapXML,address);
                        if (response!=null){
                            logger.info("server reply: "+response);
                        }
                    }
                    else{
                        String soapXML = generatecomplexXML(targetNamespace,operationName,paramInput);
                        logger.info("complex soapXML generate compete:" + soapXML);
                        String response = pushSoap(soapXML,address);
                        logger.info("server reply:" + response);
                    }



                }
            }
        }
    }

    public static String generatesimpleXML(String targetNamespace,String operationName){
        logger.info("generating simple soapXML");
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("soapenv:Envelope");
        root.addNamespace("soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        root.addNamespace("q0",targetNamespace);
//        root.addAttribute("xmlns:q0",targetNamespace);
        root.addNamespace("xsd","http://www.w3.org/2001/XMLSchema");
        root.addNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Element Body = root.addElement("soapenv:Body");
        Element Operation = Body.addElement("q0:"+operationName);
        String soapXML = document.asXML();
        return soapXML;
    }
    public static String generatecomplexXML(String targetNamespace,String operationName,ArrayList<Parameter> paramInput){

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("soapenv:Envelope");
        root.addNamespace("soapenv","http://schemas.xmlsoap.org/soap/envelope/");
        root.addNamespace("q0",targetNamespace);
//        root.addAttribute("xmlns:q0",targetNamespace);
        root.addNamespace("xsd","http://www.w3.org/2001/XMLSchema");
        root.addNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Element Body = root.addElement("soapenv:Body");
        Element Operation = Body.addElement("q0:"+operationName);
        for (Parameter param:paramInput){
            Element parameter = Operation.addElement("q0:"+param.getName());
            parameter.setText(generateParamValue(operationName, param));
        }
        String soapXML = document.asXML();
        return soapXML;
    }


    //根据传入操作名和参数信息生成参数值
    public static String generateParamValue(String operationName, Parameter parameter){
        logger.info("parameter information: "+parameter.toString());
        int minOccurs = parameter.getMinOccurs();
        int maxOccurs = parameter.getMaxOccurs();
        int occurs = getOccurs(minOccurs,maxOccurs,0);

        int num = 1;
        //如果约束信息足够生成参数值
        if (parameter.getType().equals("self-defined")){
            if (parameter.getSimpleType()!=null){
                simpleType simpleType = parameter.getSimpleType();
                String base = simpleType.getBase();
                if (base.equals("string")){
                    List<String> values = new ArrayList<>();
                    if (simpleType.getEnumeration()!=null){
                        List<String> enumeration = simpleType.getEnumeration();
                        for (int occur = 0; occur < occurs; occur++) {
                            String randomEnum = enumeration.get(new Random().nextInt(enumeration.size()));
                            values.add(randomEnum);
                        }
                        return values.get(0);
                    }else if (simpleType.getPattern()!=null){
                        return generateRegex(simpleType.getPattern());
                    }
                }

            }else if (parameter.getTypes()!=null){
                logger.info("generate values for compelxTypes: "+parameter.getType());
            }
        }

        String paramName = parameter.getName();
        //清洗参数
        String param = GenerateParam.generateParameter(paramName);
        //occurs默认为1 后期会根据不同occurs多次测试
        String value = retriveFromDataBase(param,occurs);
        if (value.equals("")){
            return DBpedia(paramName);
        }else
            return value;

        //约束信息无法生成参数值

        //        String value = DBpedia(paramName);



    }

    //从数据库中获取
    public static String retriveFromDataBase(String param,int occurs){
        DBHelper dbHelper = new DBHelper();
        dbHelper.getConnection();
        Connection conn = dbHelper.getConn();
        //传入处理过的参数值和occurs
        String sqlString1 = "select code,value from parameter_semantic where tag like\""+param+"\" order by rand() limit "+occurs+";";
        String sqlString2 = "select value from parameter_semantic where tag like\""+param+"\" order by rand() limit "+occurs+";";

        try {
            PreparedStatement psta = conn.prepareStatement(sqlString1);
            java.sql.ResultSet rs = psta.executeQuery();

            while (rs.next()) {
//                return "CNY";
                if (!rs.getString(1).equals("")){
                    logger.info("retriving from database: " +  rs.getString(1));
                    return rs.getString(1);
                }else{
                    logger.info("retriving from database: " +  rs.getString(2));
                    return rs.getString(2);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info("Faied to retrive value from database.");
        return "";
    }

    //提取 ?y=" " 格式中的instance
    public static String processQueryLine1(String line){
        String pattern = "(?<=\").*?(?=\")";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find()){
            return m.group(0);

        }
        return "";
    }
    public static String processQueryLine2(String line){
        String pattern = "(?<=resource/).*?(?=>)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(line);
        while (m.find()){
            return m.group(0);

        }
        return "";
    }
    public static String DBpedia(String name) {
        String queryString =
                "PREFIX prop: <http://dbpedia.org/property/>" +
                        "PREFIX res: <http://dbpedia.org/resource/>" +
                        "PREFIX ont: <http://dbpedia.org/ontology/>" +
                        "PREFIX category: <http://dbpedia.org/resource/Category:>" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                        "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" +
                        "PREFIX geo: <http://www.georss.org/georss/>" +

                        //TODO:关于SPARQL语法部分还不熟悉，需要思考如何优化...是结果更准确
                        "select ?y " +
                        "where {?x ont:" + name + " ?y." +
                        "} " +
                        "LIMIT 100";

        //创建一个查询实例
        Query query = QueryFactory.create(queryString);
        //初始化queryExecution factory
        QueryExecution qexec = QueryExecutionFactory.sparqlService("https://dbpedia.org/sparql", query);
        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution qs = results.next();
                if (!processQueryLine1(qs.toString()).equals("")) {
                    logger.info("DBpedia query result: " + processQueryLine1(qs.toString()));

                    return processQueryLine1(qs.toString());
                }
                //匹配失败
                else if (!processQueryLine2(qs.toString()).equals("")) {
                    logger.info("DBpedia query result: " + processQueryLine2(qs.toString()));
                    return processQueryLine2(qs.toString());
                }
                //TODO:结果是例如：( ?Concept = <http://www.openlinksw.com/schemas/virtrdf#QuadStorage> )这样的URI
                //需要对URI结果进行进一步处理，提取单词

            }
        } finally {
            qexec.close();
            logger.info("DBpedia query finished!");
        }
        return "";
    }




}
