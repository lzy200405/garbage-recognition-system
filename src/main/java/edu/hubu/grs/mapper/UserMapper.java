package edu.hubu.grs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import edu.hubu.grs.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
