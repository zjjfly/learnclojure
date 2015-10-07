(ns learnclojure.chapt2.core2)
;;ƫ����vs����������
;;�����������ṩ��ƫ�������ܵĳ���������ͨ������������ʵ������ƫ�����Ĺ���
(#(filter string? %) [1 "D" 2 "T"])
(#(filter % [1 "D" 2 "T"]) string?)
(#(filter % [1 "D" 2 "T"]) number?)
;;����������Ҫ���ƶ����������в�������ƫ��������
;;������룺(#(map *) [1 3 5] [2 4 6] [8 9 0])��û��ָ������
(#(map * % %2 %3) [1 3 5] [2 4 6] [8 9 7])
;;������룺(#(map * % %2 %3) [1 3 5] [8 9 7])������Ĳ������ƶ��Ĳ�����������
;;һ�ֿ��еķ�����ʹ��apply
(#(apply map * %&) [1 3 5] [2 4 6] [8 9 7])
(#(apply map * %&) [1 3 5])
((partial map *) [123] [4 5 6])

;;���������
;;����һ���б�����֣�������Щ���ֵ��ܺ͵ĸ������ַ�����ʽ
(defn negated-sun-str1
  [& numbers]
  (str (- (apply + numbers))))
(negated-sun-str1 1 2 4)
;;Ҳ����д�ɣ�
;;(defn negated-sun-str1
;;       [numbers]
;;       (str (- (apply + numbers)))
;;    )
;;(negated-sun-str1 [1 2 4])

;;ʹ��compʵ�ֺ�����ϣ������
;;comp���ܵĲ���������comp�����һ���������ܵĲ���������ȣ�����ֵ��comp��һ�������ķ���ֵ
(def negated-sun-str2
  (comp str - +))
(negated-sun-str2 3 4 7)
;;comp�����ò�������дhello world������С����
;;���ӣ���CamelCaseʽ���ַ���ת��clojure���Ժ��߷ָ���Сд����
;;interpose���������е�Ԫ����ָ�����ַ��ָ�,keyword�������ַ�����Ϊ�ؼ���
(require '[clojure.string :as str])
(def camel->keyword1 (comp keyword
                          str/join
                          (partial interpose "-")
                          (partial map str/lower-case)
                          #(str/split % #"(?<=[a-z])(?=[A-Z])")))
(camel->keyword1 "NotBad")
;;����ʹ��->��->>��ʵ�����Ƶ�comp�Ĺ���
;;������Щ��ĵ�һ����������Ϊ���溯���ĵ�һ��(->)�����һ������(->>),��������
(defn  camel->keyword2
  [s]
  (->> (str/split  s #"(?<=[a-z])(?=[A-Z])")
       (map  str/lower-case)
       (interpose "-")
       str/join
       keyword))
(camel->keyword2 "HeHe")
;;�Ѵ����map�е�CamelCase��keyת����clojure����key
;;map-indexed��index��map����
(def camel->pairs->map (comp (partial apply hash-map)
                             (partial map-indexed (fn [i x]
                                                    (if (odd? i)
                                                      x
                                                      (camel->keyword1 x))))))
(camel->pairs->map ["CamelCase" 5 "lowCamelCase" 33])