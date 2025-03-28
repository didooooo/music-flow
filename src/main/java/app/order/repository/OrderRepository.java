package app.order.repository;

import app.order.model.Order;
import app.order.model.OrderStatus;
import app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findTop3ByOrderByCreatedAtDesc();

    List<Order> findAllByStatus(OrderStatus status);

    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    List<Order> findAllByCreatedAtBetween(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}
