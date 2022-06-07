package woowacourse.shoppingcart.dto;

import woowacourse.shoppingcart.domain.Cart;

public class CartItemResponse {
    private final Long id;
    private final String name;
    private final int price;
    private final int quantity;
    private final String imageUrl;

    public CartItemResponse(Long id, String name, int price, int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }
}
