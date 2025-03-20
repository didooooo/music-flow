package app.shopping_cart.service;

import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.repository.ShoppingCartInfoRepository;
import app.user.model.User;
import app.record.model.Record;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

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

    public void decreaseQuantityOfProduct(Record record, ShoppingCart shoppingCart) {
        for (int i = 0; i < shoppingCart.getShoppingCartInfos().size(); i++) {
            ShoppingCartInfo shoppingCartInfo = shoppingCart.getShoppingCartInfos().get(i);
            if (shoppingCartInfo.getRecord().equals(record)) {
                shoppingCartInfo.setQuantity(shoppingCartInfo.getQuantity() - 1);
                shoppingCartInfo.getShoppingCart().setTotalPrice(shoppingCartInfo.getShoppingCart().getTotalPrice().subtract(shoppingCartInfo.getRecord().getPrice()));
                shoppingCartInfo.getShoppingCart().setTotalQuantity(shoppingCartInfo.getShoppingCart().getTotalQuantity() - 1);
                shoppingCartInfoRepository.save(shoppingCartInfo);
                if (shoppingCartInfo.getQuantity() == 0) {
                    shoppingCart.getShoppingCartInfos().remove(i);
                    shoppingCartInfoRepository.delete(shoppingCartInfo);
                }
                return;
            }
        }
    }

    public void increaseQuantityOfProduct(Record record, ShoppingCart shoppingCart) {
        for (int i = 0; i < shoppingCart.getShoppingCartInfos().size(); i++) {
            ShoppingCartInfo shoppingCartInfo = shoppingCart.getShoppingCartInfos().get(i);
            if (shoppingCartInfo.getRecord().equals(record)) {
                shoppingCartInfo.setQuantity(shoppingCartInfo.getQuantity() + 1);
                shoppingCartInfo.getShoppingCart().setTotalPrice(shoppingCartInfo.getShoppingCart().getTotalPrice().add(shoppingCartInfo.getRecord().getPrice()));
                shoppingCartInfo.getShoppingCart().setTotalQuantity(shoppingCartInfo.getShoppingCart().getTotalQuantity() + 1);
                shoppingCartInfoRepository.save(shoppingCartInfo);
                return;
            }
        }
    }

    public void deleteProduct(Record record, ShoppingCart shoppingCart) {
        for (int i = 0; i < shoppingCart.getShoppingCartInfos().size(); i++) {
            ShoppingCartInfo shoppingCartInfo = shoppingCart.getShoppingCartInfos().get(i);
            if (shoppingCartInfo.getRecord().equals(record)) {
                shoppingCartInfo.getShoppingCart().setTotalPrice(shoppingCartInfo.getShoppingCart().getTotalPrice().subtract(shoppingCartInfo.getRecord().getPrice().multiply(BigDecimal.valueOf(shoppingCartInfo.getQuantity()))));
                shoppingCartInfo.getShoppingCart().setTotalQuantity(shoppingCartInfo.getShoppingCart().getTotalQuantity() - shoppingCartInfo.getQuantity());
                shoppingCart.getShoppingCartInfos().remove(i);
                shoppingCartInfoRepository.delete(shoppingCartInfo);
                return;
            }
        }
    }
}
