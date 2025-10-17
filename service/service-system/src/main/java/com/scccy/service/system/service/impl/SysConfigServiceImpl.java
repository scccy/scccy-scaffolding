package com.scccy.service.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.mapper.SysConfigMapper;
import com.scccy.service.system.domain.SysConfig;
import com.scccy.service.system.service.SysConfigService;
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService{

}
