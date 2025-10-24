package com.scccy.common.modules.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;

public class EntityModelConverter implements EntityConverter {
    private EntityModelConverter() {
    }

    public static EntityModelConverter getInstance() {
        return EntityModelConverter.SingletonHolder.sInstance;
    }

    public <F, P> P convert(F source, Class<P> targetClass) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        P target = (P)targetClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private static class SingletonHolder {
        private static final EntityModelConverter sInstance = new EntityModelConverter();
    }
}