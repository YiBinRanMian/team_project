package Entity;

import java.util.ArrayList;

public class PortType {
	private String portType;
	private ArrayList<Operation> operations;
	public String getPortType() {
		return portType;
	}
	public void setPortType(String portType) {
		this.portType = portType;
	}
	public ArrayList<Operation> getOperations() {
		return operations;
	}
	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	@Override
	public String toString() {
		return "PortType{" +
				"portType='" + portType + '\'' +
				", operations=" + operations +
				'}';
	}
}
