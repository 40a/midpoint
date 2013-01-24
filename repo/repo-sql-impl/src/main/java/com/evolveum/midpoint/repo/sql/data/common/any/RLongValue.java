/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common.any;

import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.namespace.QName;

/**
 * @author lazyman
 */
@Embeddable
public class RLongValue extends RValue<Long> {
    
    private Long value;

    public RLongValue() {
    }
    
    public RLongValue(Long value) {
        this(null, null, value);
    }

    public RLongValue(QName name, QName type, Long value) {
        setName(name);
        setType(type);
        setValue(value);
    }

    @Index(name = "iLong")
    @Column(name = "longValue")
    @Override
    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RLongValue that = (RLongValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
