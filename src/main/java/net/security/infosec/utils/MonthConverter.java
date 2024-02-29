package net.security.infosec.utils;

import java.time.Month;

public class MonthConverter {
    public static String convert(Month month){
        switch (month){
            case JANUARY -> {
                return "Январь";
            }
            case FEBRUARY -> {
                return "Февраль";
            }
            case MARCH -> {
                return "Март";
            }
            case APRIL -> {
                return "Апрель";
            }
            case MAY -> {
                return "Май";
            }
            case JUNE -> {
                return "Июнь";
            }
            case JULY -> {
                return "Июль";
            }
            case AUGUST -> {
                return "Август";
            }
            case SEPTEMBER -> {
                return "Сентябрь";
            }
            case OCTOBER -> {
                return "Октябрь";
            }
            case NOVEMBER -> {
                return "Ноябрь";
            }
            case DECEMBER -> {
                return "Декабрь";
            }
            default -> {
                return "Месяца";
            }
        }
    }
}
