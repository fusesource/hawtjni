package org.fusesource.hawtjni.generator.model;

/**
 * @author <a href="mailto:calin.iorgulescu@gmail.com">Calin Iorgulescu</a>
 */
public interface JNIFieldAccessor {
    public String getter();

    public String setter();

    public boolean isNonMemberGetter();

    public boolean isNonMemberSetter();

    public boolean isMethodGetter();

    public boolean isMethodSetter();

}
