package org.aggregateframework.discovery.registry.nacos;

/**
 * @author Nervose.Wu
 * @date 2022/5/12 18:31
 */

public class NacosRegistryProperties {
    private String serverAddr = "127.0.0.1:8848";
    private String namespace = "public";
    private String group = "AGG_GROUP";
    private String username = "nacos";
    private String password = "nacos";
    private String serviceNameForClient = "agg-server-for-client";
    private String serviceNameForDashboard = "agg-server-for-dashboard";

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceNameForClient() {
        return serviceNameForClient;
    }

    public void setServiceNameForClient(String serviceNameForClient) {
        this.serviceNameForClient = serviceNameForClient;
    }

    public String getServiceNameForDashboard() {
        return serviceNameForDashboard;
    }

    public void setServiceNameForDashboard(String serviceNameForDashboard) {
        this.serviceNameForDashboard = serviceNameForDashboard;
    }
}
