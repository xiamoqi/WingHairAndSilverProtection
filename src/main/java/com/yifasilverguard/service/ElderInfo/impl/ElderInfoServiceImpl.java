package com.yifasilverguard.service.ElderInfo.impl;

import com.yifasilverguard.dao.ElderInfoDAO;
import com.yifasilverguard.dto.elder.ElderInfoAddDTO;
import com.yifasilverguard.dto.elder.ElderInfoUpdateDTO;
import com.yifasilverguard.entity.ElderInfo;
import com.yifasilverguard.exception.BusinessException;
import com.yifasilverguard.service.ElderInfo.ElderInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ElderInfoServiceImpl implements ElderInfoService {

    @Autowired
    private ElderInfoDAO elderInfoDAO;

    @Override
    @Transactional
    public void addElderInfo(ElderInfoAddDTO addDTO) {
        if(addDTO.getUserId()==null){
            throw new RuntimeException("userId不能为空");
        }
        //绑定老人档案的时候要检验改用户角色是老人[未写]
        /**
         * 因为需要确定什么情况下会增加老人档案，如果是家属增加档案
         * 那岂不是老人要先注册账号然后才能添加档案
         * 家属是不是只能添加联系的老人档案，好像是不能直接无userid去添加老人档案的吧
         */
        /**
         * 在老人档案里user-id可以存家属的userId，就是当老人没有创建账号的时候可以这样
         * 然后如果老人创建了账号，那么就要修改user-id为老人的userId
         * {修改方法}家属有个选项是【档案绑定老人账号】家属选中要绑定的老人档案
         * 家属通过输入老人的username（唯一）去确认老人的账号
         * 然后获取老人的userId，这样可以去进行修改档案绑定了
         */

        if(addDTO.getAge()<=0||addDTO.getAge()>150){
            throw new RuntimeException("年龄不合法");
        }
        //检查该用户是否已有档案（一个用户只能有一条档案）
        ElderInfo existing=elderInfoDAO.selectElderInfoByUserId(addDTO.getUserId());
        if (existing != null) {
            throw new BusinessException("该用户已存在档案，不能重复添加");
        }
        ElderInfo elderInfo=new ElderInfo();
        BeanUtils.copyProperties(addDTO,elderInfo);
        int rows=elderInfoDAO.insertElderInfo(elderInfo);
        if (rows != 1) {
            throw new BusinessException("添加档案失败");
        }
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

        ElderInfo update = new ElderInfo();
        BeanUtils.copyProperties(updateDTO,update);
        int result = elderInfoDAO.updateElderInfoById(update);
        if (result != 1) {
            throw new BusinessException("更新档案失败");
        }
    }

    @Override
    public void deleteElderInfo(Long id) {
/**
 * 删除档案，
 * 如果是家属删除档案应该是断开family_bind的联系，但是elder—info仍然存在
 * 家属删除：获取当前用户的userID，在family_bind查找档案关系，
 * 老人也可以删除档案
 * 如果是老人注销用户，那么通过查找userID进行修改elder—info中stayus=0
 */
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
    public ElderInfo getElderInfoByUserId(Long userId) {
        if (userId==null){
            throw new BusinessException("用户ID不能为空");
        }
        ElderInfo info=elderInfoDAO.selectElderInfoByUserId(userId);
        if(info==null) {
            throw new BusinessException("档案不存在");
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
            throw new BusinessException("更新档案失败");
        }
    }
}
