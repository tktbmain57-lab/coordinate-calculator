package jp.dev.tanaka.coordinatecalculator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import jp.dev.tanaka.coordinatecalculator.R
import java.text.DecimalFormat

/**
 * シンプルな面取り計算画面
 *
 * 1パターンのみ:
 *            P2●──────────
 *             ╱
 *            ╱ ツールパス
 *           ╱
 *       ───●P1
 *          │
 *          │
 *
 * 入力: 角の座標(X,Y), 面取り量C
 * 出力: P1(入口), P2(出口)
 */
class SimpleChamferFragment : Fragment() {

    private lateinit var inputCornerX: TextInputEditText
    private lateinit var inputCornerY: TextInputEditText
    private lateinit var inputChamferC: TextInputEditText
    private lateinit var resultP1: TextView
    private lateinit var resultP2: TextView
    private lateinit var resultSection: View

    private val decimalFormat = DecimalFormat("0.00")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_chamfer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View の初期化
        inputCornerX = view.findViewById(R.id.inputCornerX)
        inputCornerY = view.findViewById(R.id.inputCornerY)
        inputChamferC = view.findViewById(R.id.inputChamferC)
        resultP1 = view.findViewById(R.id.resultP1)
        resultP2 = view.findViewById(R.id.resultP2)
        resultSection = view.findViewById(R.id.resultSection)

        // 初期状態では結果を非表示
        resultSection.visibility = View.GONE

        // 計算ボタン
        view.findViewById<MaterialButton>(R.id.btnCalculate).setOnClickListener {
            calculate()
        }
    }

    private fun calculate() {
        // 入力値の取得
        val cornerX = inputCornerX.text.toString().toDoubleOrNull()
        val cornerY = inputCornerY.text.toString().toDoubleOrNull()
        val chamferC = inputChamferC.text.toString().toDoubleOrNull()

        // 検証
        if (cornerX == null || cornerY == null) {
            Toast.makeText(requireContext(), "角の座標を入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        if (chamferC == null || chamferC <= 0) {
            Toast.makeText(requireContext(), "面取り量Cを正の値で入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        // 計算（1パターン固定: 左上の角）
        // P1: 入口 (X, Y - C)
        // P2: 出口 (X - C, Y)
        val p1x = cornerX
        val p1y = cornerY - chamferC
        val p2x = cornerX - chamferC
        val p2y = cornerY

        // 結果を表示
        resultP1.text = "P1 (入口): X=${decimalFormat.format(p1x)}  Y=${decimalFormat.format(p1y)}"
        resultP2.text = "P2 (出口): X=${decimalFormat.format(p2x)}  Y=${decimalFormat.format(p2y)}"
        resultSection.visibility = View.VISIBLE
    }
}
