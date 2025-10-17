package com.example.osgi.bundle4.spring;

import com.example.osgi.bundle2.api.IBundle2;
import com.example.osgi.bundle3.api.IBundle3;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/hello"})
public class HelloController {

    private IBundle2 bundle2;
    private IBundle3 bundle3;

    // Lazy fetch to match dynamic osgi service context
    public IBundle2 getBundle2() {
        if (bundle2 == null) {
            bundle2 = OsgiServiceLocator.getService(IBundle2.class);
        }
        return bundle2;
    }

    // Lazy fetch to match dynamic osgi service context
    public IBundle3 getBundle3() {
        if (bundle3 == null) {
            this.bundle3 = OsgiServiceLocator.getService(IBundle3.class);
        }
        return bundle3;
    }

    @GetMapping
    public String getMessage() {
        if (getBundle2() != null && getBundle3() != null) {
            return "Hello World!" + bundle2.hello() + bundle3.hello();
        }
        return "Hello lonely World without bundle service availability!";
    }
}
