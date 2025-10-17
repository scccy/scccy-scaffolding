package com.scccy.service.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.mapper.SysDeptMapper;
import com.scccy.service.system.domain.SysDept;
import com.scccy.service.system.service.SysDeptService;
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService{

}
