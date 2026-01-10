# ツールパス座標計算アプリ v2 実装プラン

## 概要

現行の座標計算アプリを「ビジュアルファースト」のツールパス座標計算アプリに全面改良する。

---

## 実装ステップ

### Step 1: プロジェクト基盤の更新

#### 1.1 アプリ名・パッケージの更新
- [x] アプリ名を「ツールパス座標計算」に変更
- [x] strings.xml のアプリ名更新

#### 1.2 ナビゲーション構造の変更
- [x] 現行: BottomNavigation (計算/履歴/設定)
- [x] 新規: シナリオベースのフロー型ナビゲーション

```
nav_graph.xml の構成:
├── scenarioSelectFragment (ホーム)
├── directionSelectFragment (向き選択)
├── parameterInputFragment (パラメータ入力)
├── resultFragment (結果表示)
├── historyFragment (履歴)
└── settingsFragment (設定)
```

---

### Step 2: 新規画面の実装

#### 2.1 シナリオ選択画面 (ScenarioSelectFragment)

**レイアウト**: カード形式のグリッド表示

```xml
<!-- fragment_scenario_select.xml -->
- ToolBar (履歴・設定へのメニュー)
- RecyclerView (GridLayoutManager, spanCount=2)
  - ScenarioCard (外角面取り)
  - ScenarioCard (内角面取り) ← 将来
  - ScenarioCard (円弧進入) ← 将来
```

**実装内容**:
- [x] ScenarioSelectFragment.kt
- [x] fragment_scenario_select.xml
- [x] item_scenario_card.xml
- [x] ScenarioAdapter.kt
- [x] Scenario.kt (シナリオ定義のsealed class)

#### 2.2 向き選択画面 (DirectionSelectFragment)

**レイアウト**: 2x2グリッドの方向カード

```xml
<!-- fragment_direction_select.xml -->
- ToolBar (戻るボタン)
- タイトル「角の向きを選択」
- GridLayout (2x2)
  - DirectionCard (左上) ┌──
  - DirectionCard (右上) ──┐
  - DirectionCard (左下) └──
  - DirectionCard (右下) ──┘
```

**実装内容**:
- [x] DirectionSelectFragment.kt
- [x] fragment_direction_select.xml
- [x] item_direction_card.xml
- [x] CornerDirection.kt (enum: TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)
- [x] カスタムDrawable (各方向のイラスト)

#### 2.3 パラメータ入力画面 (ParameterInputFragment)

**レイアウト**: 選択した向きのイラスト + 入力フォーム

```xml
<!-- fragment_parameter_input.xml -->
- ToolBar (戻るボタン)
- 選択した向きのイラスト表示 (ImageView or CustomView)
- 入力フォーム (ScrollView)
  - 角の座標セクション
    - X入力 (TextInputLayout)
    - Y入力 (TextInputLayout)
  - 面取り角度セクション
    - プリセットボタン群 (30°, 45°, 60°)
    - 自由入力 (TextInputLayout)
  - 進入距離セクション
    - 入力 (TextInputLayout, default: 2)
- 計算ボタン (MaterialButton)
```

**実装内容**:
- [x] ParameterInputFragment.kt
- [x] fragment_parameter_input.xml
- [x] ChamferParameters.kt (入力パラメータのdata class)

#### 2.4 結果表示画面 (ResultFragment)

**レイアウト**: 結果の図示 + 座標表示

```xml
<!-- fragment_result.xml -->
- ToolBar (戻るボタン、保存メニュー)
- 結果キャンバス (ChamferCanvasView)
  - 基準点表示
  - 面取りライン表示
  - 進入開始点ハイライト
- 結果カード
  - 進入開始点 X, Y
- アクションボタン
  - コピーボタン
  - 保存ボタン
  - 新しい計算ボタン
```

**実装内容**:
- [x] ResultFragment.kt
- [x] fragment_result.xml
- [x] ChamferResult.kt (計算結果のdata class)

---

### Step 3: 計算ロジックの実装

#### 3.1 面取り進入点計算

**ファイル**: `util/ChamferCalculator.kt`

```kotlin
object ChamferCalculator {

    fun calculateApproachPoint(
        cornerPoint: Point,      // 基準点
        direction: CornerDirection,  // 向き
        chamferAngle: Double,    // 面取り角度（度）
        approachDistance: Double // 進入距離（mm）
    ): Point {
        // 向きに応じた角度計算
        // 進入点座標を返す
    }
}
```

**実装内容**:
- [x] ChamferCalculator.kt
- [x] 各向きの角度計算ロジック
- [ ] 単体テスト

---

### Step 4: ViewModel・データ層の更新

#### 4.1 ViewModel

**ファイル**: `ui/ToolPathViewModel.kt`

```kotlin
class ToolPathViewModel : ViewModel() {
    // 選択状態
    val selectedScenario: StateFlow<Scenario?>
    val selectedDirection: StateFlow<CornerDirection?>

    // 入力パラメータ
    val chamferParameters: StateFlow<ChamferParameters?>

    // 計算結果
    val calculationResult: StateFlow<ChamferResult?>

    // アクション
    fun selectScenario(scenario: Scenario)
    fun selectDirection(direction: CornerDirection)
    fun calculate(params: ChamferParameters)
    fun saveToHistory()
    fun reset()
}
```

