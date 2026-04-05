package com.yiguardsilverfa.service.ElderInfo.impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.FamilyBindDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dto.elder.ElderInfoAddDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoUpdateDTO;
import com.yiguardsilverfa.dto.familyBind.BindElderAccountDTO;
import com.yiguardsilverfa.dto.user.SearchUserInfoDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.FamilyBind;
import com.yiguardsilverfa.entity.User;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.ElderInfo.ElderInfoService;
import com.yiguardsilverfa.utils.BaseContext;
import com.yiguardsilverfa.utils.PhoneNumberUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElderInfoServiceImpl implements ElderInfoService {

    @Autowired
    private ElderInfoDAO elderInfoDAO;
    @Autowired
    private FamilyBindDAO familyBindDAO;
    @Autowired
    private LoginDAO loginDAO;

    /**
     * 获取当前登录用户的角色（通过 userId 查表）
     */
    private Integer getCurrentUserRole() {
        Long currentUserId = BaseContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        Integer role = loginDAO.selectUserById(currentUserId).getRole();
        if (role == null) {
            throw new BusinessException("用户角色不存在");
        }
        return role;
    }
    @Override
    @Transactional
    public void addElderInfo(ElderInfoAddDTO addDTO) {
        if(addDTO.getUserId()==null){
            throw new RuntimeException("userId不能为空");
        }
        if(addDTO.getAge()<=0||addDTO.getAge()>150){
            throw new RuntimeException("年龄不合法");
        }
        //检验紧急联系人电话是否合法
        if (addDTO.getEmergencyPhone() != null && !PhoneNumberUtil.isValidPhoneNumber(addDTO.getEmergencyPhone())) {
            throw new BusinessException("紧急联系人电话格式不正确");
        }
        //获取当前用户角色
        Integer currentRole = getCurrentUserRole();
        //获取当前userid
        Long currentUserId = BaseContext.getCurrentUserId();

        ElderInfo elderInfo = new ElderInfo();
        BeanUtils.copyProperties(addDTO, elderInfo);
        elderInfo.setStatus(1);
        if(currentRole==1){
            //如果是老人，则检验是否已有档案，若有，则不能添加
            //检查该老人是否已有自己的档案
            List<ElderInfo> existing = elderInfoDAO.selectElderInfoByUserId(addDTO.getUserId());
            if (existing != null && existing.isEmpty()&&existing.get(0).getStatus()==1) {
                throw new BusinessException("您只能添加自己的档案,您档案档案已存在，不能重复添加");
            }
            int rows = elderInfoDAO.insertElderInfo(elderInfo);
            if (rows != 1) {
                throw new BusinessException("添加档案失败");
            }
        } else if (currentRole==2) {
            //如果是家属，那就可以添加档案，同时在family_bind表中添加一条连接数据
            if(addDTO.getRelation()==null|| addDTO.getRelation().isEmpty()){
                throw new BusinessException("家属添加档案时必须填写与老人的关系");
            }
            int rows = elderInfoDAO.insertElderInfo(elderInfo);
            if (rows != 1) {
                throw new BusinessException("添加档案失败");
            }
            Long elderInfoId=elderInfo.getId();// 获取自动生成的主键
            // 同时插入家属绑定关系
            FamilyBind bind = new FamilyBind();
            bind.setFamilyUserId(currentUserId);
            bind.setElderId(elderInfoId);
            bind.setRelation(addDTO.getRelation());
            bind.setStatus(1); // 绑定中
            int bindRows = familyBindDAO.insert(bind);
            if (bindRows != 1) {
                throw new BusinessException("添加家属绑定关系失败");
            }
        }else {
            throw new BusinessException("只有老人或家属可以添加档案");
        }

        /**
         * 如果是老人添加档案，就直接存uderid
         * 如果是家属添加老人档案，而且老人没注册账号，那么userid存家属id
         * 那就说明elderinfo表中的userid不用唯一，一个【家属】用户可以有多个，但是老人只能有一个
         */

    }

    @Override
    public void updateElderInfo(ElderInfoUpdateDTO updateDTO) {
        if (updateDTO.getId() == null) {
            throw new BusinessException("档案ID不能为空");
        }
        ElderInfo existing=elderInfoDAO.selectElderInfoById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException("档案不存在");
        }
        //检验紧急联系人电话是否合法
        if (updateDTO.getEmergencyPhone() != null && !PhoneNumberUtil.isValidPhoneNumber(updateDTO.getEmergencyPhone())) {
            throw new BusinessException("紧急联系人电话格式不正确");
        }
        ElderInfo update = new ElderInfo();
        BeanUtils.copyProperties(updateDTO,update);
        int result = elderInfoDAO.updateElderInfoById(update);
        if (result != 1) {
            throw new BusinessException("更新档案失败");
        }
    }

    /**
     * 逻辑删除档案
     * @param id：档案ID
     */
    @Override
    public void deleteElderInfo(Long id) {
/**
 * 删除档案，
 * 如果是家属删除档案应该是断开family_bind的联系，但是elder—info仍然存在
 * 家属删除：获取当前用户的userID，在family_bind查找档案关系，
 * 老人也可以删除档案
 * 如果是老人注销用户，那么通过查找userID进行修改elder—info中stayus=0
 */
        //获取档案信息
        ElderInfo elderInfo = elderInfoDAO.selectElderInfoById(id);
        if (elderInfo == null) {
            throw new BusinessException("档案不存在");
        }
        //获取当前用户角色和ID
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer currentRole = loginDAO.selectUserById(currentUserId).getRole();
        if (currentRole == null) {
            throw new BusinessException("无法获取用户角色");
        }
        if (currentRole == 1) {
            /**
             * 其实在老人端里面，老人只能看到自己的档案，看不到其他人的档案
             * 但是还是做个判断以防万一吧
             */
            //判断当前用户ID是否与被删档案ID一致
            if (!currentUserId.equals(elderInfo.getUserId())) {
                throw new BusinessException("老人只能删除自己的档案");
            }
            int rows = elderInfoDAO.softDeleteById(id);
            if (rows != 1) {
                throw new BusinessException("注销档案失败");
            }
        } else if (currentRole==2) {
            /**
             * 这里也是做个防御性判断
             */
            // 检查是否有绑定关系
            int rows = familyBindDAO.deleteByFamilyAndElder(currentUserId, elderInfo.getUserId());
            if (rows == 0) {
                throw new BusinessException("您与该老人没有绑定关系，无法解除");
            }
        }
    }

    @Override
    public ElderInfo getElderInfoById(Long id) {
        if (id==null){
            throw new BusinessException("ID不能为空");
        }
        ElderInfo info=elderInfoDAO.selectElderInfoById(id);
        if(info==null) {
            throw new BusinessException("档案不存在");
        }
        return info;
    }

    @Override
    public List<ElderInfo> getElderInfoByUserId(Long userId) {
        if (userId==null){
            throw new BusinessException("用户ID不能为空");
        }
        List<ElderInfo> info=elderInfoDAO.selectElderInfoByUserId(userId);
        if(info==null||info.isEmpty()) {
            return new ArrayList<>();
        }
        return info;
    }

    @Override
    public void restoreElderInfo(Long id) {
        if(id==null){
            throw new BusinessException("档案ID不能为空");
        }
        ElderInfo info=elderInfoDAO.selectElderInfoById(id);
        if(info==null) {
            throw new BusinessException("档案不存在");
        }
        if(info.getStatus()==1){
            throw new BusinessException("档案已存在");
        }
        int result = elderInfoDAO.restoreById(id);
        if (result != 1) {
            throw new BusinessException("修复档案失败");
        }
    }

    @Override
    public SearchUserInfoDTO getUserByUsername(String username) {
        User user =loginDAO.selectUserByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SearchUserInfoDTO searchUser = new SearchUserInfoDTO();
        BeanUtils.copyProperties(user,searchUser);
        return searchUser;
    }

    @Override
    public void bindElderAccount(BindElderAccountDTO bindDTO) {
        Long elderInfoId = bindDTO.getElderInfoId();
        Long elderuserId = bindDTO.getElderuserId();
        if (elderInfoId == null || elderuserId == null) {
            throw new BusinessException("档案ID和用户ID不能为空");
        }
        //检查档案是否存在
        ElderInfo elderInfo = elderInfoDAO.selectElderInfoById(elderInfoId);
        if (elderInfo == null) {
            throw new BusinessException("档案不存在");
        }
        //检查档案是否已经绑定了老人账号
        Long userId =elderInfo.getUserId();
        if (userId != null) {
            User user = loginDAO.selectUserById(userId);
            if(user != null&&user.getRole()==1){
                throw new BusinessException("该档案已绑定老人账号，无法重复绑定");
            }
        }
        //检查目标用户是否存在且角色为老人
        User user2 = loginDAO.selectUserById(elderuserId);
        if (user2 == null) {
            throw new BusinessException("指定的用户不存在");
        }
        if (user2.getRole() == null||user2.getRole() != 1) {
            throw new BusinessException("指定的用户不是老人，无法绑定");
        }
        //检查该老人账号是否已经绑定了其他档案（唯一性）
        List<ElderInfo> existing = elderInfoDAO.selectElderInfoByUserId(elderuserId);
        if (existing != null && !existing.isEmpty()) {
            throw new BusinessException("该老人账号已绑定档案（档案ID：" + existing.get(0).getId() + "），不能重复绑定");
        }
        ElderInfo update = new ElderInfo();
        update.setId(elderInfoId);
        update.setUserId(elderuserId);
        int result = elderInfoDAO.updateElderInfoById(update);
        if (result != 1) {
            throw new BusinessException("绑定失败，请稍后重试");
        }

    }
}
