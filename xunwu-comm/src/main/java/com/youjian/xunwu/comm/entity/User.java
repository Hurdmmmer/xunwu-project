package com.youjian.xunwu.comm.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity(name = "user")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String email;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String password;
    private long status;
    @Column(name = "create_time")
    private java.sql.Timestamp createTime;
    @Column(name = "last_login_time")
    private java.sql.Timestamp lastLoginTime;
    @Column(name = "last_update_time")
    private java.sql.Timestamp lastUpdateTime;
    private String avatar;

    // 用户权限, 取消 JPA 验证
    @Transient
    private List<GrantedAuthority> grantedAuthorityList;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorityList.stream().map(e -> new SimpleGrantedAuthority("ROLE_" + e.getAuthority())).collect(Collectors.toList());
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
