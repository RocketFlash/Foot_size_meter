package com.tryfit.common.db.models;

import java.util.List;

/**
 * Created by alexeyreznik on 12/09/2017.
 */

public class Product {

    private String barcode;
    private String code;
    private String contractorID;
    private String groupID;
    private String id;
    private String name;
    private List<String> pictures;
    private float price;
    private int sex;
    private List<Size> sizes;

    @Override
    public String toString() {
        return "Product{" +
                "barcode='" + barcode + '\'' +
                ", code='" + code + '\'' +
                ", contractorID='" + contractorID + '\'' +
                ", groupID='" + groupID + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", pictures='" + pictures + '\'' +
                ", price=" + price +
                ", sex=" + sex +
                ", size=s" + sizes +
                '}';
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContractorID() {
        return contractorID;
    }

    public void setContractorID(String contractorID) {
        this.contractorID = contractorID;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public List<Size> getSizes() {
        return sizes;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
    }
}
