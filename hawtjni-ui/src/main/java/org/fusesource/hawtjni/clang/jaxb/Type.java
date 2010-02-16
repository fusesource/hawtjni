package org.fusesource.hawtjni.clang.jaxb;

abstract public class Type {

    public <T> T to(Class<T> type) {
        if( this.getClass()==type ) {
            return type.cast(this);
        }
        return null;
    }
    
    abstract public String getId();

    abstract public String getCSignature(ClangXml root, String value);

}
