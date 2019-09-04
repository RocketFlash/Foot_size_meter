package com.tryfit.common.db.models;

/**
 * Created by alexeyreznik on 19/09/2017.
 */

public class FittingItem {
    private Product product;
    private Size size;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }
}
