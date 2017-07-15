package org.martysoft.jsoniter.jsr353;

import com.google.common.math.DoubleMath;
import com.jsoniter.any.Any;

import javax.json.JsonNumber;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsoniterNumber extends JsoniterValue implements JsonNumber {

    public JsoniterNumber(Any wrapped) {
        super(wrapped);
    }

    @Override
    public boolean isIntegral() {
        return DoubleMath.isMathematicalInteger(wrapped.toDouble());
    }

    @Override
    public int intValue() {
        return wrapped.toInt();
    }

    @Override
    public int intValueExact() {
        return wrapped.toInt();
    }

    @Override
    public long longValue() {
        return wrapped.toLong();
    }

    @Override
    public long longValueExact() {
        return wrapped.toLong();
    }

    @Override
    public BigInteger bigIntegerValue() {
        return BigInteger.valueOf(wrapped.toLong());
    }

    @Override
    public BigInteger bigIntegerValueExact() {
        return BigInteger.valueOf(wrapped.toLong());
    }

    @Override
    public double doubleValue() {
        return wrapped.toDouble();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return BigDecimal.valueOf(wrapped.toDouble());
    }
}
