Fern
=====

### Opus #5

A clean and simple storage or configuration format with easy readability, minimal meta-characters, lenient parsing and
simple conversion.

This is built to be a minimalist alternatives to popular configuration formats such as YAML or JSON, but without their
individual pitfalls, such as YAML's strict whitespace grammar and JSON's reliance on quotes and brackets.

Fern is also designed to be easily customisable and extendable, allowing a user to build domain-specific features into
the general parser or framework, either to build functional programmatic features or handlers for custom data types.

Fern ignores all whitespace, except for the single space that separates `key value` pairs.

Fern-Query - the second module - is one such extension, allowing simple logic and set-notation queries to be written in
plain text, which can then be matched against Java objects.

#### Built-in Data Types

| Type | Delimiter | Description |
|------|-----------|-------------|
|Comment|`//` or `/* */`|A comment. Stripped before parsing begins.|
|Branch|`()`|A new sub-branch, a key-value comma-separated map.|
|List|`[]`|A comma-separated list.|
|String|`"text"`|Simple strings, supports both simple character escapes and multi-line content.|
|Integer|`100`|Simple int-32.|
|Long|`100L`|Simple int-64. Suffix is only required to differentiate from an integer in small numbers.|
|Short|`100S`|A short. Suffix is always required.|
|Double|`100.0D`|A double. Suffix is not required, unless the number is not a decimal.|
|Float|`0.5F`|A floating point number. Suffix is always required.|
|Byte|`13B`|A byte. Suffix is always required.|
|Null|`null`|A null-value.|
|Boolean|`true`|A boolean true/false value.|
|Class|`a.b.c.ClassName$Nested`|A class name. Recognised via legal classpath.|
|UUID|`7dcad757-3d50-4720-9e18-5bdf55537040`|A UUID. Recognised via legal representation.|
|Colour|`#ff0000`|A Java `Color`, recognised via hex format.|

Domain-specific data types can be added by custom parser implementations by adding a new `ValueHandler` - they require
only a recognisable delimiter.

#### Examples

Basic uses of types.

```fern
name "Tony",        // A string
age 42,             // an integer
height 1.85,        // a double
weight 70.2,        // a double
spouse null,        // null
likes               // a list, the alternative format
  -> "chicken"
  -> "pasta"
  -> "bacon",
dislikes [          // a list, the normal format
    "fish",
    "ice cream",
    74
],
education (         // a map
    school "Hartlepool Secondary School",
    college "Wisham College",
    university null
)

```

More examples of type usage.

```fern
a_key ( // A map.
    abc 1, // An int value.
    xyz 2D, // A double value.
    pqr 5.0 // An implied double value.
),
another_key "value" // A root-level key/value pair.
```

Fern does not require whitespace, except to separate keys and values. This means a user can mix indentation units to
their heart's content.

```fern
map (key "value", thing 66), list [1, 2, 3]
```

#### Extending the Fern Grammar

Fern is an incredibly simple structure to extend - requiring a simple extension to add entirely new types or concepts.

A new type requires a handler, which is used both to recognise the type and to extract the Java object value from it.

An instance of the handler can then be added to a parser's list of handlers.

```java
public class MyHandler implements ValueHandler<Character> {
    @Override
    public boolean matches(String string) {
        return string.length() == 3 // asserts we match 'a'
            && string.startsWith("'") // asserts we have single quotes
            && string.endsWith("'");
    }
    
    @Override
    public Character parse(String string) {
        return string.charAt(1); // returns the second character of the string, after the first quote
    }
}
```

As demonstrated, it is incredibly simple to add new types.

Only three things are required:

1. The type must be recognisable and unambiguous.
2. The type must be expressible as a string.
3. The type must be reliable.

For a provided example of how Fern can be extended to add a functional purpose, see the Fern-Query module.

#### Fern-Query

Fern-Query comes in a separate, optional module. It contains a new parser that extends the basic one, and adds some new
data types. The Query parser also contains a matcher function that allows a query to be matched against an object.

Queries take inspiration from NoSQL and similar formats in being simple to write and recognise.

The query parser is also an example of how Fern's grammar can be extended to add functional content.

An example query:

```fern
age > 20
```

Would be parsed into a regular `FernBranch`, and the leaf object would contain a query. This `Query<Integer>` would
match a number greater than `20`.

The user could individually unwrap the query from the tree structure to use in code, or could match the Fern branch
directly against a Java object, which would search for an `int age` field and match its value against the query.

Queries can, however, be more complex.

#### Built-in Query Types

##### Equality Comparisons

| Type | Delimiter | Accepts | Description |
|:-----|-----------|---------|:------------|
|Equals|`=` or `==`|All types|Attempts an equality comparison.|
|Not-equals|`≠` or `!=`|All types|Inverse of equals.|
|Less than|`<`|Numbers|Checks whether the input is less than the given value.|
|Greater than|`>`|Numbers|Checks whether the input is greater than the given value.|
|LEQ|`<=`|Numbers|Less than or equal to the given value.|
|GEQ|`>=`|Numbers|Greater than or equal to the given value.|

##### Set Comparisons

| Type | Delimiter | Accepts | Description |
|:-----|-----------|---------|:------------|
|Element of|`∈`|All types|Whether the input is in the given list.|
|Not-element of|`∉`|All types|Whether the input is not in the given list.|
|Superset|`⊃`|Collections|Whether the input list contains all of the given list.|
|Subset|`⊂`|Collections|Whether all of the input list is contained by the given list.|

##### Set Processes

These are not technically queries as they do not allow boolean comparisons.

| Type | Delimiter | Accepts | Description |
|:-----|-----------|---------|:------------|
|Union|`∪`|Collections|Returns a set of all unique elements in either list.|
|Intersection|`∩`|Collections|Returns a set of all unique elements common to both lists.|

##### Boolean Logic

| Type | Delimiter | Accepts | Description |
|:-----|-----------|---------|:------------|
|And|`∧`|Boolean|Whether both the input and the given value are true.|
|Or|`∨`|Boolean|Whether the input or the given value are true.|
|Xor|`⊻`|Boolean|Whether exactly one of the input and the given value is true.|
|Implication|`⊃`|Boolean|Whether the input value implies the given value. NOTE: This overloads the superset notation|

#### Extending the Query Parser

Much like basic Fern, the query parser can itself be extended and new query types added with relative ease, simply by
adding new `ValueHandler` objects.

These query handlers will recursively parse the value inside the query to provide the given value.

Below is a very simple implementation of a "default value" handler.

```java
public class DefaultValueHandler implements ValueHandler<Query<?>> {
    
    @Override
    public boolean matches(String string) {
        return string.startsWith("?");
    }
    
    @Override
    public Query<Object> parse(String string, FernParser parser) {
        final String value = string.substring(1).trim();
        final Object object = parser.getHandler(value).parse(value, parser);
        return thing -> thing != null ? thing : object;
    }
    
    @Override
    public Query<Object> parse(String string) {
        throw new IllegalStateException("Query handler cannot parse input without FernParser instance.");
    }

}
```

This implementation would add the grammar:

```fern
key ? "hello"
```

Which would return the queried `key` value, or our `"hello"` if it were null.




