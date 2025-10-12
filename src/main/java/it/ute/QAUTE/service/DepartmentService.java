package it.ute.QAUTE.service;

import it.ute.QAUTE.entity.Department;
import it.ute.QAUTE.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    @Autowired
    DepartmentRepository departmentRepository;
    public Page<Department> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    public Page<Department> searchNameDepartment(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.equals("")) {
            return departmentRepository.findByNameDepartment(keyword, pageable);
        } else {
            return departmentRepository.findAll(pageable);
        }
    }

    public Department findById(Integer id){
        return departmentRepository.findById(id).orElse(null);
    }

    public List<Department> findAllNoPaging(){
        return departmentRepository.findByType(Department.DepartmentType.Faculty);
    }

    public void updateDepartment(Department department) {
        Department saved = departmentRepository.save(department);

        if (saved.getParent() == null) {
            saved.setParent(saved);
            departmentRepository.save(saved);
        }
    }

    public void deleteDepartment(Integer id) {
        departmentRepository.deleteById(id);
    }
}
