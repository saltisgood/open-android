package com.nickstephen.openandroid.misc;

import com.nickstephen.lib.misc.StatMethods;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Static class that just has some helpers for printing out the nice looking versions of the names
 * of certain types.
 */
public final class TypeHelpers {
    private TypeHelpers() {}

    public static String printMember(Member member) {
        if (member instanceof Constructor<?>) {
            return member.getDeclaringClass().getSimpleName() + " Constructor";
        }
        return member.getName();
    }

    public static String printMethod(Method method) {
        Class<?>[] params = method.getParameterTypes();
        String paramList = "";
        if (params.length != 0) {
            for (int i = 0; i < params.length - 1; i++) {
                paramList += params[i].getSimpleName() + ", ";
            }
            paramList += params[params.length - 1].getSimpleName();
        }

        return getModifiers(method) + method.getReturnType().getSimpleName() + " " +
                method.getName() + "(" + paramList + ")";
    }

    public static String printConstructor(Constructor<?> c) {
        Class<?>[] params = c.getParameterTypes();
        String paramList = "";
        if (params.length != 0) {
            for (int i = 0; i < params.length - 1; i++) {
                paramList += params[i].getSimpleName() + ", ";
            }
            paramList += params[params.length - 1].getSimpleName();
        }

        return getModifiers(c) + c.getDeclaringClass().getSimpleName() + "(" + paramList + ")";
    }

    public static String printField(Field f) {
        return getModifiers(f) + f.getType().getSimpleName() + " " + f.getName();
    }

    public static String getModifiers(Member member) {
        String mod = Modifier.toString(member.getModifiers());
        return StatMethods.IsStringNullOrEmpty(mod) ? "" : mod + " ";
    }

    public static String printClass(Class<?> c) {
        if (c.isAnonymousClass()) {
            return "Anonymous class: " + c.getName().replace(c.getEnclosingClass().getName() + "$", "");
        }
        return c.getSimpleName();
    }
}
