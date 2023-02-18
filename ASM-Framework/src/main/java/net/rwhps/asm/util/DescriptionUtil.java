/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.asm.util;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Basically {@link Type#getDescriptor(Class)} since that class might not be
 * available at Runtime.
 */
public class DescriptionUtil {
    private static final Map<Class<?>, String> PRIMITIVES = new HashMap<>();

    public static final String ObjectClassName = "java/lang/Object";

    static {
        PRIMITIVES.put(boolean.class, "Z");
        PRIMITIVES.put(byte.class, "B");
        PRIMITIVES.put(short.class, "S");
        PRIMITIVES.put(int.class, "I");
        PRIMITIVES.put(long.class, "J");
        PRIMITIVES.put(float.class, "F");
        PRIMITIVES.put(double.class, "D");
        PRIMITIVES.put(char.class, "C");
        PRIMITIVES.put(void.class, "V");
    }

    public static String getDesc(Method method) {
        StringBuilder desc = new StringBuilder(method.getName()).append("(");
        for (Class<?> parameter : method.getParameterTypes()) {
            putDesc(parameter, desc);
        }

        putDesc(method.getReturnType(), desc.append(")"));
        return desc.toString();
    }

    public static String getDesc(Class<?> type) {
        StringBuilder result = new StringBuilder();
        putDesc(type, result);
        return result.toString();
    }

    private static void putDesc(Class<?> type, StringBuilder builder) {
        while (type.isArray()) {
            builder.append("[");
            type = type.getComponentType();
        }

        String primitive = PRIMITIVES.get(type);
        if (primitive == null) {
            builder.append("L");
            String name = type.getName();
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                builder.append(ch == '.' ? '/' : ch);
            }

            builder.append(";");
        } else {
            builder.append(primitive);
        }
    }

}
