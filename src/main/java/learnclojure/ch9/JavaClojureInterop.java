package learnclojure.ch9;

import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 要使用自定义的命名空间,需要先获取clojure.core的require,use或load函数,然后使用这些函数加载命名空间 然后再用var函数获取到你想要使用的函数或值.对于函数,可以使用invoke进行调用
 *
 * @author z00405ze
 */
public class JavaClojureInterop {

    /**
     * fn和deref的区别是它为我们做了强制转换为IFn的操作
     */
    private static final IFn requireFn = RT.var("clojure.core", "require").fn();

    private static final IFn randIntFn = RT.var("clojure.core", "rand-int").fn();

    static {
        requireFn.invoke(Symbol.intern("learnclojure.ch9.histogram"));
    }

    private static final IFn frequencies = RT.var("clojure.core", "frequencies").fn();

    private static final Object keywords = RT.var("learnclojure.ch9.histogram", "keywords").deref();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void main(String[] args) {
        Map<Keyword, Integer> sampleHistogram = (Map<Keyword, Integer>) frequencies
            .invoke(keywords);
        System.out.println("Number of :a keyword in sample histogram: " + sampleHistogram
            .get(Keyword.intern("a")));
        System.out.println("Complete sample histogram:" + sampleHistogram);
        System.out.println();
        //由于clojure函数的高度抽象,所以frequencies可以作用于java的集合和字符串
        System.out.println("Histogram of chars in 'I left my heart in San Fransisco':" +
            frequencies.invoke("I left my heart in San Fransisco".toLowerCase()));
        List<Object> randInts = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            randInts.add(randIntFn.invoke(10));
        }
        System.out.println("Histogram of 500 random ints [0,10): " + frequencies.invoke(randInts));
    }

}
