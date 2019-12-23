import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static Util.SoapUtil.searchNamepace;
import static Util.SoapUtil.trimNamespace;

public class WSDLParser {
    private static JSONObject JsonObj = new JSONObject();

    public static JSONObject parseXML(String file){
        List<JSONObject> message = new ArrayList<>();
        List<JSONObject> portType = new ArrayList<>();
        List<JSONObject> binding = new ArrayList<>();
        JsonObj.put("message", message);
        JsonObj.put("portType", portType);
        JsonObj.put("binding", binding);
        try {
            // 创建dom4j解析器
            SAXReader reader = new SAXReader();
            File wsdlFile = new File(file);
            // 获取Document节点
            Document document = reader.read(wsdlFile.getAbsolutePath());

            Element root = document.getRootElement();
            JSONObject rootInfo = new JSONObject();
            List<Attribute> attributes = root.attributes();
            List<Namespace> namespaces = root.declaredNamespaces();
            for (Attribute a:attributes){
                rootInfo.put(a.getName(),a.getValue());
            }
            for (Namespace n:namespaces){
                rootInfo.put(n.getPrefix(),n.getStringValue());

            }
            JsonObj.put("definitions",rootInfo);
            //used for soap
            List<Element> elements= root.elements();
            for(Element node: elements) {
//				System.out.println(node.getName());
                parseNode(node,wsdlFile);
            }
            return JsonObj;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void parseNode(Element node,File file) {

        String nodeName = node.getName();

        if(nodeName.equals("types")){
            //获取子节点中element属性值
            //如果存在参数，获取参数
            JSONObject types = new JSONObject();
            Element schema = node.element("schema");

            List<Element> eles = schema.elements();  //schema下的多个element，可能是element-->complex，也可能是simpleType
            for(Element e: eles) {
                if (e.getName().equals("import")) {
                    String location = e.attributeValue("schemaLocation");
                    if (!location.equals("")) {
                        String xsdPath = file.getParent()+"/"+location;
                        try{
                            SAXReader reader = new SAXReader();
                            Document document = reader.read(xsdPath);
                            Element root = document.getRootElement();
                            List<Element> elements = root.elements();
                            for (Element ele:elements){
                                JSONObject restriction= getRestriction(ele);
                                if (restriction.length()>0) {
                                    types.put(ele.attributeValue("name"), restriction);
                                }
                            }
                        }
                        catch (DocumentException err){
                            err.printStackTrace();
                        }
                    }
                }
            }
            for(Element e: eles) {

                //调用约束获取函数，返回JSON对象
                JSONObject restriction= getRestriction(e);
                //如果存在约束条件，则添加进对应方法中
                if (restriction.length()>0) {
                    types.put(e.attributeValue("name"), restriction);
                }
            }
            JsonObj.put("types", types);  ///加入全局变量
        }
        else if(nodeName.equals("message")){
            //获取message的name以及子节点中element属性值
            JSONObject msg = new JSONObject();
            String msg_name = node.attributeValue("name");
            if (node.elements().size()>0) {
                List<Element> parts = node.elements();
                JSONArray partsArray = new JSONArray();
                for (Element part : parts) {
                    //TODO: 不确定直接通过parameter是否安全
                    if (part.attribute("element") != null) {
                        String ele_name = part.attributeValue("element");
                        msg.put("name", msg_name);
                        msg.put("element", ele_name);
                    } else if (part.attribute("type") != null) {
                        JSONObject singlePart = new JSONObject();
                        String part_name = part.attributeValue("name");
                        String part_type = part.attributeValue("type");
                        singlePart.put("name", part_name);
                        singlePart.put("type", part_type);
                        partsArray.put(singlePart);
                    }
                    //TODO: 是否存在别的写法
                    else {
                    }
                }
                if (partsArray.length() > 0) {
                    msg.put(msg_name, partsArray);
                }
            }else{
                msg.put("name", msg_name);
                msg.put("element", "");
            }
            if(msg.length()>=1) {
                JSONArray message = JsonObj.getJSONArray("message");
                message.put(msg);
                JsonObj.put("message", message);
            }
        }
        else if(nodeName.equals("portType")){
            JSONObject portType = new JSONObject();
            List<Element> operations = node.elements();
            for(Element operation: operations) {
                JSONObject param = new JSONObject();
                List<Element> parameters = operation.elements();
                //获取输入和输出message
                for(Element parameter: parameters) {
                    if(parameter.getName().equals("input")) {
                        String inputMessage = parameter.attributeValue("message");
                        param.put("input", trimNamespace(inputMessage));
                    }
                    if(parameter.getName().equals("output")) {
                        String ouputMessage = parameter.attributeValue("message");
                        param.put("output", trimNamespace(ouputMessage));
                    }
                }
                if(param.length()>0) {
                    portType.put(operation.attributeValue("name"), param);
                }
            }
            JSONObject portName = new JSONObject();
            portName.put(node.attributeValue("name"),portType);
            JSONArray portTypes = JsonObj.getJSONArray("portType");
            portTypes.put(portName);
            JsonObj.put("portType",portTypes);
        }else if (nodeName.equals("binding")){
            JSONObject binding = new JSONObject();
            JSONObject bindingInfoJson = new JSONObject();
            String bindingType = searchNamepace(node);
            bindingInfoJson.put("namespace",bindingType);
            String bindingName = node.attributeValue("name");
            bindingInfoJson.put("name",bindingName);
            String portTypeName = trimNamespace(node.attributeValue("type"));
            bindingInfoJson.put("type",portTypeName);
            binding.put("binding",bindingInfoJson);
            JSONArray bindings = JsonObj.getJSONArray("binding");
            bindings.put(binding);
            JsonObj.put("binding",bindings);
        }
        else if (nodeName.equals("service")){
            JSONObject service = new JSONObject();
            JSONObject serviceInfo = new JSONObject();
            JSONArray servicePorts = new JSONArray();
            String serviceName = node.attributeValue("name");
            List<Element> elements = node.elements();
            for (Element e:elements){
                if (e.getName().equals("port")) {
                    JSONObject servicePort = new JSONObject();
                    servicePort.put("name",e.attributeValue("name"));
                    servicePort.put("binding",trimNamespace(e.attributeValue("binding")));
                    servicePort.put("address",e.element("address").attributeValue("location"));
                    //TODO: 如果所有 service 的 Namespace 都在 port 下则可优化
                    servicePort.put("namespace",searchNamepace(e));
                    servicePorts.put(servicePort);
                }
            }
            serviceInfo.put("port",servicePorts);
            service.put(serviceName,serviceInfo);
            JsonObj.put("service",service);

        }
    }


    //获取types子节点的属性信息（约束条件）
    private static JSONObject getRestriction(Element node) {
        JSONObject jsonObj = new JSONObject();
        try {
            //如果是element-->complexType
            if(node.getName().equals("element")) {
                //
                if(node.element("complexType") != null) {
                    Element complexType = node.element("complexType");
                    if(complexType.element("sequence") != null) {
                        //传入complexType 将sequence中的变量打包，返回一个List<JSONObject>
                        List<JSONObject> elems = appendComplexType(complexType);

                        if(elems.size()>0) {
                            jsonObj.put("sequence", elems);
                        }
                        //空element，即不包含<s:sequence>，直接添加"sequence":[]到对象里
                    }else {
                        List<JSONObject> elems = new ArrayList<>();
                        jsonObj.put("sequence", elems);
                    }
                }
                //TODO: 单独定义element的情况，不确定是否else或添加别的条件
                else {
                    String type_name = trimNamespace(node.attributeValue("type"));
                    List<Element> elems = node.getParent().elements();
                    for(Element elem : elems) {
                        if((elem.getName().equals("complexType")) && elem.attributeValue("name").equals(type_name)) {
                            List<JSONObject> elements = appendComplexType(elem);
                            if(elements.size()>0) {
                                jsonObj.put("sequence", elements);
                            }
                            break;
                        }
//                        TODO: 理论上不存在这种情况
//                        else if(elem.getName().equals("simpleType")&& elem.attributeValue("name").equals(type_name)){
//                            List<JSONObject> elements = appendComplexType(elem);
//                            JSONObject element = elements.get(0);
//                            System.out.println(elements.toString());
//                            JSONObject jsonObject = new JSONObject();
//                            JSONObject simpleObject = new JSONObject();
//                            jsonObject.put("name",element.getString("name"));
//                            jsonObject.put("type",element.getString("self-defined"));
//                            simpleObject.put(type_name,elements);
//                            jsonObject.put("simpleType",simpleObject);
//                            if(elements.size()>0) {
//                                jsonObj.put("sequence", new JSONArray().put(jsonObject));
//                            }
//                        }

                    }

                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return jsonObj;
    }

    //传入complexType 将sequence中的变量打包
    private static List<JSONObject> appendComplexType(Element elem){
        int order_num = 1;
        List<JSONObject> elements = new ArrayList<>();
        if(elem.getName().equals("complexType")) {
            List<Element> params = elem.element("sequence").elements();
            for (Element p : params) {
                JSONObject curElem = new JSONObject();
                String[] attributes = {"minOccurs", "maxOccurs", "name", "nillable"};
                for (int i = 0; i < attributes.length; i++) {
                    if (p.attributeValue(attributes[i]) != null) {
                        curElem.put(attributes[i], trimNamespace(p.attributeValue(attributes[i])));
                    }
                }
                if (p.attributeValue("type") != null) {
                    List<String> typeArray = new ArrayList<>();
                    //TODO: 只是xsd常用的数据类型
                    typeArray.add("string");
                    typeArray.add("decimal");
                    typeArray.add("integer");
                    typeArray.add("int");
                    typeArray.add("boolean");
                    typeArray.add("double");
                    typeArray.add("date");
                    typeArray.add("time");
                    typeArray.add("dateTime");
                    typeArray.add("float");
                    typeArray.add("long");
                    typeArray.add("short");
                    String type = trimNamespace(p.attributeValue("type"));
                    if (typeArray.contains(type)) {
                        curElem.put("type", type);
                    } else {
                        curElem.put("type", "self-defined");
                        JSONObject complexTypeJson = new JSONObject();
                        List<Element> elems;
                        //不在element中的complextype
                        if (elem.getParent().getName().equals("schema")) {
                            elems = elem.getParent().elements();
                            //element中的complextype
                        } else {
                            elems = elem.getParent().getParent().elements();
                        }
                        for(Element e : elems) {
                            if(e.getName().equals("complexType") && e.attributeValue("name").equals(type)) {
                                List<JSONObject> elementList = appendComplexType(e);
                                if(elementList.size()>0) {
                                    complexTypeJson.put(type, elementList);
                                    curElem.put("complexType", complexTypeJson);

                                }else {
                                    List<JSONObject> emptyElementList = new ArrayList<>();
                                    complexTypeJson.put(type,emptyElementList);
                                    curElem.put("complexType", complexTypeJson);

                                }
                                break;
                            }else if(e.getName().equals("simpleType")&& e.attributeValue("name").equals(type)){
                                List<JSONObject> elementList = appendComplexType(e);
                                if(elementList.size()>0) {
                                    complexTypeJson.put(type,elementList);
                                    curElem.put("simpleType", complexTypeJson);

                                }else {
                                    List<JSONObject> emptyElementList = new ArrayList<>();
                                    complexTypeJson.put(type,emptyElementList);
                                    curElem.put("simpleType", complexTypeJson);

                                }
                            }
                        }
                    }
                    //TODO: hand Different Namespace
                }
                curElem.put("order_of_seq", order_num++);

                elements.add(curElem);
            }
        }else if (elem.getName().equals("simpleType")){
            JSONObject simpleJson = new JSONObject();
            String name = elem.attributeValue("name");
            simpleJson.put("name",name);
            Element restriction = elem.element("restriction");
            String base = trimNamespace(restriction.attributeValue("base"));
            simpleJson.put("base",base);
            if(restriction.element("enumeration")!=null) {
                List<String> enums = new ArrayList<>();
                List<Element> enus = restriction.elements("enumeration");
                for(Element e: enus) {
                    enums.add(e.attributeValue("value"));
                }
                simpleJson.put("enumeration", enums);
            }
            else if (restriction.element("pattern")!=null){
                simpleJson.put("pattern",restriction.element("pattern").attributeValue("value"));
            }
            else if (restriction.element("whiteSpace")!=null){
                simpleJson.put("whiteSpace",restriction.element("whiteSpace").attributeValue("value"));
            }
            else if (restriction.element("length")!=null){
                simpleJson.put("length",restriction.element("length").attributeValue("value"));
            }
            else if (restriction.element("minLength")!=null){
                simpleJson.put("minLength",restriction.element("minLength").attributeValue("value"));
            }else if (restriction.element("maxLength")!=null){
                simpleJson.put("maxLength",restriction.element("maxLength").attributeValue("value"));
            }
            elements.add(simpleJson);
        }
        return elements;
    }



}