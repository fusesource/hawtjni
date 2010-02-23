/**
 * Copyright (C) 2009 Progress Software, Inc.
 * http://fusesource.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtjni.clang.jaxb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parmVar"
})

@XmlRootElement(name = "Function")
public class Function {

    @XmlElement(name = "ParmVar")
    public List<ParmVar> parmVar = new ArrayList<ParmVar>();
        
    @XmlAttribute
    public long col;
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String context;
    
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String file;
    
    @XmlAttribute(name = "function_type")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String functionType;
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String id;
    
    @XmlAttribute
    public long inline;
    
    @XmlAttribute
    public long line;
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String name;
    
    @XmlAttribute(name = "num_args")
    public BigInteger numArgs;
    
    @XmlAttribute(name = "storage_class")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String storageClass;
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String type;

    public String getCPrototype(ClangXml root) {
        StringBuffer rc = new StringBuffer();
        if( "extern".equals("extern") ) {
            rc.append("extern ");
        }
        
        FunctionType ft = root.type(functionType).to(FunctionType.class);
        
        String sig = root.type(type).getCSignature(root, null);
        rc.append(sig+" ");
        rc.append(name+"(");
        boolean first=true;
        for( ParmVar p : parmVar ) {
            if( !first ) {
                rc.append(", ");
            }
            first = false;
            Type paramType = root.type(p.type);
            String pname=""; 
            if( p.name != null && !p.name.isEmpty() ) {
                pname = " "+p.name;
            }
            sig = paramType.getCSignature(root, pname);
            rc.append(sig);
        }
        if( ft!=null && ft.variadic !=0 ) {
            if( !first ) {
                rc.append(", ");
            }
            rc.append("...");
        }
        rc.append(")");
        return rc.toString();
    }

    public java.io.File getFile(ClangXml xml) {
        return xml.file(file).file();
    }

}
