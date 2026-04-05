package com.yiguardsilverfa.controller;


import com.yiguardsilverfa.dto.elder.ElderInfoAddDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoUpdateDTO;
import com.yiguardsilverfa.entity.ElderInfo;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.service.ElderInfo.ElderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/elderInfo")
public class ElderInfoController {
    @Autowired
    private ElderInfoService elderInfoService;

    /**档案添加
     * 前端检验紧急联系人电话是否合法
     * @param addDTO
     * @return
     */
    @PostMapping("/add")
    public Result<?> addElderInfo(@RequestBody ElderInfoAddDTO addDTO) {
        elderInfoService.addElderInfo(addDTO);
        return Result.success("添加档案成功");
    }

    /**档案修改
     * 前端检验紧急联系人电话是否合法
     * @param updateDTO
     * @return
     */
    @PostMapping("/update")
    public Result<?> updateElderInfo(@RequestBody ElderInfoUpdateDTO updateDTO) {
        elderInfoService.updateElderInfo(updateDTO);
        return Result.success("修改档案成功");
    }

    /**
     * 家属删除档案
     * @param id:档案的id
     * @return
     */

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteElderInfo(@PathVariable Long id) {
        elderInfoService.deleteElderInfo(id);
        return Result.success("删除成功");
    }

    //根据ID查询档案
    @GetMapping("/{id}")
    public Result<?> getElderInfoById(@PathVariable Long id) {
        ElderInfo info = elderInfoService.getElderInfoById(id);
        if (info == null) {
            return Result.success(null); // 或返回空数据，前端自行处理
        }
        return Result.success(info);
    }

    //根据用户ID查询档案
    @PostMapping("/user/{userId}")
    public Result<?> getElderInfoByUserId(@PathVariable Long userId) {
        List<ElderInfo> info = elderInfoService.getElderInfoByUserId(userId);
        if (info == null) {
            return Result.success(null); // 或返回空数据，前端自行处理
        }
        return Result.success(info);
    }
}
