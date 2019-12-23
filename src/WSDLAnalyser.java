import Entity.*;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static Util.SoapUtil.trimNamespace;

public class WSDLAnalyser {
    private static JSONObject JsonObj = new JSONObject();
    private static HashMap<String,Integer > messElementMap = new HashMap<String,Integer>();//两个message类型名会冲突
    private static HashMap<String,Integer > operElementMap = new HashMap<String,Integer>();
    private static HashMap<String,Integer > emptyElementMap = new HashMap<String,Integer>();

    public static Service parse_and_retrive(String filename){
//        String targetNamespace = parseXML("/Users/harodfinvh/Desktop/y1/docs/webservice/apache-tomcat-8.5.47/webapps/ode/WEB-INF/processes/creditRating/2.wsdl");
        JsonObj = WSDLParser.parseXML(filename);
//		parseXML("E:\\WSDL_Documents\\8.wsdl");
        ArrayList<PortType> portTypes = retriveOperations();
        ArrayList<Binding> bindings = retriveBindings(portTypes);
        Service services = retriveServices(bindings);
        services.setTargetNamespace(JsonObj.getJSONObject("definitions").getString("targetNamespace"));
        return services;
    }

    static private Service retriveServices(ArrayList<Binding> bindings){
        Service service = new Service();
        ArrayList<Service.port> ports = new ArrayList<Service.port>();
        JSONObject jsonServiceJSONObject = JsonObj.getJSONObject("service");
        String serviceName = jsonServiceJSONObject.keys().next();
        JSONArray portArray = jsonServiceJSONObject.getJSONObject(serviceName).getJSONArray("port");
        for (int i = 0; i < portArray.length(); i++) {
            JSONObject portJSON = portArray.getJSONObject(i);
            Service.port port = new Service.port();
            port.setAddress(portJSON.getString("address"));
            port.setName(portJSON.getString("name"));
            port.setNamespace(portJSON.getString("namespace"));
            String bindingName = portJSON.getString("binding");
            port.setBinding(bindingName);
            for (Binding binding:bindings){
                if (binding.getName().equals(bindingName)){
                    port.setBindingObj(binding);
                    break;
                }
            }
            ports.add(port);
        }
        service.setName(serviceName);
        service.setPorts(ports);
        return service;
    }
    static private ArrayList<Binding> retriveBindings(ArrayList<PortType> portTypes){
        ArrayList<Binding> bindingArrayList = new ArrayList<>();
        JSONArray jsonBindingJSONArray = JsonObj.getJSONArray("binding");
        for (int i = 0; i < jsonBindingJSONArray.length(); i++) {
            JSONObject bindingJson = jsonBindingJSONArray.getJSONObject(i);
            JSONObject bindingInfo = bindingJson.getJSONObject("binding");
            Binding binding = new Binding();
            binding.setName(bindingInfo.getString("name"));
            binding.setNamespace(bindingInfo.getString("namespace"));
            String portType = bindingInfo.getString("type");
            binding.setBindingType(portType);
            for (PortType type:portTypes){
                if (type.getPortType().equals(portType)){
                    binding.setOperations(type.getOperations());
                    binding.setPortType(type);
                    break;
                }
            }
            bindingArrayList.add(binding);
//TODO: 可能存在binding与portType不匹配的情况
        }
        return bindingArrayList;
    }
    static private void processMap(){
        //预存储 message JSONArray 中每一个 message 的 index
        JSONArray messagesJson = JsonObj.getJSONArray("message");
        for (int i=0;i<messagesJson.length();i++) {
            JSONObject messageJson = messagesJson.getJSONObject(i);
//			System.out.println(messageJson.toString());
            //message中未直接定义变量和类型
            if(messageJson.has("name")) {
                String elementName = trimNamespace(messageJson.getString("name"));
                messElementMap.put(elementName,i);
                //message中直接定义好变量和类型的情况
            }else if(messageJson.length()==1){
                String messageName = messageJson.keys().next();
                operElementMap.put(messageName,i);
            }
        }
    }

