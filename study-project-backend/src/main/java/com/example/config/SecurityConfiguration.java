package com.example.config;

import com.alibaba.fastjson.JSONObject;
import com.example.entity.RestBean;
import com.example.service.impl.AuthorizeServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.io.IOException;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    //加上可以出现循环引用。
    //以前都以为是A里面注入B，B里面注入A才会出现
    //这里的原因是SecurityConfiguration里注入了AuthorizeServiceImpl
    //而AuthorizeServiceImpl注入了BCryptPasswordEncoder为Bean
    //而SecurityConfiguration配置了BCryptPasswordEncoder
    //也就是说也可以是需要的是那个类里面注册的Bean,也会出现循环引用
//    @Resource
//    AuthorizeServiceImpl authorizeService;
    @Resource
    DataSource dataSource;
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,PersistentTokenRepository tokenRepository) throws Exception {
        return http.
                    authorizeHttpRequests(auth -> {
                        auth.requestMatchers("/api/auth/**").permitAll();
                        auth.anyRequest().authenticated();
                    })
                    .formLogin(conf -> {
                        conf.loginProcessingUrl("/api/auth/login");
                        conf.permitAll();
                        conf.successHandler(this::onAuthenticationSuccess);
                        conf.failureHandler(this::onAuthenticationFailure);
                   })
                    .logout(conf -> {
                            conf.logoutUrl("/api/auth/logout");
                            conf.logoutSuccessHandler(this::onAuthenticationSuccess);
                            conf.permitAll();
                    })
                    .rememberMe(conf -> {
                        conf.alwaysRemember(false);
                        conf.rememberMeParameter("remember");
                        conf.tokenRepository(tokenRepository);
                        conf.tokenValiditySeconds(3600*24*7);
                    })
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                    .userDetailsService(authorizeService)
                    .exceptionHandling(conf -> conf.authenticationEntryPoint(this::onAuthenticationFailure))
                    .csrf(AbstractHttpConfigurer::disable)
                   .build();
    }
    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        //第一次启动设置
//        jdbcTokenRepository.setCreateTableOnStartup(true);
        return jdbcTokenRepository;
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOriginPattern("*"); // 允许任何源
        corsConfig.addAllowedMethod("*"); // 允许任何HTTP方法
        corsConfig.addAllowedHeader("*"); // 允许任何HTTP头
        corsConfig.setAllowCredentials(true); // 允许证书（cookies）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // 对所有路径应用这个配置
        return source;
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        if(request.getRequestURI().endsWith("/login"))
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("登陆成功")));
        else if(request.getRequestURI().endsWith("/logout"))
            response.getWriter().write(JSONObject.toJSONString(RestBean.success("退出登陆成功")));
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401,exception.getMessage())));
    }

}
