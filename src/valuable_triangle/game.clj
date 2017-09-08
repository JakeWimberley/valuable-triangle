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
  "Select a random subject at given level.
   With one integer argument, pick a random subject equivalent to that level and return it as a string.
   With three arguments [level chosen-subjects unchosen-subjects-with-equiv], return a vector of the
   next level, the new chosen-subjects vector and the rest of unchosen-subjects-with-equiv.
   The three-arg version is meant for recursive selection of unique subjects despite
   each subject possibly matching multiple levels."
  ([lev]
   (first (rand-nth (filter #(contains? (last %) lev) subjects-with-equiv))))
  ([lev chosen-subjects unchosen-subjects-with-equiv]
   (let [chosen-subject (first (rand-nth (filter #(contains? (last %) lev) unchosen-subjects-with-equiv)))]
     [(inc lev) (conj chosen-subjects chosen-subject) (remove #(= (first %) chosen-subject) unchosen-subjects-with-equiv)]
     ))
)

(defn new-triangle
  "Generate a full set of subjects for a new game of Valuable Triangle."
  [& args]
  (loop [randomizer-args [1 [] subjects-with-equiv]]
    (if (> (nth randomizer-args 0) 6)
        (nth randomizer-args 1)
        (recur (get-random-for-level (nth randomizer-args 0) (nth randomizer-args 1) (nth randomizer-args 2)))))
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
