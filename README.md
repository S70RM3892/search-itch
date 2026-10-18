# サッと調べる（QuickLookup）

Androidで文字を選択 →「調べる」で、小さいウィンドウに辞書を表示するアプリ。

- タブ：国語（Weblio）／英和（Weblio英和）／Google／Wikipedia
- 英字だけの選択は英和、それ以外は国語で自動的に開く
- 上の欄で検索語を直して再検索、↗でブラウザ、✕か外側タップで閉じる
- 選択メニューに出ないアプリは「共有」→「調べる」
- Android 6.0 以上

## インストール

[Releases](https://github.com/S70RM3892/search-itch/releases/latest) から `QuickLookup-vX.Y.apk` をダウンロードしてインストール。

新しいバージョンを出すとき：`app/build.gradle.kts` の `versionCode`/`versionName` を上げて push → Actions がその版の APK をビルドして Release（タグ `v<versionName>`）に添付する。同じ版が既にあれば何もしない。

## ビルド方法

### A. GitHub Actions（PC不要）
1. GitHubで新規リポジトリを作り、このフォルダの中身を全部アップロード（`.github` フォルダも含める）
2. Actions タブ →「Build APK」の実行完了を待つ（数分）
3. 実行結果ページ下部の Artifacts から `QuickLookup-apk` をダウンロード → 解凍して `app-debug.apk` をインストール

### B. 自宅PC
- Android Studio でこのフォルダを開いて Build > Build APK(s)
- またはコマンド：`./gradlew assembleDebug`（Android SDK と JDK 17 が必要）
  → `app/build/outputs/apk/debug/app-debug.apk`
