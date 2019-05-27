(ns learnclojure.ch9.imaging
  (:use [clojure.java.io :only [file]])
  (:import [java.awt Image Graphics2D]
           javax.imageio.ImageIO
           java.awt.image.BufferedImage
           java.awt.geom.AffineTransform))
(defn load-img
  ^BufferedImage [file-or-path]
  (-> file-or-path
      file
      ImageIO/read))

(defn resize-image
  ^BufferedImage [^Image original factor]
  (let [scaled (BufferedImage. (* factor (.getWidth original nil))
                               (* factor (.getHeight original nil))
                               BufferedImage/TYPE_INT_RGB)]
    (.drawImage ^Graphics2D (.getGraphics scaled)
                original
                (AffineTransform/getScaleInstance factor factor)
                nil)
    scaled))
(gen-class
 :name learnclojure.ch9.ResizeImage
 :main true
 :methods [^static [resizeFile [String String String] void]
           ^static [resize [java.awt.Image double] java.awt.image.BufferedImage]])
(def ^:private -resize resize-image)
(defn- -resizeFile
  [path outpath factor]
  (ImageIO/write (-> path load-img (resize-image factor))
                 "png"
                 (file outpath)))
(defn -main
  [& [path outpath factor]]
  (when-not (and path outpath factor)
    (println "Usage: java -jar example-uberjar.jar ResizeImage [INFILE] [OUTFILE] [SCALE]")
    (System/exit 1))
  (-resizeFile path outpath (Double/parseDouble factor)))
