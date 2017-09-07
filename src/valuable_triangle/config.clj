(ns valuable-triangle.config
  (:gen-class))

; gameplay

(def game-length-sec 60)

(def level-equivs {
  1 (set '(1 2))
  2 (set '(2 3))
  3 (set '(2 3))
  4 (set '(4 5))
  5 (set '(4 5))
  6 (set '(6))
})

(def subject-values [50 100 150 200 250 300])

(def value-symbol "$")

(def grand-prize-value 10000)


; look-n-feel

(def spec-frame-rate 30)

(load "subjects")
