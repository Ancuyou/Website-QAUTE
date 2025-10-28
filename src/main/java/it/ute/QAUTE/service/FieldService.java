package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Field;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FieldService {
    Page<Field> searchField(Integer departmentId, String keyword, Pageable pageable);

    Field getFieldById(int fieldId);

    List<Field> getAllFields();

    long countAll();
}
