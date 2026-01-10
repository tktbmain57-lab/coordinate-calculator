# 交点座標計算アプリ 仕様書

## 概要

金属加工現場で使用するための座標計算Androidアプリケーション。2つの幾何学図形（直線・円）の交点座標を計算し、視覚的に表示する。

---

## 主な機能

| 機能 | 説明 |
|------|------|
| 交点座標計算 | 2つの幾何学図形の交点を計算 |
| 対応図形 | 直線（2点指定 or 1点+角度）、円（中心+半径） |
| 計算パターン | 直線×直線、直線×円、円×円 |
| 履歴管理 | 計算結果をタイトル・メモ付きで保存 |
| 設定 | 小数点以下の桁数（1〜3桁）を設定可能 |

---

## 画面構成

### 1. 計算画面（メイン）

メインの計算インターフェース。

#### レイアウト構成
- **キャンバスエリア（高さ35%）**: 幾何学図形の視覚的表示
- **結果カード**: 計算結果の交点座標を表示
- **入力セクション（スクロール可能）**:
  - 図形A入力カード
  - 図形B入力カード
- **操作ボタン**: クリア、計算

#### キャンバス機能
- グリッド表示（10mm間隔）
- X軸・Y軸表示（原点は中央）
- 入力図形の描画（直線・円）
- 交点のマーカー表示（タップで選択可能）
- ピンチズーム（0.1倍〜10倍）
- 2本指ドラッグでパン
- ダブルタップでビューリセット

#### 図形入力タイプ
| タイプ | 入力項目 |
|--------|----------|
| 直線（2点） | P1(x, y), P2(x, y) |
| 直線（点+角度） | Point(x, y), 角度（度） |
| 円 | 中心(x, y), 半径R |

#### ツールバーメニュー
- 保存: 現在の計算結果を履歴に保存
- 履歴: 履歴画面へ遷移
- 設定: 設定画面へ遷移

### 2. 履歴画面

保存した計算結果の一覧表示と管理。

#### 表示項目
- タイトル: ユーザーが入力した計算名
- 結果: フォーマットされた交点座標
- メモ: オプションのユーザーメモ（入力時のみ表示）
- 日時: 作成日時（yyyy/MM/dd HH:mm形式）
- 削除ボタン: 確認ダイアログ付き

#### 空の状態
履歴がない場合は「履歴がありません」メッセージを表示。

### 3. 設定画面

アプリの動作設定。

#### 設定項目
- **小数点桁数**: ラジオボタングループ（1, 2, 3桁）
  - デフォルト: 2桁
  - 変更は計算画面と履歴表示に即座に反映
- **データ管理**:
  - 「全履歴削除」ボタン（確認ダイアログ付き）

---

## 計算アルゴリズム

### 直線×直線の交点

連立一次方程式を行列式（クラメルの公式）で解く。

- **入力**: 2本の直線（ax + by + c = 0 形式）
- **出力**:
  - 1点の交点（一般的なケース）
  - 交点なし（平行線の場合、行列式 ≈ 0）

### 直線×円の交点

円の中心から直線への垂直距離を計算。

- **判定**:
  - 距離 > 半径: 交点なし
  - 距離 ≈ 半径: 接点（1点）
  - 距離 < 半径: 2点の交点
- **出力**: 0点、1点（接点）、または2点

### 円×円の交点

2つの円の相対位置を分析。

- **ケース**:
  1. 同一の円: 交点なし
  2. 同心円（半径異なる）: 交点なし
  3. 離れすぎ: 交点なし
  4. 一方が他方の内部: 交点なし
  5. 外接: 1点（接点）
  6. 内接: 1点（接点）
  7. 通常の交差: 2点

### 直線の生成方法

| 方法 | 説明 |
|------|------|
| 2点指定 | P1(x1,y1)とP2(x2,y2)から法線ベクトルを計算 |
| 点+角度 | 点と角度（度）から方向ベクトルを計算し、直線方程式を導出 |

### 数値精度

- 浮動小数点比較のイプシロン: 1e-10
- 丸め処理: 指定桁数で適切に丸め

---

## データモデル

### 履歴テーブル（history）

| カラム | 型 | 説明 |
|--------|-----|------|
| id | Long | 自動生成の主キー |
| title | String | ユーザー入力の計算名 |
| memo | String | オプションのメモ |
| inputsJson | String | シリアライズされた図形入力 |
| resultPointsJson | String | シリアライズされた交点結果 |
| createdAt | Long | タイムスタンプ |

### 設定テーブル（settings）

| カラム | 型 | 説明 |
|--------|-----|------|
| id | Int | 主キー（固定値: 1） |
| decimalPlaces | Int | 小数点桁数（1-3、デフォルト: 2） |

### 図形入力（ShapeInput）

```kotlin
sealed class ShapeInput {
    // 2点で定義された直線
    data class LineTwoPoints(p1: Point, p2: Point)

    // 点と角度（度）で定義された直線
    data class LinePointAngle(point: Point, angleDegrees: Double)

    // 中心と半径で定義された円
    data class CircleInput(center: Point, radius: Double)
}
```

