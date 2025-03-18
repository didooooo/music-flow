package app.shopping_cart.service;

import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.user.model.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
public class ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository) {
        this.shoppingCartRepository = shoppingCartRepository;
    }

    public ShoppingCart createShoppingCartForUser(User user) {
        ShoppingCart build = ShoppingCart.builder()
                .user(user)
                .shoppingCartInfos(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();
        return shoppingCartRepository.save(build);
    }
}
