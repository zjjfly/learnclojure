(ns learnclojure.ch11.mandelbrot-set
  (:import java.awt.image.BufferedImage
           (java.awt Color RenderingHints Graphics2D)
           (javax.imageio ImageIO)))

(defn- escape
  [a0 b0 depth]
  (loop [a a0
         b b0
         iteration 0]
    (cond
      (< 4 (+ (* a a) (* b b))) iteration
      (>= iteration depth) -1
      :else (recur (+ a0 (- (* a a) (* b b)))
                   (+ b0 (* 2 (* a b)))
                   (inc iteration)))))

(defn mandelbrot
  [rmin rmax imin imax & {:keys [width height depth]
                          :or   [width 80 height 49 depth 1000]}]
  (let [rmin (double rmin)
        imin (double imin)
        stride-w (/ (- rmax rmin) width)
        stride-h (/ (- imax imin) height)]
    (loop [x 0
           y (dec height)
           escapes []]
      (if (= x width)
        (if (zero? y)
          (partition width escapes)
          (recur 0 (dec y) escapes))
        (recur (inc x) y (conj escapes (escape (+ rmin (* x stride-w))
                                               (+ imin (* y stride-h))
                                               depth)))))))
(defn render-text
  [mandelbrot-grid]
  (doseq [row mandelbrot-grid]
    (doseq [escape-iter row]
      (print (if (neg? escape-iter) \* \space))
      println)))

(defn render-image
  [mandelbrot-grid]
  (let [palette (vec (for [c (range 500)]
                       (Color/getHSBColor 0.0 0.0 (/ (Math/log c) (Math/log 500)))))
        height (count mandelbrot-grid)
        width (count (first mandelbrot-grid))
        img (BufferedImage. width height BufferedImage/TYPE_INT_RGB)
        ^Graphics2D g (.getGraphics img)]
    (doseq [[y row] (map-indexed vector mandelbrot-grid)
            [x escape-iter] (map-indexed vector row)]
      (.setColor g (if (neg? escape-iter)
                     (palette 0)
                     (palette (mod (dec (count palette)) (inc escape-iter)))))
      (.drawRect g x y 1 1))
    (.dispose g)
    img))

(render-text (mandelbrot -2.25 0.75 -1.5 1.5 :width 80 :height 40 :depth 100))

;但如果要放大这个图,需要对escape进行优化
(do (time (mandelbrot -2.25 0.75 -1.5 1.5
                      :width 1600 :height 1200 :depth 1000))
    nil)
;Elapsed time: 1465.814648 msecs
(defn- escape
  [^double a0 ^double b0 depth]
  (loop [a a0
         b b0
         iteration 0]
    (cond
      (< 4 (+ (* a a) (* b b))) iteration
      (>= iteration depth) -1
      :else (recur (+ a0 (- (* a a) (* b b)))
                   (+ b0 (* 2 (* a b)))
                   (inc iteration)))))

(defn draw
  [img name]
  (ImageIO/write img "png" (clojure.java.io/file (str name ".png"))))

(draw (render-image (mandelbrot -2.25 0.75 -1.5 1.5 :width 800 :height 800 :depth 500)) "mandelbrot1")

(draw (render-image (mandelbrot -1.5 -1.3 -0.1 0.1 :width 800 :height 800 :depth 500)) "mandelbrot2")
