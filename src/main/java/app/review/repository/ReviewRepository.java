package app.review.repository;

import app.review.model.Review;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
@Registered
public interface ReviewRepository extends JpaRepository<Review, UUID> {
}
