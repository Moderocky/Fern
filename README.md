Fern
=====

### Opus #5

A clean and simple storage or configuration format with easy readability, minimal meta-characters, lenient parsing and
simple conversion.

This is built to be a minimalist alternatives to popular configuration formats such as YAML or JSON, but without their
individual pitfalls, such as YAML's strict whitespace grammar and JSON's reliance on quotes and brackets.

Fern is also designed to be easily customisable and extendable, allowing a user to build domain-specific features into
the general parser or framework to build handlers for custom data types.

Fern ignores all whitespace, except for the single character that separates entities.

## Data Types

| Type    | Delimiter                              | Description                                                                               |
|---------|----------------------------------------|-------------------------------------------------------------------------------------------|
| Branch  | `()`                                   | A new sub-branch, a `key` `value` map.                                                    |
| List    | `[]`                                   | A list.                                                                                   |
| String  | `"text"`                               | Simple strings, supports both simple character escapes and multi-line content.            |
| Integer | `100`                                  | Simple int-32.                                                                            |
| Long    | `100L`                                 | Simple int-64. Suffix is only required to differentiate from an integer in small numbers. |
| Short   | `100S`                                 | A short. Suffix is always required.                                                       |
| Double  | `100.0D`                               | A double. Suffix is not required, unless the number is not a decimal.                     |
| Float   | `0.5F`                                 | A floating point number. Suffix is always required.                                       |
| Byte    | `13B`                                  | A byte. Suffix is always required.                                                        |
| Null    | `null`                                 | A null-value.                                                                             |
| Boolean | `true`                                 | A boolean true/false value.                                                               |

Domain-specific data types can be added by custom parser implementations by adding a new `ValueHandler` - they require
only a recognisable starting character.

## Examples

Basic uses of types.

```fern
name "Tony"
age 42    
height 1.85 
weight 70.2 
spouse null  
likes [ "chicken" "pasta" "bacon" ]
dislikes [ "fish" "ice cream" 74 ]
education (   
    school "Hartlepool Secondary School"
    college "Wisham College"
    university null
)

```

More examples of type usage.

```fern
a_key ( 
    abc 1 
    xyz 2D 
    pqr 5.0 
)
another_key "value" 
```

Fern does not require whitespace, except to separate entities. This means a user can mix indentation units to
their heart's content.

```fern
map ( key "value" thing 66 ) list [ 1 2 3 ]
```

## Maven Information
```xml
<repository>
    <id>kenzie</id>
    <name>Kenzie's Repository</name>
    <url>https://repo.kenzie.mx/releases</url>
</repository>
``` 

```xml
<dependency>
    <groupId>mx.kenzie</groupId>
    <artifactId>fern</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Adding Types

Fern is built to support additional value types for domain-specific use.

A type must have a recognisable starting character (e.g. `.`) and a recognisable end.
Types are responsible for recognising their own end, so this may not be a single distinct character.



