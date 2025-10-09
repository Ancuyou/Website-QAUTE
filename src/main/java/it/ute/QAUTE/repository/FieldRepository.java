package it.ute.QAUTE.repository;

import it.ute.QAUTE.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {
}