package app.record.repository;

import app.record.model.Format;
import app.record.model.Genre;
import app.record.model.Record;
import app.record.model.Type;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecordRepository extends JpaRepository<Record, UUID> {
    Page<Record> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Record> findAll(Specification<Record> spec, Pageable pageable);

}
