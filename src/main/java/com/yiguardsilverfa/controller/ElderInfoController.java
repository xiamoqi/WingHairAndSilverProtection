package com.yiguardsilverfa.controller;


import com.yiguardsilverfa.dto.elder.ElderInfoAddDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoUpdateDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoWithRelationDTO;
import com.yiguardsilverfa.dto.familyBind.BindElderAccountDTO;
import com.yiguardsilverfa.dto.user.SearchUserInfoDTO;
import com.yiguardsilverfa.dto.user.SearchUserNameDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.ElderInfo.ElderInfoService;
import com.yiguardsilverfa.utils.BaseContext;
import com.yiguardsilverfa.utils.PhoneNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/elderInfo")
public class ElderInfoController {
    @Autowired
    private ElderInfoService elderInfoService;

    /**档案添加
     * 检验紧急联系人电话是否合法
     * 添加成功后前端通过调用userid的查询语句去刷新页面
     * @param addDTO
     * @return
     */
    @PostMapping("/add")
    public Result<?> addElderInfo(@RequestBody ElderInfoAddDTO addDTO) {
        if(addDTO.getName() == null || addDTO.getName().trim().isEmpty()){
            return Result.failure("姓名不能为空");
        }
        if(addDTO.getAge()<=0||addDTO.getAge()>150){
            return Result.failure("年龄不合法");
        }
        //检验紧急联系人电话是否合法
        if (addDTO.getEmergencyPhone() != null && !PhoneNumberUtil.isValidPhoneNumber(addDTO.getEmergencyPhone())) {
            return Result.failure("紧急联系人电话格式不正确");
        }
        //地址不能为空
        if (addDTO.getAddress() == null || addDTO.getAddress().trim().isEmpty()) {
            return Result.failure("地址不能为空");
        }
        return elderInfoService.addElderInfo(addDTO);
    }

    /**档案修改
     * 前端检验紧急联系人电话是否合法
     * @param updateDTO
     * @return
     */
    @PostMapping("/update")
    public Result<?> updateElderInfo(@RequestBody ElderInfoUpdateDTO updateDTO) {
        if(updateDTO.getId() == null){
            return Result.failure("档案ID不能为空");
        }
        Integer age = updateDTO.getAge();
        if (age != null && (age <= 0 || age > 150)) {
            return Result.failure("年龄不合法");
        }
        String phone = updateDTO.getEmergencyPhone();
        if (phone != null && !PhoneNumberUtil.isValidPhoneNumber(phone)) {
            return Result.failure("紧急联系人电话格式不正确");
        }
        Integer weight = updateDTO.getWeight();
        if (weight != null && (weight <= 0 || weight > 500)) {
            return Result.failure("体重不合法");
        }
        return elderInfoService.updateElderInfo(updateDTO);
    }

    /**
     * 家属删除档案
     * @param id:档案的id
     * @return
     */

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteElderInfo(@PathVariable Long id) {
        if (id==null){
            return Result.failure("ID不能为空");
        }
        return elderInfoService.deleteElderInfo(id);
    }

    //根据ID查询档案
    @GetMapping("/{id}")
    public Result<?> getElderInfoById(@PathVariable Long id) {
        if (id==null){
            return Result.failure("ID不能为空");
        }
        ElderInfoWithRelationDTO info = elderInfoService.getElderInfoById(id);
        if (info == null) {
            return Result.success("档案不存在"); // 或返回空数据，前端自行处理
        }
        return Result.success(info);
    }

    //根据用户ID查询档案
    @PostMapping("/user/{userId}")
    public Result<?> getElderInfoByUserId(@PathVariable Long userId) {
        if (userId==null){
            return Result.failure("用户ID不能为空");
        }
        List<ElderInfoWithRelationDTO> info = elderInfoService.getElderInfoByUserId(userId);
        return Result.success(info);
    }

    /**
     * 家属绑定档案所对应的老人账号
     * 老人档案是没有绑定老人用户
     */
    @PostMapping("/bind-account")
    public Result<?> bindElderAccount(@RequestBody BindElderAccountDTO bindDTO) {
        if(bindDTO.getElderInfoId()==null || bindDTO.getElderuserId()==null){
            return Result.failure("档案ID和用户ID不能为空");
        }
        Result<?> result=elderInfoService.bindElderAccount(bindDTO);
        if(result.getSuccess()!=200L){
            return result;
        }
        ElderInfo info = elderInfoService.getElderInfoById(bindDTO.getElderInfoId());
        return Result.success(info);
    }

    /**
     * 通过username查找对应的user信息
     */
    @PostMapping("/search/byusername")
    public Result<?> searchUsername(@RequestBody SearchUserNameDTO request) {
        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return Result.failure("用户名不能为空");
        }
        SearchUserInfoDTO userInfo = elderInfoService.getUserByUsername(username);
        return Result.success(userInfo);
    }

    /**
     * 家属修改绑定的老人档案
     * 老人档案是已经有绑定老人用户的
     */

    /**
     *  家属获取所有绑定老人的姓名列表
     */
    @GetMapping("/bound-elders/names")
    public Result<?> getBoundElderNames() {
        return elderInfoService.getBoundElderNames();
    }

    @GetMapping("/my-elders")
    public Result<?> getMyElderInfos() {
        List<ElderInfo> list = elderInfoService.getMyBindElderInfos();
        return Result.success(list);
    }
}
