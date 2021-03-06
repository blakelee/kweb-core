package io.kweb.state

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FreeSpec

/**
 * Created by ian on 6/18/17.
 */
class BindableSpec : FreeSpec() {
    init {
        "A Bindable should initialize correctly" {
            val sw = ReadOnlyBindable("Test")
            sw.value shouldBe "Test"
        }

        "A Bindable should notify a listener of a change" {
            val sw = Bindable("Foo")
            var old : String? = null
            var new : String? = null
            sw.addListener { o, n ->
                old shouldBe null
                new shouldBe null
                old = o
                new = n
            }
            sw.value = "Bar"
            old shouldBe "Foo"
            new shouldBe "Bar"
        }
        "A removed listener shouldn't be called" {
            val sw = Bindable("Foo")
            var old : String? = null
            var new : String? = null
            val listenerHandler = sw.addListener { o, n ->
                old shouldBe null
                new shouldBe null
                old = o
                new = n
            }
            sw.removeListener(listenerHandler)
            sw.value = "Bar"
            old shouldBe null
            new shouldBe null
        }

        "A read-only mapped watcher should work" {
            val sw = Bindable("Foo")
            val mapped = sw.map {it -> it.length}
            mapped.value shouldBe 3
            sw.value = "Hello"
            mapped.value shouldBe 5
        }

        "A bi-directional mapped watcher should work" {
            data class Foo(var bar : Int)
            val sw = Bindable(Foo(12))
            val mapped = sw.map({it.bar.toString()}, {n, o -> n.copy(bar = o.toInt())})
            sw.value shouldBe Foo(12)
            mapped.value = "143"
            sw.value shouldBe Foo(143)
        }
    }
}