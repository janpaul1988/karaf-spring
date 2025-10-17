package com.example.osgi.bundle3.impl;

import com.example.osgi.bundle1.Bundle1;
import com.example.osgi.bundle3.api.IBundle3;
import org.osgi.service.component.annotations.Component;

@Component(service = IBundle3.class)
public class Bundle3 implements IBundle3 {
    public String hello() {
        var bundle1 = new Bundle1();
        return bundle1.hello() + "called from bundle 3";
    }

}