    private static ArrayList<PortType> retriveOperations() {
        processMap();
        ArrayList<PortType> porttypes = new ArrayList<>();
        //从 portType 获取输入的 message 和输出的 message
        JSONArray portTypesJson = JsonObj.getJSONArray("portType");
        for(int i =0 ;i<portTypesJson.length();i++) {
            JSONObject portTypeJson = portTypesJson.getJSONObject(i);
            PortType porttype = new PortType();
            String portTypeName = portTypeJson.keys().next();
            porttype.setPortType(portTypeName);
            ArrayList<Operation> operationArray = new ArrayList<>();
            Iterator iterator = portTypeJson.keys();
            while(iterator.hasNext()){
                String portTypeKey = (String) iterator.next();
                PortType portType = new PortType();
                portType.setPortType(portTypeKey);
                JSONObject operationsJson = portTypeJson.getJSONObject(portTypeKey);
                Iterator operationIter = operationsJson.keys();
                while(operationIter.hasNext()) {
                    String operationKey = (String) operationIter.next();
                    Operation operation = new Operation();
                    operation.setOperation(operationKey);
                    JSONObject operationJson = operationsJson.getJSONObject(operationKey);
                    String inputMessage = operationJson.getString("input");
                    String outputMessage = operationJson.getString("output");
                    //从portType 中提取inputmessage 如：<wsdl:input message="tns:GetAllCurrenciesSoapIn" />
                    ArrayList<Parameter> inputElements = retriveElements(inputMessage);
                    operation.setParamInput(inputElements);
                    //从portType 中提取outputmessage 如：<wsdl:output message="tns:GetAllCurrenciesSoapOut" />
                    ArrayList<Parameter> outputElements = retriveElements(outputMessage);

                    operation.setParamOutput(outputElements);
                    operationArray.add(operation);

                }

            }
            porttype.setOperations(operationArray);
            porttypes.add(porttype);
        }
        return porttypes;
    }
    //根据inputmessage 和 outputmessage 获得参数列表和对应数据属性
    private static ArrayList<Parameter> retriveElements(String name){
        JSONArray messagesJson = JsonObj.getJSONArray("message");
//        System.out.println(name);
        ArrayList<Parameter> parameterArray = new ArrayList<>();
        //message Type <name element>
        if(messElementMap.containsKey(name)) {
            int elementKey = messElementMap.get(name);
            JSONObject messageJson = messagesJson.getJSONObject(elementKey);  //获取message里对应的JSON
            String element = trimNamespace(messageJson.getString("element"));
            //根据element从 <Types> 中获取参数列表
            JSONObject typesJson = JsonObj.getJSONObject("types");
            if(typesJson.has(element)) {
                JSONObject typeJson = typesJson.getJSONObject(element);
                JSONArray sequenceArray = typeJson.getJSONArray("sequence");
                for(int i=0;i<sequenceArray.length();i++) {
                    JSONObject paramJson = sequenceArray.getJSONObject(i);
                    Parameter param = setPrameterFromJson(paramJson);
                    parameterArray.add(param);
                }
            }
            else {
                //TODO: 有些output中的element是数据类型，不属于types中的element，所以在typesJson中获取不到
            }
            return parameterArray;
        }
        /*** message Type <message name=...>
         *                  <part name=... type=...>
         *                  <part name=... type=...>
         *                </message>
         ***/
        else if(operElementMap.containsKey(name)) {
            int elementKey = operElementMap.get(name);
            //message 中直接获取参数列表
            JSONObject messageJson = messagesJson.getJSONObject(elementKey);
            JSONArray operationArray = messageJson.getJSONArray(name);
//            System.out.println(operationArray.toString());
            for(int i =0;i<operationArray.length();i++) {
                JSONObject operJson = operationArray.getJSONObject(i);
                Parameter param = setPrameterFromJson(operJson);
                parameterArray.add(param);
            }
            return parameterArray;
        }
        else {
            System.out.println("unknown message kind");
            return null;
        }
    }

    //	输入参数为 <sequence> 下的<element>标签 或 <message> 下的<part> 标签
    private static Parameter setPrameterFromJson(JSONObject operationJson) {

        Parameter param = new Parameter();
        if(operationJson.has("minOccurs")) {
            param.setMinOccurs(Integer.parseInt(operationJson.getString("minOccurs")));
        }if(operationJson.has("maxOccurs")) {
            if(operationJson.getString("maxOccurs").equals("unbounded")) {
                param.setMaxOccurs(-1);
            }else {
                param.setMaxOccurs(Integer.parseInt(operationJson.getString("maxOccurs")));
            }
        }if(operationJson.has("name")) {
            param.setName(operationJson.getString("name"));
        }if(operationJson.has("type")) {
            String typeName = trimNamespace(operationJson.getString("type"));
            //复杂类型
            if(typeName.equals("self-defined")) {

                param.setType(typeName);
                if (operationJson.has("complexType")){
                    JSONObject complexJson = operationJson.getJSONObject("complexType");
                    String keyOfComplexJson = complexJson.keys().next();
                    JSONArray complexElements = complexJson.getJSONArray(keyOfComplexJson);
                    ArrayList<Parameter> subParams = new ArrayList<>();
                    for (int i=0;i<complexElements.length();i++) {
                        Parameter subParam = setPrameterFromJson(complexElements.getJSONObject(i));
                        subParams.add(subParam);
                    }
                    param.setTypes(subParams);
                }
                else if(operationJson.has("simpleType")){
                    JSONObject simpleJson = operationJson.getJSONObject("simpleType");
                    String keyOfSimpleJson = simpleJson.keys().next();
                    JSONObject simpleElement = simpleJson.getJSONArray(keyOfSimpleJson).getJSONObject(0);
                    simpleType simpleType = new simpleType();
                    if (simpleElement.has("name")){
                        simpleType.setName(simpleElement.getString("name"));
                    }
                    if (simpleElement.has("base")){
                        simpleType.setBase(simpleElement.getString("base"));
                    }
                    if (simpleElement.has("enumeration")){
                        JSONArray enums = simpleElement.getJSONArray("enumeration");
                        List<String> enumString = new ArrayList<>();
                        for (int i = 0; i < enums.length(); i++) {
                            String e = (String) enums.get(i);
                            enumString.add(e);
                        }
                        simpleType.setEnumeration(enumString);
                    }if (simpleElement.has("pattern")){
                        simpleType.setPattern(simpleElement.getString("pattern"));
                    }
                    if (simpleElement.has("whiteSpace")){
                        simpleType.setWhiteSpace(simpleElement.getString("whiteSpace"));
                    }if (simpleElement.has("length")){
                        simpleType.setLength(simpleElement.getString("length"));
                    }
                    if (simpleElement.has("minLength")){
                        simpleType.setMinLength(simpleElement.getString("minLength"));
                    }
                    if (simpleElement.has("maxLength")){
                        simpleType.setMaxLength(simpleElement.getString("maxLength"));
                    }
                    param.setSimpleType(simpleType);
                }
            }else {
                param.setType(typeName);
            }
        }if(operationJson.has("order_of_seq")){
            param.setOrder_of_seq(operationJson.getInt("order_of_seq"));
        }

        return param;
    }


}
