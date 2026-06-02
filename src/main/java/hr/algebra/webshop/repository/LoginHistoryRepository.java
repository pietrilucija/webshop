package hr.algebra.webshop.repository;

import hr.algebra.webshop.model.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    Page<LoginHistory> findAllByOrderByLoginAtDesc(Pageable pageable);
}