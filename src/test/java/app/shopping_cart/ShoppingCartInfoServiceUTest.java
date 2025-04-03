package app.shopping_cart;

import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.model.ShoppingCartInfo;
import app.shopping_cart.repository.ShoppingCartInfoRepository;
import app.shopping_cart.service.ShoppingCartInfoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import app.record.model.Record;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartInfoServiceUTest {
    @Mock
    private ShoppingCartInfoRepository shoppingCartInfoRepository;

    @InjectMocks
    private ShoppingCartInfoService shoppingCartInfoService;

    @Test
    void givenRecordAndShoppingCart_whenAddNewProductInShoppingCart_thenUpdateCartAndSaveInfo() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("15.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(BigDecimal.ZERO)
                .totalQuantity(0)
                .shoppingCartInfos(new ArrayList<>())
                .build();

        ShoppingCartInfo cartInfo = ShoppingCartInfo.builder()
                .quantity(1)
                .record(record)
                .shoppingCart(shoppingCart)
                .build();


        when(shoppingCartInfoRepository.save(any(ShoppingCartInfo.class))).thenReturn(cartInfo);

        // When
        ShoppingCartInfo result = shoppingCartInfoService.addNewProductInShoppingCart(record, shoppingCart);
        result.getShoppingCart().getShoppingCartInfos().add(result);
        // Then
        assertNotNull(result);
        assertEquals(1, shoppingCart.getTotalQuantity());
        assertEquals(new BigDecimal("15.00"), shoppingCart.getTotalPrice());
        assertEquals(1, shoppingCart.getShoppingCartInfos().size());
        assertEquals(record.getId(), result.getRecord().getId());

        verify(shoppingCartInfoRepository, times(1)).save(any(ShoppingCartInfo.class));
    }
    @Test
    void givenRecordInCart_whenDecreaseQuantity_thenUpdateCart() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(new BigDecimal("20.00"))
                .totalQuantity(2)
                .shoppingCartInfos(new ArrayList<>())
                .build();

        ShoppingCartInfo shoppingCartInfo = ShoppingCartInfo.builder()
                .quantity(2)
                .record(record)
                .shoppingCart(shoppingCart)
                .build();

        shoppingCart.getShoppingCartInfos().add(shoppingCartInfo);

        when(shoppingCartInfoRepository.save(any())).thenReturn(shoppingCartInfo);

        // When
        shoppingCartInfoService.decreaseQuantityOfProduct(record, shoppingCart);

        // Then
        assertEquals(1, shoppingCartInfo.getQuantity());
        assertEquals(new BigDecimal("10.00"), shoppingCart.getTotalPrice());
        assertEquals(1, shoppingCart.getTotalQuantity());

        verify(shoppingCartInfoRepository, times(1)).save(any());
        verify(shoppingCartInfoRepository, never()).delete(any());
    }

    @Test
    void givenRecordWithOneQuantity_whenDecreaseQuantity_thenRemoveFromCart() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(new BigDecimal("10.00"))
                .totalQuantity(1)
                .shoppingCartInfos(new ArrayList<>())
                .build();

        ShoppingCartInfo shoppingCartInfo = ShoppingCartInfo.builder()
                .quantity(1)
                .record(record)
                .shoppingCart(shoppingCart)
                .build();

        shoppingCart.getShoppingCartInfos().add(shoppingCartInfo);

        when(shoppingCartInfoRepository.save(any())).thenReturn(shoppingCartInfo);

        // When
        shoppingCartInfoService.decreaseQuantityOfProduct(record, shoppingCart);

        // Then
        assertEquals(0, shoppingCart.getShoppingCartInfos().size());
        assertEquals(new BigDecimal("0.00"), shoppingCart.getTotalPrice());
        assertEquals(0, shoppingCart.getTotalQuantity());

        verify(shoppingCartInfoRepository, times(1)).delete(any());
    }
    @Test
    void givenRecordInCart_whenIncreaseQuantity_thenUpdateCart() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("15.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(new BigDecimal("30.00"))
                .totalQuantity(2)
                .shoppingCartInfos(new ArrayList<>())
                .build();

        ShoppingCartInfo shoppingCartInfo = ShoppingCartInfo.builder()
                .quantity(2)
                .record(record)
                .shoppingCart(shoppingCart)
                .build();

        shoppingCart.getShoppingCartInfos().add(shoppingCartInfo);

        when(shoppingCartInfoRepository.save(any())).thenReturn(shoppingCartInfo);

        // When
        shoppingCartInfoService.increaseQuantityOfProduct(record, shoppingCart);

        // Then
        assertEquals(3, shoppingCartInfo.getQuantity());
        assertEquals(new BigDecimal("45.00"), shoppingCart.getTotalPrice());
        assertEquals(3, shoppingCart.getTotalQuantity());

        verify(shoppingCartInfoRepository, times(1)).save(any());
    }

    @Test
    void givenRecordNotInCart_whenIncreaseQuantity_thenDoNothing() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("15.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(new BigDecimal("30.00"))
                .totalQuantity(2)
                .shoppingCartInfos(new ArrayList<>())
                .build();

        // When
        shoppingCartInfoService.increaseQuantityOfProduct(record, shoppingCart);

        // Then
        assertEquals(0, shoppingCart.getShoppingCartInfos().size());
        assertEquals(new BigDecimal("30.00"), shoppingCart.getTotalPrice());
        assertEquals(2, shoppingCart.getTotalQuantity());

        verify(shoppingCartInfoRepository, never()).save(any());
    }
    @Test
    void givenProductInCart_whenDeleteProduct_thenRemoveProductAndUpdateCart() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(new BigDecimal("30.00"))
                .totalQuantity(3)
                .shoppingCartInfos(new ArrayList<>())
                .build();

        ShoppingCartInfo shoppingCartInfo = ShoppingCartInfo.builder()
                .quantity(2)
                .record(record)
                .shoppingCart(shoppingCart)
                .build();

        shoppingCart.getShoppingCartInfos().add(shoppingCartInfo);

        // When
        shoppingCartInfoService.deleteProduct(record, shoppingCart);

        // Then
        assertEquals(0, shoppingCart.getShoppingCartInfos().size());
        assertEquals(new BigDecimal("10.00"), shoppingCart.getTotalPrice());
        assertEquals(1, shoppingCart.getTotalQuantity());

        verify(shoppingCartInfoRepository, times(1)).delete(any()); // проверка дали е извикан delete
    }

    @Test
    void givenProductNotInCart_whenDeleteProduct_thenDoNothing() {
        // Given
        Record record = Record.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.00"))
                .build();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .totalPrice(new BigDecimal("30.00"))
                .totalQuantity(3)
                .shoppingCartInfos(new ArrayList<>()) // празен списък
                .build();

        // When
        shoppingCartInfoService.deleteProduct(record, shoppingCart);

        // Then
        assertEquals(0, shoppingCart.getShoppingCartInfos().size());
        assertEquals(new BigDecimal("30.00"), shoppingCart.getTotalPrice());
        assertEquals(3, shoppingCart.getTotalQuantity());

        verify(shoppingCartInfoRepository, never()).delete(any());
    }



}
