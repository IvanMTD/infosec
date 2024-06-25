package net.security.infosec.utils;

public class Checker {
    public static boolean isInteger(String data){
        try{
            Integer.parseInt(data);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }
}
