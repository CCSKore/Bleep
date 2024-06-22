# Bleep
Bleep is a very open language which is natively implemented in Java but can be ported to any other language.

## Features
- Change how Bleep logs
- Add already existing fields, methods and classes before runtime

## Syntax
### Variables, Constants and Fields:
Declaring a variable is simple enough
```
const valueName = "This is string!";
```

Bleep does not have type hints, however once a variable has a type, that type cannot change
```
const valueName = "This is string!";
valueName = 5; // Error! `valueName`'s type cannot be changed
```

You can overcome this issue with the standard `var` syntax
```
var valueName = "This is string!";
valueName = 5; // It works! var does NOT lock down the type
```

As you can see `var` or `const` refers to whether or not the type can change, const should be the most commonly used

To define a variable which perhaps isn't so variable, you can use the `field` keyword
```
field valueName = "This is string!";
valueName = "This is string too!"; // Error! `valueName`'s value cannot be changed
```

### Methods and some more language intro
You can declare a function like this
```
fun myFunction() {
    info("You ran myFunction!");
}
```

You have spotted logging! You can use `info`, `warn` and `error` for logging

You can also have parameters for your functions
```
fun myFunction(arg1, arg2) {
    info("You ran myFunction with " + arg1 + " and " + arg2 + "!");
}
```

If you need to use normal Java classes then you can use the JVM class, this is an example of how to use `System.out.println`
```
JVM.invokeMethod(JVM.getClass("java.io.PrintStream"), JVM.getField(JVM.getClass("java.lang.System"), empty, "out"), "println", "Hello System.out.println!");
```

### Classes
You can declare a class like this
```
class MyClass {
    init() {
        info("Made an instance of MyClass!");
        method1();
    }
    
    method1() {
        info("Crazy!");
    }
}
```

You can extend a class with the `<` syntax
```
class MySecondClass < MyClass {
    init() {
        info("Get overriden >:3");
        super.init();
    }
}
```