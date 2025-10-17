package com.scccy.service.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.domain.SysUser;
import com.scccy.service.system.mapper.SysUserMapper;
import com.scccy.service.system.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

}
