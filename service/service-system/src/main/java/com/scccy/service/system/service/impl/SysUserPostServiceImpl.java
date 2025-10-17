package com.scccy.service.system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scccy.service.system.mapper.SysUserPostMapper;
import com.scccy.service.system.domain.SysUserPost;
import com.scccy.service.system.service.SysUserPostService;
@Service
public class SysUserPostServiceImpl extends ServiceImpl<SysUserPostMapper, SysUserPost> implements SysUserPostService{

}
