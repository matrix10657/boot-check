(ns tolitius.boot-check
  {:boot/export-tasks true}
  (:require [tolitius.checker.yagni :as yagni :refer [yagni-deps]]
            [tolitius.checker.kibit :as kibit :refer [kibit-deps]]
            [tolitius.checker.eastwood :as eastwood :refer [eastwood-deps]]
            [tolitius.boot.helper :refer :all]
            [boot.core :as core :refer [deftask user-files tmp-file set-env! get-env]]
            [boot.pod  :as pod]))

(def pod-deps
  '[[org.clojure/tools.namespace "0.2.11" :exclusions [org.clojure/clojure]]])

(defn bootstrap [fresh-pod]
  (doto fresh-pod
    (pod/with-eval-in
     (require '[clojure.java.io :as io]
              '[clojure.tools.namespace.find :refer [find-namespaces-in-dir]])

     (defn all-ns* [& dirs]
       (distinct (mapcat #(find-namespaces-in-dir (io/file %)) dirs))))))

(deftask with-kibit
  "Static code analyzer for Clojure, ClojureScript, cljx and other Clojure variants.

  This task will run all the kibit checks within a pod.

  At the moment it takes no arguments, but behold..! it will. (files, rules, reporters, etc..)"
  ;; [f files FILE #{sym} "the set of files to check."]      ;; TODO: convert these to "tmp-dir/file"
  []
  (let [pod-pool (make-pod-pool (concat pod-deps kibit-deps) bootstrap)]
    (core/with-pre-wrap fileset
      (kibit/check pod-pool fileset) ;; TODO with args
      fileset)))

(deftask with-yagni
  "Static code analyzer for Clojure that helps you find unused code in your applications and libraries.

  This task will run all the yagni checks within a pod.

  At the moment it takes no arguments, but behold..! it will."
  []
  (let [pod-pool (make-pod-pool (concat pod-deps yagni-deps) bootstrap)]
    (core/with-pre-wrap fileset
      (yagni/check pod-pool fileset) ;; TODO with args
      fileset)))

(deftask with-eastwood
  "Clojure lint tool that uses the tools.analyzer and tools.analyzer.jvm libraries to inspect namespaces and report possible problems

  This task will run all the eastwood checks within a pod.

  At the moment it takes no arguments, but behold..! it will. (linters, namespaces, etc.)"
  ;; [f files FILE #{sym} "the set of files to check."]      ;; TODO: convert these to "tmp-dir/file"
  []
  (let [pod-pool (make-pod-pool (concat pod-deps eastwood-deps) bootstrap)]
    (core/with-pre-wrap fileset
      (eastwood/check pod-pool fileset) ;; TODO with args
      fileset)))
