<!-- 編集時は本ファイルと英語版 README.en.md を必ず同期させること。 -->
<p align="right"><sub><a href="README.en.md">🌐 English</a></sub></p>

## kei-1111.github.ioとは
このリポジトリ（kei-1111.github.io）は、kei-1111について知ってもらうことを目指したWebアプリケーションのリポジトリです。

UIはAndroid Studio（New UI）を模したIDE風デザインになっています。

### 機能
- プロジェクトツリー・エディタ・プレビュー・ツールウィンドウで構成されたIDE風UI
- テーマ切替（Islands Dark / Islands Light）
- 表示言語切替（日本語 / English）
- Search Everywhere（ページ・リンク・アクションのあいまい検索）
- コマンドを入力できるターミナルパネル

### 掲載している情報
- 自己紹介（エディタに表示されるREADME）
- GitHubプロフィールのライブ統計・ピン留めリポジトリ・使用言語シェア
- コントリビューションカレンダー
- open Issue一覧（TODOパネル）
- SNSへのリンク
- サードパーティライセンス

## このアプリで目指したこと
このアプリで目指したことは主に以下の3つです。

- **Android開発者らしいポートフォリオを作る**  
  自分のポートフォリオを作るにあたり、普段からJetpack Composeを使っているため、Compose Multiplatform（CMP）での実装を選びました。また、どうせならAndroid開発者であることが一目で伝わるものにしたいと考え、Android Studio風のUIにしました。
- **AIをフル活用してどこまで開発を楽にできるか試す**  
  AI活用のためのドキュメント整備について色々と調べ、AI用ドキュメントを`ai-docs/`に集約し、各エージェントの参照場所へsymlinkを張る方法で運用しています。
- **AIの力を借りてサーバー実装に初挑戦する**  
  普段Kotlinを書いているため、サーバーサイドKotlin（Ktor）で実装しました。モノレポでの開発も試したかったため、クライアントとサーバーを1つのリポジトリのマルチモジュール構成にまとめ、共有モジュールを介して実装を共有しています。

## アプリのURL
https://kei-1111.github.io/

## 画面
| Desktop | Mobile |
|-------|-------|
| <img src="https://github.com/user-attachments/assets/831d240d-adb7-4082-ae9d-6f3ad0a94d84" width="650" /> | <img src="https://github.com/user-attachments/assets/fba370ab-8263-46c2-9b0a-47e79412314e" width="250" /> |
| <img src="https://github.com/user-attachments/assets/4df213dd-3b96-499b-bb22-d68f75644888" width="650" /> | <img src="https://github.com/user-attachments/assets/6d4febf8-55b4-446f-8824-35872c0d99ea" width="250" /> |

## アーキテクチャ
クライアント（`:app`）は、マルチモジュールのClean Architecture（`app:feature` → `app:core:domain` → `app:core:data`）とMVIパターンを組み合わせた構成です。配布ターゲットはwasmJsのみで、Androidターゲットは`@Preview`の描画と単体テストのホスト実行のための開発専用ターゲットです。

データは自作APIサーバー（`:server`、Ktor / Cloud Run）が配信します。サーバーはGitHub公式GraphQL APIからプロフィール統計・コントリビューション・open Issueをライブ取得して`GET /api/profile` / `GET /api/contributions` / `GET /api/issues`として提供し、PAT（アクセストークン）はサーバー側に秘匿されます。クライアントとサーバーは共有DTOモジュール`:shared:model`を介してJSON契約を共有します。

詳細は以下を参照してください。
- [docs/ArchitectureOverview.md](docs/ArchitectureOverview.md)：アーキテクチャ・データフロー・DI・ナビゲーション
- [docs/ModuleOverview.md](docs/ModuleOverview.md)：モジュール構成と依存関係

## 使用した技術

| 項目     | 技術　    | 補足     |
|-------------|-------------|-------------|
| 言語    | Kotlin   | 型安全でシンプルな記述が可能    |
| UIフレームワーク    | Jetpack Compose (Compose Multiplatform)    | Android の UI フレームワークを Web でも利用    |
| DI    | Metro    | コンパイル時DI。Repository/UseCase/ViewModelの自動バインド    |
| ナビゲーション    | Navigation 3    | 型安全なNavKeyによる画面遷移    |
| バックエンド    | Ktor    | プロフィール・Contribution・open Issue を配信する自作 API サーバー（`:server`）    |
| 外部 API    | GitHub GraphQL API    | 統計・Contribution・open Issue をサーバー経由でライブ取得（PAT はサーバーに秘匿）    |
| デプロイ（フロント）    | GitHub Pages   | GitHub Actions を活用して自動デプロイを実施     |
| デプロイ（サーバー）    | Cloud Run   | scale-to-zero のコンテナ実行環境へ自動デプロイ    |
| CI/CD    | GitHub Actions    | Pull Request 時に自動でコード解析/テスト、main マージ時に自動デプロイ    |
| 静的解析ツール    | detekt   | コードの品質維持に活用    |
| 単体テスト    | kotlin-test   | `:server` のサーバーテストと、クライアント単体テスト（Android ホストテストとして実行）    |
| E2E テスト    | Playwright   | ビルド済みアプリを実ブラウザで検証する UI 回帰テストに活用    |
