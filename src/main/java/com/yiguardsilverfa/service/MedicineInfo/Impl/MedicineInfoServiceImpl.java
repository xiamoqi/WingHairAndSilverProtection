package com.yiguardsilverfa.service.MedicineInfo.Impl;

import com.yiguardsilverfa.dao.ElderInfoDAO;
import com.yiguardsilverfa.dao.FamilyBindDAO;
import com.yiguardsilverfa.dao.LoginDAO;
import com.yiguardsilverfa.dao.MedicineInfoDAO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineAddDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineSelectDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineUpdateDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.FamilyBind;
import com.yiguardsilverfa.entity.MedicineInfo;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.MedicineInfo.MedicineInfoService;
import com.yiguardsilverfa.utils.BaseContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yiguardsilverfa.entity.Result.UNAUTHORIZED_CODE;

@Service
public class MedicineInfoServiceImpl implements MedicineInfoService {
    @Autowired
    private MedicineInfoDAO medicineInfoDAO;
    @Autowired
    private LoginDAO loginDAO;
    @Autowired
    private ElderInfoDAO elderInfoDAO;
    @Autowired
    private FamilyBindDAO familyBindDAO;

    /**
     * 获取当前用户角色
     */
    private Result<?> getCurrentUserRole() {
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer role = loginDAO.selectUserById(currentUserId).getRole();
        if (role == null) {
            return Result.failure("用户角色不存在");
        }
        return Result.success(role);
    }
    /**
     * 获取当前用户可访问的老人档案ID列表
     */
    private Result<?> getAccessibleElderIds(){
        Long currentUserId = BaseContext.getCurrentUserId();
        Result<?> result1 = getCurrentUserRole();
        //登录出问题的时候
        if(result1.getSuccess()!=Result.SUCCESS_CODE){
            return result1;
        }
        Integer role = (Integer) result1.getData();
        List<Long> elderIds = new ArrayList<>();
        if (role == 1) { // 老人：只能看到自己的药品
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
                return Result.success(elderIds);
            }
        }else {
            return Result.failure("无权限访问");
        }
        return Result.success(elderIds);
    }

    /**
     * 添加药品信息
     */
    @Override
    @Transactional
    public Result<?> addMedicineInfo(MedicineAddDTO medicineAddDTO) {
        Long currentUserId = BaseContext.getCurrentUserId();

        Result<?> result1 = getCurrentUserRole();
        //登录出问题的时候
        if(result1.getSuccess()!=Result.SUCCESS_CODE){
            return result1;
        }
        Integer currentRole = (Integer) result1.getData();

        MedicineInfo medicine = new MedicineInfo();
        BeanUtils.copyProperties(medicineAddDTO, medicine);
        medicine.setQuantity(medicineAddDTO.getQuantity()!=null?medicineAddDTO.getQuantity():0);
        if(currentRole==1){
            //老人添加药品
            Result<?> result2=getAccessibleElderIds();
            if(result2.getSuccess()!=Result.SUCCESS_CODE){
                return result2;
            }
            List<Long> elderIds =(List<Long>)result2.getData();
            if (elderIds.isEmpty()){
                return Result.failure("请先添加您的档案，再添加药品");
            }
            // 老人只有一份档案
            medicine.setElderId(elderIds.get(0));
        } else if (currentRole==2) {
            //家属添加药品
            if (medicineAddDTO.getElderId() == null) {
                return Result.failure("家属添加药品时必须指定老人账号");
            }
            Long elderUserId = medicineAddDTO.getElderId(); // 前端传入的老人用户ID
            // 1. 根据老人用户ID查询档案信息
            List<ElderInfo> elderInfo = elderInfoDAO.selectElderInfoByUserId(elderUserId);
            if (elderInfo.isEmpty()|| elderInfo.get(0).getStatus() != 1) {
                return Result.failure("该老人尚未创建档案，请先创建档案");
            }
            Long elderInfoId = elderInfo.get(0).getId(); // 档案ID
            // 校验该老人是否与当前家属绑定
            int bind = familyBindDAO.selectByFamilyAndElder(currentUserId, elderInfoId);
            if (bind == 0) {
                return Result.failure("您未绑定该老人，无法为其添加药品");
            }
            medicine.setElderId(elderInfoId);
        }else {
            return Result.failure("只有老人或家属可以添加药品");
        }
        medicine.setStatus(1);
        int rows = medicineInfoDAO.insertMedicine(medicine);
        if (rows==1){
            return Result.success("添加药品信息成功！");
        }else{
            return Result.failure("添加药品信息失败！");
        }
    }

    @Override
    @Transactional
    public Result<?> updateMedicineInfo(MedicineUpdateDTO medicineUpdateDTO) {
        // 查询原药品
        MedicineInfo existing = medicineInfoDAO.selectById(medicineUpdateDTO.getId());
        if (existing == null) {
            return Result.failure("药品不存在");
        }
        //权限校验：检查药品所属档案是否可访问
        Result<?> result2=getAccessibleElderIds();
        if(result2.getSuccess()!=Result.SUCCESS_CODE){
            return result2;
        }
        List<Long> accessibleElderIds =(List<Long>)result2.getData();
        if (!accessibleElderIds.contains(existing.getElderId())) {
            return Result.failure("无权修改该药品");
        }
        MedicineInfo update = new MedicineInfo();
        BeanUtils.copyProperties(medicineUpdateDTO, update);
        int rows = medicineInfoDAO.updateMedicineById(update);
        if (rows==1){
            return Result.success(medicineInfoDAO.selectById(update.getId()));
        }else{
            return Result.failure("修改药品信息失败！");
        }
    }
    /**
     * 删除药品信息
     */
    @Override
    @Transactional
    public Result<?> deleteMedicineInfo(Long id) {
        MedicineInfo existing = medicineInfoDAO.selectById(id);
        if (existing == null) {
            return Result.failure("药品不存在");
        }

        Result<?> result2=getAccessibleElderIds();
        if(result2.getSuccess()!=Result.SUCCESS_CODE){
            return result2;
        }
        List<Long> accessibleElderIds =(List<Long>)result2.getData();

        if (!accessibleElderIds.contains(existing.getElderId())) {
            return Result.failure("无权删除该药品");
        }
        int rows = medicineInfoDAO.softdeleteById(id);
        if (rows==1){
            return Result.success("删除药品信息成功！");
        }else{
            return Result.failure("删除药品信息失败！");
        }
    }

    /**
     * 根据老人档案ID查询药品列表
     */
    @Override
    public Result<?> getMedicineInfoByElderId(Long elderId) {
        if (elderId == null) {
            return Result.success(new ArrayList<>());
        }
        // 权限校验：当前用户是否可访问该档案

        Result<?> result2=getAccessibleElderIds();
        if(result2.getSuccess()!=Result.SUCCESS_CODE){
            return result2;
        }
        List<Long> accessibleElderIds =(List<Long>)result2.getData();

        if (!accessibleElderIds.contains(elderId)) {
            return Result.failure("无权查看该老人的药品");
        }
        return Result.success(medicineInfoDAO.selectByElderId(elderId));
    }

    /**
     * 老人获取自己所有的药品信息
     * @return
     */
    @Override
    public Result<?> getMyMedicineList() {
        Long currentUserId = BaseContext.getCurrentUserId();

        Result<?> result1 = getCurrentUserRole();
        //登录出问题的时候
        if(result1.getSuccess()!=Result.SUCCESS_CODE){
            return result1;
        }
        Integer role = (Integer) result1.getData();

        if (role != 1) {
            return Result.failure("只有老人可以查看自己的药品列表");
        }
        // 查询老人的档案
        List<ElderInfo> elder = elderInfoDAO.selectElderInfoByUserId(currentUserId);
        if (elder.isEmpty() || elder.get(0).getStatus() != 1) {
            return Result.failure("请先创建您的档案");
        }
        // 根据档案ID查询药品
        return Result.success(medicineInfoDAO.selectByElderId(elder.get(0).getId()));
    }

    /**
     * 条件查询药品（支持模糊查询、类型筛选）
     */
    @Override
    public Result<?> getMedicineInfoByCondition(MedicineSelectDTO medicineSelectDTO) {
        Result<?> result2=getAccessibleElderIds();
        if(result2.getSuccess()!=Result.SUCCESS_CODE){
            return result2;
        }
        List<Long> accessibleElderIds =(List<Long>)result2.getData();

        if (accessibleElderIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        // 调用 DAO 层多条件查询，传入可访问的档案ID列表
        return Result.success(medicineInfoDAO.selectByCondition(
                accessibleElderIds,
                medicineSelectDTO.getMedicineName(),
                medicineSelectDTO.getType()
        ));
    }
}
