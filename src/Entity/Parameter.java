package Entity;

import java.util.ArrayList;
import java.util.Arrays;

public class Parameter {
	

	public ArrayList<Parameter> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<Parameter> types) {
		this.types = types;
	}
	public int getMinOccurs() {
		return minOccurs;
	}
	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs;
	}
	public int getMaxOccurs() {
		return maxOccurs;
	}
	public void setMaxOccurs(int maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	private int minOccurs = 1;
	private int maxOccurs = 1;
	private String name;
	private String type;
	private String value;

	public int getOrder_of_seq() {
		return order_of_seq;
	}

	public void setOrder_of_seq(int order_of_seq) {
		this.order_of_seq = order_of_seq;
	}

	private int order_of_seq=1;
	private ArrayList<Parameter> types;
	private simpleType simpleType;

	@Override
	public String toString() {
		return "Parameter{" +
				"minOccurs=" + minOccurs +
				", maxOccurs=" + maxOccurs +
				", name='" + name + '\'' +
				", type='" + type + '\'' +
				", value='" + value + '\'' +
				", order_of_seq=" + order_of_seq +
				", types=" + types +
				", simpleType=" + simpleType +
				'}';
	}

	public Entity.simpleType getSimpleType() {
		return simpleType;
	}

	public void setSimpleType(Entity.simpleType simpleType) {
		this.simpleType = simpleType;
	}
}
