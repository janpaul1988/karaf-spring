package com.example.osgi.bundle3;

import com.example.osgi.bundle1.Bundle1;

public class Bundle3 {
    public String hello() {
        var bundle1 = new Bundle1();
        return bundle1.hello() + "called from bundle 3";
    }

}
