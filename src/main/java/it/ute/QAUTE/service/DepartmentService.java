package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    Page<Department> searchNameDepartment(String keyword, Pageable pageable);

    List<Department> findAll();

    Department findById(Integer id);

    List<Department> findAllNoPaging();

    void updateDepartment(Department department);

    void deleteDepartment(Integer id);

    List<Department> findAllById(List<Integer> departmentIds);
}
