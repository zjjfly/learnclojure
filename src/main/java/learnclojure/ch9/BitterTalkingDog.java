package learnclojure.ch9;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import learnclojure.ch9.protocol.Talkable;

/**
 * @author z00405ze
 */
public class BitterTalkingDog implements Talkable {

    @Override
    public Object speak() {
        return "You probably expect me to say 'Woof!',don't you? Typical.";
    }

    Talkable mellow() {
        return new Talkable() {
            @Override
            public Object speak() {
                return "It's a wonderful day,don't you think?";
            }
        };
    }

    public static void main(String[] args) {
        IFn require = RT.var("clojure.core", "require").fn();
        require.invoke(Symbol.intern("learnclojure.ch9.protocol"));
        IFn speakFn = RT.var("learnclojure.ch9.protocol", "speak")
            .fn();
        BitterTalkingDog dog = new BitterTalkingDog();
        System.out.println(dog.speak());
        System.out.println(speakFn.invoke(5));
        System.out.println(speakFn.invoke("Hello,World!"));
        System.out.println(speakFn.invoke(dog.mellow()));
    }
}
