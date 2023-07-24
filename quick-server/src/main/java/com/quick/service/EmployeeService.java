package com.quick.service;

import com.quick.dto.EmployeeLoginDTO;
import com.quick.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

}
