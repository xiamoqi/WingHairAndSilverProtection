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
                throw new BusinessException("您尚未绑定任何老人，无法查看药品");
            }
        }else {
            throw new BusinessException("无权限访问");
        }
        return elderIds;
    }

    /**
     * 添加药品信息
     */
    @Override
    @Transactional
    public Boolean addMedicineInfo(MedicineAddDTO medicineAddDTO) {
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer currentRole = getCurrentUserRole();
        MedicineInfo medicine = new MedicineInfo();
        BeanUtils.copyProperties(medicineAddDTO, medicine);
        medicine.setQuantity(medicineAddDTO.getQuantity()!=null?medicineAddDTO.getQuantity():0);
        if(currentRole==1){
            //老人添加药品
            List<Long> elderIds = getAccessibleElderIds();
            if (elderIds.isEmpty()){
                throw new BusinessException("请先添加您的档案，再添加药品");
            }
            // 老人只有一份档案
            medicine.setElderId(elderIds.get(0));
        } else if (currentRole==2) {
            //家属添加药品
            if (medicineAddDTO.getElderId() == null) {
                throw new BusinessException("家属添加药品时必须指定老人账号");
            }
            Long elderUserId = medicineAddDTO.getElderId(); // 前端传入的老人用户ID
            // 1. 根据老人用户ID查询档案信息
            List<ElderInfo> elderInfo = elderInfoDAO.selectElderInfoByUserId(elderUserId);
            if (elderInfo.isEmpty()|| elderInfo.get(0).getStatus() != 1) {
                throw new BusinessException("该老人尚未创建档案，请先创建档案");
            }
            Long elderInfoId = elderInfo.get(0).getId(); // 档案ID
            // 校验该老人是否与当前家属绑定
            int bind = familyBindDAO.selectByFamilyAndElder(currentUserId, elderInfoId);
            if (bind == 0) {
                throw new BusinessException("您未绑定该老人，无法为其添加药品");
            }
            medicine.setElderId(elderInfoId);
        }else {
            throw new BusinessException("只有老人或家属可以添加药品");
        }
        medicine.setStatus(1);
        int rows = medicineInfoDAO.insertMedicine(medicine);
        return rows == 1;
    }

    @Override
    @Transactional
    public Boolean updateMedicineInfo(MedicineUpdateDTO medicineUpdateDTO) {
        if (medicineUpdateDTO.getId() == null) {
            throw new BusinessException("药品ID不能为空");
        }
        // 查询原药品
        MedicineInfo existing = medicineInfoDAO.selectById(medicineUpdateDTO.getId());
        if (existing == null) {
            throw new BusinessException("药品不存在");
        }
        MedicineInfo update = new MedicineInfo();
        BeanUtils.copyProperties(medicineUpdateDTO, update);
        int rows = medicineInfoDAO.updateMedicineById(update);
        return rows == 1;
    }
    /**
     * 删除药品信息
     */
    @Override
    @Transactional
    public Boolean deleteMedicineInfo(Long id) {
        if (id == null) {
            throw new BusinessException("药品ID不能为空");
        }
        MedicineInfo existing = medicineInfoDAO.selectById(id);
        if (existing == null) {
            throw new BusinessException("药品不存在");
        }
        int rows = medicineInfoDAO.softdeleteById(id);
        return rows == 1;
    }

    /**
     * 根据老人档案ID查询药品列表
     */
    @Override
    public List<MedicineInfo> getMedicineInfoByElderId(Long elderId) {
        if (elderId == null) {
            return new ArrayList<>();
        }
        return medicineInfoDAO.selectByElderId(elderId);
    }

    /**
     * 老人获取自己所有的药品信息
     * @return
     */
    @Override
    public List<MedicineInfo> getMyMedicineList() {
        Long currentUserId = BaseContext.getCurrentUserId();
        Integer role = getCurrentUserRole();
        if (role != 1) {
            throw new BusinessException("只有老人可以查看自己的药品列表");
        }
        // 查询老人的档案
        List<ElderInfo> elder = elderInfoDAO.selectElderInfoByUserId(currentUserId);
        if (elder.isEmpty() || elder.get(0).getStatus() != 1) {
            throw new BusinessException("请先创建您的档案");
        }
        // 根据档案ID查询药品
        return medicineInfoDAO.selectByElderId(elder.get(0).getId());
    }

    /**
     * 条件查询药品（支持模糊查询、类型筛选）
     */
    @Override
    public List<MedicineInfo> getMedicineInfoByCondition(MedicineSelectDTO medicineSelectDTO) {
        List<Long> accessibleElderIds = getAccessibleElderIds();
        if (accessibleElderIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 调用 DAO 层多条件查询，传入可访问的档案ID列表
        return medicineInfoDAO.selectByCondition(
                accessibleElderIds,
                medicineSelectDTO.getMedicineName(),
                medicineSelectDTO.getType()
        );
    }
}
