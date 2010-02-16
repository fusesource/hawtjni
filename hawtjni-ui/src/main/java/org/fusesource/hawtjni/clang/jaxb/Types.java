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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "types"
})
@XmlRootElement(name = "Types")
public class Types {

    @XmlElements({
        @XmlElement(name = "CvQualifiedType", type = CvQualifiedType.class),
        @XmlElement(name = "TypeOfExprType", type = TypeOfExprType.class),
        @XmlElement(name = "FunctionType", type = FunctionType.class),
        @XmlElement(name = "ArrayType", type = ArrayType.class),
        @XmlElement(name = "PointerType", type = PointerType.class),
        @XmlElement(name = "Typedef", type = Typedef.class),
        @XmlElement(name = "BlockPointerType", type = BlockPointerType.class),
        @XmlElement(name = "Record", type = Record.class),
        @XmlElement(name = "FunctionNoProtoType", type = FunctionNoProtoType.class),
        @XmlElement(name = "IncompleteArrayType", type = IncompleteArrayType.class),
        @XmlElement(name = "FundamentalType", type = FundamentalType.class)
    })
    public List<Type> types = new ArrayList<Type>();

}
