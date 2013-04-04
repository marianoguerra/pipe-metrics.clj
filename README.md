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

## License

Copyright © 2013 marianoguerra

Distributed under the Eclipse Public License, the same as Clojure.
