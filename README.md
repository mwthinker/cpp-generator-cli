# cpp-generator-cli [![CI build](https://github.com/mwthinker/cpp-generator-cli/actions/workflows/ci.yml/badge.svg)](https://github.com/mwthinker/cpp-generator-cli/actions/workflows/ci.yml) [![codecov](https://codecov.io/gh/mwthinker/cpp-generator-cli/graph/badge.svg?token=T6CE5XBPEQ)](https://codecov.io/gh/mwthinker/cpp-generator-cli) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
A java application based on JDK 25 and maven. Implements a C++ generator CLI.

## How to use
Generates a C++ project template based on cmake and vcpkg.

Usage of the generated binary file:  
```bash
cppgen --help
Script to generate a CMake C++ project

Usage: cppgen [-cghkotvV] [-d=DESCRIPTION] [-l=LICENSE] [PROJECT_NAME]
C++ generator using CMake
      [PROJECT_NAME]      The project name.
  -c, --cmake             Run cmake.
  -d, --description=DESCRIPTION
                          Short description set in CMakeLists.txt.
  -g, --gui               Add gui library.
  -h, --help              Display this help message.
  -k, --keepFiles         Keep generated files on error.
  -l, --license=LICENSE   Add MIT license with author.
  -o, --open              Open visual studio solution.
  -t, --test              Add test.
  -v, --version           Display version info.
  -V, --verbose           Show verbose output.
```

Example of generating a C++ project and open in Visual Studio C++ 2022:
```bash
cppgen.exe --new NewProject -o
```

A Visual Studio C++ 2022 solution should be opened and ready to be used. Assumes that vcpkg is installed and environment variable VCPKG_ROOT pointing to it. Visual Studio 2022 and CMake >=3.24v must also be installed and available in the PATH environment variable.

## License
MIT
