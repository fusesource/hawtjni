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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "CvQualifiedType")
public class CvQualifiedType extends Type {
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String id;
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    public String type;

    @XmlAttribute(name="restrict")
    public long restrictType;

    @XmlAttribute(name = "const")
    public long constType;

    public String getId() {
        return id;
    }

    @Override
    public String getCSignature(ClangXml root, String value) {
        StringBuffer rc = new StringBuffer();
        if( constType!=0 )
            rc.append("const ");
        rc.append(root.type(type).getCSignature(root, getRestrictClause()+value) );
        return rc.toString();
    }
    
    String getConstClause() {
        if( restrictType!=0 )
            return " __restrict ";
        return "";
    }

    String getRestrictClause() {
        if( restrictType!=0 )
            return " __restrict ";
        return "";
    }


}
