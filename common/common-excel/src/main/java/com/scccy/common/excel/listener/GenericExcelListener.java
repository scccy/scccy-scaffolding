package com.scccy.common.excel.listener;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class GenericExcelListener<T> extends AnalysisEventListener<T> {

    private final List<T> list = new ArrayList<>();
    private final Consumer<List<T>> batchConsumer; // 用于处理读取完的数据

    /**
     * 构造方法
     * @param batchConsumer 数据读取完成后的处理逻辑，比如保存到数据库
     */
    public GenericExcelListener(Consumer<List<T>> batchConsumer) {
        this.batchConsumer = batchConsumer;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        log.info("读取到一条数据: {}", JSON.toJSONString(data));
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据读取完成！");
        if (batchConsumer != null && !list.isEmpty()) {
            batchConsumer.accept(list); // 调用外部传入的处理逻辑
        }
    }
}