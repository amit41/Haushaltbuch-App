package com.example.amit.haushaltsbuchapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Utils {

    // convert String to Date
    public static Date convertStringToDate(String dateInString){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = df.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // convert Date to String
    public static String convertDateToString(Date date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateInString = df.format(date);
        return dateInString;
    }

    // convert the date in German date format
    public static String dateInGermanFormat(Date date) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyy");
        String dateInString = df.format(date);
        return dateInString;
    }

    // convert the string date in German date format
    public static Date dateFromGermanFormat(String dateInString)
    {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        Date date = null;
        try {
            date = df.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // returns the 3 characters of Month with year
    public static String getMonthlyTranstion(Calendar cal)
    {
        DateFormat df = new SimpleDateFormat("MMM yyyy");
        String monthlyTransaction = df.format(cal.getTime());
        return monthlyTransaction;
    }

    // remove the zero from decimal such as 2.00 is returned as 2.
    public static String truncZero(double value)
    {
        String truncValue;
        if(value%1 == 0)
        {
            truncValue = String.valueOf((int)value);
        }
        else {
            truncValue = String.format("%.2f",(double) value);
        }
        return truncValue;
    }
}
