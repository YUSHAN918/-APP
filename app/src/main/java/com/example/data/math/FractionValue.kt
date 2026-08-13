package com.example.data.math

data class FractionValue(val numerator: Int, val denominator: Int) {
    fun gcd(a: Int, b: Int): Int {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    fun isSimplified(): Boolean {
        if (denominator == 0) return false
        if (denominator == 1) return true
        return gcd(numerator, denominator) == 1
    }

    fun simplify(): FractionValue {
        if (denominator == 0) return this
        val common = gcd(numerator, denominator)
        val sign = if (denominator < 0) -1 else 1
        return FractionValue((numerator / common) * sign, (denominator / common) * sign)
    }

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()

    companion object {
        fun parse(str: String): FractionValue? {
            val trimmed = str.trim().replace("\\s+".toRegex(), "")
            if (!trimmed.contains("/")) {
                val intVal = trimmed.toIntOrNull()
                return if (intVal != null) FractionValue(intVal, 1) else null
            }
            val parts = trimmed.split("/")
            if (parts.size != 2) return null
            val num = parts[0].toIntOrNull() ?: return null
            val den = parts[1].toIntOrNull() ?: return null
            if (den == 0) return null
            return FractionValue(num, den)
        }
    }
}

data class RatioValue(
    val left: FractionValue,
    val right: FractionValue
) {
    fun toFraction(): FractionValue? {
        if (right.numerator == 0) return null
        val num = left.numerator * right.denominator
        val den = left.denominator * right.numerator
        if (den == 0) return null
        return FractionValue(num, den).simplify()
    }

    fun toSimplestIntegerRatio(): RatioValue? {
        val frac = toFraction() ?: return null
        return RatioValue(
            left = FractionValue(frac.numerator, 1),
            right = FractionValue(frac.denominator, 1)
        )
    }

    fun hasIntegerTerms(): Boolean {
        return left.denominator == 1 && right.denominator == 1
    }

    fun isSimplestIntegerRatio(): Boolean {
        if (!hasIntegerTerms()) return false
        if (right.numerator == 0) return false
        val l = left.numerator
        val r = right.numerator
        if (l == 0) return r == 1
        return gcd(l, r) == 1
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = kotlin.math.abs(a)
        var y = kotlin.math.abs(b)
        while (y != 0) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    companion object {
        fun parseTerm(str: String): FractionValue? {
            val trimmed = str.trim().replace("\\s+".toRegex(), "")
            if (trimmed.isEmpty()) return null
            if (trimmed.contains("/")) {
                return FractionValue.parse(trimmed)
            }
            if (trimmed.contains(".")) {
                val parts = trimmed.split(".")
                if (parts.size != 2) return null
                val intPart = parts[0].toIntOrNull() ?: return null
                val decPartStr = parts[1]
                val decVal = decPartStr.toIntOrNull() ?: return null
                val scale = Math.pow(10.0, decPartStr.length.toDouble()).toInt()
                val totalNum = intPart * scale + (if (intPart < 0) -decVal else decVal)
                return FractionValue(totalNum, scale).simplify()
            }
            val intVal = trimmed.toIntOrNull() ?: return null
            return FractionValue(intVal, 1)
        }

        fun parse(str: String): RatioValue? {
            val trimmed = str.trim().replace("\\s+".toRegex(), "")
            val parts = if (trimmed.contains(":")) {
                trimmed.split(":")
            } else if (trimmed.contains("：")) {
                trimmed.split("：")
            } else {
                return null
            }
            if (parts.size != 2) return null
            val left = parseTerm(parts[0]) ?: return null
            val right = parseTerm(parts[1]) ?: return null
            return RatioValue(left, right)
        }

        fun parse(leftStr: String, rightStr: String): RatioValue? {
            val left = parseTerm(leftStr) ?: return null
            val right = parseTerm(rightStr) ?: return null
            return RatioValue(left, right)
        }
    }
}
