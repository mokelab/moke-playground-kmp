# MokePlayground KMP

[MokePlayground](https://github.com/mokelab/moke-playground-android) の Kotlin Multiplatform 版。Compose Multiplatform の API を Android / iOS で試すためのプレイグラウンド。

各実験は `feature/` 以下の KMP モジュールとして作り、[devportal-kmp](https://github.com/mokelab/devportal-kmp) のメニュー項目として表示される（DevPortal がアプリのランチャー画面）。

## feature を試す

feature モジュールはデフォルトではビルドされない。`local.properties`（git 管理外）で選ぶ:

```properties
features=m3:calendar
```

`feature/` 以下のパスを `:` 区切りで書き、複数ある場合はカンマで区切る。`features=*` と書くと全 feature を有効にする。

```bash
./gradlew :androidApp:installDebug      # Android
open iosApp/iosApp.xcodeproj            # iOS（Xcode から Run）
```

## 利用可能な feature

| `features` の値 | メニュー名 |
|---|---|
| `m3:calendar` | M3 Calendar |

## feature の追加

1. `feature/<area>/<name>/build.gradle.kts` を作る（中身は `plugins { alias(libs.plugins.mokelab.kmp.feature) }` だけでよい）
2. `src/commonMain/kotlin/com/mokelab/playground/feature/<area>/<name>/` に画面と `Module.kt`（`val featureModule = module { singleScreenFeature("名前") { back -> XxxScreen(back) } }`）を置く
3. `local.properties` の `features=` に追加して実行する
4. 上の表に行を追加する

詳細は [CLAUDE.md](CLAUDE.md) を参照。
