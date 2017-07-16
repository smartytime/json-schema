package io.dugnutt.json;

import com.google.common.base.Preconditions;
import com.google.common.math.DoubleMath;
import lombok.EqualsAndHashCode;

import javax.json.JsonNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class JsonNumberImpl implements JsonNumber {
    private final BigDecimal number;

    public JsonNumberImpl(Number number) {
        Preconditions.checkNotNull(number, "number must not be null");
        this.number = BigDecimal.valueOf(number.doubleValue());
    }

    public JsonNumberImpl(BigDecimal number) {
        this.number = checkNotNull(number);
    }

    /**
     * Returns the value type of this JSON value.
     *
     * @return JSON value type
     */
    @Override
    public ValueType getValueType() {
        return ValueType.NUMBER;
    }

    /**
     * Returns true if this JSON number is a integral number. This method
     * semantics are defined using {@code bigDecimalValue().scale()}. If the
     * scale is zero, then it is considered integral type. This integral type
     * information can be used to invoke an appropriate accessor method to
     * obtain a numeric value as in the following example:
     * <p>
     * <pre>
     * <code>
     * JsonNumber num = ...
     * if (num.isIntegral()) {
     *     num.longValue();     // or other methods to get integral value
     * } else {
     *     num.doubleValue();   // or other methods to get decimal number value
     * }
     * </code>
     * </pre>
     *
     * @return true if this number is a integral number, otherwise false
     */
    @Override
    public boolean isIntegral() {
        return DoubleMath.isMathematicalInteger(number.doubleValue());
    }

    /**
     * Returns this JSON number as an {@code int}. Note that this conversion
     * can lose information about the overall magnitude and precision of the
     * number value as well as return a result with the opposite sign.
     *
     * @return an {@code int} representation of the JSON number
     * @see BigDecimal#intValue()
     */
    @Override
    public int intValue() {
        return number.intValue();
    }

    /**
     * Returns this JSON number as an {@code int}.
     *
     * @return an {@code int} representation of the JSON number
     * @throws ArithmeticException if the number has a nonzero fractional
     *                             part or if it does not fit in an {@code int}
     * @see BigDecimal#intValueExact()
     */
    @Override
    public int intValueExact() {
        return number.intValueExact();
    }

    /**
     * Returns this JSON number as a {@code long}. Note that this conversion
     * can lose information about the overall magnitude and precision of the
     * number value as well as return a result with the opposite sign.
     *
     * @return a {@code long} representation of the JSON number.
     * @see BigDecimal#longValue()
     */
    @Override
    public long longValue() {
        return number.longValue();
    }

    /**
     * Returns this JSON number as a {@code long}.
     *
     * @return a {@code long} representation of the JSON number
     * @throws ArithmeticException if the number has a non-zero fractional
     *                             part or if it does not fit in a {@code long}
     * @see BigDecimal#longValueExact()
     */
    @Override
    public long longValueExact() {
        return number.longValueExact();
    }

    /**
     * Returns this JSON number as a {@link BigInteger} object. This is a
     * a convenience method for {@code bigDecimalValue().toBigInteger()}.
     * Note that this conversion can lose information about the overall
     * magnitude and precision of the number value as well as return a result
     * with the opposite sign.
     *
     * @return a {@code BigInteger} representation of the JSON number.
     * @see BigDecimal#toBigInteger()
     */
    @Override
    public BigInteger bigIntegerValue() {
        return number.toBigInteger();
    }

    /**
     * Returns this JSON number as a {@link BigInteger} object. This is a
     * convenience method for {@code bigDecimalValue().toBigIntegerExact()}.
     *
     * @return a {@link BigInteger} representation of the JSON number
     * @throws ArithmeticException if the number has a nonzero fractional part
     * @see BigDecimal#toBigIntegerExact()
     */
    @Override
    public BigInteger bigIntegerValueExact() {
        return number.toBigIntegerExact();
    }

    /**
     * Returns this JSON number as a {@code double}. This is a
     * a convenience method for {@code bigDecimalValue().doubleValue()}.
     * Note that this conversion can lose information about the overall
     * magnitude and precision of the number value as well as return a result
     * with the opposite sign.
     *
     * @return a {@code double} representation of the JSON number
     * @see BigDecimal#doubleValue()
     */
    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    /**
     * Returns this JSON number as a {@link BigDecimal} object.
     *
     * @return a {@link BigDecimal} representation of the JSON number
     */
    @Override
    public BigDecimal bigDecimalValue() {
        return number;
    }
}
