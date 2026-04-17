package com.yiguardsilverfa.service.MedicineRemind.Impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.FamilyBindDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dao.MedicineRemindDAO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindAddDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindQueryDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindUpdateDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.FamilyBind;
import com.yiguardsilverfa.entity.MedicineRemind;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.MedicineRemind.MedicineRemindService;
import com.yiguardsilverfa.utils.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineRemindServiceImpl implements MedicineRemindService {
    @Autowired
    private MedicineRemindDAO medicineRemindDAO;
    @Autowired
    private ElderInfoDAO elderInfoDAO;
    @Autowired
    private FamilyBindDAO familyBindDAO;
    @Autowired
    private LoginDAO loginDAO;

    /**
     * 获取当前用户角色
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
    /**
     * 获取当前用户可访问的老人档案ID列表
     */
    private List<Long> getAccessibleElderIds(){
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer role = getCurrentUserRole();
        List<Long> elderIds = new ArrayList<>();
        if (role == 1) { // 老人只能看到自己的药品
            //去elderinfo表找到对应的档案id
            List<ElderInfo> elder=elderInfoDAO.selectElderInfoByUserId(currentUserId);
            if (!elder.isEmpty() && elder.get(0).getStatus() == 1) {
                elderIds.add(elder.get(0).getId());
            }
        } else if (role==2) {
            List<FamilyBind> binds = familyBindDAO.selectByFamilyUserId(currentUserId);
            elderIds = binds.stream()
                    .filter(bind -> bind.getStatus() == 1)
                    .map(FamilyBind::getElderId)
                    .collect(Collectors.toList());
            if (elderIds.isEmpty()) {
                throw new BusinessException("您尚未绑定任何老人，无法查看药品");
            }
        }else {
            throw new BusinessException("无权限访问");
        }
        return elderIds;
    }

    @Transactional
    @Override
    public Result<?> addRemind(MedicineRemindAddDTO addDTO) {
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer role = getCurrentUserRole();
        MedicineRemind remind = new MedicineRemind();
        BeanUtils.copyProperties(addDTO,remind);

        if (role == 1) {
            List<Long> ids =getAccessibleElderIds();
            if(ids.isEmpty()){
                return Result.failure("请先创建档案");
            }
            remind.setElderId(ids.get(0));
        }else if (role == 2){
            if(addDTO.getElderId()==null){
                return Result.failure("家属添加提醒时必须指定老人");
            }
            int count=familyBindDAO.selectByFamilyAndElder(currentUserId,addDTO.getElderId());
            if(count==0){
                return Result.failure("您没有绑定该老人");
            }
            //如果有绑定关系，那就设置绑定的id
            remind.setElderId(addDTO.getElderId());
        }else {
            return Result.failure("只有老人或家属可操作");
        }
        if(medicineRemindDAO.insertMedicineRemind(remind)!=1) {
            return Result.failure("添加提醒失败");
        }else{
            return Result.success("添加提醒成功");
        }
    }

    @Override
    public Result<?> updateRemind(MedicineRemindUpdateDTO updateDTO) {
        MedicineRemind existing=medicineRemindDAO.selectById(updateDTO.getId());
        if (existing == null) {
            return Result.failure("提醒不存在");
        }
        List<Long> accessibe=getAccessibleElderIds();
        if (!accessibe.contains(existing.getElderId())){
            return Result.failure("无权修改该提醒");
        }
        MedicineRemind update = new MedicineRemind();
        BeanUtils.copyProperties(updateDTO,update);
        if(medicineRemindDAO.updateMedicineRemind(update)!=1){
            return Result.failure("修改提醒失败");
        }else {
            return Result.success("修改提醒成功");
        }
    }

    @Override
    public Result<?> deleteRemind(Long id) {
        MedicineRemind existing=medicineRemindDAO.selectById(id);
        if (existing == null) {
            return Result.failure("提醒不存在");
        }
        List<Long> accessibe=getAccessibleElderIds();
        if (!accessibe.contains(existing.getElderId())){
            return Result.failure("无权修改该提醒");
        }
        if(medicineRemindDAO.deleteMedicineRemind(id)!=1){
            return Result.failure("删除提醒失败");
        }else {
            return Result.success("删除提醒成功");
        }
    }

    @Override
    public List<MedicineRemind> getMyReminds() {
        List<Long> accessibe=getAccessibleElderIds();
        if (accessibe.isEmpty()){
            return new ArrayList<>();
        }
        return medicineRemindDAO.selectByElderId(accessibe.get(0));
    }

}
