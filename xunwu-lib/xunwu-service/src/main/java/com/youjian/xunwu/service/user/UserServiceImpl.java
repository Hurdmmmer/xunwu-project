package com.youjian.xunwu.service.user;

import com.youjian.xunwu.comm.entity.Role;
import com.youjian.xunwu.comm.entity.User;
import com.youjian.xunwu.comm.vo.ServiceResult;
import com.youjian.xunwu.comm.vo.UserVo;
import com.youjian.xunwu.dao.RoleRepository;
import com.youjian.xunwu.dao.UserRepository;
import com.youjian.xunwu.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements IUserService, UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Pattern pattern = Pattern.compile("^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$");

    @Override
    public User findUserByName(String username) {
        User user = null;
        if (pattern.matcher(username).matches()) {
            user = userRepository.findUserByPhoneNumber(username);
        } else {
            user = userRepository.findUserByName(username);
        }
        if (user == null) {
            return null;
        }
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }
        user.setGrantedAuthorityList(new ArrayList<>(roles));
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return findUserByName(s);
    }

    @Override
    public ServiceResult<UserVo> findById(Long adminId) {
        Optional<User> optUser = userRepository.findById(adminId);
        if (optUser.isPresent()) {
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(optUser.get(), userVo);
            return ServiceResult.ofSuccess(userVo);
        }
        return null;
    }
}
