package com.example.osgi.bundle2.impl;

import com.example.osgi.bundle2.api.IBundle2;
import org.osgi.service.component.annotations.Component;

@Component(service = IBundle2.class)
public class Bundle2 implements IBundle2 {

    public String hello(){
        return "hello from bundle2";
    }

}
