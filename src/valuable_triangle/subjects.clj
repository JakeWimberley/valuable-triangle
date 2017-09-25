(in-ns 'valuable-triangle.config)

(defn list-subject-files
  "Return a seq of files in the specified dir that match the subject file pattern."
  [dir]
  (->> dir
       clojure.java.io/file
       .list
       (map str)
       (filter #(re-find #"vt-subjects-" %))))

(defn process-subject-matches
  [re-vec]
  [(nth re-vec 1) (Integer/parseInt (nth re-vec 2))])

(defn read-subject-file
  "read file supplied as argument, return [title ([subj1] [subj2] ... [subjN])]"
  [filename & args]
  (let [file-str (slurp filename)
        list-title (second (re-find #"<(.+)>" file-str))]
    {list-title (map process-subject-matches (re-seq #"(.+)\s*,\s*(\d)" file-str))})
  )

(defn load-subjects-from-dir
  "read all subject files in dir, return map of list-name to list."
  [dirname & args]
; (into {} (map #(read-subject-file (str dirname "/" %)) (list-subject-files dirname))))
  (def subjects (into {} (map #(read-subject-file (str dirname "/" %)) (list-subject-files dirname)))))
