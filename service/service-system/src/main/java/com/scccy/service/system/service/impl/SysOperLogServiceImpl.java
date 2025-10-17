package com.scccy.service.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.domain.SysOperLog;
import com.scccy.service.system.mapper.SysOperLogMapper;
import com.scccy.service.system.service.SysOperLogService;
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService{

}
