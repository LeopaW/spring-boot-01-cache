package com.xa.springboot01cache.service;

import com.xa.springboot01cache.bean.Department;
import com.xa.springboot01cache.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DeptService {

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    CacheManager cacheManager;

//    @Cacheable(cacheNames = "dept")
//    public Department getDeptById(Integer id){
//        System.out.println("查询部门"+id);
//        Department department = departmentMapper.getDeptById(id);
//        return department;
//    }
    @Cacheable(cacheNames = "dept")
    public Department getDeptById(Integer id){
        System.out.println("查询部门"+id);
        Department department = departmentMapper.getDeptById(id);
        Cache dept = cacheManager.getCache("dept");
        dept.put("dept:1",department);
        return department;
    }
}
