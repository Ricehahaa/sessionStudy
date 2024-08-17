package com.example.service.impl;

import com.example.entity.auth.Account;
import com.example.mapper.UserMapper;
import com.example.service.AuthorizeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {
    @Resource
    UserMapper userMapper;

    @Value("${spring.mail.username}")
    String from;
    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate template;

    @Resource
    BCryptPasswordEncoder encoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username == null){
            throw new UsernameNotFoundException("用户名为空");
        }
        Account account = userMapper.findAccountByNameorEmail(username);
        if(account == null){
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    @Override
    public String sendValidateEmail(String email, String sessionId,boolean hasAccount) {
        String key = "email" + sessionId +":"+email + ":" +hasAccount;
        if(Boolean.TRUE.equals(template.hasKey(key))){
            Long expire = Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if(expire > 120)
                return "请求频繁";
        }
        Account account = userMapper.findAccountByNameorEmail(email);
        if(hasAccount && account == null) return "无此邮件用户";
        if(!hasAccount && account != null) return "邮箱已注册";
        Random random = new Random();
        int code = random.nextInt(90000) + 100000;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("你的验证邮件");
        message.setText("验证码是:"+code);
        try{
            mailSender.send(message);

            template.opsForValue().set(key, String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }catch (MailException exception){
            exception.printStackTrace();
            return "邮箱发生失败";
        }
    }

    //验证验证码加注册(注册用)
    @Override
    public String validateAndRegister(String username, String password, String email, String code,String sessionId) {
        String key = "email" + sessionId +":"+email + ":false";
        if(Boolean.TRUE.equals(template.hasKey(key))){
            String s  = template.opsForValue().get(key);
            if(s == null)
                return "验证码失效";
            if(s.equals(code)){
                Account account = userMapper.findAccountByNameorEmail(username);
                if(account != null) return  "用户名已注册";
                password = encoder.encode(password);
                template.delete(key);
                if (userMapper.createAccount(username,password,email) > 0) {
                    return null;
                } else {
                    return "数据库错误";
                }
            }else{
                return "验证码错误";
            }
        }
        else
            return "请先请求验证码";
    }

    //单纯验证验证码是否正确(重置密码用)
    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email" + sessionId +":"+email + ":true";
        if(Boolean.TRUE.equals(template.hasKey(key))){
            String s  = template.opsForValue().get(key);
            if(s == null){
                template.delete(key);
                return "验证码失效";
            }
            if(s.equals(code)){
                return null;
            }else{
                return "验证码错误";
            }
        }
        else
            return "请先请求验证码";
    }

    @Override
    public boolean resetPassword(String password, String email) {
        password =encoder.encode(password);
        return userMapper.resetPasswordByEmail(password,email) > 0;
    }
}
