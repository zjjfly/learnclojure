package learnclojure.ch9;

import org.junit.Test;

import java.net.URL;

/**
 * @author zjjfly
 */
public class ResizeImageTest {
    @Test
    public void resizeFile() {
        URL image = Thread.currentThread().getContextClassLoader().getResource("clj.png");
        System.out.println(image);
        assert image != null;
        ResizeImage.resizeFile(image.toString(), "/Users/zjjfly/Desktop/1.png", "0.5");
    }

}
