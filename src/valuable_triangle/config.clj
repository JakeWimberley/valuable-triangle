(ns valuable-triangle.config
  (:gen-class))

(def show-debug-data false)

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

; font sizes are configurable for portability's sake
; e.g. award values in Japanese yen probably would be in the many thousands
(def font-size-subject-text 30)
(def line-spacing-subject-text (+ font-size-subject-text 6))
(def font-size-award-value 60)
(def line-spacing-award-value (+ font-size-award-value 15))

(load "subjects")
