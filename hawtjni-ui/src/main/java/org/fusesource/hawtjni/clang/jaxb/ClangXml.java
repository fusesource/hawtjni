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

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "translationUnit",
    "referenceSection"
})
@XmlRootElement(name = "CLANG_XML")
public class ClangXml {

    @XmlElement(name = "TranslationUnit")
    public TranslationUnit translationUnit;
    @XmlElement(name = "ReferenceSection")
    public ReferenceSection referenceSection;

    transient private HashMap<String, Type> types = new HashMap<String, Type>();
    transient private HashMap<String, File> files = new HashMap<String, File>();
    
    public void index() {
        for (Type type : referenceSection.types.types) {
            types.put(type.getId(), type);
        }
        for (File file : referenceSection.files.files ) {
            files.put(file.id, file);
        }
    }
    
    public Type type(String id) {
        return types.get(id);
    }
    
    public File file(String id) {
        return files.get(id);
    }
    

}
