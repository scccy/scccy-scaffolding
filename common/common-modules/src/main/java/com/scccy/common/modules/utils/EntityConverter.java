package com.scccy.common.modules.utils;

import java.lang.reflect.InvocationTargetException;

public interface EntityConverter {
    <F, P> P convert(F var1, Class<P> var2) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException;
}