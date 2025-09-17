import com.example.osgi.bundle2.Bundle2;
import com.example.osgi.bundle3.Bundle3;

public class Bundle4 {
    public String hello(){
        var bundle2 = new Bundle2();
        var bundle3 = new Bundle3();
        return bundle2.hello() + bundle3.hello() + "called from bundle 4";
    }
}
