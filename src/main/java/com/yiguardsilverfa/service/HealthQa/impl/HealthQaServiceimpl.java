package com.yiguardsilverfa.service.HealthQa.impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.FamilyBindDAO;
import com.yiguardsilverfa.dao.HealthQaDAO;
import com.yiguardsilverfa.dto.healthQa.SelectHealthQaDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.FamilyBind;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.entity.Result;
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
     * 通过老人档案id获取指定老人的问答记录
     */
    @Override
    public Result<?> getElderQaByElderId(Long elderId) {
        //获取当前用户id
        Long currentUserId = BaseContext.getCurrentUserId();
        //先获取该老人的id
        ElderInfo elderInfo = elderInfoDAO.selectElderInfoById(elderId);
        if (elderInfo == null) {
            return Result.failure("未找到该老人");
        }
        //检查是否有关联
        int bindCount =familyBindDAO.selectByFamilyAndElder(currentUserId, elderId);
        if(bindCount==0){
            return Result.failure("您尚未绑定该老人，无法查看提问记录");
        }

        //再通过id去获取该老人的问答记录
        List<SelectHealthQaDTO> records = healthQaDAO.selectByElderId(elderId);
        return Result.success(records);
    }

    /**
     * 获取所有绑定老人提问记录
     */
    @Override
    public Result<?> getAllElderQuestions() {
        //获取当前用户id
        Long currentUserId = BaseContext.getCurrentUserId();
// 查询当前家属绑定的所有老人档案ID（elder_id）用于检验
        List<FamilyBind> binds = familyBindDAO.selectByFamilyUserId(currentUserId);
        if (binds == null || binds.isEmpty()) {
            return Result.failure("您尚未绑定任何老人，无法查看提问记录");
        }
        List<Long> elderIds = binds.stream()
                .filter(bind -> bind.getStatus() == 1) // 确认status=1是“有效绑定”
                .map(FamilyBind::getElderId)
                .collect(Collectors.toList());
        //校验筛选后是否有有效老人ID
        if (elderIds.isEmpty()) {
            return Result.failure("暂无有效绑定的老人，无法查看提问记录");
        }
        //查询问答记录
        List<SelectHealthQaDTO> result = healthQaDAO.selectAllByElderId(elderIds);
        return Result.success(result);
    }
}
