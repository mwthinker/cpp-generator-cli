name: CI
on: [push, workflow_dispatch]

jobs:
  job:
    name: ${r"${{ matrix.os }}-${{ github.workflow }}"})
    runs-on: ${r"${{ matrix.os }}"})
    strategy:
      fail-fast: false
      matrix:
        include:
          - os: windows-latest
            preset: 'windows'
          - os: ubuntu-latest
            preset: 'unix'

    steps:
      - name: Check out repository code
        uses: actions/checkout@v3

      - name: Set C++/C compiler on macOS
        shell: bash
        run: echo "CC=gcc-11" >> "$GITHUB_ENV"; echo "CXX=g++-11" >> "$GITHUB_ENV"
        if: runner.os == 'macOS'

      - name: Set C++ VCPKG_ROOT
        shell: bash
        run: echo VCPKG_ROOT="$VCPKG_INSTALLATION_ROOT" >> "$GITHUB_ENV"; cat "$GITHUB_ENV"


      - name: Run CMake DEBUG
        shell: bash
        run: cmake --preset=${r"${{ matrix.preset }}"}) -B build_debug -D${projectName}_Test=1 -DCMAKE_BUILD_TYPE=Debug -DCMAKE_VERBOSE_MAKEFILE=1

      - name: Compile binaries DEBUG
        shell: bash
        run: cmake --build build_debug

<#if hasTests>
      - name: Run tests DEBUG
        shell: bash
        run: ctest --rerun-failed --output-on-failure --test-dir build_debug/${projectName}_Test

</#if>

      - name: Run CMake RELEASE
        shell: bash
        run: cmake --preset=${r"${{ matrix.preset }}"}) -B build_release -D${projectName}_Test=1 -DCMAKE_BUILD_TYPE=Release -DCMAKE_VERBOSE_MAKEFILE=1

      - name: Compile binaries RELEASE
        shell: bash
        run: cmake --build build_release

<#if hasTests>
      - name: Run tests RELEASE
        shell: bash
        run: ctest --rerun-failed --output-on-failure --test-dir build_release/${projectName}_Test

</#if>