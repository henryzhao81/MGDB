/**
********************************
Copyright 2013 Proteus Digital Health, Inc.
********************************

CONFIDENTIAL INFORMATION OF PROTEUS DIGITAL HEALTH, INC.

Author : hzhao@proteusdh.com
Nov 3, 2013
*/

package com.mgdb.datatypes;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public class PersonFunnel implements Funnel<Person> {
    private static final long serialVersionUID = 1L;
    public PersonFunnel() {
    }
    
    @Override
    public void funnel(Person person, PrimitiveSink into) {
        into
        .putInt(person.ID);
        //.putString(person.name, Charsets.UTF_8);
    }

}
