package com.yiguardsilverfa.service.ElderInfo.impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.FamilyBindDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dto.elder.ElderInfoAddDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoUpdateDTO;
import com.yiguardsilverfa.dto.familyBind.BindElderAccountDTO;
import com.yiguardsilverfa.dto.familyBind.BindElderInfoDTO;
import com.yiguardsilverfa.dto.user.SearchUserInfoDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.FamilyBind;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.entity.User;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.ElderInfo.ElderInfoService;
import com.yiguardsilverfa.utils.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public Result<?> addElderInfo(ElderInfoAddDTO addDTO) {
        //获取当前用户角色
        Integer currentRole = getCurrentUserRole();
        //获取当前userid
        Long currentUserId = BaseContext.getCurrentUserId();

        ElderInfo elderInfo = new ElderInfo();
        BeanUtils.copyProperties(addDTO, elderInfo);
        elderInfo.setStatus(1);
        if(currentRole==1){
            elderInfo.setUserId(currentUserId);
            //如果是老人，则检验是否已有档案，若有，则不能添加
            //检查该老人是否已有自己的档案
            List<ElderInfo> existing = elderInfoDAO.selectElderInfoByUserId(currentUserId);
            boolean hasActive = existing != null && existing.stream().anyMatch(e -> e.getStatus() == 1);
            if (hasActive) {
                return Result.failure("您已存在有效档案，不能重复添加");
            }
            int rows = elderInfoDAO.insertElderInfo(elderInfo);
            if (rows != 1) {
                return Result.failure("添加档案失败");
            }
        } else if (currentRole==2) {
            //如果是家属，那就可以添加档案，同时在family_bind表中添加一条连接数据
            if(addDTO.getRelation()==null|| addDTO.getRelation().isEmpty()){
                return Result.failure("家属添加档案时必须填写与老人的关系");
            }
            elderInfo.setUserId(currentUserId);
            int rows = elderInfoDAO.insertElderInfo(elderInfo);
            if (rows != 1) {
                return Result.failure("添加档案失败");
            }
            Long elderInfoId=elderInfo.getId();// 获取自动生成的主键
            // 同时插入家属绑定关系
            FamilyBind bind = new FamilyBind();
            bind.setFamilyUserId(currentUserId);
            bind.setElderId(elderInfoId);
            bind.setElderUserId(currentUserId); // 家属创建的，默认存家属ID
            bind.setRelation(addDTO.getRelation());
            bind.setStatus(1); // 绑定中
            int bindRows = familyBindDAO.insert(bind);
            if (bindRows != 1) {
                return Result.failure("添加家属绑定关系失败");
            }
        }else {
            return Result.failure("只有老人或家属可以添加档案");
        }
        return Result.success("添加老人档案成功");
        /**
         * 如果是老人添加档案，就直接存uderid
         * 如果是家属添加老人档案，而且老人没注册账号，那么userid存家属id
         * 那就说明elderinfo表中的userid不用唯一，一个【家属】用户可以有多个，但是老人只能有一个
         */

    }

    @Override
    public Result<?> updateElderInfo(ElderInfoUpdateDTO updateDTO) {
        ElderInfo existing=elderInfoDAO.selectElderInfoById(updateDTO.getId());
        if (existing == null) {
            return Result.failure("档案不存在");
        }
        ElderInfo update = new ElderInfo();
        BeanUtils.copyProperties(updateDTO,update);
        int result = elderInfoDAO.updateElderInfoById(update);
        if (result != 1) {
            return Result.failure("更新档案失败");
        }
        return Result.success(elderInfoDAO.selectElderInfoById(updateDTO.getId()));
    }

    /**
     * 逻辑删除档案
     * @param id：档案ID
     */
    @Override
    public Result<?> deleteElderInfo(Long id) {
/**
 * 删除档案，
 * 如果是家属删除档案应该是断开family_bind的联系，但是elder—info仍然存在
 * 家属删除：获取当前用户的userID，在family_bind查找档案关系，
 * 老人也可以删除档案
 * 如果是老人注销用户，那么通过查找userID进行修改elder—info中status=0
 */
        //获取档案信息
        ElderInfo elderInfo = elderInfoDAO.selectElderInfoById(id);
        if (elderInfo == null) {
            return Result.failure("档案不存在");
        }
        // 如果档案已经是软删除状态，直接返回失败（或成功？根据业务决定）
        if (elderInfo.getStatus() == 0) {
            return Result.failure("档案已被删除，无法重复操作");
        }
        //获取当前用户角色和ID
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer currentRole = loginDAO.selectUserById(currentUserId).getRole();
        if (currentRole == null) {
            return Result.failure("无法获取用户角色");
        }
        if (currentRole == 1) {
            /**
             * 其实在老人端里面，老人只能看到自己的档案，看不到其他人的档案
             * 但是还是做个判断以防万一吧
             */
            //判断当前用户ID是否与被删档案ID一致
            if (!currentUserId.equals(elderInfo.getUserId())) {
                return Result.failure("老人只能删除自己的档案");
            }
            int rows = elderInfoDAO.softDeleteById(id);
            if (rows != 1) {
                return Result.failure("注销档案失败");
            }
        } else if (currentRole==2) {
            /**
             * 这里也是做个防御性判断
             */
            // 检查是否有绑定关系
            int rows = familyBindDAO.deleteByFamilyAndElder(currentUserId, elderInfo.getId());
            if (rows == 0) {
                return Result.failure("您与该老人没有绑定关系，无法解除");
            }
        }else {
            return Result.failure("无权删除档案");
        }
        return Result.success("删除档案成功");
    }

    @Override
    public ElderInfo getElderInfoById(Long id) {

        ElderInfo info=elderInfoDAO.selectElderInfoById(id);

        return info;
    }

    @Override
    public List<ElderInfo> getElderInfoByUserId(Long userId) {

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
    @Transactional
    public Result<?> bindElderAccount(BindElderAccountDTO bindDTO) {
        Long elderInfoId = bindDTO.getElderInfoId();
        Long elderuserId = bindDTO.getElderuserId();
        //检查档案是否存在
        ElderInfo elderInfo = elderInfoDAO.selectElderInfoById(elderInfoId);
        if (elderInfo == null) {
            return Result.failure("档案不存在");
        }
        //检查目标用户是否存在且角色为老人
        User user2 = loginDAO.selectUserById(elderuserId);
        if (user2 == null) {
            return Result.failure("指定的用户不存在");
        }
        if (user2.getRole() == null||user2.getRole() != 1) {
            return Result.failure("指定的用户不是老人，无法绑定");
        }
        //检查该老人账号是否已经绑定了其他档案（唯一性）
        List<ElderInfo> existing = elderInfoDAO.selectElderInfoByUserId(elderuserId);
        if (existing != null && !existing.isEmpty()) {
            return Result.failure("该老人账号已绑定档案，不能重复绑定");
        }

        Long currentUserId = BaseContext.getCurrentUserId();
        // 这里必须用 elder_user_id 查询
        int bind = familyBindDAO.selectByFamilyAndElder(currentUserId, elderInfoId);
        if (bind == 0) {
            return Result.failure("您没有权限绑定此档案");
        }

        // 更新档案归属
        ElderInfo update = new ElderInfo();
        update.setId(elderInfoId);
        update.setUserId(elderuserId);
        int result = elderInfoDAO.updateElderInfoById(update);
        if (result != 1) {
            return Result.failure("绑定失败，请检查是否输入有误！");
        }

        // 2. 同步更新 family_bind 里的 elder_user_id（关键修复）
        FamilyBind bindUpdate = new FamilyBind();
        bindUpdate.setFamilyUserId(currentUserId);
        bindUpdate.setElderId(elderInfoId);
        bindUpdate.setElderUserId(elderuserId);
        familyBindDAO.updateElderUserId(bindUpdate);

        return Result.success("绑定成功");
    }

    /**
     * 获取家属绑定的所有老人名字
     */
    @Override
    public Result<?> getBoundElderNames() {
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer currentRole = getCurrentUserRole();
        if (currentRole != 2) {
            return Result.failure("只有家属可以查看绑定的老人列表");
        }
        // 获取该家属的绑定关系
        List<FamilyBind> binds = familyBindDAO.selectByFamilyUserId(currentUserId);
        if (binds == null || binds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<BindElderInfoDTO> resultList=new ArrayList<>();
        for(FamilyBind bind : binds){
            if(bind.getStatus()!=1)
                continue;
            ElderInfo elderInfo=elderInfoDAO.selectElderInfoById(bind.getElderId());
            if (elderInfo!=null&&elderInfo.getStatus()==1){
                BindElderInfoDTO dto=new BindElderInfoDTO();
                dto.setElderInfoId(elderInfo.getId());
                dto.setUserId(elderInfo.getUserId());
                dto.setUsername(elderInfo.getName());
                resultList.add(dto);
            }
        }
        return Result.success(resultList);
    }

    @Override
    public List<ElderInfo> getMyBindElderInfos() {
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer role = getCurrentUserRole();

        // 老人：查自己
        if (role == 1) {
            return elderInfoDAO.selectElderInfoByUserId(currentUserId);
        }

        // 家属：查绑定的老人
        if (role == 2) {
            List<FamilyBind> binds = familyBindDAO.selectByFamilyUserId(currentUserId);
            if (binds == null || binds.isEmpty()) {
                return new ArrayList<>();
            }

            List<Long> elderIds = binds.stream()
                    .map(FamilyBind::getElderId)
                    .collect(Collectors.toList());

            return elderInfoDAO.selectElderInfoByIds(elderIds);
        }

        return new ArrayList<>();
    }
}