**実装内容**:
- [x] ToolPathViewModel.kt
- [x] 既存MainViewModelからの履歴・設定機能の移行

#### 4.2 データ層の更新

**履歴エンティティの拡張**:

```kotlin
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioType: String,      // "CHAMFER_OUTER" など
    val title: String,
    val memo: String,
    val inputsJson: String,        // ChamferParameters のJSON
    val resultJson: String,        // ChamferResult のJSON
    val createdAt: Long
)
```

**実装内容**:
- [x] HistoryEntity の更新（マイグレーション）
- [x] 新しいシリアライザの実装

---

### Step 5: 既存コードのリファクタリング

#### 5.1 削除するファイル
- [x] CalculatorFragment.kt (新画面に置き換え)
- [x] CoordinateCanvasView.kt (ChamferCanvasViewに置き換え)
- [x] MainViewModel.kt (ToolPathViewModelに置き換え)
- [x] 旧レイアウトファイル (fragment_calculator.xml, input_shape.xml)

#### 5.2 継続利用するファイル
- [x] HistoryFragment.kt (微修正)
- [x] SettingsFragment.kt (そのまま)
- [x] Repository.kt (そのまま)
- [x] AppDatabase.kt (マイグレーション追加)

#### 5.3 修正するファイル
- [x] MainActivity.kt (ナビゲーション更新)
- [x] nav_graph.xml (全面書き換え)
- [x] strings.xml (新しい文字列追加)
- [x] HistoryAdapter.kt (新しい表示形式)

---

### Step 6: UIリソースの作成

#### 6.1 Drawable
- [x] ic_chamfer_outer.xml (外角面取りアイコン)
- [x] ic_chamfer_inner.xml (内角面取りアイコン - coming soon)
- [x] ic_arc_approach.xml (円弧進入アイコン - coming soon)
- [x] ic_corner_top_left.xml (左上の角)
- [x] ic_corner_top_right.xml (右上の角)
- [x] ic_corner_bottom_left.xml (左下の角)
- [x] ic_corner_bottom_right.xml (右下の角)
- [x] ic_copy.xml (コピーアイコン)

#### 6.2 文字列リソース
- [x] シナリオ名
- [x] 方向名
- [x] 入力ラベル
- [x] ボタンテキスト
- [x] エラーメッセージ

---

## ファイル構成（実装後）

```
app/src/main/java/jp/dev/tanaka/coordinatecalculator/
├── ui/
│   ├── scenario/
│   │   ├── ScenarioSelectFragment.kt
│   │   ├── ScenarioAdapter.kt
│   │   └── Scenario.kt
│   ├── direction/
│   │   ├── DirectionSelectFragment.kt
│   │   └── CornerDirection.kt
│   ├── parameter/
│   │   ├── ParameterInputFragment.kt
│   │   └── ChamferParameters.kt
│   ├── result/
│   │   ├── ResultFragment.kt
│   │   └── ChamferResult.kt
│   ├── HistoryFragment.kt
│   ├── HistoryAdapter.kt
│   ├── SettingsFragment.kt
│   ├── common/
│   │   └── ChamferCanvasView.kt
│   ├── MainActivity.kt
│   └── ToolPathViewModel.kt
├── data/
│   ├── HistoryEntity.kt
│   ├── SettingsEntity.kt
│   ├── HistoryDao.kt
│   ├── SettingsDao.kt
│   ├── AppDatabase.kt
│   └── Repository.kt
├── util/
│   ├── ChamferCalculator.kt
│   ├── Geometry.kt
│   └── RoundingUtil.kt
└── CoordinateCalculatorApp.kt
```

---

## 実装順序

| 順序 | タスク | 依存関係 | 状態 |
|------|--------|---------|------|
| 1 | Scenario, CornerDirection, ChamferParameters 定義 | なし | ✅完了 |
| 2 | ChamferCalculator 実装 | 1 | ✅完了 |
| 3 | ToolPathViewModel 実装 | 1, 2 | ✅完了 |
| 4 | ScenarioSelectFragment 実装 | 1, 3 | ✅完了 |
| 5 | DirectionSelectFragment 実装 | 1, 3 | ✅完了 |
| 6 | ParameterInputFragment 実装 | 1, 3 | ✅完了 |
| 7 | ResultFragment 実装 | 1, 2, 3 | ✅完了 |
| 8 | ナビゲーション統合 | 4, 5, 6, 7 | ✅完了 |
| 9 | 履歴・設定の統合 | 8 | ✅完了 |
| 10 | UIリソース・文字列整備 | 9 | ✅完了 |
| 11 | テスト・デバッグ | 10 | 🔄進行中 |

---

## 注意事項

- 既存の履歴データとの互換性を考慮（マイグレーション）
- 既存のGeometry.ktの計算ロジックは将来の拡張で再利用可能
- Material Design 3 のコンポーネントを継続使用

---

## 更新履歴

| 日付 | バージョン | 内容 |
|------|-----------|------|
| 2026-01-10 | 1.0 | 初版作成 |
| 2026-01-10 | 1.1 | v2実装完了（外角面取り機能）、ビルド成功 |
