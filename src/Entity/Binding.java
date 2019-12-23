package Entity;

import java.util.ArrayList;

public class Binding {
	private String name;

	public PortType getPortType() {
		return portType;
	}

	public void setPortType(PortType portType) {
		this.portType = portType;
	}

	@Override
	public String toString() {
		return "Binding{" +
				"name='" + name + '\'' +
				", portType=" + portType +
				", namespace='" + namespace + '\'' +
				", bindingType='" + bindingType + '\'' +
				", operations=" + operations +
				'}';
	}

	private PortType portType;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ArrayList<Operation> getOperations() {
		return operations;
	}

	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	private String namespace;

	private String bindingType;

	public String getBindingType() {
		return bindingType;
	}

	public void setBindingType(String bindingType) {
		this.bindingType = bindingType;
	}

	private ArrayList<Operation> operations;

}
