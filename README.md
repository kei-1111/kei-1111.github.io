## kei-1111.github.ioとは
このリポジトリ（kei-1111.github.io）は、kei-1111について知ってもらうことを目指したWebアプリケーションのリポジトリです。

主に以下の情報を掲載しています。
- 基本情報（名前や大学など）
- 制作物
- 自分のスキル
- SNSへのリンク

## このアプリで目指したこと
このアプリで目指したことは主に以下の2つです。
- 私、kei-1111がどのようなものを作っているのかなど知ってもらえるWebサイトを作る。
- Compose Multiplatform（以下、CMP）の勉強。  
  今回の実装を通して、CMP開発とAndroid開発の違い、CMPでのbuild-logicなどについて学ぶことができました。
  - CMP開発とAndroid開発の違い：各モジュールsrc/以下のファイル名違いなど
  - CMPでのbuild-logic：build.gradle.ktsでkotlin{}にしなければ行けない

## アプリのURL
https://kei-1111.github.io/

## 画面
| Mobile | Desktop |
|-------|-------|
| <img src="https://github.com/user-attachments/assets/6332e6a3-75e2-48de-bec9-c289a4aceb32" width="300" /> | <img src="https://github.com/user-attachments/assets/b76db3cd-a1af-4481-a54c-645f1452ae6a" width="600" /> |

## 使用した技術

| 項目     | 技術　    | 補足     |
|-------------|-------------|-------------|
| 言語    | Kotlin   | 型安全でシンプルな記述が可能    |
| UIフレームワーク    | Jetpack Compose (Compose Multiplatform)    | Android の UI フレームワークを Web でも利用    |
| デプロイ    | GitHub Pages   | GitHub Actions を活用して自動デプロイを実施     |
| CI/CD    | GitHub Actions    | Pull Request/Merge 時に自動でコード解析/デプロイ    |
| 静的解析ツール    | detekt   | コードの品質維持に活用    |

