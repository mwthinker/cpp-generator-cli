# cpp-generator-cli [![CI build](https://github.com/mwthinker/cpp-generator-cli/actions/workflows/ci.yml/badge.svg)](https://github.com/mwthinker/cpp-generator-cli/actions/workflows/ci.yml)
A java application based on JDK 19 and maven. Implements a C++ generator CLI.

## How to use
Generate a C++ project template based on cmake and vcpkg.

Usage of the generated binary file:
```bash
cppgen --help
C++ generator
  -c, --cmake         run cmake
  -d, --description=DESCRIPTION
                      short description used in the template
  -g, --gui           add gui library
  -i, --interactive   interactive input
  -n, --new=NEW       the project name
  -o, --open          open visual studio solution
  -t, --test          add test
  -V, --verbose       show verbose output
```

Example of generating a C++ project and open in Visual Studio C++ 2022:
```bash
cppgen.exe --new NewProject -o
```

A Visual Studio C++ 2022 solution should be opened and ready to be used. Asumes that vcpkg is installed and enviroment variable VCPKG_ROOT pointing to it. Visual Studio 2022 and CMake 3.24 must also be installed and availabe in the PATH enviroment variable.

## License
MIT