### 計算結果（CalculationResult）

```kotlin
sealed class CalculationResult {
    data class Success(points: List<IntersectionResult>)
    data class NoIntersection(reason: String)
    data class Error(message: String)
}

data class IntersectionResult(
    point: Point,
    type: IntersectionType  // INTERSECTION または TANGENT
)
```

---

## 技術スタック

| 項目 | 値 |
|------|-----|
| 言語 | Kotlin |
| 最小SDK | 26（Android 8.0） |
| ターゲットSDK | 34（Android 14） |
| Javaバージョン | 17 |
| アーキテクチャ | MVVM + Repository パターン |
| データベース | Room 2.6.1 |
| UIフレームワーク | Fragments + Navigation Component |
| 非同期処理 | Coroutines + Flow |
| ビルドシステム | Gradle + KSP |

---

## プロジェクト構造

```
coordinate-calculator/
├── app/src/main/
│   ├── java/jp/dev/tanaka/coordinatecalculator/
│   │   ├── ui/                    # UI層（Fragments, ViewModels, Custom Views）
│   │   │   ├── CalculatorFragment.kt
│   │   │   ├── HistoryFragment.kt
│   │   │   ├── SettingsFragment.kt
│   │   │   ├── MainViewModel.kt
│   │   │   ├── CoordinateCanvasView.kt
│   │   │   └── HistoryAdapter.kt
│   │   ├── data/                  # データ層（Entities, DAOs, Repository）
│   │   │   ├── HistoryEntity.kt
│   │   │   ├── SettingsEntity.kt
│   │   │   ├── HistoryDao.kt
│   │   │   ├── SettingsDao.kt
│   │   │   ├── AppDatabase.kt
│   │   │   ├── Repository.kt
│   │   │   └── ShapeInput.kt
│   │   ├── util/                  # ユーティリティ（幾何学計算）
│   │   │   └── Geometry.kt
│   │   ├── MainActivity.kt
│   │   └── CoordinateCalculatorApp.kt
│   └── res/
│       ├── layout/                # UIレイアウトXML
│       └── navigation/            # ナビゲーショングラフ
└── docs/
    └── specification.md           # 本仕様書
```

---

## 主要クラス

### UI層

| クラス | ファイル | 役割 |
|--------|----------|------|
| MainActivity | MainActivity.kt | シングルアクティビティコンテナ、ナビゲーション設定 |
| MainViewModel | MainViewModel.kt | アプリ状態管理、計算実行、履歴管理 |
| CalculatorFragment | CalculatorFragment.kt | メイン計算UI、入力解析、結果表示 |
| HistoryFragment | HistoryFragment.kt | 履歴一覧表示と削除 |
| SettingsFragment | SettingsFragment.kt | 小数点桁数設定 |
| CoordinateCanvasView | CoordinateCanvasView.kt | 幾何学図形の2Dキャンバス表示 |
| HistoryAdapter | HistoryAdapter.kt | 履歴リストのRecyclerViewアダプター |

### データ層

| クラス | ファイル | 役割 |
|--------|----------|------|
| HistoryEntity | HistoryEntity.kt | 履歴レコードのRoomエンティティ |
| SettingsEntity | SettingsEntity.kt | 設定のRoomエンティティ |
| HistoryDao | HistoryDao.kt | 履歴CRUD操作のRoom DAO |
| SettingsDao | SettingsDao.kt | 設定管理のRoom DAO |
| AppDatabase | AppDatabase.kt | Roomデータベースインスタンス |
| Repository | Repository.kt | データアクセス抽象化層 |
| ShapeInput | ShapeInput.kt | 3種類の入力タイプを表すシールドクラス |

### ユーティリティ層

| クラス | ファイル | 役割 |
|--------|----------|------|
| Point | Geometry.kt | 2D座標表現、距離計算 |
| Line | Geometry.kt | 直線方程式（ax + by + c = 0）、ファクトリメソッド |
| Circle | Geometry.kt | 中心と半径を持つ円 |
| IntersectionCalculator | Geometry.kt | 交点計算アルゴリズム（3種類） |
| RoundingUtil | Geometry.kt | 小数点桁数の丸めユーティリティ |

---

## UI/UX考慮事項

- Material Design 3コンポーネント使用
- タッチフレンドリーなボタンサイズ（最小48dp）
- 現場での視認性を考慮した大きめのテキスト
- 日本語ローカライズ
- 空の状態のメッセージ表示
- 破壊的操作には確認ダイアログ

---

## 入力検証

- 座標の数値フォーマット検証
- ゼロまたは負の半径の拒否
- 直線定義時の同一点検出
- 計算時のNaN保護

---

## 更新履歴

| 日付 | バージョン | 内容 |
|------|------------|------|
| 2026-01-10 | 1.0 | 初版作成 |
