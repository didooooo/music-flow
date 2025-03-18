package app.shopping_cart.service;

import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.repository.ShoppingCartInfoRepository;
import app.user.model.User;
import app.record.model.Record;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartInfoService {
    private final ShoppingCartInfoRepository shoppingCartInfoRepository;

    public ShoppingCartInfoService(ShoppingCartInfoRepository shoppingCartInfoRepository) {
        this.shoppingCartInfoRepository = shoppingCartInfoRepository;
    }

    public ShoppingCartInfo addNewProductInShoppingCart(Record record, ShoppingCart shoppingCart) {
        shoppingCart.setTotalPrice(shoppingCart.getTotalPrice().add(record.getPrice()));
        shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity() + 1);
        ShoppingCartInfo cartInfo = ShoppingCartInfo.builder()
                .quantity(1)
                .record(record)
                .shoppingCart(shoppingCart)
                .build();
        return shoppingCartInfoRepository.save(cartInfo);
    }
}
