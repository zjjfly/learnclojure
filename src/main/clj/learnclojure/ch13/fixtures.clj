(ns learnclojure.ch13.fixtures
  (:use [clojure.test])
  (:require [learnclojure.ch12.design-pattern :as dp]))

;fixture是一个用来设置和清理服务,数据库状态,模拟函数和测试数据的的函数
;从而确保命名空间中的所有测试在一个受控的环境里调用测试,类似Junit的@Before和@After
;它只是一个高阶函数,接受一个函数作为参数,这个函数就是实际要执行的测试函数
;有两种方式对一个命名空间使用fixture:
;1.对这个命名空间中的每一个测试方法都调用fixture
;2.一个命名空间只调用一次fixture
;这两种方式的fixture的实现都类似下面的代码
(defn some-fixture
  [f]
  (try
    ;;set up env
    (f)
    (finally
      ;;clean up env
)))
;fixture确实不如test-ns-hook灵活,但对于大多数情况,fixture是够用了
;fixture和test-ns-hook是互斥的,如果定义了后者,前者就不会被使用
(def ^:private dummy-petstore (dp/->PetStore (dp/->Chihuahua 12 "$95")))
(deftest test-configured-petstore
  (is (= (dp/configured-petstore) dummy-petstore)))

(run-tests)
;Testing learnclojure.ch13.fixtures
;
;FAIL in (test-configured-petstore) (fixtures.clj:24)
;expected: (= (dp/configured-petstore) dummy-petstore)
;actual: (not (= #learnclojure.ch12.design_pattern.PetStore{:dog #learnclojure.ch12.design_pattern.Chihuahua{:weight 12, :price "$84"}}
;#learnclojure.ch12.design_pattern.PetStore{:dog #learnclojure.ch12.design_pattern.Chihuahua{:weight 12, :price "$95"}}))
;
;Ran 1 tests containing 1 assertions.
;1 failures, 0 errors.
;=> {:test 1, :pass 0, :fail 1, :error 0, :type :summary}

;configured-petstore从磁盘中读取记录进行初始化,所以如果没有这个配置文件或配置文件的内容和预期的不符,这个测试就无法通过
;这个时候可以使用fixture来确保这个文件存在并内容符合预期
(defn petstore-config-fixture
  [f]
  (let [file (clojure.java.io/file "petstore_config.clj")
        origin (slurp file)]
    (try
      (spit file (with-out-str (pr dummy-petstore)))
      (f)
      (finally
        (spit file origin)))))
;注册fixture
(use-fixtures :once petstore-config-fixture)
;运行测试
(run-tests)
