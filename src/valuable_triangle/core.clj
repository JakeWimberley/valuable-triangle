(ns valuable-triangle.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.string :as cljstr]
            [valuable-triangle.config :as config]
            [valuable-triangle.game :as game]))


(defn shadow-text
  "Draw text twice within the rectangle x-min y-min x-max y-max, first in bg color, then in fg color, with specified offset."
  [text-str offset fg bg x-min y-min x-max y-max]
  (apply q/fill bg)
  (q/text text-str (+ x-min offset) (+ y-min offset) (- x-max offset) (- y-max offset))
  (apply q/fill fg)
  (q/text text-str x-min y-min x-max y-max)
  )

(defn setup []
  (q/frame-rate config/spec-frame-rate)
                                        ; Load our fonts
  (def font-subject-text (q/create-font "Oswald-Regular.ttf" config/font-size-subject-text true))
  (def font-award-value (q/create-font "Shrikhand-Regular.ttf" config/font-size-award-value true))
  (def font-grand-prize-value (q/create-font "Shrikhand-Regular.ttf" (* 2 config/font-size-award-value) true))
  ; Load the SVG shapes for the timer display
  (let [segment-names '("a" "b" "c" "d" "e" "f" "g")]
    (def timer-shapes-tens (zipmap (map keyword segment-names) (map #(q/load-shape (str "elements/tens-" % ".svg")) segment-names)))
    (def timer-shapes-ones (zipmap (map keyword segment-names) (map #(q/load-shape (str "elements/ones-" % ".svg")) segment-names)))
  )
  (def digit-patterns {
    :0 [:a :b :c :d :e :f]
    :1 [:b :c]
    :2 [:a :b :g :e :d]
    :3 [:a :b :c :d :g]
    :4 [:b :c :f :g]
    :5 [:a :f :g :c :d]
    :6 [:f :e :d :c :g]
    :7 [:a :b :c]
    :8 [:a :b :c :d :e :f :g]
    :9 [:g :f :a :b :c]
  })
  ; setup function returns initial state.
  {:game-phase 0
   :timer (+ config/game-length-sec 1) ; add 1 for the initial second
   :subjects-correct []
   :subjects-remaining [5 4 3 2 1 0] ; reverse order since push/pop used
   :key-pressed nil
   :subject-list-key-index 0})

(def game-phases
  {:title-screen 0
   :pause-before-game 1
   :timer-running 2
   :pause-after-game 3
   :end-screen 4})

(config/load-subjects-from-dir "elements")

; origin points of subject "prisms" and the illuminated "frames"
(def subject-rect-positions
  [[52.341 437.204] [306.851 437.204] [561.361 437.204] [179.596 256.154] [434.106 256.154] [306.851 74.397]])
(def frame-rect-positions
  [[41.372 425.932] [295.882 425.932] [550.392 425.932] [168.627 244.883] [423.137 244.883] [295.882 63.125]])

; vertices of the "pyramid" logo quad relative to an origin point
(def logo-quad-points
  [[0 0] [159.966 0] [90.845 -116.519] [71.097 -116.519]])
; and here are the origins for each prism
(def logo-origins
  [[65.511 562.28] [320.021 562.28] [574.531 562.28] [192.766 381.231] [447.276 381.231] [320.031 199.473]])
; Maybe this is a lib fn in Clojure...I don't know...
(defn vector-add [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn update-runner [last-keypress state]
  (let [sgp (:game-phase state)
        sc (:subjects-correct state)
        sp (:subjects-passed state)
        sr (:subjects-remaining state)
        slki (:subject-list-key-index state)
        slki-max (dec (count (keys config/subjects)))
        next-tick (- (:timer state) (if (zero? (mod (q/frame-count) config/spec-frame-rate)) 1 0))]
    (case sgp
      ; title screen: press key to advance to next phase, otherwise do not change state
      0 (if (= :a last-keypress) (assoc state :game-phase (inc sgp) :key-pressed last-keypress)
                                 (assoc state :key-pressed nil))
      ; pause before game: select subject list
      1 (case last-keypress
          :l (if (>= slki slki-max)
               (assoc state :subject-list-key-index 0)
               (assoc state :subject-list-key-index (inc slki)))
          :h (if (<= slki 0)
               (assoc state :subject-list-key-index slki-max)
               (assoc state :subject-list-key-index (dec slki)))
          ; start game
          :s (do
               (def subject-text (game/new-triangle (nth (sort (keys config/subjects)) slki)))
               (assoc state :game-phase (inc sgp) :key-pressed last-keypress)
               )
          ; default: no change in state
          (assoc state :key-pressed nil)
          )
      2
        ; if time ran out, or no subjects are remaining, jump to end phase
        (if (or (<= (:timer state) 0) (= 0 (count (:subjects-remaining state))))
          {:game-phase (:pause-after-game game-phases)
           :timer (max (:timer state) 0)
           :subjects-correct sc
           :subjects-passed sp
           :subjects-remaining sr
           :subject-list-key-index slki
           :key-pressed last-keypress}
          ; else we need to decrement timer one frame at a time and check for user responses
          (case last-keypress
            :c (let [ding-sc-sr (game/ding sc sr sp)]
              {
                :game-phase sgp
                :timer next-tick
                :subjects-correct (first ding-sc-sr)
                :subjects-passed (last ding-sc-sr)
                :subjects-remaining (second ding-sc-sr)
                :subject-list-key-index slki
                :key-pressed :g ; g is "dead key"
              })
            :b (let [buzz-sr (game/buzz sr)]
              {
                :game-phase sgp
                :timer next-tick
                :subjects-correct sc
                :subjects-passed sp
                :subjects-remaining buzz-sr
                :subject-list-key-index slki
                :key-pressed last-keypress
              })
            :p (let [pass-sp-sr (game/pass sp sr)]
              {
                :game-phase sgp
                :timer next-tick
                :subjects-correct sc
                :subjects-passed (first pass-sp-sr)
                :subjects-remaining (second pass-sp-sr)
                :subject-list-key-index slki
                :key-pressed last-keypress
              })
            ; reset - run setup again to reset subjects, but keep timer running
            :R (assoc (setup) :timer next-tick :game-phase (:timer-running game-phases))
            ; reset timer only
            :Z (assoc state :timer config/game-length-sec :key-pressed last-keypress)
            ; default: no key was pressed so only state change is new timer value; also clear key-pressed
            (assoc state :timer next-tick :key-pressed nil)
          )
        )
        3 (if (= :e last-keypress)
            {:game-phase 4
             :timer (+ config/game-length-sec 1)
             :subjects-correct []
             :subjects-remaining [5 4 3 2 1 0] ; reverse order since push/pop used
             :key-pressed last-keypress
             :subject-list-key-index (:subject-list-key-index state)}
            (assoc state :key-pressed nil))
      4 (if (= :r last-keypress) (assoc state :game-phase (:pause-before-game game-phases) :key-pressed last-keypress)
            (assoc state :key-pressed nil))
    )
  )
)

(defn update-state [state]
  ;(if (and (not (q/key-pressed?)) (:key-pressed state)) ; if key went up since last check
  (if (and (q/key-pressed?) (nil? (:key-pressed state))) ; if key went down since last check
    (update-runner (q/key-as-keyword) state)             ; let key get used this cycle
    (update-runner :g state)                             ; else do not register keypress
  )
)

(defn draw-state [state]
  ; A nice 70s brown for the background color.
  (apply q/background config/color-window-bg)
  (q/no-stroke)
  ; Always draw the "big board."
  (apply q/fill config/color-chase-border) ; border with chase lights
  (q/begin-shape)
  (doseq [p [[255 0] [0 417.5] [0 600] [800 600] [800 417.5] [545 0]]]
    (apply q/vertex p))
  (q/end-shape)
  (apply q/fill config/color-board-bg)
  (q/begin-shape)
  (doseq [p '([280 0] [0 458.75] [0 600] [800 600] [800 458.75] [520 0])]
    (apply q/vertex p))
  (q/end-shape)
  (q/text-align :center :center)
  ; Always draw the frames around the rotating "prisms," but the color changes when a subject has been guessed.
  ; We'll also draw the logo "side" of each prism. The other "sides" will be drawn over this if necessary.
  (doseq [subject-index (range 0 6)]
    (let [frame-pos (nth frame-rect-positions subject-index)
          prism-pos (nth subject-rect-positions subject-index)]
      (if (nil? (some #{subject-index} (:subjects-correct state)))
        (do
          (apply q/fill config/color-prism-frame-bright)
          (q/rect (first frame-pos) (second frame-pos) 208.236 156.177)
          (apply q/fill config/color-prism-notsubject-bg)
          (q/rect (first prism-pos) (second prism-pos) 186.298 133.634)
          (apply q/fill config/color-prism-notsubject-fg)
          (apply q/quad (flatten (map (partial vector-add (nth logo-origins subject-index)) logo-quad-points)))
          )
        (do
          (apply q/fill config/color-prism-frame-dim) ; dim border if subject is not still in play
          (q/rect (first frame-pos) (second frame-pos) 208.236 156.177)
          )
        )
      )
    )
  ; On the title screen draw the name of the game after a delay.
  (if (= (:game-phase state) (:title-screen game-phases))
    (let [delayed-frame-count (max 0 (- (q/frame-count) (* 1.2 config/spec-frame-rate)))
          frame-factor (min 600 (* 600 (/ delayed-frame-count (* config/spec-frame-rate 0.4))))
          title-corner-y (- 600.0 frame-factor)
          title-size-y (- 600.0 title-corner-y)]
      (q/shape (q/load-shape "elements/titlecard.svg") 0 title-corner-y 800 title-size-y)
      (q/text-font font-subject-text)
      (q/text-leading config/line-spacing-subject-text)
      (if (= frame-factor 600) (shadow-text "Press [A] to continue" 4 config/color-infotext-fg config/color-infotext-bg 0 450 800 150))
      ))
  ; Allow selection of list name on pause screen.
  (if (= (:game-phase state) (:pause-before-game game-phases))
    (do
      (q/stroke 0 0 0)
      (q/stroke-weight 3)
      (q/text-font font-award-value)
      (q/text-leading config/line-spacing-award-value)
      (shadow-text (str "Subject list:\n" (nth (sort (keys config/subjects)) (:subject-list-key-index state))) 6 config/color-infotext-fg config/color-infotext-bg 0 0 800 400)
      (q/text-font font-subject-text)
      (q/text-leading config/line-spacing-subject-text)
      (shadow-text "[H] Previous list    [L] Next list    [S] Begin game!" 4 config/color-infotext-fg config/color-infotext-bg 0 400 800 200)
      (q/no-stroke)))
  (doseq [subject-index (:subjects-remaining state)]
    (let [rect-pos (nth subject-rect-positions subject-index)]
      (if (and (>= (:game-phase state) (:timer-running game-phases))
               (< (:game-phase state) (:end-screen game-phases)))
        (do
                                      ; If subject is first in remaining list or has been passed, show white face of "prism" on which we can display the subject.
          (if (or (= subject-index (peek (:subjects-remaining state)))
                  (not (nil? (some #{subject-index} (:subjects-passed state)))))
            (do
              (apply q/fill config/color-prism-subject-bg)
              (q/rect (first rect-pos) (second rect-pos) 186.298 133.634)
              )
            )
                                      ; Else if subject is in correct list then redraw a blank red face on which we can display the award value.
          (if (not (nil? (some #{subject-index} (:subjects-correct state))))
            (do
              (apply q/fill config/color-prism-notsubject-bg)
              (q/rect (first rect-pos) (second rect-pos) 186.298 133.634)
              )
            )
          )
        )
      )
    )
  ; Once we have started the game it is necessary to show the subject text or award value for revealed subjects.
  (if (and (>= (:game-phase state) (:timer-running game-phases))
           (< (:game-phase state) (:end-screen game-phases)))
    (let [subject-index-to-show (peek (:subjects-remaining state))
          rect-pos-current (if (not (nil? subject-index-to-show)) (nth subject-rect-positions subject-index-to-show) nil)]
       ; draw current subject (the first remaining one)
       (apply q/fill config/color-prism-subject-fg)
       (q/text-font font-subject-text)
       (q/text-leading config/line-spacing-subject-text)
       (if (not (nil? subject-index-to-show))
         (q/text (cljstr/upper-case (nth subject-text subject-index-to-show)) (first rect-pos-current) (second rect-pos-current) 186.298 133.634))
                                        ; draw text of passed subjects also
       (doseq [passed-index (:subjects-passed state)]
         (let [rect-pos-passed (nth subject-rect-positions passed-index)
               passed-subject-text (cljstr/upper-case (nth subject-text passed-index))]
           (q/text passed-subject-text (first rect-pos-passed) (second rect-pos-passed) 186.298 133.634))
         )
                                        ; draw values of the subjects the player has gotten correct
       (apply q/fill config/color-prism-notsubject-fg) ; yellowey text
       (q/text-font font-award-value)
       (doseq [value-to-draw (:subjects-correct state)]
         (let [rect-pos-value (nth subject-rect-positions value-to-draw)]
           (q/text (str config/value-symbol (nth config/subject-values value-to-draw)) (first rect-pos-value) (second rect-pos-value) 186.298 133.634))
         )
       )
  )
  ; draw timer display based on actual value during game (not on title or end screens)
  (if (and (> (:game-phase state) (:title-screen game-phases))
           (< (:game-phase state) (:end-screen game-phases)))
    (if (> (:timer state) config/game-length-sec) ; freeze clock at max during initial second
      (let [gls config/game-length-sec
            tens-digit (if (> gls 9) (keyword (subs (str gls) 0 1)) nil)
            ones-digit (if (> gls 9) (keyword (subs (str gls) 1)) (keyword (str gls)))]
        (if (not (nil? tens-digit)) (doseq [segment (tens-digit digit-patterns)] (q/shape (segment timer-shapes-tens))))
        (doseq [segment (ones-digit digit-patterns)] (q/shape (segment timer-shapes-ones)))
      )
      (let [tens-digit (if (> (:timer state) 9) (keyword (subs (str (:timer state)) 0 1)) nil)
            ones-digit (if (> (:timer state) 9) (keyword (subs (str (:timer state)) 1)) (keyword (str (:timer state))))]
        (if (not (nil? tens-digit)) (doseq [segment (tens-digit digit-patterns)] (q/shape (segment timer-shapes-tens))))
        (doseq [segment (ones-digit digit-patterns)] (q/shape (segment timer-shapes-ones)))
      )
    )
  )
  ; Print final winnings and some host blather during the pause after game ends
  (if (= (:game-phase state) (:pause-after-game game-phases))
    (do
      (if (= 6 (count (:subjects-correct state)))
        (do ; Jackpot!
          (q/text-font font-award-value)
          (q/text-leading config/line-spacing-award-value)
          (shadow-text "Grand prize winner!" 6 [255 255 255] [0 0 0] 0 100 800 300)
          (q/text-font font-grand-prize-value)
          (shadow-text (str config/value-symbol config/grand-prize-value) 6 [255 255 255] [0 0 0] 0 270 800 250)
          )
        (let [blather-str (if (= 0 (:timer state)) "Sorry!\nTime's up!" "Be careful giving those clues!")
              final-score (if (= 0 (count (:subjects-correct state)))
                            0
                            (apply + (for [s (:subjects-correct state)] (nth config/subject-values s))))]
          (q/text-font font-award-value)
          (q/text-leading config/line-spacing-award-value)
          (shadow-text blather-str 6 [30 250 180] [0 0 0] 0 75 800 300)
          (shadow-text (str "Total winnings " config/value-symbol final-score) 6 [30 250 180] [0 0 0] 0 350 800 200)
          ))
      (q/text-font font-subject-text)
      (q/text-leading config/line-spacing-subject-text)
      (shadow-text "[E] Continue" 4 [30 250 180] [0 0 0] 0 500 800 100))
    )
  ; end screen
  (if (= (:game-phase state) (:end-screen game-phases))
    (do
      (q/text-font font-award-value)
      (q/text-leading config/line-spacing-award-value)
      (shadow-text "This has been a\nJake Wimberley\nproduction" 6 config/color-infotext-fg config/color-infotext-bg 0 0 800 400)
      (q/text-font font-subject-text)
      (q/text-leading config/line-spacing-subject-text)
      (shadow-text "Version 0.6.1 \u00a9 2017\nFree software, released under the Eclipse Public License" 4 config/color-infotext-fg config/color-infotext-bg 0 350 800 100)
      (shadow-text "[R] Play again!" 4 config/color-infotext-fg config/color-infotext-bg 0 470 800 100)
    )
  )
  (if config/show-debug-data
    (do
      (q/text-font font-subject-text)
      (q/fill 255 255 255)
      (q/text-align :left)
      (q/text-num (:game-phase state) 20 30)
      (q/text-num (q/frame-count) 20 60)
      (q/text-num (:timer state) 20 90)
      (q/text (str "rem: " (:subjects-remaining state)) 20 120)
      (q/text (str "cor: " (:subjects-correct state)) 20 150)
      (q/text (str "pas: " (:subjects-passed state)) 20 180)
      (q/text (str "k-p: " (:key-pressed state)) 20 210)
      ))
)


(defn -main
  []
  (q/defsketch valuable-triangle
    :title "The Valuable Triangle"
    :size [800 600]
                                        ; setup function called only once, during sketch initialization.
    :setup setup
                                        ; update-state is called on each iteration before draw-state.
    :update update-state
    :draw draw-state
    :middleware [m/fun-mode];  m/pause-on-error]
    )
)
