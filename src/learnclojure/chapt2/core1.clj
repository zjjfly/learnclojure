(ns learnclojure.chapt2.core1
  (:import (learnclojure.chapt2 StatefulInteger)))
;;����ʽ��̵��ص㣺
;;1.ϲ���������ɱ�ֵ
;;2.ϲ�������ݽ�������ʽ�Ĵ���
;;3.ϲ��ͨ��������ϡ��߽׺����Ͳ��ɱ����ݽṹ����������
;;ֵ������ʱ��ĸı�ı�
(= 5 5)
(= 5 (+ 2 3))
(= "boot" (str "bo" "ot"))
(= nil nil)
(let [a 5]
  (= a 5))
;; ����javaʵ�ֵĿɱ�Integer������integer�ɱ��˻���ô��
(def five (StatefulInteger. 5))
(def six (StatefulInteger. 6))
(.intValue five)
(.intValue six)
(= five six)