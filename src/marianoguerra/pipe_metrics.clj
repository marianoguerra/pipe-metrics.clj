(ns marianoguerra.pipe-metrics
  (:import
    com.yammer.metrics.Metrics
    com.yammer.metrics.core.MetricName)
  (:require
    metrics.utils
    [metrics.meters :as meters]
    [metrics.histograms :as histograms]
    [metrics.counters :as counters]))

; Internal

(def default-registry (Metrics/defaultRegistry))
(defn- default-counter-fn [counter value inc! dec! clear!]
  (inc! counter))

(defn- default-meter-fn [meter value mark!]
  (mark! meter))

(defn- default-histogram-fn [histogram value update! clear!]
  (update! histogram 1))

; Main API

(defn counter [counter-name & {:keys [update-fn] :or {update-fn default-counter-fn}}]
  (let [mcounter (counters/counter counter-name)]
    (fn [value]
      (update-fn mcounter value counters/inc! counters/dec! counters/clear!)
      value)))

(defn meter [meter-name unit-name & {:keys [update-fn]
                                     :or {update-fn default-meter-fn}}]
  (let [mmeter (meters/meter meter-name unit-name)]
    (fn [value]
      (update-fn mmeter value meters/mark!)
      value)))

(defn histogram [title & {:keys [update-fn] :or {update-fn default-histogram-fn}}]
  (let [mhistogram (histograms/histogram title)]
    (fn [value]
      (update-fn mhistogram value histograms/update! histograms/clear!)
      value)))

(defn dynamic-counter [value-to-metric-name-fn &
                       {:keys [update-fn] :or {update-fn default-counter-fn}}]
  (fn [value]
    (let [counter-name (value-to-metric-name-fn value)
          mcounter (counters/counter counter-name)]
      (update-fn mcounter value counters/inc! counters/dec! counters/clear!)
      value)))

(defn dynamic-meter [value-to-metric-name-fn unit-name &
                       {:keys [update-fn] :or {update-fn default-meter-fn}}]
  (fn [value]
    (let [meter-name (value-to-metric-name-fn value)
          mmeter (meters/meter meter-name unit-name)]
      (update-fn mmeter value meters/mark!)
      value)))

(defn dynamic-histogram [value-to-metric-name-fn &
                         {:keys [update-fn] :or {update-fn default-histogram-fn}}]
  (fn [value]
    (let [title (value-to-metric-name-fn value)
          mhistogram (histograms/histogram title)]
      (update-fn mhistogram value histograms/update! histograms/clear!)
      value)))

; Utilities

(defn get-metric [name & [registry]]
  (let [metrics (.allMetrics (or registry default-registry))
        metric-name (metrics.utils/metric-name name)
        metric (.get metrics metric-name)]
    metric))
