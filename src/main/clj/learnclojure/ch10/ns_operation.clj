(ns learnclojure.ch10.ns-operation)

;repl提供的一些遍历的获取命名空间中的symbol到var或引进的类的映射
;获取命名空间中的别名(使用:as声明的别名)
(ns-aliases *ns*)

(defn- private-fn
  [])
;获取命名空间中的public成员
(contains? (ns-publics *ns*) 'private-fn)
;false

(def x 0)
(contains? (ns-publics *ns*) 'x)
;true
;删除symbol到var或引进的类的映射
(ns-unmap *ns* 'x)
(contains? (ns-publics *ns*) 'x)
;false

(alias (symbol "set") 'clojure.set)
(contains? (ns-aliases *ns*) 'set)
;true
;删除命名空间别名
(ns-unalias *ns* 'set)
(contains? (ns-aliases *ns*) 'set)
;false

;新建一个命名空间
(ns clean-ns)
(some #(= 'clean-ns (ns-name %)) (all-ns))
;true
;删除一个命名空间
(in-ns 'learnclojure.ch10.ns-operation)
(remove-ns 'clean-ns)
(some #(= 'clean-ns (ns-name %)) (all-ns))
;nil
;命名空间被删除之后,其中的var所定义的所有代码和数据都将无法被访问并等待GC.但如果有其他地方使用了它们,那不会被GC

;repl有些情况下也可以远程连接到生产环境进行监测,分析和作为偶尔的打补丁的工具
;一个分析运行时生成的日志的例子
(let [log-capacity 5000
      events (agent [])]
  (defn log-event [e]
    (send events #(if (== log-capacity (count %))
                    (-> % (conj e) (subvec 1))
                    (conj % e)))
    e)
  (defn events [] @events))
;模拟发送一些日志
(doseq [request (repeatedly 5000 #(rand-nth [{:referer "twitter.com"}
                                             {:referer "facebook.com"}
                                             {:referer "twitter.com"}
                                             {:referer "reddit.com"}]))]
  (log-event request))
(count (events))
(frequencies (events))

;在生产环境使用repl的注意点:
;1.所有改变都是临时的,只是修改了当前java进程,不会把修改反映到服务器的class文件中
;2.repl远程连接需要时安全的,并且最好对repl中传输的代码的访问权限进行控制,这一点可以通过clojail实现

;repl中可以重新定义部分程序的能力:从函数到顶层数据结构到deftype定义的类型和通过defrecord定义的协议和多重方法的实现
;但这种能力也有一些限制:
;1.classpath无法动态修改.当然这个可以通过使用pomegranata绕过这个限制
;2.gen-class生产的class无法动态修改
;3.类实例永远保留inline实现.由deftype和defrecord定义的类的inline接口和协议实现不能动态更新.
;解决方法是把实现委派给单独的函数,就像fn-browser中的基于reify实现的ActionListener
;在用extend实现协议时不是提供var命名的函数,而用#'写法直接提供var
;4.定义宏不会重新扩展对宏的使用.因为宏是在编译的时候起作用的,你需要重新载入所有使用这个宏的代码
;5.重新定义多重方法不会更新多次方法的转发函数.因为多重方法由defonce的语义.解决方法时ns-unmap来解除多重方法的var.
;这要求重新载入多重方法的每一个实现
;6.理解何时捕获一个值而不是解引用一个值.def等宏定义的var包含的是当前的值,如果只是一个简单的函数调用,那没问题,但如果
;var是另一个函数的名称,那就把var的值作为参数传递了,而不是var本身.下面是例子
(defn a [b]
  (+ 5 b))
(def b (partial a 5))
(b)
;10
(defn a [b]
  (+ 10 b))
(b)
;10,没有因为a的定义变化而变化
;解决办法
(def b (partial #'a 5))
(b)
(defn a [b]
  (+ 1 b))
(b)
;6
