package com.xa.springboot01cache.mapper;

import com.xa.springboot01cache.bean.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DepartmentMapper {
    @Select("SELECT * FROM department WHERE id = #{id}")
    Department getDeptById(Integer id);
}
