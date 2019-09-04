package com.tryfit.common.db.models;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by alexeyreznik on 19/06/2017.
 */

public class Client extends RealmObject {

    @PrimaryKey
    private String id;
    private String contractorID;
    private String loyaltyCard;
    private String shoeSize;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private boolean sendEmail;
    private boolean sendSms;
    private double weight;
    private double height;
    private int sex;

    private @Nullable
    Scan scan;

    private @Nullable
    Scan scan2D;

    public Client() {
        id = "";
        contractorID = "";
        loyaltyCard = "";
        shoeSize = "";
        name = "";
        surname = "";
        email = "";
        phone = "";
        sendEmail = false;
        sendSms = false;
        weight = 0.;
        height = 0.;
        sex = 1;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("contractorID", contractorID);
        map.put("loyaltyCard", loyaltyCard);
        map.put("shoeSize", shoeSize);
        map.put("name", name);
        map.put("surname", surname);
        map.put("email", email);
        map.put("phone", phone);
        map.put("sendEmail", sendEmail);
        map.put("sendSMS", sendSms);
        map.put("weight", weight);
        map.put("height", height);
        map.put("sex", sex);
        return map;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", contractorID='" + contractorID + '\'' +
                ", loyaltyCard='" + loyaltyCard + '\'' +
                ", shoeSize='" + shoeSize + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", sendEmail=" + sendEmail +
                ", sendSms=" + sendSms +
                ", weight=" + weight +
                ", height=" + height +
                ", sex=" + sex +
                ", scan=" + scan +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContractorID() {
        return contractorID;
    }

    public void setContractorID(String contractorID) {
        this.contractorID = contractorID;
    }

    public String getLoyaltyCard() {
        return loyaltyCard;
    }

    public void setLoyaltyCard(String loyaltyCard) {
        this.loyaltyCard = loyaltyCard;
    }

    public String getShoeSize() {
        return shoeSize;
    }

    public void setShoeSize(String shoeSize) {
        this.shoeSize = shoeSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isSendSms() {
        return sendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Nullable
    public Scan getScan() {
        return scan;
    }

    public void setScan(@Nullable Scan scan) {
        this.scan = scan;
    }

    @Nullable
    public Scan getScan2D() {
        return scan2D;
    }

    public void setScan2D(@Nullable Scan scan2D) {
        this.scan2D = scan2D;
    }
}
