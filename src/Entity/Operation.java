package Entity;

import java.util.ArrayList;
import java.util.Arrays;

public class Operation {
	private String operation;
	private ArrayList<Parameter> paramInput;
	private ArrayList<Parameter> paramOutput;
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public ArrayList<Parameter> getParamInput() {
		return paramInput;
	}
	public void setParamInput(ArrayList<Parameter> paramInput) {
		this.paramInput = paramInput;
	}
	public ArrayList<Parameter> getParamOutput() {
		return paramOutput;
	}
	public void setParamOutput(ArrayList<Parameter> paramOutput) {
		this.paramOutput = paramOutput;
	}

	@Override
	public String toString() {
		return "Operation{" +
				"operation='" + operation + '\'' +
				", paramInput=" + paramInput +
				", paramOutput=" + paramOutput +
				'}';
	}
}
