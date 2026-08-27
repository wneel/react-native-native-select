# Contributing

Thanks for looking. This is a small, single-purpose library: a Select component that renders the actual OS widget, for React Native's New Architecture. Anything that keeps it small and native is welcome.

## Before anything else: the New Architecture

Every local test needs the New Architecture enabled. The old bridge is not supported and cannot be, because the code is generated against it: `ios/RTNSelect.mm` imports the codegen headers from `RTNSelectSpec`, and `SelectViewManager` implements the codegen-generated `RTNSelectManagerInterface`. Without codegen output, neither platform compiles.

For iOS that means `RCT_NEW_ARCH_ENABLED=1` in the environment when you run `pod install`. If a build complains that it cannot find `react/renderer/components/RTNSelectSpec/Props.h`, that flag is missing.

## There is no build step

`main`, `react-native`, `types` and `source` in `package.json` all point at `src/index.ts`. The package ships raw TypeScript and has no `dist/`, so nothing is compiled before publish and the file you edit is the file consumers run. Do not add a bundler or a build output directory without a reason that survives that sentence.

The published surface is `src/`, `android/`, `ios/` and the podspec (see the `files` array). There are no npm scripts in this package: no test runner, no linter. Verification is running the component on both platforms.

## Running it against the demo screens

The repository has no example app of its own. Use a bare React Native app (0.71 or newer, New Architecture on) and point it at your working copy.

```bash
# in your working copy
npm pack

# in your test app
npm install ../react-native-native-select/react-native-native-select-1.2.0.tgz
cd ios && RCT_NEW_ARCH_ENABLED=1 pod install && cd ..
```

`npm pack` is worth the extra step over a `file:` dependency: it respects the `files` array, so you are testing exactly what a consumer would install and you find out immediately if a source file stopped being published.

Then copy one of the demo screens in as your app entry point:

* [`tests/ios_demo.tsx`](./tests/ios_demo.tsx) renders both iOS modes side by side, the wheel (`dialog`) and the pull-down (`dropdown`), each with a long option to check text truncation.
* [`tests/android_demo.tsx`](./tests/android_demo.tsx) renders the invisible-overlay pattern three ways plus a bare spinner, which is the Android case worth exercising because the native widget does not reliably reflect a controlled `selectedIndex`.

Note that `tests/android_demo.tsx` calls `console.d`, which comes from the host app it was written in. Change it to `console.log` when you run it.

What to rebuild after what:

| You changed | What to do |
| :--- | :--- |
| Anything under `src/` other than the props | Restart Metro. No build, no pod install. |
| The props in `src/RTNSelectNativeComponent.ts` | Rebuild the app. Codegen runs at build time and regenerates the spec headers and the Android manager interface, so a new prop needs a native rebuild on both platforms (and `pod install` again on iOS). |
| `ios/*.mm` or `ios/*.h` | Rebuild from Xcode. Re-run `pod install` if you added or removed a file. |
| Anything under `android/` | Rebuild with Gradle. |

## What to check before opening a pull request

There is no CI here, so this is on you and on me:

* iOS `dialog` mode and iOS `dropdown` mode, since they are two different UIKit views behind one prop.
* Android, including one select inside an invisible overlay at `opacity: 0.02`.
* If you touched props, that codegen still succeeds on both platforms. A prop that exists in TypeScript but not in `SelectViewManager` breaks the Android build (that was the 1.1.1 fix).
* If you touched anything iOS-only, that Android still builds. `mode` and `textColor` are no-op stubs there on purpose, and they need to stay present.

In the pull request, say what you ran it on: platform, OS version, React Native version, device or simulator.

## Commit messages

Conventional commits, matching the existing history:

```
feat(ios): add textColor prop to RTNSelect to override system appearance
fix(android): add missing setTextColor method on android to prevent build crashes
feat(android): drop native spinner background so overlay pattern renders cleanly
docs(preview): add gifs to show the Select component
```

Scopes in use: `ios`, `android`, `package`, `docs`, `deps`. One concern per commit, and write the summary so it reads as what changed and why, not what file you touched.

## Reporting a bug

Open an issue at [github.com/wneel/react-native-native-select/issues](https://github.com/wneel/react-native-native-select/issues). Four things decide whether it is reproducible, so please include all four:

1. **Platform and OS version.** iOS 14 vs iOS 13 changes whether `dropdown` mode has a menu at all, and Android's spinner behaves differently across API levels.
2. **React Native version**, and the library version you installed.
3. **Whether the New Architecture is enabled.** If it is off, that is the bug.
4. **Which `mode`**, and whether the `Select` is visible or sitting invisibly over your own view. The overlay pattern has its own failure mode: at `opacity: 0` iOS drops the tap, because `hitTest:withEvent:` ignores views at alpha 0.01 or below.

A short snippet that reproduces it beats a description of the screen it happens on. Known-and-documented behaviours (Android ignoring `mode` and `textColor`, Android not honouring a controlled `selectedIndex`, `dropdown` doing nothing below iOS 14) are listed in the README under Platform differences and Limitations, so it is worth a look before filing.

## Scope

Things that fit: bugs, platform edge cases, native-side improvements, better docs, keeping up with React Native releases.

Things that probably do not: features that mean drawing the list ourselves. `{label, value}` items, icons, search, theming and multi-select are all reasonable wants, and all of them mean a JS list instead of the OS widget, which is the one thing this library is for. Several excellent libraries already do that, and the README names them.

There is no roadmap and no promised review turnaround. Issues and pull requests get read; a small, well-described one gets read faster.
