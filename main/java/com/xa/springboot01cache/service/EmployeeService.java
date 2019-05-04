package com.xa.springboot01cache.service;

import com.xa.springboot01cache.bean.Employee;
import com.xa.springboot01cache.mapper.EmployeeMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

@CacheConfig(cacheNames = "emp")  //抽取缓存的公共配置
@Service
public class EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 将方法的运行结果进行缓存; 以后再要相同的数据,直接从缓存中获取,不用调用方法
     *
     * CacheManager:管理福讴歌Cache组件的,对缓存的真正CRUD操作在Cache组件中,每一个缓存组件中有自己唯一一个的名字
     * 几个属性:
     *          cacheNames/value:   指定缓存的名字;将方法的返回结果放在哪个缓存中,是数组的方式,可以指定多个缓存
     *
     *          key:缓存数据时用的key; 可以用它来指定, 默认是使用方法参数的值  1-方法的返回值
     *                  编写SpEL ;  #id;参数id的值   #a0  #p0  #root.args[0]
     *                  key = "#root.methodName+'['+#id+']'
     *
     *          keyGenerator: key的生成器;可以自己指定key的生成器的组件id
     *                  key/keyGenerator   : 二选一
     *          cacheManager: 指定缓存管理器;  或者cacheResolver  也是二选一  作用都一样
     *          condition: 指定符合条件的情况下才缓存;
     *                  ,condition = "#id > 0"
     *                  condition = "#a0>1"  第一个参数的值>1才进行缓存
     *                  "#a0>1 and #root.methodName eq ''"
     *          unless:否定缓存,当unless指定的条件为true,方法的返回值就不会缓存;可以获取到结果进行判断
     *                  ,unless = "#result == null"
     *                  unless = "#a0==2"   如果第一个参数的值是2,结果不缓存
     *          sync:是否使用异步模式    异步模式下  unless就不支持了
     *
     * 原理:
     *  1.自动配置类;CacheAutoConfiguration
     *  2.缓存的配置类
     *  org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.JCacheCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.EhCacheCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.HazelcastCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.InfinispanCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.CouchbaseCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration
     *  org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration
     *  3.哪个配置类默认生效  SimpleCacheConfiguration
     *  4.给容器中注册了一个CacheManager:ConcurrentMapCacheManager
     *  5.可以获取和穿件ConcurrentMapCache类型的缓存组件;它的作用键该数据保存在ConcurrentMap中;
     *
     *  运行流程:
     *  @Cacheable:
     *  1.方法运行之前,先去查询Cache(缓存组件),按照cacheNames指定的名字获取;
     *         (CacheManager先获取相应的缓存),第一次获取缓存如果没有Cache组件会自动创建
     *  2.去Cache中查找缓存的内容,使用一个key,默认就是方法的参数;
     *      key是按照某种策略生成的; 默认是使用keyGenerator生成的,默认使用SimpleKeyGenerator生成key
     *              SimpleKeyGenerator生成key的默认策略;
     *                  如果没有参数; key=new SimpleKey();
     *                  如果有一个参数; key=参数的值
     *                  如果有多个参数; key=new SimpleKey(params);
     *  3.没有查到缓存就调用目标方法;
     *  4.将目标方法返回的结果,放进缓存中
     *
     *  @Cacheable标注的方法执行之前先来检查缓存中有没有这个数据,默认按照参数的值作为key去查询缓存,如果没有
     *  就运行方法,并将结果放入缓存;以后再来调用就可以直接使用缓存中的数据;
     *
     *  核心:
     *      1).使用CacheManager[ConcurrentMapCacheManager]按照名字得到Cache[ConcurrentMapCache]组件
     *      2).key使用keyGenerator生成的,默认是SimpleKeyGenerator
     *
     * @param id
     * @return
     */
    @Cacheable(cacheNames = {"emp"}/*,keyGenerator = "myKeyGenerator",condition = "#a0>1",unless = "#a0==2"*/)
    public Employee getEmp(Integer id){
        System.out.println("查询"+id+"号员工");
        Employee emp = employeeMapper.getEmpById(id);
        return emp;
    }

    /**
     * @CachePut : 即调用方法,又更新缓存数据;  同步更新缓存
     *  修改了数据库的某个数据,同时更新缓存
     *  运行时机:
     *      1,先调用目标方法
     *      2.将目标方法的结果缓存起来
     *
     *  测试步骤
     *      1.查询1号员工;查到的结果会放在缓存中
     *              key:1  value: lastName:张三
     *      2.以后查询还是之前的结果
     *      3.更新一下一号员工;
     *              将方法的返回值也放进缓存了;
     *              key: 传入的employee对象,  值: 返回的employee对象;
     *      4.查询一号员工?
     *          应该是更新后的员工;
     *                  key = "#employee.id" 使用传入的参数的员工的id
     *                  key = "#result.id" 使用返回后的id
     *                      @Cacheable 的key是不能用#result 取出数据
     *          为什么是没更新前的?[一号员工没有在缓存中更新(实际上是key不一样)]
     *
     *          取缓存的key和放缓存的key必须一致
     */
    @CachePut(value = "emp",key = "#reusult.id")
    public Employee updateEmp(Employee employee){
        System.out.println("update : "+employee);
        employeeMapper.updataEmp(employee);
        return employee;
    }

    /**
     *  @CacheEvict: 缓存清除
     *      key: 指定要清除的数据   默认是传入的参数
     *      allEntries: 是不是要把emp缓存中的所有数据都删除
     *      ,allEntries = true 清除所有数据  默认是false
     *      beforeInvocation = false; 缓存的清除是否在方法之前执行
     *          默认是false 代表缓存清除操作 在方法之后执行,如果出现异常缓存就不会清除,因为在方法执行之后
     *
     *      beforeInvocation = true;
     *          代表清除缓存操作是在方法执行之前执行,无论方法是否出现异常,都会清除缓存
     */
    @CacheEvict(value = "emp" ,key = "#id")
    public void deleteEmp(Integer id){
        System.out.println("deleteEmp:"+id);
        //employeeMapper.deleteEmpById(id);
    }

    //@Caching 定义复杂缓存注解    CachePut导致方法一定要执行
    @Caching(
            cacheable = {
                    @Cacheable(value = "emp",key = "#lastName")
            },
            put = {
                    @CachePut(value = "emp",key = "#result.id"),
                    @CachePut(value = "emp",key = "#result.email")
            }
    )
    public Employee getEmpByLastName(String lastName){
        return employeeMapper.getEmpByLastName(lastName);
    }
}
