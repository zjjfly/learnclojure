;命名空间的名称如果含有连字符-,如namespace-file,则对应的源文件会使用下划线代替连字符
;原因是JVM不允许包名或类名含有连字符
;还要避免声明单节段命名空间(节段指的是以.号分隔的单词)
(ns learnclojure.ch8.namespace-file)

;要避免命名空间的循环引用

;clojure是从前向后解析的,所以如果使用了一个没有声明的var会报错,但可以通过使用declare来避免这个问题
(declare constant b)
(defn a [x] (+ constant (b x)))
(def constant 42)
(defn b [y] (max y constant))
(a 66)
;108
