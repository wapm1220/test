package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.entity.MyLock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyLockMapper extends BaseMapper<MyLock> {
    public void deleteByMethodName(String methodName);
}