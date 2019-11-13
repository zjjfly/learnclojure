(ns learnclojure.ch10.fn-browser
  (:import [javax.swing JList JFrame JScrollPane JButton JOptionPane JTextArea]
           java.util.Vector))

;defonce定义的成员不会在命名空间reload的时候重新求值,这一般用于定义在程序运行的整个生命周期都不会改变的对象
(defonce fn-names (->> (ns-publics 'clojure.core)
                       (map key)
                       sort
                       Vector.
                       JList.))

;先声明这个方法,在初始化界面之后实现它,这样就可以看出repl可以动态的修改程序的行为
(declare show-info)

(defonce window (doto (JFrame. "\"Interactive Development\"")
                  (.setSize (java.awt.Dimension. 400 300))
                  (.add (JScrollPane. fn-names))
                  (.add java.awt.BorderLayout/SOUTH
                        (doto (JButton. "Show Info")
                          (.addActionListener (reify java.awt.event.ActionListener
                                                (actionPerformed [_ e] (show-info))))))
                  (.setVisible true)))

(defn show-info []
  (when-let [selected-fn (.getSelectedValue fn-names)]
    (JOptionPane/showMessageDialog
     window
     (-> (ns-resolve 'clojure.core selected-fn)
         meta
         :doc
         (JTextArea. 10 40)
         JScrollPane.)
     (str "Do string for clojure.core/" selected-fn)
     JOptionPane/INFORMATION_MESSAGE)))

(defn trigger-window
  []
  (.setVisible window (not (.isVisible window))))

;(trigger-window)
