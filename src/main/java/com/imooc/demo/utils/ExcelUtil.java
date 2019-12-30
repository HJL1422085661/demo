package com.imooc.demo.utils;


import com.imooc.demo.VO.ResultVO;
import com.imooc.demo.model.*;
import com.imooc.demo.service.CompanyService;
import com.imooc.demo.service.EmployeeService;
import com.imooc.demo.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by tofuchen on 2018/10/26.
 */

@Component
@Slf4j
public class ExcelUtil {


    @Autowired
    public EmployeeService employeeService;
    @Autowired
    public ResourceService resourceService;
    @Autowired
    public CompanyService companyService;

    public static ExcelUtil excelUtil;


    @PostConstruct
    public void init() {
        excelUtil = this;
        excelUtil.resourceService = this.resourceService;
        excelUtil.employeeService = this.employeeService;
        excelUtil.companyService = this.companyService;
    }


    /**
     * 导入人才数库需要员工ID，所以需要先创建所有员工
     *解析人才Excel
     * @param file
     * @return
     */
    public static ResultVO<Map<String, String>> parseResourceExcel(MultipartFile file) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            //1、获取文件输入流
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            //2、获取Excel工作簿对象
            Sheet sheet;
            Workbook workbook;
            //2、获取Excel工作簿对象
            if (fileName.indexOf(".xlsx") != -1) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                throw new IOException("不支持的文件类型");
                // workbook = new HSSFWorkbook(inputStream);
            }
            //3、得到Excel工作表对象
            sheet = workbook.getSheetAt(0);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            //4、循环读取表格数据

            List<Integer> repeatRow = new ArrayList<>();
            Integer successRow = 0;
            List<Integer> exceptionRow = new ArrayList<>();
            //取第i行(第一行i=0是表头）
            for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
                Resource resource = new Resource();
                Row row = sheet.getRow(i);
                String resourceName = row.getCell(0).getStringCellValue();
                Integer gender = row.getCell(1).getStringCellValue().equals("女") ? 1 : 2;
                String phoneNumber = row.getCell(2).getStringCellValue();
                String certificate = row.getCell(3).getStringCellValue();
                String endDate = row.getCell(4).getStringCellValue();
                String province = row.getCell(5).getStringCellValue();
                String weChat = row.getCell(6).getStringCellValue();
                String qq = row.getCell(7).getStringCellValue();
                String email = row.getCell(8).getStringCellValue();
                String info = row.getCell(9).getStringCellValue();
                String ResourceStatus = row.getCell(10).getStringCellValue();

                Integer status = 1; //status默认值为啥？
                if (ResourceStatus.equals("潜在客户")) status = 1;
                else if (ResourceStatus.equals("意向客户")) status = 2;
                else if (ResourceStatus.equals("成交客户")) status = 3;
                else if (ResourceStatus.equals("失败客户")) status = 4;
                else if (ResourceStatus.equals("已流失客户")) status = 5;

                String city = row.getCell(10).getStringCellValue();
                String createDate = row.getCell(13).getStringCellValue(); //时间格式需要约定
                String employeeName = row.getCell(14).getStringCellValue();
                //去数据库查重
                Boolean flag = excelUtil.resourceService.existsByPhoneNumber(phoneNumber);
                if (flag) {
                    repeatRow.add(i);
                } else {
                    Employee employee = excelUtil.employeeService.getEmployeeByEmployeeName(employeeName);
                    if (employee == null) {
                        //员工不存在
                        log.error("【导入人才信息】经办人不存在");
                    } else {
                        resource.setResourceName(resourceName);
                        resource.setPhoneNumber(phoneNumber);
                        resource.setEmployeeName(employeeName);
                        resource.setEmployeeId(employee.getEmployeeId());
                        resource.setEndDate(endDate);
                        resource.setCreateDate(createDate);
                        resource.setStatus(status);
                        resource.setInfo(info);
                        resource.setQq(qq);
                        resource.setGender(gender);
                        resource.setCity(city);
                        resource.setEmail(email);
                        resource.setProvince(province);
                        resource.setCertificate(certificate);
                        //默认都是私有的人才 1 公有  2 私有
                        resource.setShareStatus(2);

                        //插入数据库
                        Boolean success = excelUtil.resourceService.saveResource(resource);
                        if (success) successRow++;
                        else exceptionRow.add(i);
                    }
                }
            }
            returnMap.put("repeatRow", repeatRow);
            returnMap.put("exceptionRow", exceptionRow);
            returnMap.put("successRow", successRow);
            //5、关闭流
            workbook.close();
        } catch (Exception e) {
            log.error("【导入人才信息】发生异常");
            e.printStackTrace();
        }
        return ResultVOUtil.success(returnMap);
    }

    /**
     * 解析公司资源
     * @param file
     * @return
     */
    public static ResultVO<Map<String, String>> parseCompanyExcel(MultipartFile file) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            //1、获取文件输入流
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            //2、获取Excel工作簿对象
            Sheet sheet;
            Workbook workbook;
            //2、获取Excel工作簿对象
            if (fileName.indexOf(".xlsx") != -1) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                throw new IOException("不支持的文件类型");
                // workbook = new HSSFWorkbook(inputStream);
            }
            //3、得到Excel工作表对象
            sheet = workbook.getSheetAt(0);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            //4、循环读取表格数据

            List<Integer> repeatRow = new ArrayList<>();
            Integer successRow = 0;
            List<Integer> exceptionRow = new ArrayList<>();
            //取第i行(第一行i=0是表头）
            for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
                Company company = new Company();
                Row row = sheet.getRow(i);
                String companyName = row.getCell(0).getStringCellValue();
                String province = row.getCell(1).getStringCellValue();
                String companyStatus = row.getCell(2).getStringCellValue();
                String info = row.getCell(3).getStringCellValue();
                String expireDate = row.getCell(4).getStringCellValue();  //时间格式需要约定
                String contactorName = row.getCell(5).getStringCellValue();
                Integer gender = row.getCell(6).getStringCellValue() == "女" ? 1:2;
                String occupation = row.getCell(7).getStringCellValue();
                String phoneNumber = row.getCell(9).getStringCellValue();
                String email = row.getCell(10).getStringCellValue();

                Integer status = 1; //status默认值为啥？
                if (companyStatus.equals("潜在客户")) status = 1;
                else if (companyStatus.equals("意向客户")) status = 2;
                else if (companyStatus.equals("成交客户")) status = 3;
                else if (companyStatus.equals("失败客户")) status = 4;
                else if (companyStatus.equals("已流失客户")) status = 5;

                String createDate = row.getCell(14).getStringCellValue();
                String employeeName = row.getCell(15).getStringCellValue();
                //去数据库查重
