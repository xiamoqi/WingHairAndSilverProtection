package com.yiguardsilverfa.service.login.impl;

import cn.hutool.core.util.RandomUtil;
import com.yiguardsilverfa.config.SecurityProperties;
import com.yiguardsilverfa.constant.ErrorConstant;
import com.yiguardsilverfa.constant.LoginConstant;
import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dto.*;
import com.yiguardsilverfa.dto.user.UpdateUserInfoDTO;
import com.yiguardsilverfa.entity.User;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.EmailService;
import com.yiguardsilverfa.service.login.LoginService;
import com.yiguardsilverfa.utils.BaseContext;
import com.yiguardsilverfa.utils.JwtUtil;
import com.yiguardsilverfa.utils.PasswordUtil;
import com.yiguardsilverfa.utils.TimeUtil;
import com.yiguardsilverfa.vo.login.LoginVO;
import com.yiguardsilverfa.vo.login.UserDetail;
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
    public UserDetail register(RegisterDTO registerDTO) {
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

        User user = loginDAO.selectUserByPhone(loginDTO.getPhone());
        if (user == null || !user.getPassword().equals(loginDTO.getPassword()) || user.getStatus() == 0) {
            return null;
        }

        LoginVO loginVO = new LoginVO();
        String token = createTokenAndSave(user);

        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(token);
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
    // 修改用户信息
    @Override
    public void updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO) {
        //检查用户是否存在
        User user=loginDAO.selectUserById(userId);
        if(user==null){
            throw new BusinessException(ErrorConstant.USER_NOT_EXIST);
        }
        //如果修改手机号，检查新手机号是否已被其他用户占用
        if(updateUserInfoDTO.getPhone()!=null&&!updateUserInfoDTO.getPhone().equals(user.getPhone())){
            User existUser=loginDAO.selectUserByPhone(updateUserInfoDTO.getPhone());
            if(existUser!=null && !existUser.getId().equals(userId)){
                throw new BusinessException(ErrorConstant.PHONE_ALREADY_EXIST);
            }
        }
        //如果修改邮箱，检查是否已被占用
        if(updateUserInfoDTO.getEmail()!=null&&!updateUserInfoDTO.getEmail().equals(user.getEmail())){
            User existUser=loginDAO.selectUserByEmail(updateUserInfoDTO.getEmail());
            if(existUser!=null && !existUser.getId().equals(userId)){
                throw new BusinessException(ErrorConstant.EMAIL_ALREADY_EXIST);
            }
        }
        //将更新内容拷贝到用户对象(不覆盖 id、密码等）
        BeanUtils.copyProperties(updateUserInfoDTO,user,"id","username","password","role","status","createTime","updateTime");
        //更新用户信息
        int rows=loginDAO.updateUserSelective(user);
        if(rows!=1){
            throw new BusinessException("修改失败，请重试");
        }
    }
    @Autowired
    private ElderInfoDAO elderInfoDAO;
    @Override
    public void cancelUser(Long userId) {
        //检查用户是否存在且状态正常
        User user=loginDAO.selectUserById(userId);
        if(user==null||user.getStatus()!=1){
            throw new BusinessException(ErrorConstant.USER_NOT_EXIST);
        }
        // 更新用户状态为取消
        int rows=loginDAO.logout(userId);
        if(rows!=1){
            throw new BusinessException(ErrorConstant.CANCELED_FAILED);
        }
        //如果是老人注销，那么老人信息那里改状态为0
        if(user.getRole()==1){
            elderInfoDAO.softDeleteByUserId(userId);
        }

        //如果是家属注销，那么关联表那里要改状态为0
        if(user.getRole()==2){
            loginDAO.deleteFamilybind(userId);
            //同时在elderInfo 表中逻辑删除该关联的信息
            elderInfoDAO.softDeleteByUserId(userId);
        }

        //清除 Redis 中的 token（假设存储 key = "token:" + userId）
        String jwt = BaseContext.getCurrentJwt();
        if (jwt != null) {
            stringRedisTemplate.delete(LoginConstant.TOKEN_CACHE_PREFIX + jwt);
        }
    }
}