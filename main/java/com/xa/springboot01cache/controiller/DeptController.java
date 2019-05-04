package com.xa.springboot01cache.controiller;

import com.xa.springboot01cache.bean.Department;
import com.xa.springboot01cache.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeptController {

    @Autowired
    DeptService deptService;

    /**
     * 缓存的数据能存入redis
     * 第二次从缓存中查询就不能反序列化回来,
     * 我们存的是dept的json数据,而CacheManager默认使用RedisTemplate<Object,Employee>
     * @param id
     * @return
     */
    @GetMapping("/dept/{id}")
    public Department getDept(@PathVariable("id") Integer id){
        Department deptById = deptService.getDeptById(id);
        return deptById;
    }
}
