(ns valuable-triangle.game
  (:require [valuable-triangle.config :as config])
  (:gen-class))

(def subjects-with-equiv
  "the list of subjects with equivalences applied"
  (map
   #(vector (first %) (conj (get config/level-equivs (last %)) (last %)))
   config/subjects)
)

(defn filter-subjects-with-equiv
  "With one arg [LEV], return a fn that will filter the subject list by LEV.
   With two args [LEV WORD], return a fn that will filter by LEV and omit subjects containing WORD. This allows, for example, only one 'What a ... would say' or 'Why you ...' subject per game."
  ([lev]
   #(contains? (last %) lev)
   )
  ([lev word]
   #(if (and (contains? (last %) lev) (nil? (re-find (re-pattern word) (first %)))) true false)
   )
  )

(defn get-random-for-level
  "Select a random subject at given level.
   With one integer argument, pick a random subject equivalent to that level and return it as a string.
   With three arguments [level chosen-subjects unchosen-subjects-with-equiv], return a vector of the
   next level, the new chosen-subjects vector and the rest of unchosen-subjects-with-equiv.
   The three-arg version is meant for recursive selection of unique subjects despite
   each subject possibly matching multiple levels."
  ([lev]
   (first (rand-nth (filter (filter-subjects-with-equiv lev) subjects-with-equiv))))
  ([lev chosen-subjects unchosen-subjects-with-equiv]
   (if (nil? (some #(re-find #"What" %) chosen-subjects)) ; if a "What a ... would say" subject has not already been chosen
     (let [chosen-subject (first (rand-nth (filter (filter-subjects-with-equiv lev) unchosen-subjects-with-equiv)))] ; any subject possible
       [(inc lev) (conj chosen-subjects chosen-subject) (remove #(= (first %) chosen-subject) unchosen-subjects-with-equiv)]
       )
     (let [chosen-subject (first (rand-nth (filter (filter-subjects-with-equiv lev "What") unchosen-subjects-with-equiv)))] ; else skip that kind of subject 
       [(inc lev) (conj chosen-subjects chosen-subject) (remove #(= (first %) chosen-subject) unchosen-subjects-with-equiv)]
       )
     )
   )
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
