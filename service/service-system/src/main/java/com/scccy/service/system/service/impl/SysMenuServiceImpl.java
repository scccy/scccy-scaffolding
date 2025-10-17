package com.scccy.service.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.mapper.SysMenuMapper;
import com.scccy.service.system.domain.SysMenu;
import com.scccy.service.system.service.SysMenuService;
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService{

}
