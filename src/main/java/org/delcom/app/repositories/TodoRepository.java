package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, UUID> {
    @Query("SELECT t FROM Todo t WHERE (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND t.userId = :userId")
    List<Todo> findByKeyword(UUID userId, String keyword);

    @Query("SELECT t FROM Todo t WHERE t.userId = :userId")
    List<Todo> findAllByUserId(UUID userId);

    @Query("SELECT t FROM Todo t WHERE t.id = :id AND t.userId = :userId")
    Optional<Todo> findByUserIdAndId(UUID userId, UUID id);
}