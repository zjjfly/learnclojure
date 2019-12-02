(ns learnclojure.ch13.clojure-test
  (:use [clojure.test]))

(is (= 5 (+ 4 2)) "I never was very good at math...")
;FAIL in () (clojure_test.clj:4)
;I never was very good at math...
;expected: (= 5 (+ 4 2))
;actual: (not (= 5 6))

(is (re-find #"foo" "foobar"))
;"foo"

;is支持很多断言
;thrown?测试某一类型的错误是否会在表达式的求值过程中抛出
(is (thrown? ArithmeticException (/ 1 0)))
;#error{:cause "Divide by zero",
;       :via [{:type java.lang.ArithmeticException,
;              :message "Divide by zero",...

(is (thrown? ArithmeticException (/ 1 1)))
;FAIL in () (clojure_test.clj:14)
;expected: (thrown? ArithmeticException (/ 1 1))
;actual: nil

;thrown-with-msg?和thrown?类似,但同时还测试某个正则表达式能否在错误信息中找到
(is (thrown-with-msg? ArithmeticException #"zero" (/ 1 0)))
;#error{:cause "Divide by zero",
;       :via [{:type java.lang.ArithmeticException,
;              :message "Divide by zero",...
(is (thrown-with-msg? ArithmeticException #"zero" (inc Long/MAX_VALUE)))
;FAIL in () (clojure_test.clj:30)
;expected: (thrown-with-msg? ArithmeticException #"zero" (inc Long/MAX_VALUE))
;  actual: #error {
; :cause "integer overflow"

;使用testing宏来给测试添加文档,扩展测试报告
(testing "Strings"
  (testing "regex"
    (is (re-find #"foo" "foobar"))
    (is (re-find #"foo" "bar")))
  (testing ".contains"
    (is (.contains "foobar" "foo"))))
;FAIL in () (clojure_test.clj:40)
;Strings regex
;expected: (re-find #"foo" "bar")
;  actual: (not (re-find #"foo" "bar"))

;定义测试函数的两种方法:
;1.使用deftest宏
(deftest test-foo
  (is (= 1 1)))
(test-foo)
;所有的clojure.test测试实际在它们的元数据的:test加了一个函数
(:test (meta #'test-foo))
;#object[learnclojure.ch13.clojure_test$fn__1273 0x1cf41173 "learnclojure.ch13.clojure_test$fn__1273@1cf41173"]
;通过(test-foo)调用的函数只是委托给了这个函数,这使得把测试和被测试函数绑在一起成为可能
;2.with-test就可以很方便的帮你做到这点,它的第一个参数是被测试函数,接下来的参数作为测试函数的主体
(with-test
  (defn hello [name]
    (str "hello," name))
  (is (= "hello,jjzi" (hello "jjzi")))
  (is (= "hello,nil" (hello nil))))
((:test (meta #'hello)))
;FAIL in () (clojure_test.clj:62)
;expected: (= "hello,nil" (hello nil))
;  actual: (not (= "hello,nil" "hello,"))
;false

;run-tests函数可以根据这个元数据在一个或多个命名空间中找到并执行所有测试
(run-tests)
;Testing learnclojure.ch13.clojure-test
;
;FAIL in (hello) (clojure_test.clj:62)
;expected: (= "hello,nil" (hello nil))
;  actual: (not (= "hello,nil" "hello,"))
;
;Ran 2 tests containing 3 assertions.
;1 failures, 0 errors.
;{:test 2, :pass 2, :fail 1, :error 0, :type :summary}
;如果没有指定命名空间,使用*ns*

;这样定义测试有一个问题是会让测试一直存在,直到JVM进程关闭
;这种情况有两个办法:
;1.ns-unmap解除var的绑定
(ns-unmap *ns* 'hello)
;2.alter-meta!改变元数据中的:test
(with-test
  (defn hello [name]
    (str "hello," name))
  (is (= "hello,jjzi" (hello "jjzi")))
  (is (= "hello,nil" (hello nil))))
(alter-meta! #'hello dissoc :test)
(run-tests)
;Testing learnclojure.ch13.clojure-test
;
;Ran 1 tests containing 1 assertions.
;0 failures, 0 errors.
;{:test 1, :pass 1, :fail 0, :error 0, :type :summary}

;可以在测试中调用其他测试函数
(deftest a
  (is (== 0 (- 3 2))))

(deftest b (a))

(deftest c (b))

(c)
;FAIL in (c b a) (clojure_test.clj:102)
;expected: (== 0 (- 3 2))
;  actual: (not (== 0 1))
;=> nil

;但这种方式有一个问题,如果你使用run-tests运行所有测试,会重复调用一些已经包含在测试套件中的测试方法
;避免的方法有两个:
;1.在命名空间定义一个run-tests入口函数,名称必须是test-ns-hook
(defn test-ns-hook [] (c))
(run-tests)
;Testing learnclojure.ch13.clojure-test
;
;FAIL in (c b a) (clojure_test.clj:102)
;expected: (== 0 (- 3 2))
;  actual: (not (== 0 1))
;
;Ran 3 tests containing 1 assertions.
;1 failures, 0 errors.
;=> {:test 3, :pass 0, :fail 1, :error 0, :type :summary}

;2.把子断言放到常规函数中
(ns-unmap *ns* 'test-ns-hook)

(defn a
  []
  (is (== 0 (- 3 2))))
(defn b
  []
  (a))
(deftest c (b))

(run-tests)
;Testing learnclojure.ch13.clojure-test
;
;FAIL in (c) (clojure_test.clj:134)
;expected: (== 0 (- 3 2))
;  actual: (not (== 0 1))
;
;Ran 2 tests containing 2 assertions.
;1 failures, 0 errors.
;=> {:test 2, :pass 1, :fail 1, :error 0, :type :summary}
