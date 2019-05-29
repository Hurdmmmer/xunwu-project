package com.youjian.xunwu.service;

import com.youjian.xunwu.comm.entity.User;
import com.youjian.xunwu.comm.vo.ServiceResult;
import com.youjian.xunwu.comm.vo.UserVo;

public interface IUserService {
    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    User findUserByName(String username);

    ServiceResult<UserVo> findById(Long adminId);
}
