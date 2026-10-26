# CLAUDE.md

## Overview

[moke-playground-android](https://github.com/mokelab/moke-playground-android) を Kotlin Multiplatform（Android + iOS）にしたもの。各実験は `feature/**` 以下の KMP モジュールとして作り、[devportal-kmp](https://github.com/mokelab/devportal-kmp) のメニュー項目（`DevPortalFeature`）として表示する。DI は Hilt ではなく Koin を使う。

## Commands

```bash
./gradlew :androidApp:assembleDebug                      # Android ビルド
./gradlew :androidApp:installDebug                       # Android 端末にインストール
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64    # iOS framework のビルド確認
open iosApp/iosApp.xcodeproj                             # iOS は Xcode から実行
```

依存のバージョンは必ず `gradle/libs.versions.toml` に書く。Kotlin / Compose Multiplatform / navigation3 / Koin のバージョンは devportal-kmp 0.1.0 のビルドに合わせてあるので、上げるときは devportal-kmp 側と揃えること。DevPortal は `https://mokelab.github.io/devportal-kmp/repo`（`settings.gradle.kts` で宣言済み）から取得する。

## Modules

新しい CMP テンプレートの構成（Android アプリと共有コードを別モジュールにする）。AGP 9 では KMP と `com.android.application` を同じモジュールに入れられず、KMP の Android ライブラリは `com.android.kotlin.multiplatform.library` を使うことになるので、それに合わせている。

- `androidApp` — Android アプリ（`com.android.application` + `org.jetbrains.kotlin.android`。後者は AGP 9 の built-in Kotlin に移行したら消す）。**MainActivity は無い**。ランチャーは `com.mokelab.devportal.kmp.DevPortalActivity`。持っているのは `PlaygroundApplication` で `initKoin()` を呼ぶことだけ
- `shared` — 共有コード。`initKoin()`、生成される `FeatureModules.kt`、有効な feature への依存、iOS framework（`Shared`、`MainViewController()` = `DevPortalViewController()`）
- `iosApp` — Xcode プロジェクト。ビルド時に `:shared:embedAndSignAppleFrameworkForXcode` が呼ばれる
- `core:devportal` — `singleScreenFeature` / `devPortalFeature` ヘルパー。`devportal-api` と `koin-core` を api で公開している
- `core:ui` — `FeatureScaffold`（戻るボタン付き TopAppBar + Scaffold）などの共通 UI
- `feature/<area>/<name>` — 実験ごとのモジュール

## Build logic (convention plugins)

`build-logic/` は included build。

| plugin id | catalog alias | 内容 |
|---|---|---|
| `mokelab.kmp.library.compose` | `libs.plugins.mokelab.kmp.library.compose` | KMP（`androidLibrary {}` + iosArm64 + iosSimulatorArm64）+ `com.android.kotlin.multiplatform.library` + Compose。commonMain に compose runtime/foundation/ui/material3 を追加 |
| `mokelab.kmp.feature` | `libs.plugins.mokelab.kmp.feature` | 上記に加えて `:core:devportal` / `:core:ui` / `koin-core` を commonMain に追加 |

Android の `namespace` はプロジェクトパスから自動で決まる（`:feature:m3:calendar` → `com.mokelab.playground.feature.m3.calendar`）。SDK レベルはカタログの `android-compileSdk` / `android-minSdk` を使う。`com.android.kotlin.multiplatform.library` はビルドバリアント（debug/release）を持たず、Android リソース（`res/`）も既定では無効なので、feature で必要なら `androidLibrary { androidResources { enable = true } }` を足す。convention plugin は AGP / Kotlin / Compose の Gradle プラグインを `compileOnly` で参照しているだけなので、ルートの `build.gradle.kts` の `apply false` を消さないこと。

## feature の登録の仕組み

各 feature は自分のパッケージ（= namespace）直下に **`val featureModule: org.koin.core.module.Module`** を置く。これが決まり。

```kotlin
package com.mokelab.playground.feature.m3.calendar

val featureModule = module {
    singleScreenFeature("M3 Calendar") { back -> CalendarScreen(back = back) }
}
```

`shared` の `generateFeatureModules` タスクが、有効な feature の `featureModule` を並べた `FeatureModules.kt` を生成し、`initKoin()` がそれを読み込む。DevPortal は `getAll<DevPortalFeature>()` でメニューを作る。

- **`single<DevPortalFeature> { ... }` を直接書かないこと。** Koin は同じ型・同じ qualifier の定義を後勝ちで上書きするので、feature が 1 つしか表示されなくなる。必ず `singleScreenFeature(...)` か `devPortalFeature(id) { ... }`（id を qualifier にして登録する）を使う
- `singleScreenFeature` は `FeatureKey(id)` という**インスタンスキー**で navigation3 の entry を作る。id の既定値はメニュー名なので、同名の feature を 2 つ有効にすると起動時に落ちる。その場合は別の `id` を渡す。クラスキーの `entry<FeatureKey>` はどこにも登録しないこと（インスタンスキーがすべて隠れてしまう）
- 複数画面の feature はヘルパーを使わず、自前のキー型と `entry<T>` を持つ `DevPortalFeature` を実装し、`devPortalFeature("id") { MyFeature(get()) }` で登録する。`DevPortalNavigator` は `get()` で取得できる
- 画面は `internal fun XxxScreen(back: () -> Unit)` とし、`FeatureScaffold(title, back) { padding -> ... }` で包む
- 片方のプラットフォームにしか無い API（通知など）は、commonMain に `expect` を置いて androidMain / iosMain に `actual` を書く。`featureModule` は必ず commonMain に置く（生成コードが commonMain から参照するため）。iOS では「未対応」と表示する画面にしてもよい
- commonMain では `java.*` や `android.R` は使えない。日時は `kotlinx-datetime` / `kotlin.time` を使う

## feature の有効化・無効化

`local.properties`（git 管理外）の `features=` に、`feature/` 以下のパスを `:` 区切りで、カンマ区切りで列挙する。`*` を指定すると全 feature が有効になる。`settings.gradle.kts` が `include` し（存在しないパスは即エラー）、`gradle.extra["playground.features"]` 経由で `shared` の依存と生成コードに反映する。切り替えたら Gradle を再同期する。

## feature の追加手順

1. `feature/<area>/<name>/build.gradle.kts` を作る。`plugins { alias(libs.plugins.mokelab.kmp.feature) }` だけでよく、追加の依存がある場合だけ `kotlin { sourceSets { commonMain.dependencies { ... } } }` を書く
2. `src/commonMain/kotlin/com/mokelab/playground/feature/<area>/<name>/` に `XxxScreen.kt` と `Module.kt` を置く
3. `local.properties` の `features=` に追加し、Android と iOS の両方で動作を確認する。`settings.gradle.kts` / `shared/build.gradle.kts` を編集する必要はない
4. `README.md` の feature 一覧に追加する

Android 版から移植するときは、Hilt の `Module.kt` を上の Koin 形式に書き換え、`TopAppBar` の定型部分は `FeatureScaffold` に置き換える。
