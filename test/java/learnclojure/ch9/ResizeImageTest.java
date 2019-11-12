package learnclojure.ch9;

import org.junit.Test;

/**
 * @author zjjfly
 */
public class ResizeImageTest {

    @Test
    public void resizeFile() {
        ResizeImage
            .resizeFile("target/default/classes/resources/clj.png", "/Users/zjjfly/Desktop/1.png",
                "0.5");
    }

}
