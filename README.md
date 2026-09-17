![swiyu GitHub banner](./.resources/swiyuBanner.jpg)

# swiyu - shared KMP libraries

An official Swiss Government project made by the [Federal Office of Information Technology, Systems and Telecommunication FOITT](https://www.bit.admin.ch/en)
as part of the electronic identity (E-ID) project.

## Table of Contents

- [swiyu - shared KMP libraries](#swiyu---shared-kmp-libraries)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Modules](#modules)
  - [Installation and building](#installation-and-building)
  - [Consuming the libraries](#consuming-the-libraries)
    - [Android](#android)
    - [iOS](#ios)
  - [Missing Features and Known Issues](#missing-features-and-known-issues)
  - [Contributions and feedback](#contributions-and-feedback)
  - [License](#license)

## Overview

This repository is part of the ecosystem developed for the future official Swiss E-ID.
It provides the shared Kotlin Multiplatform (KMP) libraries consumed by the swiyu wallet on both iOS and Android, offering a single, versioned home for the shared code and a stable API surface for the platform clients.

The goal of this repository is to engage with the community and collaborate on developing the Swiss ecosystem for E-ID and other credentials. We warmly encourage you to engage with us by creating an issue in the repository.

For more information about the project please visit the [introduction into Public Beta](https://www.eid.admin.ch/en/public-beta-e). The technical documentation of the swiyu Public Beta Trust Infrastructure can be found [here](https://swiyu-admin-ch.github.io/).

## Modules

The repository is a Gradle multi-module project containing the following Kotlin Multiplatform modules (no app UI or platform-specific app code):

- `dcql`: DCQL (Digital Credentials Query Language) support — query matching, credential models and helpers built on top of the Heidi DCQL libraries.
- `proximity`: Proximity presentation support for in-person / offline credential presentation, exposing the Heidi proximity APIs.
- `consistency`: Check consistency between SD-JWTs
- `password-strength-estimator`: Wrapper around zxcvbn to indicate strength of a password string
- `library`: Umbrella module that exports the shared APIs and builds the `BITSwiyuSharedKMP` XCFramework for iOS.

Supported targets: `android`, `iosArm64`, `iosSimulatorArm64` and `jvm` (used to run the tests).

## Installation and building

The project targets Android and iOS.
The iOS artifacts currently target at least iOS 16.0.
The CI build uses Java 17.

In your terminal, after having cloned the current repository, run the following command:

```bash
./gradlew build
```

The command verifies that all modules compile and runs the configured tests.

To build the iOS XCFramework, run:

```bash
./gradlew :library:assembleXCFramework
```

For release packaging, the repository CI uses:

```bash
./gradlew :library:assembleXCFramework -PreleaseBuild=true -PforceBuildNativeLibs=true
```

The generated XCFramework can then be found under `library/build/XCFrameworks/`.

## Consuming the libraries

### Android

The release AARs for all Android modules are attached to each GitHub release as `BITSwiyuSharedKMP-android-aars.zip`. The `dcql` module is additionally published to GitHub Packages under the group `ch.admin.foitt.swiyu.shared.kmp`:

```kotlin
dependencies {
    implementation("ch.admin.foitt.swiyu.shared.kmp:dcql:<version>")
}
```

### iOS

For iOS, the `library` module produces the `BITSwiyuSharedKMP` XCFramework, which re-exports the shared APIs. The XCFramework is attached to each GitHub release and can be integrated into an Xcode project like any other binary framework.

## Missing Features and Known Issues

The swiyu Public Beta Trust Infrastructure was deliberately released at an early stage to enable future ecosystem participants. The [feature roadmap](https://github.com/orgs/swiyu-admin-ch/projects/1/views/7) shows the current discrepancies between Public Beta and the targeted productive Trust Infrastructure. There may still be minor bugs or security vulnerabilities in the test system. Repository-specific issues are tracked in the [issue tracker](https://github.com/admin-ch-ssi/PERA_swiyu_shared_kmp/issues).

## Contributions and feedback

The code for this repository is developed privately and will be released after each sprint. The published code can therefore only be a snapshot of the current development and not a thoroughly tested version. However, we welcome any feedback on the code regarding both the implementation and security aspects. Please follow the guidelines for contributing found in [CONTRIBUTING.md](./CONTRIBUTING.md).

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE) file for details.
