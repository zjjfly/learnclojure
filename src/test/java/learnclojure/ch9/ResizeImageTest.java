package learnclojure.ch9;

import org.junit.Test;

import java.net.URL;

/**
 * @author zjjfly
 */
public class ResizeImageTest {

    @Test
    public void resizeFile() {
        URL image = ResizeImageTest.class.getClassLoader().getResource("clj.png");
        assert image!=null;
        ResizeImage
            .resizeFile(image, "/Users/zjjfly/Desktop/1.png",
                        "0.5");
    }

}
