package com.quick.service;

import com.quick.dto.EmployeeDTO;
import com.quick.dto.EmployeeLoginDTO;
import com.quick.dto.EmployeePageQueryDTO;
import com.quick.entity.Employee;
import com.quick.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询
     * @param employeePageQueryDTO
     * @return
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);
}
