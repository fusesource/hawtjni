package org.fusesource.hawtjni.clang;


import static org.fusesource.hawtjni.clang.ClangSupport.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.fusesource.hawtjni.clang.jaxb.ClangXml;
import org.fusesource.hawtjni.clang.jaxb.Enum;
import org.fusesource.hawtjni.clang.jaxb.Function;
import org.fusesource.hawtjni.clang.jaxb.Record;
import org.fusesource.hawtjni.clang.jaxb.Typedef;
import org.fusesource.hawtjni.clang.jaxb.Var;
import org.junit.Test;

public class ParseTest {
    
    @Test
    public void canLoad() throws JAXBException, XMLStreamException, IOException, InterruptedException {
        ClangXml xml = load("stdio.h", null);
        
        for (Object unit : xml.translationUnit.units) {
            if( unit.getClass() == Function.class ) {
                Function v = (Function) unit;
                System.out.println(v.getCPrototype(xml));
            }
            if( unit.getClass() == Record.class ) {
                
            }
            if( unit.getClass() == Var.class ) {
                
            }
            if( unit.getClass() == Enum.class ) {
                
            }
            if( unit.getClass() == Typedef.class ) {
                
            }
        }
    }
    
}
