(ns jb.core-test
  (:require [clojure.test :refer :all]
            [jb.core :refer :all]))

(deftest tdd
  (is (= nil (browse nil)))

  (is (= {:foo {:type "java.lang.Long" :required true}}
         (browse {:foo 1})))

  (is (= {:foo {:type "java.lang.Long" :required true}
          :bar {:type "java.lang.String" :required true}}
         (browse {:foo 1 :bar "baz"})))

  (is (= {:qux {:type [{}] :required true}}
         (browse {:qux []})))

  (is (= {:qux {:type [{:foo {:type "java.lang.Long" :required true}}]
                :required true}}
         (browse {:qux [{:foo 1}]})))

  (is (= {:qux {:type [{:foo {:type "java.lang.Long" :required false}
                        :bar {:type "java.lang.String" :required false}}]
                :required true}}
         (browse {:qux [{:foo 1} {:bar "bar"}]})))

  (is (= {:qux {:type {:foo {:type "java.lang.Long" :required true}} :required true}}
         (browse {:qux {:foo 1}})))

  (is (= {:qux {:type [{:foo {:type {:bar {:type "java.lang.String" :required false}}
                              :required true}}]
                :required true}}
         (browse {:qux [{:foo {:bar "1"}} {:foo {}}]}))))
