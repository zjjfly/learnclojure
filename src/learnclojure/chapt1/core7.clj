(ns learnclojure.chapt1.core7
  (:import (java.util ArrayList)))
;; ����var��var
(def x 5)
x
;;��ʱ����Ҫ���ָ��var��������ã�������var��ֵ
(var x)
;;��repl�п������ܶ�var�����е���ʾ��#'�����һ������
;;clojure��Ҳ��һ��reader�﷨����ֵ��var���������ʽ
#'x

;;��java������:.��new
;;���к�java�Ļ���������ʼ������̬��ʵ���������û����ֶη��ʶ���ͨ��new��.������������ʽʵ�ֵ�
;;����һЩ�﷨��ʹ����������࣬���Һ�clojure�ķ��һ�¡����к��ٿ���ֱ����.��new�ġ�

;;�����ʼ����java���룺new java.util.ArrayList(100)
;;�﷨��
(ArrayList. 100)
;;������ʽ
(new ArrayList 100)

;;���þ�̬����,java���룺Math.pow(2,10)
;;�﷨��
(Math/pow 2 10)
;;������ʽ
(. Math pow 2 10)

;;����ʵ��������java���룺"hello".substring(1,3)
;;�﷨��
(.substring "hello" 1 3)
;;������ʽ
(. "hello" substring 1 3)

;;���ʾ�̬��Ա������java���룺Integer.MAX_VALUE
;;�﷨��
Integer/MAX_VALUE
;;������ʽ
(. Integer MAX_VALUE)

;;����ʵ����Ա����
;;java���룺someObj.someField
;;�﷨�ǣ�(.someField someObj)
;;������ʽ��(. someObj someField)

;;�쳣����,ʹ��try��throw����������ʽ

;;״̬�޸ģ�set��

;;��ԭ��:monitor-enter��monitor-exit����һ���ú�locking


;;eval��������һ��clojure��ʽ��Ȼ����������ʽ��ֵ���������ʹ��eval�ĵط������ú������
(eval :foo)
(eval [1 2 3])
(eval "ad")
(eval '(learnclojure.charpt1.core1/average [60 80 100 400]))

;;���ڿ����Լ�ʵ��һ��clojure��repl��
;;��read-string��ȡ�ַ�������evalִ�б��ʽ
(defn embedded-repl
  "A native Clojure REPL implementation.Enter ':quit' to exit"
  []
  (print (str (ns-name *ns*) ">>>"))
  (flush)
  (let [expr (read)
        value (eval expr)]
    (when (not= :quit value)
      (println value)
      (recur))))
(embedded-repl)