(ns learnclojure.ch6.core5)

;实现自己的符合clojure抽象的set

;先定义一个工具函数用于查看实现set需要实现那些接口和方法
(defn scaffold
  [interface]
  (doseq [[iface methods] (->> interface
                               .getMethods
                               (map #(vector (.getName (.getDeclaringClass %))
                                             (symbol (.getName %))
                                             (count (.getParameterTypes %))))
                               (group-by first))]
    (println (str " " iface))
    (doseq [[_ name argcount] methods]
      (println
       (str " "
            (list name (into '[this] (take argcount (repeatedly gensym)))))))))
(scaffold clojure.lang.IPersistentSet)

(declare empty-array-set)
(def ^:private ^:const max-size 4)
(deftype ArraySet [^objects items
                   ^int size
                   ^:unsynchronized-mutable ^int hashcode]
  clojure.lang.IPersistentSet
  (get [this x]
    (loop [i 0]
      (when (< i size)
        (if (= x (aget items i))
          (aget items i)
          (recur (inc i))))))
  (contains [this x]
    (boolean
     (loop [i 0]
       (when (< i size)
         (or (= x (aget items i)) (recur (inc i)))))))
  (disjoin [this x]
    (loop [i 0]
      (if (= i size)
        this
        (if (not= x (aget items i))
          (recur (inc i))
          (ArraySet. (doto (aclone items)
                       (aset i (aget items (dec size)))
                       (aset (dec size) nil))
                     (dec size)
                     -1)))))
  clojure.lang.IPersistentCollection
  (count [this] size)
  (cons [this x]
    (cond
      (contains? this x) this
      (== size max-size) (into #{x} this)
      :else (ArraySet. (doto (object-array (inc size))
                         ((partial #(System/arraycopy items 0 % 0 size)))
                         (aset size x))
                       (inc size)
                       -1)))
  (empty [this] empty-array-set)
  (equiv [this that] (.equals this that))
  clojure.lang.Seqable
  (seq [this] (take size items))
  Object
  (hashCode [this]
    (when (== -1 hashcode)
      (set! hashcode (int (areduce items idx ret 0
                                   (unchecked-add-int ret (hash (aget items idx)))))))
    hashcode)
  (equals [this that]
    (or
     (identical? this that)
     (and (or (instance? java.util.Set that)
              (instance? clojure.lang.IPersistentSet that))
          (= (count this) (count that))
          (every? #(contains? this %) that))))
  clojure.lang.IFn
  (invoke [this key] (.get this key))
  (applyTo [this args]
    (when (not= 1 (count args))
      (throw (clojure.lang.ArityException. (count args) "ArraySet")))
    (this (first args)))
  java.util.Set
  (isEmpty [this] (zero? size))
  (size [this] size)
  (toArray [this array]
    (.toArray ^java.util.Collection (sequence items) array))
  (toArray [this]
    (into-array (seq this)))
  (iterator [this] (.iterator ^java.util.Collection (sequence this))) ;sequence不会返回null,seq会,所以使用前者
  (containsAll [this colls]
    (every? #(contains? this %) colls)))
(def ^:private empty-array-set (ArraySet. (object-array max-size) 0 -1))
(defn
  array-set
  [& vals]
  (into empty-array-set vals))
(array-set)
(conj (array-set) 1)
(apply array-set "hello")
(get (apply array-set "hello") \w)
(get (apply array-set "hello") \h)
(contains? (apply array-set "hello") \h)
((apply array-set [1 2]) 2)
;apply实际调用的是IFn的applyTo方法
(apply (apply array-set [1 2]) [2])
(= #{3 1 2 0} (array-set 0 1 2 3))

;一个简单的benchmark测试函数
(defn micro-benchmark
  [f & {:keys [size trials] :or {size 4 trials 1e6}}]
  (let [items (repeatedly size gensym)]
    (time (loop [s (apply f items)
                 n trials]
            (when (pos? n)
              (doseq [x items] (contains? s x))
              (let [x (rand-nth items)]
                (recur (-> s (disj x) (conj x)) (dec n))))))))
(doseq [n (range  1 5)
        f [#'array-set #'hash-set]]
  (println n (-> f meta :name) ": ")
  (micro-benchmark @f :size n))
