(ns valuable-triangle.game
  (:require [valuable-triangle.config :as config])
  (:gen-class))

(def subjects-with-equiv
  "the list of subjects with equivalences applied"
  (map
   #(vector (first %) (conj (get config/level-equivs (last %)) (last %)))
   config/subjects)
)

(defn get-random-for-level
  "Select a random subject at given level."
  [lev]
  (first (rand-nth (filter #(contains? (last %) lev) subjects-with-equiv)))
)

(defn new-triangle
  "Generate a full set of subjects for a new game of Valuable Triangle."
  [& args]
  (for [x (range 1 7)] (get-random-for-level x))
;  (loop [level-count 1 category-set ()]
;    (when (<= level-count 6)
;      (recur (inc level-count) (conj category-set (get-random-for-level level-count)))))
)

(defn buzz
  "remove current subject from list"
  [subjects-remaining]
  (pop subjects-remaining)
)

(defn ding
  "move subject from remaining list to correct list, remove from pass list if present"
  [subjects-correct subjects-remaining subjects-passed]
  [
   (conj subjects-correct (peek subjects-remaining))
   (pop subjects-remaining)
   (into [] (remove #(= (peek subjects-remaining) %) subjects-passed))
  ]
)

(defn pass
  "move subject to end of remaining list; also copy to passed list"
  [subjects-passed subjects-remaining]
  [(conj subjects-passed (peek subjects-remaining)) (apply vector (peek subjects-remaining) (pop subjects-remaining))]
)