//                Boolean flag = excelUtil.resourceService.existsByPhoneNumber(phoneNumber);
//                if (flag) {
//                    repeatRow.add(i);
//                } else {
                    Employee employee = excelUtil.employeeService.getEmployeeByEmployeeName(employeeName);
                    if (employee == null) {
                        //员工不存在
                        log.error("【导入公司信息】经办人不存在");
                    } else {
                        company.setEmployeeId(employee.getEmployeeId());
                        company.setEmployeeName(employeeName);
                        company.setGender(gender);
                        company.setPhoneNumber(phoneNumber);
                        company.setCompanyName(companyName);
                        company.setStatus(status);
                        company.setCreateDate(createDate);
                        company.setExpireDate(expireDate);
                        company.setOccupation(occupation);
                        company.setContactorName(contactorName);
                        company.setProvince(province);
                        company.setEmail(email);
                        company.setInfo(info);
                        //设置公司类别，表中没有这个字段，默认填其他
                        company.setCompanyCategory(5);
                        //默认为私有
                        company.setShareStatus(2);

                        //插入数据库
                        Boolean success = excelUtil.companyService.saveCompany(company);
                        if (success) successRow++;
                        else exceptionRow.add(i);
                    }
              //  }
            }
            returnMap.put("repeatRow", repeatRow);
            returnMap.put("exceptionRow", exceptionRow);
            returnMap.put("successRow", successRow);
            //5、关闭流
            workbook.close();
        } catch (Exception e) {
            log.error("【导入公司信息】发生异常");
            e.printStackTrace();
        }
        return ResultVOUtil.success(returnMap);
    }

}
