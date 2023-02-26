<h1><img src="logo.svg" width="200" alt="getargv"></h1>

[![Java](https://github.com/getargv/getargv.java/actions/workflows/java.yml/badge.svg)](https://github.com/getargv/getargv.java/actions/workflows/java.yml)

This package allows you to query the arguments of other processes on macOS.

## Installation

Add the package to your project's pom.xml by adding:

```
<dependency>
  <groupId>cam.narzt.getargv</groupId>
  <artifactId>Getargv</artifactId>
  <version>0.1</version>
</dependency>
```

If maven is not being used to manage dependencies, more installation instructions are available [here](https://central.maven.repo/getargv/dependency-info.html).

## Usage

```java
Getargv.asBytes(some_process_id) #=> "arg0\x00arg1"
Getargv.asArray(some_process_id) #=> ["arg0","arg1"]
```

## Development

After checking out the repo, run `mvn compile` to install dependencies. Then, run `mvn test` to run the tests. You can also run `jshell` for an interactive prompt that will allow you to experiment.

Java code goes in the dirs `src/main/java` and `src/test/java`, C code goes in the dir `src/main/native`.

To install this package onto your local machine, run `mvn install`. To release a new version, update the version number in `pom.xml` and the Readme, and then run `mvn deploy`, which will push the `.jar` files to [maven.apache.org](https://maven.apache.org/repository/).

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/getargv/getargv.java.

## License

The package is available as open source under the terms of the [BSD 3-clause License](https://opensource.org/licenses/BSD-3-Clause).
