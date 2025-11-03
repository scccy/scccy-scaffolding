package com.example.utils;

import cn.idev.excel.FastExcel;
import com.scccy.common.excel.listener.GenericExcelListener;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

/**
 * Excel 工具类（基于 FastExcel）
 */
public class ExcelUtil {

    /**
     * 导出 Excel 文件
     * @param response HttpServletResponse
     * @param fileName 导出的文件名（不带扩展名）
     * @param data 导出的数据列表
     * @param clazz 导出实体类类型
     * @param <T> 实体类类型
     * @throws IOException 文件输出异常
     */
    public static <T> void export(HttpServletResponse response, String fileName, List<T> data, Class<T> clazz) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // FastExcel 导出
        FastExcel.write(response.getOutputStream(), clazz)
                .sheet("Sheet1")
                .doWrite(data);
    }

    /**
     * 导入 Excel 文件
     * @param file 上传的 Excel 文件
     * @param clazz 目标实体类
     * @param batchConsumer 批量处理函数（如保存数据库）
     * @param <T> 数据类型
     * @throws Exception 读取异常
     */
    public static <T> void importData(MultipartFile file, Class<T> clazz, Consumer<List<T>> batchConsumer) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择一个文件上传！");
        }
        FastExcel.read(file.getInputStream(), clazz,
                        new GenericExcelListener<>(batchConsumer))
                .sheet()
                .doRead();
    }
}