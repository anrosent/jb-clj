# jb

A tool written in Clojure to perform basic schema inference on JSON inputs, and output a description of the inferred schema.

## Example Usage

```
$   echo '{"foo": 4, "bar": [{"baz": "value"}]}' | lein run -m jb.core
{"foo" {:type "java.lang.Integer", :required true},
 "bar"
  {:type [{"baz" {:type "java.lang.String", :required true}}],
    :required true}}
```

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
