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

                                        ; colors
(def color-window-bg [0x80 0x4e 0x06])
(def color-chase-border [0xc6 0x32 0x06])
(def color-board-bg [0xb2 0xba 0xdd])
(def color-prism-frame-bright [0xd6 0x32 0x06])
(def color-prism-frame-dim [0xb5 0x19 0x13])
(def color-prism-subject-bg [255 255 255])
(def color-prism-subject-fg [0 0 0])
(def color-prism-notsubject-bg [0xb5 0x19 0x13])
(def color-prism-notsubject-fg [0xff 0xa5 0x2c])
(def color-infotext-fg [255 255 255])
(def color-infotext-bg [0 0 0])

(load "subjects")
