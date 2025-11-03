package com.scccy.service.demo.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2023/8/6 10:44
 * @since knife4j-spring-boot3-demo
 */
@Slf4j
@Tag(name = "Accept")
@RequestMapping(value = "/demo")
@RestController
public class AcceptController {

    @Operation(summary = "测试Accept", description = "GitHub:https://github.com/xiaoymin/knife4j/issues/597")
    @GetMapping("/get")
    public ResponseEntity<String> get(String user) {
        log.info("测试哦");

        return ResponseEntity.ok(user);
    }

//    @PostMapping("/export")
//    @Operation(summary = "导出excel")
//    public void export(HttpServletResponse response, SysUser user) throws IOException {
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setCharacterEncoding("utf-8");
//        String fileName = URLEncoder.encode("demo", "UTF-8").replaceAll("\\+", "%20");
//        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
//        List<SysUser> list = SysUerServiceImpl.list();
//        FastExcel.write(response.getOutputStream(), SysUser.class)
//                .sheet("Sheet1")
//                .doWrite(list);
//}


//    @Operation(summary="上传excel")
//    @PostMapping("/importData")
//    public ResultData<?> importData(@RequestParam("file") MultipartFile file) throws Exception {
////        if (file.isEmpty()) {
//            return ResultData.fail("请选择一个文件上传！");
//        }
//        // 读取 Excel 并处理 SysUser
//        FastExcel.read(file.getInputStream(), SysUser.class,
//                        new GenericExcelListener<SysUser>(list -> {
//                            // 批量保存到数据库
//                            SysUerServiceImpl.saveBatch(list);
//                        }))
//                .sheet()
//                .doRead();
//        return ResultData.ok("文件上传并处理成功！");
//    }
}

