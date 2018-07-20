(ns learnclojure.chapt4.core8)

;;clojure的作用域和其他语言一样是词法意义上的作用域,也就是一个var的作用域在定义它的形式之内
(let [a 1
      b 2]
  (println (+ a b))
  (let [b 3
        + -]
    (println (+ a b))))
;3
;-2,b重新复制为3,函数-覆盖了函数+

;;有一个例外是:var支持动态作用域.var都有一个根绑定,如果var的元数据:dynamic是true,那么可以用binding在每个线程中覆盖它的根绑定
;;对于动态var,一般会在命名的时候前后加*
(def ^:dynamic *max-value* 255)
(defn valid-value?
  [v]
  (<= v *max-value*))
(binding [*max-value* 500]
  (valid-value? 299))
;true
;;binding只是修改了*max-value*在本线程中的值,其他线程中*max-value*还是原来的值
(binding [*max-value* 300]
  (println (valid-value? 299))
  (doto (Thread. #(println "In other thread:" (valid-value? 299)))
    .start .join))
;;动态作用域广泛用于提供一种修改API默认配置的方法
;;动态作用域的原理是ThreadLocal,一个var可以有任意数量的线程本地绑定,它们放在一个栈中,在这些绑定中,只有位于栈顶的绑定能被访问到
(def ^:dynamic *var* :root);根绑定
(defn get-*var* [] *var*)
(binding [*var* :a]
  (binding [*var* :b]
    (binding [*var* :c]
      (get-*var*))))
;:c

;;动态作用域对函数行为的控制实际上是为函数添加了隐式参数
;;它不止可以把一个参数从调用树上层传到下层,还可以让下层的函数通过这个动态作用域给上层的函数返回一些东西
;;例子,获取url请求返回的状态码
;;如果不使用动态作用域
(defn http-get
  [url]
  (let [conn (-> url java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (if (== 404 response-code)
      [response-code]
      [response-code (-> conn .getInputStream slurp)])))
(http-get "http://www.acfun.cn/v/ac44514651")
;;这种实现迫使我们调用之后还要对返回中的状态码进行处理,使用动态作用域可以实现只有当状态是我们感兴趣的时候才去获取获取url的内容
(def ^:dynamic *response-code* nil)
(defn http-get
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (when (thread-bound? #'*response-code*)
      (set! *response-code* response-code))
    (when (not= 404 response-code) (-> conn .getInputStream slurp))))
(http-get "http://www.baidu.com")
;<!DOCTYPE html><!--STATUS OK--><html> <head><meta http-equiv=content-type content=text/html;...
*response-code*
;nil
(binding [*response-code* nil]
  (let [content (http-get "http://www.baidu.com")]
    (println "Response code is:" *response-code*)))
;Response code is: 200

;通过set!来设置动态var在本地绑定的当前值,因此通过binding建立的本地绑定的调用栈的上层还是上50层都可以拿到这个值
;这对于任意数量的var,任意数量的绑定,也不管通过set!设置的是什么类型的值都可以

;;动态绑定会通过clojure原生的并发形式传播,称为绑定传播,当使用agent(send,sendoff),future,pmap和它的变种
(binding [*max-value* 500]
  (println (valid-value? 299))
  @(future (valid-value? 299)))
;true
;true

;虽然pmap支持绑定传播,要注意的是一般的惰性序列不支持绑定传播
(binding [*max-value* 500]
  (map valid-value? [299]))
;(false)

;;def定义的是顶级的var,不管你是在哪定义的
;;除了动态作用域之外,var一般是用来保存一些值,这些值知道程序或repl结束都不会变.如果需要一个可以修改的对象,请使用其他引用类型

;;如果实在想要修改var的根绑定,需要使用alter-var-root函数
(def x 0)
(alter-var-root #'x inc)
;;可以通过with-redefs来临时修改一些var的根绑定,这个对于测试比较有用,可以让我们对一些函数或一些函数依赖的和环境有关的变量进行mock
(defn increase-x
  []
  (inc x))
(with-redefs [x 10]
  (increase-x))
;11

;;前缀声明,就是在定义var的时候暂时不赋值.由于clojure的编译和求值是按源码中的顺序来的,所以要求引用var之前先定义好它.
;;如果var只在运行的时候才需要,例如是某些函数的占位符,那么可以在后面重新定义这些var,给它们真正的值,这种就叫前置声明
(def j)
j
;;#object[clojure.lang.Var$Unbound
;;使用declare进行前置声明更好,因为可以一次设置多个
(declare fn1 fn2)
(defn public-api-fun
  [arg1 arg2]
  (fn1 arg1 arg2 (fn2 arg1 arg2)))
(defn fn1
  [x y z]
  (+ x y z))
(defn fn2
  [x y]
  (/ x y))

