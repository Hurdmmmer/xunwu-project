package com.youjian.xunwu.dao;

import com.youjian.xunwu.comm.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findUserByName(String username);

    User findUserByPhoneNumber(String phone);
}
