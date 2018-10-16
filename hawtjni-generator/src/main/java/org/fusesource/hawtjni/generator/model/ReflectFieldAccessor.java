package org.fusesource.hawtjni.generator.model;

/**
 * @author <a href="mailto:calin.iorgulescu@gmail.com">Calin Iorgulescu</a>
 */
public class ReflectFieldAccessor implements JNIFieldAccessor {

    private String getter;
    private String setter;
    private boolean nonMemberGetter;
    private boolean nonMemberSetter;

    public ReflectFieldAccessor(String value) {
       this.getter = this.setter = value;
       this.nonMemberGetter = this.nonMemberSetter = false;
    }

    public ReflectFieldAccessor(String getter, boolean nonMemberGetter, String setter, boolean nonMemberSetter) {
        this.getter = getter;
        this.nonMemberGetter = nonMemberGetter;
        this.setter = setter;
        this.nonMemberSetter = nonMemberSetter;
    }

    public String getter() {
        return getter;
    }

    public String setter() {
        return setter;
    }

    public boolean isNonMemberGetter() {
        return nonMemberGetter;
    }

    public boolean isNonMemberSetter() {
        return nonMemberSetter;
    }

    public boolean isMethodGetter() {
        return getter.contains("(");
    }

    public boolean isMethodSetter() {
        return setter.contains("(");
    }
}
