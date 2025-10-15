package com.example.osgi.bundle4.spring;

import com.example.osgi.bundle2.Bundle2;
import com.example.osgi.bundle3.Bundle3;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/hello"})
public class HelloController {

    @GetMapping
    public String getMessage() {

        var bundle2 = new Bundle2();
        var bundle3 = new Bundle3();

        return "Hello World!" + bundle2.hello() + bundle3.hello();
    }
}
