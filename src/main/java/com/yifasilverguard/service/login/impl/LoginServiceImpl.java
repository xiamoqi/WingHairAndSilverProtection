package com.yifasilverguard.service.login.impl;

import cn.hutool.core.util.RandomUtil;
import com.yifasilverguard.config.SecurityProperties;
import com.yifasilverguard.constant.LoginConstant;
import com.yifasilverguard.dao.LoginDAO;
import com.yifasilverguard.dto.login.*;
import com.yifasilverguard.entity.User;
import com.yifasilverguard.service.EmailService;
import com.yifasilverguard.service.login.LoginService;
import com.yifasilverguard.utils.BaseContext;
import com.yifasilverguard.utils.JwtUtil;
import com.yifasilverguard.utils.PasswordUtil;
import com.yifasilverguard.utils.TimeUtil;
import com.yifasilverguard.vo.login.LoginVO;
import com.yifasilverguard.vo.login.UserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private LoginDAO loginDAO;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EmailService emailService;

    @Override
    public String sendCode(CodeDTO codeDTO) {
        return sendAndSaveCode(codeDTO);
    }

    @Override
    public void sendSmsCode(SendSmsCodeDTO sendSmsCodeDTO) {
        int num = RandomUtil.randomInt(LoginConstant.VERIFY_CODE_RANDOM_START, LoginConstant.VERIFY_CODE_RANDOM_END);
        String code = String.format("%0" + LoginConstant.VERIFY_CODE_LENGTH + "d", num);

        stringRedisTemplate.opsForValue().set(
                LoginConstant.BIND_PHONE_CODE_CACHE_PREFIX + sendSmsCodeDTO.getPhone(),
                code, 5, TimeUnit.MINUTES);

        log.info("短信验证码发送到 {}: {}", sendSmsCodeDTO.getPhone(), code);
    }

    @Override
    public UserDetail register(RegisterDTO registerDTO) {
        // 修复：使用正确的参数顺序 (type, target, code)
        if (!checkCode(LoginConstant.CODE_TYPE_REGISTER, registerDTO.getEmail(), registerDTO.getVerifyCode())) {
            log.warn("注册失败：验证码错误，邮箱={}", registerDTO.getEmail());
            return null;
        }

        if (loginDAO.checkEmailExists(registerDTO.getEmail()) > 0) {
            log.warn("注册失败：邮箱已存在，邮箱={}", registerDTO.getEmail());
            return null;
        }

        if (loginDAO.checkUsernameExists(registerDTO.getUsername()) > 0) {
            log.warn("注册失败：用户名已存在，用户名={}", registerDTO.getUsername());
            return null;
        }

        registerDTO.setPassword(PasswordUtil.encodePassword(registerDTO.getPassword()));

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1);
        user.setPhoneBound(0);
        loginDAO.insertUser(user);

        user = loginDAO.selectUserById(user.getId());
        UserDetail userDetail = new UserDetail();
        BeanUtils.copyProperties(user, userDetail);
        userDetail.setCreateTime(TimeUtil.toString(user.getCreateTime()));
        return userDetail;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        loginDTO.setPassword(PasswordUtil.encodePassword(loginDTO.getPassword()));

        User user = loginDAO.selectUserByUsername(loginDTO.getUsername());
        if (user == null || !user.getPassword().equals(loginDTO.getPassword()) || user.getStatus() == 0) {
            return null;
        }

        LoginVO loginVO = new LoginVO();
        String token = createTokenAndSave(user);

        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);
        loginVO.setPhoneBound(user.getPhoneBound());
        return loginVO;
    }

    @Override
    public UserDetail getUserDetailById(Long userId) {
        User user = loginDAO.selectUserById(userId);
        if (user == null) return null;
        UserDetail userDetail = new UserDetail();
        BeanUtils.copyProperties(user, userDetail);
        userDetail.setCreateTime(TimeUtil.toString(user.getCreateTime()));
        return userDetail;
    }

    @Override
    public boolean changePassword(ChangePasswordDTO changePasswordDTO, Long userId) {
        changePasswordDTO.setOldPassword(PasswordUtil.encodePassword(changePasswordDTO.getOldPassword()));
        changePasswordDTO.setNewPassword(PasswordUtil.encodePassword(changePasswordDTO.getNewPassword()));

        User user = loginDAO.selectUserById(userId);
        if (user == null) return false;

        if (!Objects.equals(changePasswordDTO.getOldPassword(), user.getPassword())) {
            return false;
        }

        // 修复：使用邮箱作为 target
        if (!checkCode(LoginConstant.CODE_TYPE_CHANGE_PASSWORD, user.getEmail(), changePasswordDTO.getVerifyCode())) {
            return false;
        }

        User newPasswordUser = new User();
        newPasswordUser.setId(userId);
        newPasswordUser.setPassword(changePasswordDTO.getNewPassword());
        loginDAO.updateUserById(newPasswordUser);

        stringRedisTemplate.delete(LoginConstant.TOKEN_CACHE_PREFIX + BaseContext.getCurrentJwt());
        return true;
    }

    @Override
    public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = loginDAO.selectUserByEmail(resetPasswordDTO.getEmail());
        if (user == null) {
            return false;
        }

        // 修复：使用邮箱作为 target
        if (!checkCode(LoginConstant.CODE_TYPE_REST_PASSWORD, resetPasswordDTO.getEmail(), resetPasswordDTO.getVerifyCode())) {
            return false;
        }

        resetPasswordDTO.setNewPassword(PasswordUtil.encodePassword(resetPasswordDTO.getNewPassword()));

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(resetPasswordDTO.getNewPassword());
        loginDAO.updateUserById(updateUser);

        return true;
    }

    @Override
    public boolean bindPhone(BindPhoneDTO bindPhoneDTO, Long userId) {
        String cachedCode = stringRedisTemplate.opsForValue()
                .get(LoginConstant.BIND_PHONE_CODE_CACHE_PREFIX + bindPhoneDTO.getPhone());
        if (!Objects.equals(bindPhoneDTO.getSmsCode(), cachedCode)) {
            return false;
        }

        loginDAO.bindPhone(userId, bindPhoneDTO.getPhone());
        stringRedisTemplate.delete(LoginConstant.BIND_PHONE_CODE_CACHE_PREFIX + bindPhoneDTO.getPhone());

        return true;
    }

    @Override
    public void logout(String jwt) {
        if (jwt != null) {
            stringRedisTemplate.delete(LoginConstant.TOKEN_CACHE_PREFIX + jwt);
        }
    }

    private String createTokenAndSave(User user) {
        String token = jwtUtil.createJwt(user.getId(), user.getUsername(), user.getRole().toString());
        stringRedisTemplate.opsForValue().set(LoginConstant.TOKEN_CACHE_PREFIX + token, String.valueOf(user.getId()),
                securityProperties.getTokenValiditySeconds(), TimeUnit.SECONDS);
        return token;
    }

    /**
     * 检查验证码是否正确
     * @param type 验证码类型
     * @param target 目标（邮箱或手机号）
     * @param code 用户输入的验证码
     * @return true-验证成功 false-验证失败
     */
    private boolean checkCode(int type, String target, String code) {
        String key;
        switch (type) {
            case LoginConstant.CODE_TYPE_REGISTER:
                key = LoginConstant.REGISTER_CODE_CACHE_PREFIX + target;
                break;
            case LoginConstant.CODE_TYPE_REST_PASSWORD:
                key = LoginConstant.RESET_PASSWORD_CODE_CACHE_PREFIX + target;
                break;
            case LoginConstant.CODE_TYPE_CHANGE_PASSWORD:
                key = LoginConstant.CHANGE_PASSWORD_CODE_CACHE_PREFIX + target;
                break;
            case LoginConstant.CODE_TYPE_BIND_PHONE:
                key = LoginConstant.BIND_PHONE_CODE_CACHE_PREFIX + target;
                break;
            default:
                return false;
        }
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        boolean isValid = code != null && code.equals(cachedCode);
        if (isValid) {
            // 验证成功后删除验证码，防止重复使用
            stringRedisTemplate.delete(key);
        }
        return isValid;
    }

    private String sendAndSaveCode(CodeDTO codeDTO) {
        int num = RandomUtil.randomInt(LoginConstant.VERIFY_CODE_RANDOM_START, LoginConstant.VERIFY_CODE_RANDOM_END);
        String code = String.format("%0" + LoginConstant.VERIFY_CODE_LENGTH + "d", num);
        int type = codeDTO.getType();
        String email = codeDTO.getEmail();

        String key;
        switch (type) {
            case LoginConstant.CODE_TYPE_REGISTER:
                key = LoginConstant.REGISTER_CODE_CACHE_PREFIX + email;
                break;
            case LoginConstant.CODE_TYPE_REST_PASSWORD:
                key = LoginConstant.RESET_PASSWORD_CODE_CACHE_PREFIX + email;
                break;
            case LoginConstant.CODE_TYPE_CHANGE_PASSWORD:
                key = LoginConstant.CHANGE_PASSWORD_CODE_CACHE_PREFIX + email;
                break;
            default:
                return null;
        }

        stringRedisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        emailService.sendCode(email, code);
        return code;
    }
}