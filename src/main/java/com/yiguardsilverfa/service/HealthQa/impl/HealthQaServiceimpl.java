package com.yiguardsilverfa.service.HealthQa.impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.FamilyBindDAO;
import com.yiguardsilverfa.dao.HealthQaDAO;
import com.yiguardsilverfa.dto.healthQa.SelectHealthQaDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.FamilyBind;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.HealthQa.HealthQaService;
import com.yiguardsilverfa.utils.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthQaServiceimpl implements HealthQaService {
    @Autowired
    private FamilyBindDAO familyBindDAO;
    @Autowired
    private HealthQaDAO healthQaDAO;
    @Autowired
    private ElderInfoDAO elderInfoDAO;
    /**
     * 通过老人名字获取指定老人的问答记录
     */
    @Override
    public List<SelectHealthQaDTO> getElderQaByElderName(String elderName) {
        //获取当前用户id
        Long currentUserId = BaseContext.getCurrentUserId();
        //先获取该老人的id
        ElderInfo elderInfo = elderInfoDAO.selectElderInfoByName(elderName);
        if (elderInfo == null) {
            throw new BusinessException("未找到该老人");
        }
        //再通过id去获取该老人的问答记录
        return healthQaDAO.selectByElderId(elderInfo.getId());

    }

    /**
     * 获取所有绑定老人提问记录
     */
    @Override
    public List<SelectHealthQaDTO> getAllElderQuestions() {
        //获取当前用户id
        Long currentUserId = BaseContext.getCurrentUserId();
// 查询当前家属绑定的所有老人档案ID（elder_id）用于检验
        List<FamilyBind> binds = familyBindDAO.selectByFamilyUserId(currentUserId);
        if (binds == null || binds.isEmpty()) {
            throw new BusinessException("您尚未绑定任何老人，无法查看提问记录");
        }
        List<Long> elderIds = binds.stream()
                .filter(bind -> bind.getStatus() == 1) // 确认status=1是“有效绑定”
                .map(FamilyBind::getElderId)
                .collect(Collectors.toList());
        //校验筛选后是否有有效老人ID
        if (elderIds.isEmpty()) {
            throw new BusinessException("暂无有效绑定的老人，无法查看提问记录");
        }
        //查询问答记录
        List<SelectHealthQaDTO> result = healthQaDAO.selectAllByElderId(elderIds);
        return healthQaDAO.selectAllByElderId(elderIds);
    }
}
