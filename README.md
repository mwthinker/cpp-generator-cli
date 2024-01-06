# cpp-generator-cli [![CI build](https://github.com/mwthinker/cpp-generator-cli/actions/workflows/ci.yml/badge.svg)](https://github.com/mwthinker/cpp-generator-cli/actions/workflows/ci.yml) [![codecov](https://codecov.io/gh/mwthinker/cpp-generator-cli/graph/badge.svg?token=T6CE5XBPEQ)](https://codecov.io/gh/mwthinker/cpp-generator-cli) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
A java application based on JDK 19 and maven. Implements a C++ generator CLI.

## How to use
Generates a C++ project template based on cmake and vcpkg.

Usage of the generated binary file:
```bash
cppgen --help
Script to generate a CMake C++ project

Usage: cppgen [-cghotvV] [-d=DESCRIPTION] [-l=LICENSE] -n=NEW
C++ generator
  -c, --cmake             run cmake
  -d, --description=DESCRIPTION
                          short description used in the template
  -g, --gui               add gui library
  -h, --help              display a help message
  -l, --license=LICENSE   add MIT license with author
  -n, --new=NEW           the project name
  -o, --open              open visual studio solution
  -t, --test              add test
  -V, --version           display version info
  -v, --verbose           show verbose output
```

Example of generating a C++ project and open in Visual Studio C++ 2022:
```bash
cppgen.exe --new NewProject -o
```

A Visual Studio C++ 2022 solution should be opened and ready to be used. Assumes that vcpkg is installed and environment variable VCPKG_ROOT pointing to it. Visual Studio 2022 and CMake 3.24 must also be installed and available in the PATH environment variable.

## License
MIT
