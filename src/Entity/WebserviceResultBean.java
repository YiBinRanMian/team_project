package Entity;

public class WebserviceResultBean {
    private String result;
    private String remark;
    private String xmlData;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "WebserviceResultBean{" +
                "result='" + result + '\'' +
                ", remark='" + remark + '\'' +
                ", xmlData='" + xmlData + '\'' +
                '}';
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getXmlData() {
        return xmlData;
    }

    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }
}
