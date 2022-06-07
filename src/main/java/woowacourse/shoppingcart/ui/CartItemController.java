package woowacourse.shoppingcart.ui;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import woowacourse.auth.support.AuthenticationPrincipal;
import woowacourse.shoppingcart.domain.Cart;
import woowacourse.shoppingcart.domain.Product;
import woowacourse.shoppingcart.dto.CartItemResponse;
import woowacourse.shoppingcart.dto.LoginCustomer;
import woowacourse.shoppingcart.dto.Request;
import woowacourse.shoppingcart.application.CartService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CartItemController {
    private final CartService cartService;

    public CartItemController(final CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartItemResponse>> showCartItems(@AuthenticationPrincipal LoginCustomer loginCustomer) {
        List<CartItemResponse> cartItemResponses = cartService.showCartItems(loginCustomer);
        return ResponseEntity.ok().body(cartItemResponses);
    }

    @PostMapping("/carts/products/{productId}")
    public ResponseEntity<Void> addCartItem(@AuthenticationPrincipal LoginCustomer loginCustomer, @PathVariable Long productId) {
        cartService.addCart(loginCustomer, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/customers/{customerName}/carts/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable final String customerName,
                                         @PathVariable final Long cartId) {
        cartService.deleteCart(customerName, cartId);
        return ResponseEntity.noContent().build();
    }
}
