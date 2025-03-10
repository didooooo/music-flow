package app.shopping_cart.repository;

import app.shopping_cart.model.ShoppingCartInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ShoppingCartInfoRepository extends JpaRepository<ShoppingCartInfo, UUID> {
}
