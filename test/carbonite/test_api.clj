(ns carbonite.test-api
  (:use clojure.test
        [carbonite api buffer])
  (:import [java.nio ByteBuffer]
           [com.esotericsoftware.kryo Kryo]))

(defn round-trip [item]
  (let [kryo (doto (default-registry)
               (clear-context))
        bytes (write-bytes kryo item)]
    (read-bytes kryo bytes)))

(defstruct mystruct :a :b)

(defn new-ts [time nano]
  (doto (java.sql.Timestamp. time)
    (.setNanos nano)))

(deftest test-round-trip-kryo
  (are [obj] (is (= obj (round-trip obj)))
       nil
       1         ;; long
       5.2       ;; double
       #'+       ;; Var
       5M        ;; BigDecimal
       (/ 1 2)   ;; Ratio
       1000000000000000000000000  ;; BigInt
       :foo        ;; keyword
       :a/foo      ;; namespaced keyword
       'foo        ;; symbol
       'a/foo      ;; namespaced symbol
       []          ;; empty vector
       [1 2]       ;; vector
       '()         ;; empty list
       '(1 2)      ;; list
       #{}         ;; empty set
       #{1 2 3}    ;; set
       {}          ;; empty map
       {:a 1}      ;; map
       {:a 1 :b 2} ;; map
       {:a {:b {:c [1 #{"abc"} ]}}}  ;; nested collections
       (range 50)    ;; LazySeq
       (cons 1 '())  ;; Cons
       (cons 1 '(2))
       (cons 1 '(2 3))
       (struct-map mystruct :a 1 :b 2)  ;; PersistentStructMap
       {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9} ;; PersistentArrayMap
       (seq "abc") ;; StringSeq
       (apply str (repeat (* 2 1024 1024) "a")) ;; previous versions of kryo would truncate big strings
       ))

(deftest test-roundtrip-iterator-seq
  (let [coll (java.util.ArrayList.)
        _ (.add coll "abc")
        iter (.iterator coll)
        iseq (iterator-seq iter)
        rt-item (round-trip iseq)]
    (is (= ["abc"] rt-item))))

;; Copyright 2011 Revelytix, Inc.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;; 
;;     http://www.apache.org/licenses/LICENSE-2.0
;; 
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
