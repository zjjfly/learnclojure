package learnclojure.ch9;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import learnclojure.ch9.classes.OrderSummary;
import learnclojure.ch9.classes.Range;

/**
 * @author z00405ze
 */
public class ClojureClassInJava {

    private static final IFn requireFn = RT.var("clojure.core", "require").fn();

    static {
        requireFn.invoke(Symbol.intern("learnclojure.ch9.classes"));
    }

    private static final IFn stringRangeFn = RT.var("learnclojure.ch9.classes", "string-range")
        .fn();

    public static void main(String[] args) {
        Range range = new Range(0, 5);
        System.out.print(range.start + "-" + range.end + ": ");
        for (Object i : range) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (Object i : (Range) stringRangeFn.invoke("5", "10")) {
            System.out.print(i + " ");
        }
        System.out.println();
        OrderSummary orderSummary = new OrderSummary(12345, "$19.45");
        System.out.println(String
            .format("order number: %s,order total: %s", orderSummary.order_number,
                orderSummary.total));
        System.out.println(orderSummary.keySet());
        System.out.println(orderSummary.values());
    }

}
