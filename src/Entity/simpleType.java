package Entity;

import java.util.Arrays;
import java.util.List;

public class simpleType {
    private String name;

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }
    private String base;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    private List<String> enumeration;
    private String pattern;

    private String whiteSpace;
    private String length;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "simpleType{" +
                "name='" + name + '\'' +
                ", base='" + base + '\'' +
                ", enumeration=" + enumeration +
                ", pattern='" + pattern + '\'' +
                ", whiteSpace='" + whiteSpace + '\'' +
                ", length='" + length + '\'' +
                ", minLength='" + minLength + '\'' +
                ", maxLength='" + maxLength + '\'' +
                '}';
    }

    public String getWhiteSpace() {
        return whiteSpace;
    }

    public void setWhiteSpace(String whiteSpace) {
        this.whiteSpace = whiteSpace;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getMinLength() {
        return minLength;
    }

    public void setMinLength(String minLength) {
        this.minLength = minLength;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    private String minLength;
    private String maxLength;
}
