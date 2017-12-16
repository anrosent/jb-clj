# jb

A tool written in Clojure to perform basic schema inference on JSON inputs, and output a description of the inferred schema.

This project provides two namespaces, `jb.core` and `jb.browse`. 

### `jb.core`

This module exposes `infer-schema`, a function that returns a Schema Descriptor given any piece of data. 

### `jb.browse`

This module exposes `browse`, a function that takes a piece of data and returns a compact, human-friendly summary of the input's Schema Descriptor. 

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

If that object is in the file `data.json`, we can pipe it through `jb.core` to see the generated Schema Descriptor
```
$   cat data.json | lein run -m jb.core
{
    "jb.core/id" : "jb.core/schema.id.object",
    "jb.core/schema.data.object" : {
        "foo" : {
            "jb.core/id" : "jb.core/type.id.always",
            "jb.core/schema" : {
                "jb.core/id" : "jb.core/schema.id.primitive",
                "jb.core/schema.data.primitive" : "java.lang.Integer"
            }
        },
        "bar" : {
            "jb.core/id" : "jb.core/type.id.always",
            "jb.core/schema" : {
                "jb.core/id" : "jb.core/schema.id.listof",
                "jb.core/schema.data.listof" : {
                    "jb.core/id" : "jb.core/type.id.always",
                    "jb.core/schema" : {
                        "jb.core/id" : "jb.core/schema.id.object",
                        "jb.core/schema.data.object" : {
                            "optional" : {
                                "jb.core/id" : "jb.core/type.id.maybe",
                                "jb.core/schema" : {
                                    "jb.core/id" : "jb.core/schema.id.primitive",
                                    "jb.core/schema.data.primitive" : "java.lang.Integer"
                                }
                            },
                            "baz" : {
                                "jb.core/id" : "jb.core/type.id.always",
                                "jb.core/schema" : {
                                    "jb.core/id" : "jb.core/schema.id.primitive",
                                    "jb.core/schema.data.primitive" : "java.lang.String"
                                }
                            },
                            "nullableField" : {
                                "jb.core/id" : "jb.core/type.id.always",
                                "jb.core/schema" : {
                                    "jb.core/id" : "jb.core/schema.id.union",
                                    "jb.core/schema.data.union" : [ {
                                        "jb.core/id" : "jb.core/schema.id.primitive",
                                        "jb.core/schema.data.primitive" : null
                                    }, {
                                        "jb.core/id" : "jb.core/schema.id.primitive",
                                        "jb.core/schema.data.primitive" : "java.lang.String"
                                    } ]
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

Consumers of `jb.core/infer-schema` will see namespaced keywords where you see `"jb.core/*"` above.

For a more readable summary of the schema, we can use `jb.browse`.
```
$   cat data.json | lein run -m jb.browse
{
    "foo" : "java.lang.Integer",
    "bar" : [ {
        "optional" : {
            "maybe" : "java.lang.Integer"
        },
        "baz" : "java.lang.String",
        "nullableField" : {
            "union" : [ null, "java.lang.String" ]
        }
    } ]
}
```
