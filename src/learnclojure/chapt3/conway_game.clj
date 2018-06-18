(ns learnclojure.chapt3.conway-game)

;;实现conway's game of life
;;先定义一个初始化空白的板的函数
(defn empty-board
  [w h]
  (vec (repeat w (vec (repeat h nil)))))
;;一个修改格子的函数
(defn populate
  [board living-cells]
  (reduce (fn [board coordinates]
            (assoc-in board coordinates :on))
          board living-cells))
(def glider (populate (empty-board 6 6) #{[2 0] [2 1] [2 2] [1 2] [0 1]}))

(defn neighbours
  [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))

(defn count-neighbours
  [board loc]
  (count (filter #(get-in board %1) (neighbours loc))))
(defn indexed-step
  [board]
  (let [w (count board)
        h (count (first board))]
    (loop [new-board board x 0 y 0]
      (cond
        (>= x w) new-board
        (>= y h) (recur new-board (inc x) 0)
        :else
        (let [new-liveness
              (case (count-neighbours board [x y])
                2 (get-in board [x y])
                3 :on
                nil)]
          (recur (assoc-in new-board [x y] new-liveness) x (inc y)))))))
(defn indexed-step2
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce
     (fn [new-board x]
       (reduce
        (fn [new-board y]
          (let [new-liveness
                (case (count-neighbours board [x y])
                  2 (get-in board [x y])
                  3 :on
                  nil)]
            (assoc-in new-board [x y] new-liveness)))
        new-board (range h)))
     board (range w))))

(defn indexed-step3
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce
     (fn [new-board [x y]]
       (let [new-liveness
             (case (count-neighbours board [x y])
               2 (get-in board [x y])
               3 :on
               nil)]
         (assoc-in new-board [x y] new-liveness)))
     board (for [x (range w) y (range h)] [x y]))))

;;重构,去掉下标的使用
(defn window
  ([coll] (window nil coll))
  ([pad coll] (partition 3 1 (concat [pad] coll [pad]))))
(defn cell-block
  [[left mid right]]
  (window (map vector left mid right)))
(defn liveness
  [block]
  (let [[_ [_ center _] _] block]
    (case (- (count (filter #{:on} (apply concat block)))
             (if (= center :on) 1 0))
      2 center
      3 :on
      nil)))
(defn- step-row
  [row-triple]
  (vec (map liveness (cell-block row-triple))))
(defn index-free-step
  [board]
  (vec (map step-row (window (repeat nil) board))))
(= (nth (iterate index-free-step glider) 8)
   (nth (iterate indexed-step3 glider) 8))

;;使用更高层次的抽象
(defn step
  [cells]
  (set (for [[loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3) (and (= n 2) (cells loc)))]
         loc)))
(->> (iterate step #{[2 0] [2 1] [2 2] [1 2] [0 1]})
     (drop 8)
     first
     (populate (empty-board 6 6)))
;;让step函数更通用,可以支持任意形式的板子
(defn stepper
  [neighbours birth? survive?]
  (fn [cells]
    (set (for [[loc n] (frequencies (mapcat neighbours cells))
              :when (if (cells loc) (survive? n) (birth? n))]
          loc))))
;;之前的step和(stepper neighbours #{3} #{2 3})

;;使用stepper实现一个六边形的板子的游戏
(defn hex-neighbours
  [[x y]]
  (for [dx [-1 0 1] dy (if (zero? dx) [-2 2] [-1 1])]
    [(+ dx x) (+ dy y)]))
(def hex-step (stepper hex-neighbours #{2} #{3 4}))
(hex-step #{[0 0] [1 1] [1 3] [0 4]})