package com.scccy.common.excel.untils;

import cn.idev.excel.FastExcel;
import cn.idev.excel.annotation.ExcelProperty;
import com.scccy.common.excel.annotation.ExcelSchemaProperty;
import com.scccy.common.excel.listener.GenericExcelListener;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        // 检查是否使用了 @ExcelSchemaProperty，如果是，使用自定义导出逻辑
        if (clazz.isAnnotationPresent(ExcelSchemaProperty.class) || hasFieldWithExcelSchemaProperty(clazz)) {
            exportWithSchemaProperty(response.getOutputStream(), data, clazz);
        } else {
            // 使用 FastExcel 标准导出
            FastExcel.write(response.getOutputStream(), clazz)
                    .sheet("Sheet1")
                    .doWrite(data);
        }
    }

    /**
     * 检查类中是否有字段使用了 @ExcelSchemaProperty
     */
    private static <T> boolean hasFieldWithExcelSchemaProperty(Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(ExcelSchemaProperty.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用 @ExcelSchemaProperty 导出 Excel（自动从 @Schema 读取 description）
     */
    private static <T> void exportWithSchemaProperty(java.io.OutputStream outputStream, List<T> data, Class<T> clazz)  {
        // 获取字段映射（字段 -> 列名）
        Map<Field, String> fieldColumnMap = getFieldColumnMapping(clazz);
        
        if (fieldColumnMap.isEmpty()) {
            // 如果没有字段需要导出，使用 FastExcel 标准导出
            FastExcel.write(outputStream, clazz)
                    .sheet("Sheet1")
                    .doWrite(data);
            return;
        }

        // 构建表头（需要是 List<List<String>> 格式）
        List<List<String>> headers = new ArrayList<>();
        List<Field> exportFields = new ArrayList<>();
        for (Map.Entry<Field, String> entry : fieldColumnMap.entrySet()) {
            headers.add(Collections.singletonList(entry.getValue()));
            exportFields.add(entry.getKey());
        }

        // 构建数据行
        List<List<Object>> rows = new ArrayList<>();
        for (T item : data) {
            List<Object> row = new ArrayList<>();
            for (Field field : exportFields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(item);
                    row.add(value != null ? value : "");
                } catch (IllegalAccessException e) {
                    row.add("");
                }
            }
            rows.add(row);
        }

        // 使用 FastExcel 写入数据
        FastExcel.write(outputStream)
                .head(headers)
                .sheet("Sheet1")
                .doWrite(rows);
    }

    /**
     * 获取字段到列名的映射
     */
    private static <T> Map<Field, String> getFieldColumnMapping(Class<T> clazz) {
        Map<Field, String> mapping = new LinkedHashMap<>();
        boolean hasClassLevelAnnotation = clazz.isAnnotationPresent(ExcelSchemaProperty.class);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // 先检查是否有 @ExcelProperty（优先使用）
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null && excelProperty.value() != null && excelProperty.value().length > 0) {
                mapping.put(field, excelProperty.value()[0]);
                continue;
            }

            // 检查字段上是否有 @ExcelSchemaProperty
            if (field.isAnnotationPresent(ExcelSchemaProperty.class)) {
                Schema schema = field.getAnnotation(Schema.class);
                if (schema != null && schema.description() != null && !schema.description().isEmpty()) {
                    mapping.put(field, schema.description());
                    continue;
                }
            }

            // 检查类上是否有 @ExcelSchemaProperty
            if (hasClassLevelAnnotation) {
                Schema schema = field.getAnnotation(Schema.class);
                if (schema != null && schema.description() != null && !schema.description().isEmpty()) {
                    mapping.put(field, schema.description());
                    continue;
                }
            }
        }

        return mapping;
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