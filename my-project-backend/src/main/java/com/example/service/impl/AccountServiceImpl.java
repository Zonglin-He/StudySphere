package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.*;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils utils;

    @Resource
    PasswordEncoder encoder;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByNameOrEmail(username);
        if (account == null) {throw new UsernameNotFoundException("Wrong username or password");}
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if(!this.verifyLimit(ip)){return "Requests are frequent, please try again later";}
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = new HashMap<>();
            data.put("type", type);
            data.put("email", email);
            data.put("ip", ip);
            data.put("code", code);
            amqpTemplate.convertAndSend("mail", data);
            stringRedisTemplate.opsForValue().set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code),
                    3, TimeUnit.MINUTES);
            return null;
        }
    }

    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
        return utils.limitOnceCheck(key, 60);
    }

    public Account findAccountByNameOrEmail(String text){
        return this.query()
                .eq("username", text)
                .or()
                .eq("email", text)
                .one();
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO vo) {
        String email = vo.getEmail();
        String verify = this.resetConfirm(new ConfirmResetVO(email, vo.getCode()));
        if (verify != null) {return verify;}
        String password = encoder.encode(vo.getPassword());
        boolean update= this.update().eq("email", email).set("password", password).update();
        if (update) {stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);}
        return null;
    }

    @Override
    public String resetConfirm(ConfirmResetVO vo) {
        String email = vo.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA + email);
        if(code == null) {return "Please get the verification code first";}
        if(!code.equals(vo.getCode())) {return "Wrong verification code, please enter correct code!";}
        return null;
    }

    @Override
    public String registerEmailAccount(EmailRegisterVO vo) {
        String email = vo.getEmail();
        String username = vo.getUsername();
        String key = Const.VERIFY_EMAIL_DATA + email;
        String code = stringRedisTemplate.opsForValue().get(key);
        if (!code.equals(vo.getCode())) {return "The verification code is wrong. Please enter it again";}
        if (this.existAccountByEmail(email)) {return "This email is already in use";}
        if (this.existAccountByUsername(username)) {return "This username is registered. Please change a new one";}
        String password = encoder.encode(vo.getPassword());
        Account account = new Account(null, username, password, email, Const.ROLE_DEFAULT, null, new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(key);
            return null;
        }
        else {return "Wrong! Please contact administrator";}
    }

    private boolean existAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email", email));
    }

    private boolean existAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("username", username));
    }


    @Override
    public Account findAccountById(int id) {
        return this.query().eq("id", id).one();
    }

    @Override
    public String modifyEmail(int id, ModifyEmailVO vo) {
        String email = vo.getEmail();
        String code = getEmailVerifyCode(email);
        if (code == null) {return "Please get the verification code first!";}
        if (!code.equals(vo.getCode())) {return "Wrong verification code! Please enter again";}
        this.deleteEmailVerifyCode(email);
        Account account = this.findAccountByNameOrEmail(email);
        if (account != null && account.getId() != id) {return "This email is already in use!";}
        this.update()
                .set("email", email)
                .eq("id", id)
                .update();
        return null;
    }

    private String getEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    private void deleteEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

    @Override
    public String changePassword(int id, ChangePasswordVO vo) {
        String password = this.query().eq("id", id).one().getPassword();
        if (passwordEncoder.matches(vo.getPassword(), password)) {return "The original password is incorrect! " +
                "Please enter again.";}
        boolean success = this.update()
                .eq("id", id)
                .set("password", passwordEncoder
                        .encode(vo.getNew_password()))
                .update();
        return success ? null : "Wrong! Please contact administrator" ;
    }
}
