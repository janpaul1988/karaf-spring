package com.example.osgi.bundle4;

import com.example.osgi.bundle2.Bundle2;
import com.example.osgi.bundle3.Bundle3;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {
    @GetMapping
    public String getMessage() {
        return new Bundle2().hello() + new Bundle3().hello();
    }
}
