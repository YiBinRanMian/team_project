import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

public class BPELParser {
    private static JSONObject JsonObj = new JSONObject();

    public static void main(String[] args) {
        String filepath = "/Users/harodfinvh/Downloads/bookloan-bpel-master/BookLoan/src/bookLoan.bpel";
        parseXML(filepath);
    }
    private static JSONObject parseXML(String file) {
        List<JSONObject> Import = new ArrayList<>();
        List<JSONObject> partnerLink = new ArrayList<>();
        List<JSONObject> variable = new ArrayList<>();
        JsonObj.put("partnerLink",partnerLink);
        JsonObj.put("import",Import);
        JsonObj.put("variable",variable);
        try{
            // 创建dom4j解析器
            SAXReader reader = new SAXReader();
            // 获取Document节点
            Document document = reader.read(file);
            Element root = document.getRootElement();
            JSONObject rootInfo = new JSONObject();
            List<Attribute> attributes = root.attributes();
            List<Namespace> namespaces = root.declaredNamespaces();
            for (Attribute a:attributes){
                rootInfo.put(a.getName(),a.getValue());
            }
            for (Namespace n:namespaces){
                if (n.getPrefix().equals("")){
                    rootInfo.put("targetNamespace",n.getStringValue());
                }else{
                    rootInfo.put(n.getPrefix(),n.getStringValue());
                }
            }
            JsonObj.put("process",rootInfo);
            List<Element> elements= root.elements();
            for(Element node: elements) {
                parseNode(node);
            }
//            System.out.println(JsonObj.toString());
        }
            catch(DocumentException e){
            e.printStackTrace();
        }
        return null;
    }

    private static void parseNode(Element node){
        String nodeName = node.getName();
        if (nodeName.equals("import")){
            JSONObject ipt = new JSONObject();
            String[] attributeList = {"namespace","location","importType"};
            for (int i = 0; i < attributeList.length; i++) {
                String attribute = node.attributeValue(attributeList[i]);
                if (attribute!=null){
                    ipt.put(attributeList[i],attribute);
                }
            }
            if (ipt.length()>=1){
                JSONArray Import = JsonObj.getJSONArray("import");
                Import.put(ipt);
            }
        }else if (nodeName.equals("partnerLinks")){
            List<Element> partnerLinks = node.elements();
            String[] attributeList = {"name","partnerLinkType","myRole","partnerRole"};
            for (Element partnerLink: partnerLinks){
                JSONObject partnerLinkJson = new JSONObject();
                for (int i = 0; i < attributeList.length; i++) {
                    String attribute = partnerLink.attributeValue(attributeList[i]);
                    if (attribute!=null) {
                        partnerLinkJson.put(attributeList[i],attribute);
                    }
                }
                if (partnerLinkJson.length()>=1){
                    JSONArray PartnerLink = JsonObj.getJSONArray("partnerLink");
                    PartnerLink.put(partnerLinkJson);
                }
            }
        }else if (nodeName.equals("variables")){
            List<Element> variables = node.elements();
            String[] attributeList = {"name","messageType","type"};
            for (Element variable: variables){
                JSONObject variableJson = new JSONObject();
                for (int i = 0; i < attributeList.length; i++) {
                    String attribute = variable.attributeValue(attributeList[i]);
                    if (attribute!=null) {
                        variableJson.put(attributeList[i],attribute);
                    }
                }
                List<Namespace> namespaces = variable.declaredNamespaces();
                if (namespaces.size()>0){
                    for (Namespace namespace: namespaces){
                        variableJson.put(namespace.getPrefix(),namespace.getStringValue());
                    }
                }
                if (variableJson.length()>=1){
                    JSONArray variableJsonArray = JsonObj.getJSONArray("variable");
                    variableJsonArray.put(variableJson);
                }
            }
        }else if (nodeName.equals("sequence")){

        }


    }

}