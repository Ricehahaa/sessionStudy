package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.impl.AuthorizeServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    private final String EMAIL_REGEXP = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$";
    private final String USERNAME_REGEXP = "^[a-zA-Z\u4e00-\u9fa5]+$";
    @Resource
    AuthorizeServiceImpl authorizeService;
    @PostMapping("/valid-register-email")
    public RestBean<String> validateRegisterEmail(@Pattern(regexp = EMAIL_REGEXP) @RequestParam("email")String email,
                                          HttpSession session){
        String s = authorizeService.sendValidateEmail(email, session.getId(),false);
        if(s == null){
            return RestBean.success("邮件发送成功");
        }
        else
            return RestBean.failure(400,s);
    }

    @PostMapping("/valid-reset-email")
    public RestBean<String> validateResetEmail(@Pattern(regexp = EMAIL_REGEXP) @RequestParam("email")String email,
                                                  HttpSession session){
        String s = authorizeService.sendValidateEmail(email, session.getId(),true);
        if(s == null){
            return RestBean.success("邮件发送成功");
        }
        else
            return RestBean.failure(400,s);
    }

    @PostMapping("/register")
    public RestBean<String> registerUser(@Pattern(regexp = USERNAME_REGEXP) @Length(min=3,max=8) @RequestParam("username") String username,
                                         @Length(min=6,max=16)@RequestParam("password") String password,
                                         @Pattern(regexp = EMAIL_REGEXP)@RequestParam("email") String email,
                                         @Length(min=6,max=6)@RequestParam("code") String code,
                                         HttpSession session){
        String s = authorizeService.validateAndRegister(username, password, email, code, session.getId());
        if(s == null){
            return RestBean.success("注册成功");
        }
        else
            return RestBean.failure(400,s);
    }

    @PostMapping("/start-reset")
    public RestBean<String> startReset(@Pattern(regexp = EMAIL_REGEXP)@RequestParam("email") String email,
                                       @Length(min=6,max=6)@RequestParam("code") String code,
                                       HttpSession session){
        String s = authorizeService.validateOnly(email, code, session.getId());
        if(s == null){
            session.setAttribute("reset-password", email);
            return RestBean.success("验证码正确");
        }
        else
            return RestBean.failure(400,s);
    }

    @PostMapping("do-reset")
    public  RestBean<String> resetPassword(@Length(min=6,max=16)@RequestParam("password") String password,
                                            HttpSession session){
        String email  = (String)session.getAttribute("reset-password");
        if(email == null){
            return RestBean.failure(401,"请先完成邮箱验证");
        }else if(authorizeService.resetPassword(password, email)){
            session.removeAttribute("reset-password");

            return RestBean.success("密码重置成功");
        }else{
            return RestBean.failure(500,"服务器内部错误");
        }
    }
}
