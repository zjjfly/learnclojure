(ns learnclojure.chapt4.rpg)

(def daylight (ref 1))
(defn attack
  [aggressor target]
  (dosync
    (let [damage (* (rand 0.1) (:strength @aggressor) (ensure daylight))]
      (println damage "," @daylight)
      (commute target update-in [:health] #(max 0 (- % damage))))))
(defn enforce-max-health
  [{:keys [name max-health]}]
  (fn [character-data]
    (or (<= (:health character-data) max-health)
        (throw (IllegalArgumentException. (str name " is already at max health"))))))
(defn heal
  [healer target]
  (dosync
    (let [aid (min (* (rand 0.1) (:mana @healer))
                   (- (:max-health @target) (:health @target)))]
      (when (and (pos? aid) (pos? (:mana @healer)))
        (commute healer update :mana - (max 5 (/ aid 5)))
        (alter target update :health + aid)))))
(defn character
  [name & {:as opts}]
  (let [cdata (merge {:name name :items #{} :health 500} opts)
        cdata (assoc cdata :max-health (:health cdata))
        validators (list* (enforce-max-health cdata) (:validators cdata))]
    (ref (dissoc cdata :validators)
         :validator #(every? (fn [v] (v %)) validators))))
(def alive? (comp pos? :health))
(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 200))))