package com.projects.mara.msm_caller;

/**
 * Created by Yashasvi on 02-06-2017.
 */

public class logdetails {

    String name;
    String phone_no;
    String callTime;
    String date;
    String balance;

    public void setName(String name){
        this.name=name;
    }
    public void setCallTime(String time){
        this.callTime=time;
    }
    public void setDate(String date){
        this.date=date;
    }
    public void setBalance(String balance){
        this.balance = balance;
    }
    public void setPhone(String phone_no){ this.phone_no = phone_no; }

    public String getName() {
        return name;
    }
    public String getCallTime() {
        return callTime;
    }
    public String getDate(){
        return date;
    }
    public String getBalance() {
        return balance;
    }

    public String getPhone_no() {
        return phone_no;
    }
}
