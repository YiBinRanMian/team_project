package Entity;

import java.util.ArrayList;

public class Service {
    private String name;

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    private String targetNamespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<port> getPorts() {
        return ports;
    }

    public void setPorts(ArrayList<port> ports) {
        this.ports = ports;
    }

    public static class port{

        private String address;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

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

        public String getBinding() {
            return binding;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        private String name;


        private String namespace;
        private String binding;

        @Override
        public String toString() {
            return "port{" +
                    "address='" + address + '\'' +
                    ", name='" + name + '\'' +
                    ", namespace='" + namespace + '\'' +
                    ", binding='" + binding + '\'' +
                    ", bindingObj=" + bindingObj +
                    '}';
        }

        public Binding getBindingObj() {
            return bindingObj;
        }

        public void setBindingObj(Binding bindingObj) {
            this.bindingObj = bindingObj;
        }

        private Binding bindingObj;

    }
    private ArrayList<port> ports;

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                ", targetNamespace='" + targetNamespace + '\'' +
                ", ports=" + ports +
                '}';
    }


}
