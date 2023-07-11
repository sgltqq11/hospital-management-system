import java.util.ArrayList;
import java.util.List;

public class GlTest {
    public static void main(String[] args) {
        List list = new ArrayList();
        list.add("111");
        list.add("222");
        list.add("333");
        for (int i = 0; i < list.size(); i++) {
            String o = (String) list.get(i);
        }
    }
}
