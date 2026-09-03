# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html). Dates are npm publish dates. Where the git history does not record what shipped in a release, the entry says so instead of filling the gap.

## [2.0.0] - 2026-09-03

### Changed

- **Breaking.** The codegen spec now imports everything from the public `react-native` root entry point instead of deep paths: `codegenNativeComponent` (was `react-native/Libraries/Utilities/codegenNativeComponent`), `ViewProps` (was `react-native/Libraries/Components/View/ViewPropTypes`), and the codegen types, now reached through the `CodegenTypes` namespace (were `react-native/Libraries/Types/CodegenTypes`). React Native's Strict TypeScript API, opt-in since 0.80 and the default from 0.87, maps every deep path to `null` in its `exports` map, so a consumer using it could not resolve the spec's imports at all: the spec fell back to `any`, and with it every `onValueChange` handler in their own code became an implicit `any`. The generated native artifacts are unchanged, verified by diffing the codegen schema before and after.
- **Breaking.** `peerDependencies.react-native` raised from `>=0.71.0` to `>=0.80.0`. The root re-exports the spec now relies on land in 0.80.0 and are absent from both the type definitions and the runtime `index.js` of 0.79.0 and earlier, where `codegenNativeComponent` would resolve to `undefined` at module load. Anyone below 0.80 stays on 1.2.1.

### Notes

- No API, prop or behaviour change, and no change to either platform's native code.

## [1.2.1] - 2026-08-27

### Documentation

- Rewrote the README: what the component actually renders on each platform, a comparison table against the four most-used picker libraries, an explicit Limitations section, an Accessibility section, and the invisible-overlay pattern (including the iOS `opacity: 0.02` hit-testing detail) written up with a complete field component.
- Added this changelog and `CONTRIBUTING.md`.
- Fixed two README bugs: the license badge's alt text named a different project, and the downloads badge said "per week" while the shield renders `npm/dm`, downloads per month.

## [1.2.0] - 2026-05-15

### Changed

- Android: `SelectView.init()` now calls `setBackground(null)`. The `AppCompatSpinner` no longer draws a background of its own, so the invisible-overlay pattern (your own styled view with a `Select` stretched over it) renders cleanly instead of showing platform spinner chrome underneath.

### Documentation

- Corrected the npm links in the README, which pointed at `npmjs.com/package/wneel/react-native-native-select` rather than `npmjs.com/package/react-native-native-select`.

### Notes

- Published to npm without a matching git tag. `v1.0.0`, `v1.1.0` and `v1.1.1` are tagged; 1.2.0 is not, and its release commit is `af2cdc8`.

## [1.1.1] - 2026-03-29

### Fixed

- Android build failure: `SelectViewManager` was missing `setTextColor`. `textColor` is an iOS-only prop, but codegen still generates it into `RTNSelectManagerInterface` from the TypeScript spec, and a manager that does not implement the generated interface does not compile. The method is a deliberate empty stub, and is documented as such.

### Added

- `LICENSE` file (MIT, Wayan NEEL).
- README badges: license, npm version, npm downloads.

## [1.1.0] - 2026-03-29

### Added

- `textColor` prop (iOS only), typed as `ColorValue`. It sets the `UIPickerView` text color through KVC and the dropdown button's title color through `setTitleColor:`. Unset, it resolves to `UIColor.labelColor`, so existing behaviour is unchanged and light/dark mode keeps working on its own. This closes [issue #1](https://github.com/wneel/react-native-native-select/issues/1), "Allow to specify textColor", where the wheel followed the system color scheme in a way that could not suit every app's palette.

### Changed

- `package.json` metadata: `repository` moved to the object form with an explicit `git+https` URL, and `publishConfig` was added (public access, npm registry).
- Dependency audit fixes in the lockfile. Development toolchain only: the package has no runtime dependencies.

## [1.0.2] - 2025-11-22

### Notes

- Published the same day as 1.0.0, with no commit of its own. See the note under 1.0.1.

## [1.0.1] - 2025-11-22

### Notes

- Published the same day as 1.0.0. The repository holds no commits between the `v1.0.0` tag and the next commit four months later, and the committed `package.json` version stayed at `1.0.0` until the 1.1.0 bump, so what changed in 1.0.1 and 1.0.2 cannot be reconstructed from history. Treat both as republishes of the 1.0.0 tree.

## [1.0.0] - 2025-11-22

### Added

- First public release: a Select component for React Native's New Architecture only, exposed as a Fabric native component through the `RTNSelectSpec` codegen spec. No JS rendering logic in the package.
- iOS (`ios/RTNSelect.mm`): one `RCTViewComponentView` holding both a `UIPickerView` and a `UIButton` with `showsMenuAsPrimaryAction` (iOS 14+), with `mode` deciding which one is hidden. Dropdown mode builds a real `UIMenu` out of `UIAction`s and marks the selected row with `UIMenuElementStateOn`. Selection funnels through one method that updates state, refreshes the menu and emits through the Fabric event emitter.
- Android (`android/src/main/java/com/rtnselect/`): `SelectView` extends `AppCompatSpinner` with an `ArrayAdapter` over the platform `simple_spinner_item` layouts, `SelectViewManager` implements the codegen-generated `RTNSelectManagerInterface` and maps the native `topValueChange` event to the JS `onValueChange` prop, and `SelectViewPackage` makes the whole thing autolink.
- Props: `options` (required `string[]`), `selectedIndex`, `mode` (`'dialog' | 'dropdown'`, default `dialog`, iOS only), `onValueChange` returning `{ value, index }`, plus all standard `ViewProps`.
- Ships raw TypeScript: `main`, `react-native`, `types` and `source` all point at `src/index.ts`, so there is no build step and no `dist/`. No runtime dependencies; peers are `react` and `react-native >= 0.71.0`.
- Demo screens for both platforms in `tests/ios_demo.tsx` and `tests/android_demo.tsx`.
- README with the three preview GIFs (dropdown, wheel, Android).

[Unreleased]: https://github.com/wneel/react-native-native-select/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/wneel/react-native-native-select/compare/v1.2.1...v2.0.0
[1.2.1]: https://github.com/wneel/react-native-native-select/releases/tag/v1.2.1
[1.2.0]: https://github.com/wneel/react-native-native-select/commit/af2cdc8
[1.1.1]: https://github.com/wneel/react-native-native-select/releases/tag/v1.1.1
[1.1.0]: https://github.com/wneel/react-native-native-select/releases/tag/v1.1.0
[1.0.2]: https://www.npmjs.com/package/react-native-native-select/v/1.0.2
[1.0.1]: https://www.npmjs.com/package/react-native-native-select/v/1.0.1
[1.0.0]: https://github.com/wneel/react-native-native-select/releases/tag/v1.0.0
