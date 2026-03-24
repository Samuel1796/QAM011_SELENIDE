package org.example.pages;

import static com.codeborne.selenide.Selenide.$;

public class ProductDetailPage {

    public String getProductName() {
        return $(".inventory_details_name").getText();
    }

    public String getPrice() {
        return $(".inventory_details_price").getText();
    }

    public String getDescription() {
        return $(".inventory_details_desc").getText();
    }

    public void addToCart() {
        $("[data-test='add-to-cart']").click();
    }

    public void backToProducts() {
        $("#back-to-products").click();
    }
}
