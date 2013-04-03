(ns marianoguerra.pipe-metrics-test
  (:require 
    [metrics.counters :as counters]
    [metrics.histograms :as histograms]
    [metrics.meters :as meters])
  (:use
        marianoguerra.pipe-metrics
        marianoguerra.pipe
        clojure.test))

(defn plus-one [value]
  {:value (inc (:value value))})

(defn- counter-two-fn [counter value inc! dec! clear!]
  (inc! counter 2))

(defn- counter-minus-one-fn [counter value inc! dec! clear!]
  (dec! counter 1))

(defn- counter-clear-fn [counter value inc! dec! clear!]
  (clear! counter))

(defn- meter-two-fn [meter value mark!]
  (mark! meter)
  (mark! meter))

(defn- histogram-two-fn [histogram value update! clear!]
  (update! histogram 2))

(defn test-counter-with-name [metric-name expected]
    (let [{result :value} (pipe {:value 42} (counter metric-name) plus-one)
          mcounter (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mcounter)))
      (is (= (counters/value mcounter) expected))))

(defn test-counter-with-name-and-fn [metric-name update-fn expected]
    (let [{result :value} (pipe {:value 42}
                               (counter metric-name :update-fn update-fn)
                               plus-one)
          mcounter (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mcounter)))
      (is (= (counters/value mcounter) expected))))

(defn test-meter-with-name [metric-name metric-unit expected]
    (let [{result :value} (pipe {:value 42}
                               (meter metric-name metric-unit)
                               plus-one)
          mmeter (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mmeter)))
      (is (= (meters/count mmeter) expected))))

(defn test-meter-with-name-and-fn [metric-name metric-unit update-fn expected]
    (let [{result :value} (pipe {:value 42}
                               (meter metric-name metric-unit :update-fn update-fn)
                               plus-one)
          mmeter (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mmeter)))
      (is (= (meters/count mmeter) expected))))

(defn test-histogram-with-name [metric-name expected]
    (let [{result :value} (pipe {:value 42}
                               (histogram metric-name)
                               plus-one)
          mhistogram (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mhistogram)))
      (is (= (histograms/smallest mhistogram) expected))
      mhistogram))

(defn test-histogram-with-name-and-fn [metric-name expected update-fn]
    (let [{result :value} (pipe {:value 42}
                               (histogram metric-name :update-fn update-fn)
                               plus-one)
          mhistogram (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mhistogram)))
      (is (= (histograms/smallest mhistogram) expected))
      mhistogram))

(defn test-dynamic-counter [value-to-metric-name-fn metric-name expected &
                            [update-fn]]
  (let [counter-pipe (if update-fn
                       (dynamic-counter value-to-metric-name-fn :update-fn update-fn)
                       (dynamic-counter value-to-metric-name-fn))
          {result :value} (pipe {:value 42} counter-pipe plus-one)
          mcounter (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mcounter)))
      (is (= (counters/value mcounter) expected))))

(defn test-dynamic-meter [value-to-metric-name-fn unit-name metric-name expected &
                          [update-fn]]
    (let [meter-pipe (if update-fn
                       (dynamic-meter value-to-metric-name-fn unit-name
                                      :update-fn update-fn)
                       (dynamic-meter value-to-metric-name-fn unit-name))
          {result :value} (pipe {:value 42} meter-pipe plus-one)
          mmeter (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mmeter)))
      (is (= (meters/count mmeter) expected))))

(defn test-dynamic-histogram [value-to-metric-name-fn metric-name expected
                              & [update-fn]]
    (let [histogram-pipe (if update-fn
                           (dynamic-histogram value-to-metric-name-fn
                                              :update-fn update-fn)
                           (dynamic-histogram value-to-metric-name-fn))
          {result :value} (pipe {:value 42} histogram-pipe plus-one)
          mhistogram (get-metric metric-name)]
      (is (= result 43))
      (is (not (nil? mhistogram)))
      (is (= (histograms/smallest mhistogram) expected))
      mhistogram))

(deftest tubes-metrics-counter-test
  (testing "counter counts with string name and default update-fn"
    (test-counter-with-name "my-counter" 1))

  (testing "counter counts and clears"
    (test-counter-with-name "my-counter42" 1)
    (test-counter-with-name-and-fn "my-counter42" counter-clear-fn 0))

  (testing "counter counts and decreases"
    (test-counter-with-name "my-counter42" 1)
    (test-counter-with-name "my-counter42" 2)
    (test-counter-with-name-and-fn "my-counter42" counter-minus-one-fn 1))

  (testing "counter counts with vec name and default update-fn"
    (test-counter-with-name ["foo" "bar" "my-counter"] 1))

  (testing "counter counts with vec name and custom update-fn"
    (test-counter-with-name-and-fn ["foo" "bar" "my-other-counter"] counter-two-fn 2))

  (testing "counting the same counter twice increments the same counter"
    (test-counter-with-name "my-other-counter" 1)
    (test-counter-with-name "my-other-counter" 2)))

(deftest tubes-metrics-meter-test
  (testing "meters alone works"
    (let [meter (meters/meter "my-meter" "clicks")]
      (meters/mark! meter)
      (is (= (meters/count meter) 1))))

  (testing "meter pipe alone works"
    ((meter "my-meter1" "clicks") {:value 42})
    (is (= (meters/count (get-metric "my-meter1")) 1)))

  (testing "meter counts with string name and default update-fn"
    (test-meter-with-name "my-meter2" "unit" 1))

  (testing "meter counts with vec name and default update-fn"
    (test-meter-with-name ["foo" "bar" "my-meter"] "unit" 1))

  (testing "meter counts with vec name and custom update-fn"
    (test-meter-with-name-and-fn ["foo" "bar" "my-other-meter"] "unit" meter-two-fn 2))

  (testing "counting the same meter twice increments the same meter"
    (test-meter-with-name "meter1" "unit" 1)
    (test-meter-with-name "meter1" "unit" 2)))

(deftest tubes-metrics-histogram-test
  (testing "histogram counts with string name and default update-fn"
    (test-histogram-with-name "h1" 1.0))

  (testing "histogram counts with vec name and default update-fn"
    (test-histogram-with-name ["a" "b" "h1"] 1.0))

  (testing "histogram counts with string name and custom update-fn"
    (test-histogram-with-name-and-fn "h2" 2.0 histogram-two-fn)))

(deftest tubes-metrics-dynamic-counter-test
  (testing "dynamic counter counts"
    (test-dynamic-counter (fn [_] "dcounter-id") "dcounter-id" 1))

  (testing "dynamic counter counts with custom update-fn"
    (test-dynamic-counter (fn [_] "dcounter-id1") "dcounter-id1" 2 counter-two-fn)))

(deftest tubes-metrics-dynamic-meter-test
  (testing "dynamic meter counts"
    (test-dynamic-meter (fn [_] "dmeter-id") "things" "dmeter-id" 1))

  (testing "dynamic meter counts with custom update-fn"
    (test-dynamic-meter (fn [_] "dmeter-id1") "things" "dmeter-id1" 2 meter-two-fn)))

(deftest tubes-metrics-dynamic-histogram-test
  (testing "dynamic histogram counts with string name and default update-fn"
    (test-dynamic-histogram (fn [_] "dh1") "dh1" 1.0))

  (testing "dynamic histogram counts with string name and custom update-fn"
    (test-dynamic-histogram (fn [_] "dh2") "dh2" 2.0 histogram-two-fn)))

