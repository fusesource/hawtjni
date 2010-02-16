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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fields"
})
@XmlRootElement(name = "Record")
public class Record extends Type {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String id;

    @XmlElement(name = "Field")
    public List<Field> fields;
    
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String context;
    
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String file;

    @XmlAttribute
    public long col;
    
    @XmlAttribute
    public long forward;
    
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String kind;
    
    @XmlAttribute(required = true)
    public String name;
    
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String type;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCSignature(ClangXml root, String value) {
        return kind+" "+name+value;
    }


}
