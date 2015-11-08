package org.zankio.cculife.CCUService.base.authentication;

public interface IAuth<T> {
    public T Auth(T connection);
}
