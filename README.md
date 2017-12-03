# jb

A tool written in Clojure to perform basic schema inference on JSON inputs, and output a description of the inferred schema.

## Example Usage

Consider the following JSON object:
```
{
    "foo": 4, 
    "bar": [{
        "baz": "value",
        "nullableField": "not null"
    }, {
        "baz": "another one",
        "optional": 42,
        "nullableField": null
    }]
}
```

If that object is in the file `data.json`, we can pipe it through `jb` to see what it looks like
```
$   cat data.json | lein run -m jb.core
{"foo" {:type "java.lang.Integer", :required true},
 "bar"
  {:type
    [{"baz" {:type "java.lang.String", :required true},
        "nullableField" {:type #{nil "java.lang.String"}, :required true},
            "optional" {:type "java.lang.Integer", :required false}}],
              :required true}}
```

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
