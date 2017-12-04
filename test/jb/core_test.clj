(ns jb.core-test
  (:require [clojure.test :refer :all]
            [jb.core :refer :all]))

(deftest tdd
  (testing "empty"
    (is (= nil (infer nil))))

  (testing "single"
    (is (= {:foo {:kind :primitive
                  :type "java.lang.Long" 
                  :required true}}
           (infer {:foo 1}))))

  (testing "two keys, different types"
    (is (= {:foo {:kind :primitive
                  :type "java.lang.Long" 
                  :required true}
            :bar {:kind :primitive
                  :type "java.lang.String" 
                  :required true}}
           (infer {:foo 1 :bar "baz"}))))

  (testing "empty array"
    (is (= {:qux {:kind :list
                  :type {}
                  :required true}}
           (infer {:qux []}))))

  (testing "array with object"
    (is (= {:qux {:kind :list
                  :type {:foo {:kind :primitive
                               :type "java.lang.Long" 
                               :required true}}
                  :required true}}
           (infer {:qux [{:foo 1}]}))))

  (testing "array with multiple objects, field sparsity"
    (is (= {:qux {:kind :list
                  :type {:foo {:kind :primitive
                               :type "java.lang.Long" 
                               :required false}
                          :bar {:kind :primitive
                                :type "java.lang.String" 
                                :required false}}
                  :required true}}
           (infer {:qux [{:foo 1} {:bar "bar"}]}))))

  (testing "nested object"
    (is (= {:qux {:kind :map
                  :type {:foo {:kind :primitive
                               :type "java.lang.Long" 
                               :required true}} 
                  :required true}}
           (infer {:qux {:foo 1}}))))

  (testing "array with nested object, field sparsity"
    (is (= {:qux {:kind :list
                  :type {:foo {:kind :map
                               :type {:bar {:kind :union
                                            :type "java.lang.String" 
                                            :required false}}
                                :required true}}
                  :required true}}
           (infer {:qux [{:foo {:bar "1"}} {:foo {}}]})))))
