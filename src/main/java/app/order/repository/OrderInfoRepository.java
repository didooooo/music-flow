package app.order.repository;

import app.order.model.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, UUID> {
}
