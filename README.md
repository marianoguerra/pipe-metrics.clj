# pipe-metrics

a lib to add coda hale's metrics to marianoguerra's pipe library

## Usage

check clojars project's site for instructions:

https://clojars.org/org.marianoguerra/pipe-metrics

example usage:

	user=> (use 'marianoguerra.pipe)
	user=> (use 'marianoguerra.pipe-metrics)
	user=> (defn plus-one [{value :value}] {:value (inc value)})

	user=> (pipe {:value 42} (counter "my-counter") plus-one)
	{:value 43}

	user=> (get-metric "my-counter")
	#<Counter com.yammer.metrics.core.Counter@b26300>

	user=> (require '[metrics.counters :as counters])
	user=> (counters/value (get-metric "my-counter"))
	1

	user=> (pipe {:value 42} (counter "my-counter") plus-one)
	{:value 43}

	user=> (pipe {:value 42} (counter "my-counter") plus-one)
	{:value 43}

	user=> (counters/value (get-metric "my-counter"))
	3

	; you can specify a custom update function that can change the
	; metric according to something else (like current value on the pipe)

	user=> (defn increase-counter-by-two [counter value inc! dec! clear!] (inc! counter 2))
	#'user/increase-counter-by-two
	user=> (defn decrease-counter [counter value inc! dec! clear!] (dec! counter))
	#'user/decrease-counter
	user=> (defn clear-counter [counter value inc! dec! clear!] (clear! counter))
	#'user/clear-counter
	user=> (pipe {:value 42} (counter "my-counter" :update-fn decrease-counter) plus-one)
	{:value 43}
	user=> (counters/value (get-metric "my-counter"))
	2
	user=> (pipe {:value 42} (counter "my-counter" :update-fn clear-counter) plus-one)
	{:value 43}
	user=> (counters/value (get-metric "my-counter"))
	0

## License

Copyright © 2013 marianoguerra

Distributed under the Eclipse Public License, the same as Clojure.
