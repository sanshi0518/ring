(ns ring.middleware.test.nested-params
  (:require [clojure.string :as string])
  (:use clojure.test
        ring.middleware.nested-params))

(deftest nested-params-test
  (let [handler (wrap-nested-params :params)]
    (testing "nested parameter maps"
      (are [p r] (= (handler {:params p}) r)
        {"foo" "bar"}      {"foo" "bar"}
        {"x[y]" "z"}       {"x" {"y" "z"}}
        {"a[b][c]" "d"}    {"a" {"b" {"c" "d"}}}
        {"a" "b", "c" "d"} {"a" "b", "c" "d"}))
    (testing "nested parameter lists"
      (are [p r] (= (handler {:params p}) r)
        {"foo[]" "bar"}         {"foo" ["bar"]}
        {"foo[]" ["bar" "baz"]} {"foo" ["bar" "baz"]})
      (let [params (handler {:params {"a[x][]" ["b"], "a[x][][y]" "c"}})]
        (is (= (keys params) ["a"]))
        (is (= (keys (params "a")) ["x"]))
        (is (= (set (get-in params ["a" "x"])) #{"b" {"y" "c"}})))
      (let [params (handler {:params {"a[][x]" "c", "a[][y]" "d"}})]
        (is (= (keys params) ["a"]))
        (is (= (set (params "a")) #{{"x" "c"} {"y" "d"}}))))
    (testing "duplicate parameters"
      (are [p r] (= (handler {:params p}) r)
        {"a" ["b" "c"]}    {"a" ["b" "c"]}
        {"a[b]" ["c" "d"]} {"a" {"b" ["c" "d"]}}))))

(deftest nested-params-test-with-options
  (let [handler (wrap-nested-params :params
                                    {:key-parser #(string/split % #"\.")})]
    (testing ":key-parser option"
      (are [p r] (= (handler {:params p}) r)
        {"foo" "bar"}      {"foo" "bar"}
        {"x.y" "z"}       {"x" {"y" "z"}}
        {"a.b.c" "d"}    {"a" {"b" {"c" "d"}}}
        {"a" "b", "c" "d"} {"a" "b", "c" "d"}))))

(deftest nested-params-request-test
  (is (fn? nested-params-request)))
