package org.zankio.cculife.CCUService.base.helper;

public final class Validate {

    private Validate() {}

    public static void isNull(Object object, String msg) {
        if(object == null)
            throw new IllegalArgumentException(msg);
    }
}
