package com.example.interceptor;

import com.example.entity.user.AccountUser;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class AuthorizeInterceptor implements HandlerInterceptor {
    @Resource
    UserMapper userMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        User user = (User)authentication.getPrincipal();

        List<String> list = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();


        AccountUser account = userMapper.findAccountUserByNameOrEmail(user.getUsername());
        System.out.println(account);
        request.getSession().setAttribute("account", account);
        return true;
    }
}