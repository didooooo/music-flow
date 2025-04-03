package app.shopping_cart;

import app.shopping_cart.model.ShoppingCart;
import app.shopping_cart.repository.ShoppingCartRepository;
import app.shopping_cart.service.ShoppingCartService;
import app.user.model.User;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceUTest {
    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private ShoppingCartService shoppingCartService;
    @Test
    void givenUser_whenCreateShoppingCartForUser_thenReturnShoppingCart() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();

        ShoppingCart expectedShoppingCart = ShoppingCart.builder()
                .user(user)
                .shoppingCartInfos(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();

        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(expectedShoppingCart);

        // When
        ShoppingCart createdShoppingCart = shoppingCartService.createShoppingCartForUser(user);

        // Then
        assertNotNull(createdShoppingCart);
        assertEquals(userId, createdShoppingCart.getUser().getId());
        assertEquals(BigDecimal.ZERO, createdShoppingCart.getTotalPrice());
        assertEquals(0, createdShoppingCart.getTotalQuantity());
        assertTrue(createdShoppingCart.getShoppingCartInfos().isEmpty());

        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
    }

}
