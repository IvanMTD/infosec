package net.security.infosec.utils;

public class NamingUtil {
    private static NamingUtil instance = null;

    private String refreshName;
    private String accessName;

    public NamingUtil() {
        this.refreshName = refreshName = "info-sec-refresh-token";
        this.accessName = accessName = "info-sec-access-token";
    }

    public static NamingUtil getInstance(){
        if(instance == null){
            instance = new NamingUtil();
        }
        return instance;
    }

    public String getRefreshName() {
        return refreshName;
    }

    public String getAccessName() {
        return accessName;
    }
}
